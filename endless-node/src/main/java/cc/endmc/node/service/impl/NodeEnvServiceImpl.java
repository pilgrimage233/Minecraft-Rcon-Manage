package cc.endmc.node.service.impl;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.framework.manager.AsyncManager;
import cc.endmc.node.domain.NodeEnv;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.mapper.NodeEnvMapper;
import cc.endmc.node.service.INodeEnvService;
import cc.endmc.node.service.INodeServerService;
import cc.endmc.node.utils.ApiUtil;
import cc.endmc.node.utils.NodeHttpUtil;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

/**
 * 节点Java多版本环境管理Service业务层处理
 *
 * @author Memory
 * @date 2025-11-25
 */
@Slf4j
@Service
public class NodeEnvServiceImpl implements INodeEnvService {
    @Autowired
    private NodeEnvMapper nodeEnvMapper;

    @Autowired
    private INodeServerService nodeServerService;

    /**
     * 查询节点Java多版本环境管理
     *
     * @param id 节点Java多版本环境管理主键
     * @return 节点Java多版本环境管理
     */
    @Override
    public NodeEnv selectNodeEnvById(Long id) {
        return nodeEnvMapper.selectNodeEnvById(id);
    }

    /**
     * 查询节点Java多版本环境管理列表
     *
     * @param nodeEnv 节点Java多版本环境管理
     * @return 节点Java多版本环境管理
     */
    @Override
    public List<NodeEnv> selectNodeEnvList(NodeEnv nodeEnv) {
        return nodeEnvMapper.selectNodeEnvList(nodeEnv);
    }

    /**
     * 新增节点Java多版本环境管理
     *
     * @param nodeEnv 节点Java多版本环境管理
     * @return 结果
     */
    @Override
    public int insertNodeEnv(NodeEnv nodeEnv) {
        // 验证节点服务器是否存在
        NodeServer nodeServer = nodeServerService.selectNodeServerById(nodeEnv.getNodeId());
        if (nodeServer == null) {
            throw new RuntimeException("节点服务器不存在");
        }

        // 如果提供了路径，验证Java环境
        if (nodeEnv.getPath() != null && !nodeEnv.getPath().trim().isEmpty()) {
            try {
                Map<String, Object> verifyResult = verifyJavaEnvironment(nodeServer, nodeEnv.getPath());

                if (Boolean.TRUE.equals(verifyResult.get("valid"))) {
                    // 自动填充版本信息（如果未提供）
                    if (nodeEnv.getVersion() == null || nodeEnv.getVersion().trim().isEmpty()) {
                        nodeEnv.setVersion((String) verifyResult.get("version"));
                    }
                    if (nodeEnv.getJavaHome() == null || nodeEnv.getJavaHome().trim().isEmpty()) {
                        nodeEnv.setJavaHome((String) verifyResult.get("javaHome"));
                    }
                    if (nodeEnv.getBinPath() == null || nodeEnv.getBinPath().trim().isEmpty()) {
                        nodeEnv.setBinPath((String) verifyResult.get("binPath"));
                    }
                    if (nodeEnv.getArch() == null || nodeEnv.getArch().trim().isEmpty()) {
                        nodeEnv.setArch((String) verifyResult.get("arch"));
                    }
                    if (nodeEnv.getSource() == null || nodeEnv.getSource().trim().isEmpty()) {
                        nodeEnv.setSource((String) verifyResult.get("vendor"));
                    }
                    nodeEnv.setValid(1);
                } else {
                    nodeEnv.setValid(0);
                    log.warn("Java环境验证失败: {}", verifyResult.get("error"));
                }
            } catch (Exception e) {
                log.error("验证Java环境时发生错误", e);
                nodeEnv.setValid(0);
            }
        }

        nodeEnv.setCreateTime(DateUtils.getNowDate());
        return nodeEnvMapper.insertNodeEnv(nodeEnv);
    }

    /**
     * 验证节点上的Java环境
     */
    private Map<String, Object> verifyJavaEnvironment(NodeServer nodeServer, String javaPath) {
        try {
            String url = ApiUtil.getJavaEnvVerifyApi(nodeServer);

            JSONObject requestBody = new JSONObject();
            requestBody.put("javaPath", javaPath);

            HttpResponse response = NodeHttpUtil.createPost(nodeServer, url)
                    .body(requestBody.toJSONString())
                    .execute();

            if (response.isOk() && StringUtils.isNotEmpty(response.body())) {
                JSONObject jsonObject = JSONObject.parseObject(response.body());
                return new HashMap<>(jsonObject);
            } else {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("valid", false);
                errorResult.put("error", "无响应或请求失败");
                return errorResult;
            }
        } catch (Exception e) {
            log.error("调用节点API验证Java环境失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("valid", false);
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }

    /**
     * 修改节点Java多版本环境管理
     *
     * @param nodeEnv 节点Java多版本环境管理
     * @return 结果
     */
    @Override
    public int updateNodeEnv(NodeEnv nodeEnv) {
        nodeEnv.setUpdateTime(DateUtils.getNowDate());
        return nodeEnvMapper.updateNodeEnv(nodeEnv);
    }

    /**
     * 批量删除节点Java多版本环境管理
     *
     * @param ids 需要删除的节点Java多版本环境管理主键
     * @return 结果
     */
    @Override
    public int deleteNodeEnvByIds(Long[] ids) {
        return nodeEnvMapper.deleteNodeEnvByIds(ids);
    }

    /**
     * 删除节点Java多版本环境管理信息
     *
     * @param id 节点Java多版本环境管理主键
     * @return 结果
     */
    @Override
    public int deleteNodeEnvById(Long id) {
        return nodeEnvMapper.deleteNodeEnvById(id);
    }

    /**
     * 验证Java环境
     */
    @Override
    public AjaxResult verifyEnvironment(NodeEnv nodeEnv) {
        if (nodeEnv.getNodeId() == null) {
            return AjaxResult.error("节点ID不能为空");
        }
        if (nodeEnv.getPath() == null || nodeEnv.getPath().trim().isEmpty()) {
            return AjaxResult.error("Java路径不能为空");
        }

        NodeServer nodeServer = nodeServerService.selectNodeServerById(nodeEnv.getNodeId());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }

        try {
            Map<String, Object> result = verifyJavaEnvironment(nodeServer, nodeEnv.getPath());
            return AjaxResult.success("验证完成", result);
        } catch (Exception e) {
            log.error("验证Java环境失败", e);
            return AjaxResult.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 扫描节点上的Java环境
     */
    @Override
    public AjaxResult scanEnvironments(Long nodeId) {
        if (nodeId == null) {
            return AjaxResult.error("节点ID不能为空");
        }

        NodeServer nodeServer = nodeServerService.selectNodeServerById(nodeId);
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }

        try {
            String url = ApiUtil.getJavaEnvScanApi(nodeServer);

            HttpResponse response = NodeHttpUtil.createGet(nodeServer, url)
                    .execute();

            if (response.isOk() && StringUtils.isNotEmpty(response.body())) {
                JSONObject jsonObject = JSONObject.parseObject(response.body());
                return AjaxResult.success("扫描完成", jsonObject);
            } else {
                return AjaxResult.error("扫描失败: 无响应或请求失败");
            }
        } catch (Exception e) {
            log.error("扫描Java环境失败", e);
            return AjaxResult.error("扫描失败: " + e.getMessage());
        }
    }

    /**
     * 一键安装Java环境（流式响应）
     */
    @Override
    public SseEmitter installJavaWithProgress(Map<String, Object> params) {
        final SseEmitter emitter = new SseEmitter(600000L);

        // 使用AsyncManager异步执行
        AsyncManager.me().execute(new TimerTask() {
            @Override
            public void run() {
                try {
                    installJavaInternal(params, emitter);
                } catch (Exception e) {
                    try {
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("type", "error");
                        errorData.put("message", e.getMessage());
                        errorData.put("progress", 0);
                        emitter.send(SseEmitter.event().data(errorData));
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                }
            }
        });

        return emitter;
    }

    /**
     * 一键安装Java环境（内部实现）
     */
    private void installJavaInternal(Map<String, Object> params,
                                     SseEmitter emitter) throws Exception {
        Long nodeId = params.get("nodeId") != null ? Long.valueOf(params.get("nodeId").toString()) : null;
        String version = (String) params.get("version");
        String installPath = (String) params.get("installPath");
        String vendor = (String) params.getOrDefault("vendor", "Adoptium");

        if (nodeId == null) {
            throw new RuntimeException("节点ID不能为空");
        }
        if (version == null || version.trim().isEmpty()) {
            throw new RuntimeException("Java版本不能为空");
        }
        if (installPath == null || installPath.trim().isEmpty()) {
            throw new RuntimeException("安装路径不能为空");
        }

        NodeServer nodeServer = nodeServerService.selectNodeServerById(nodeId);
        if (nodeServer == null) {
            throw new RuntimeException("节点服务器不存在");
        }

        try {
            String url = ApiUtil.getJavaEnvInstallApi(nodeServer);

            JSONObject requestBody = new JSONObject();
            requestBody.put("version", version);
            requestBody.put("installPath", installPath);
            requestBody.put("vendor", vendor);

            log.info("开始在节点 {} 上安装Java {}, 路径: {}", nodeId, version, installPath);

            // 使用流式请求
            URL apiUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("X-Endless-Token", nodeServer.getToken());
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(600000);

            // 发送请求体
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toJSONString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 读取SSE流
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                JSONObject lastData = null;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String jsonStr = line.substring(5).trim();
                        if (!jsonStr.isEmpty()) {
                            JSONObject data = JSONObject.parseObject(jsonStr);
                            lastData = data;

                            // 转发到前端
                            Map<String, Object> dataMap = new HashMap<>();
                            for (String key : data.keySet()) {
                                dataMap.put(key, data.get(key));
                            }
                            // 关键修复:添加comment强制刷新
                            emitter.send(SseEmitter.event().data(dataMap).comment(""));

                            // 立即刷新,确保数据被发送
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }

                            // 如果安装成功，保存到数据库
                            if (Boolean.TRUE.equals(data.get("success"))) {
                                String installedVersion = data.getString("version");
                                log.info("节点返回的version: {}, 安装时传入的version: {}", installedVersion, version);

                                if (installedVersion == null || installedVersion.trim().isEmpty()) {
                                    installedVersion = version;  // 如果返回的version为空,使用传入的参数
                                    log.warn("节点返回的version为空,使用传入的参数: {}", version);
                                }

                                // 先检查是否已存在相同版本
                                NodeEnv queryEnv = new NodeEnv();
                                queryEnv.setNodeId(nodeId);
                                queryEnv.setVersion(installedVersion);
                                log.info("检查是否存在: nodeId={}, version={}", nodeId, installedVersion);
                                List<NodeEnv> existingEnvs = selectNodeEnvList(queryEnv);

                                if (existingEnvs == null || existingEnvs.isEmpty()) {
                                    NodeEnv nodeEnv = new NodeEnv();
                                    nodeEnv.setNodeId(nodeId);
                                    nodeEnv.setVersion(installedVersion);
                                    nodeEnv.setPath(data.getString("javaHome"));
                                    nodeEnv.setJavaHome(data.getString("javaHome"));
                                    nodeEnv.setBinPath(data.getString("javaHome") + "/bin");
                                    nodeEnv.setType(data.getString("type"));
                                    nodeEnv.setArch(data.getString("arch"));
                                    nodeEnv.setSource(data.getString("vendor"));
                                    nodeEnv.setEnvName("Java " + data.getString("fullVersion") + " (" + data.getString("vendor") + ")");
                                    nodeEnv.setIsDefault(0);
                                    nodeEnv.setValid(1);
                                    nodeEnv.setRemark("自动安装");
                                    nodeEnv.setCreateTime(DateUtils.getNowDate());

                                    insertNodeEnv(nodeEnv);
                                    log.info("Java环境已保存到数据库: nodeId={}, version={}", nodeId, data.getString("version"));
                                } else {
                                    NodeEnv existingEnv = existingEnvs.get(0);
                                    existingEnv.setPath(data.getString("javaHome"));
                                    existingEnv.setJavaHome(data.getString("javaHome"));
                                    existingEnv.setBinPath(data.getString("javaHome") + "/bin");
                                    existingEnv.setType(data.getString("type"));
                                    existingEnv.setArch(data.getString("arch"));
                                    existingEnv.setSource(data.getString("vendor"));
                                    existingEnv.setEnvName("Java " + data.getString("fullVersion") + " (" + data.getString("vendor") + ")");
                                    existingEnv.setValid(1);
                                    existingEnv.setUpdateTime(DateUtils.getNowDate());

                                    updateNodeEnv(existingEnv);
                                    log.info("Java环境已更新: nodeId={}, version={}", nodeId, data.getString("version"));
                                }
                            }
                        }
                    }
                }

                emitter.complete();
            }
        } catch (Exception e) {
            log.error("安装Java环境失败", e);
            throw e;
        }
    }
}
