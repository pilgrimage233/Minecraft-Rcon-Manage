package cc.endmc.node.task;

import cc.endmc.common.utils.ip.IpUtils;
import cc.endmc.node.common.NodeCache;
import cc.endmc.node.mapper.NodeServerMapper;
import cc.endmc.node.utils.ApiUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * ClassName: NodeScheduled <br>
 * Description:
 * date: 2025/10/26 22:45 <br>
 *
 * @author Memory <br>
 * @since JDK 1.8
 */
@Slf4j
@Component
public class NodeScheduled {

    @Autowired
    private NodeServerMapper nodeServerMapper;

    /**
     * 心跳检查任务
     * 每5分钟执行一次，检查所有注册的节点服务器的心跳状态
     */
    @Scheduled(fixedDelay = 300000)
    public void heartbeatCheck() {
        log.debug("开始执行节点服务器心跳检查任务");
        if (NodeCache.getMap().isEmpty()) {
            return;
        }

        NodeCache.getMap().values().forEach(nodeServer -> {
            log.debug("正在检查节点服务器 [{}] 的心跳状态", nodeServer.getName());
            HttpResponse execute;
            try {
                execute = HttpUtil.createGet(ApiUtil.getHeartbeatApi(nodeServer))
                        .header(ApiUtil.X_ENDLESS_TOKEN, nodeServer.getToken())
                        .timeout(3000)
                        .execute();
            } catch (Exception e) {
                log.error("节点服务器 [{}] 心跳检查失败: {}", nodeServer.getName(), e.getMessage());
                return;
            }

            if (execute != null && execute.isOk()) {
                final JSONObject body = JSONObject.parseObject(execute.body(), JSONObject.class);
                if (body.getString("status").equals("OJBK")) {
                    // 更新最后心跳时间
                    nodeServer.setLastHeartbeat(new Date());
                    nodeServerMapper.updateNodeServer(nodeServer);
                }
            }

            // 获取IP地址
            final String hostIp = IpUtils.getHostIp();
            final String masterUuid = nodeServer.getUuid();
            // 回调节点服务器心跳结果
            final HttpResponse callback = HttpUtil.createGet(ApiUtil.getCallbackLastCommunicationApi(nodeServer, masterUuid, hostIp))
                    .header(ApiUtil.X_ENDLESS_TOKEN, nodeServer.getToken())
                    .timeout(3000)
                    .execute();

            if (callback.isOk()) {
                final JSONObject callbackBody = JSONObject.parseObject(callback.body(), JSONObject.class);
                if (callbackBody.getString("success").equals("true")) {
                    // 回调成功
                    log.info("节点服务器 [{}] 心跳回调成功", nodeServer.getName());
                } else {
                    log.warn("节点服务器 [{}] 心跳回调失败: {}", nodeServer.getName(), callbackBody.getString("message"));
                }
            }

            try {
                Thread.sleep(500); // 避免请求过于频繁，暂停500毫秒
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        log.debug("节点服务器心跳检查任务执行完毕");
    }

}
