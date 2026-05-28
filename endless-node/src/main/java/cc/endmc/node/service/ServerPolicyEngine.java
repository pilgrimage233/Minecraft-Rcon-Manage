package cc.endmc.node.service;

import cc.endmc.common.email.EmailService;
import cc.endmc.node.common.NodeCache;
import cc.endmc.node.domain.NodeMinecraftServer;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.domain.NodeServerSettings;
import cc.endmc.node.mapper.NodeMinecraftServerMapper;
import cc.endmc.node.mapper.NodeServerMapper;
import cc.endmc.node.utils.ApiUtil;
import cc.endmc.node.utils.NodeHttpUtil;
import cc.endmc.node.utils.StartScriptBuilder;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 实例运维策略引擎
 * 定时检查所有实例的策略配置并执行相应操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServerPolicyEngine {

    private final INodeServerSettingsService settingsService;
    private final INodeEmailTemplateService templateService;
    private final EmailService emailService;
    private final NodeServerMapper nodeServerMapper;
    private final NodeMinecraftServerMapper mcsMapper;

    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Scheduled(fixedDelay = 60000)
    public void enforcePolicies() {
        List<NodeServerSettings> allSettings = settingsService.selectNodeServerSettingsList(new NodeServerSettings());
        for (NodeServerSettings settings : allSettings) {
            try {
                enforceSinglePolicy(settings);
            } catch (Exception e) {
                log.error("策略执行异常，实例ID: {}", settings.getNodeServerId(), e);
            }
        }
    }

    private void enforceSinglePolicy(NodeServerSettings settings) {
        Long nodeServerId = settings.getNodeServerId();
        NodeMinecraftServer mcs = mcsMapper.selectNodeMinecraftServerById(nodeServerId);
        if (mcs == null || mcs.getNodeId() == null) return;

        NodeServer node = NodeCache.getOrLoad(mcs.getNodeId(), nodeServerMapper::selectNodeServerById);
        if (node == null) return;

        // 1. 保活检查
        if (Integer.valueOf(1).equals(settings.getKeepAliveEnabled())) {
            checkKeepAlive(settings, mcs, node);
        }
    }

    private void checkKeepAlive(NodeServerSettings settings, NodeMinecraftServer mcs, NodeServer node) {
        Integer nodeInstanceId = mcs.getNodeInstancesId();
        if (nodeInstanceId == null) {
            log.warn("保活检测跳过：实例 {} 无节点实例ID", mcs.getName());
            return;
        }
        try {
            HttpResponse resp = NodeHttpUtil.createGet(node,
                    ApiUtil.getStatusInstanceApi(node, nodeInstanceId)).timeout(10000).execute();
            if (!resp.isOk()) {
                log.warn("保活检测状态查询失败：实例 {}，HTTP {}", mcs.getName(), resp.getStatus());
                return;
            }
            JSONObject body = JSONObject.parseObject(resp.body());
            boolean isRunning = Boolean.TRUE.equals(body.getBoolean("isRunning"));
            if (!isRunning) {
                log.info("保活检测：实例 {} 已停止，正在重启...", mcs.getName());
                String script = StartScriptBuilder.prepareStartScript(null, mcs);
                log.debug("保活重启脚本：{}", script);
                HttpRequest request = NodeHttpUtil.createPost(node,
                        ApiUtil.getStartInstanceApi(node, nodeInstanceId)).timeout(20000);
                if (script != null) {
                    JSONObject startBody = new JSONObject();
                    startBody.put("script", script);
                    request.body(startBody.toJSONString());
                }
                HttpResponse startResp = request.execute();
                log.info("保活重启响应：实例 {}，HTTP {}，body={}", mcs.getName(), startResp.getStatus(),
                        startResp.body());
                if (startResp.isOk()) {
                    JSONObject startResult = JSONObject.parseObject(startResp.body());
                    if (Boolean.TRUE.equals(startResult.getBoolean("success"))) {
                        log.info("保活重启成功：实例 {}", mcs.getName());
                        sendNotification(settings, "restart_notify", mcs, node,
                                Map.of("result", "重启成功", "timestamp", SDF.format(new Date())));
                    } else {
                        log.warn("保活重启失败：实例 {}，原因：{}", mcs.getName(),
                                startResult.getString("error") != null ? startResult.getString("error") : startResult.getString("message"));
                    }
                } else {
                    log.warn("保活重启请求失败：实例 {}，HTTP {}", mcs.getName(), startResp.getStatus());
                }
            }
        } catch (Exception e) {
            log.error("保活检查异常：实例 {}，{}", mcs.getName(), e.getMessage(), e);
        }
    }

    private void sendNotification(NodeServerSettings settings, String templateKey,
                                   NodeMinecraftServer mcs, NodeServer node, Map<String, String> extraVars) {
        if (settings.getNotifyEmail() == null || settings.getNotifyEmail().isEmpty()) return;
        try {
            Map<String, String> vars = new HashMap<>();
            vars.put("serverName", mcs.getName() != null ? mcs.getName() : "Unknown");
            vars.put("nodeName", node.getName() != null ? node.getName() : "Unknown");
            vars.put("nodeIp", node.getIp() != null ? node.getIp() : "Unknown");
            vars.put("timestamp", SDF.format(new Date()));
            if (extraVars != null) vars.putAll(extraVars);

            String subject = templateService.renderSubject(templateKey, vars);
            String content = templateService.renderTemplate(templateKey, vars);
            if (content == null) return;
            if (subject == null) subject = "服务器通知 - " + mcs.getName();

            emailService.push(settings.getNotifyEmail(), subject, content);
        } catch (Exception e) {
            log.error("发送通知失败，模板：{}，收件人：{}", templateKey, settings.getNotifyEmail(), e);
        }
    }
}
