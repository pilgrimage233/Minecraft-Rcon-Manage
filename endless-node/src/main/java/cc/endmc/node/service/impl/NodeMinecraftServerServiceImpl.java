package cc.endmc.node.service.impl;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.node.common.NodeCache;
import cc.endmc.node.domain.NodeMinecraftServer;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.mapper.NodeMinecraftServerMapper;
import cc.endmc.node.mapper.NodeServerMapper;
import cc.endmc.node.model.ServerInstances;
import cc.endmc.node.service.INodeMinecraftServerService;
import cc.endmc.node.utils.ApiUtil;
import cc.endmc.node.utils.NodeHttpUtil;
import cc.endmc.node.utils.StartScriptBuilder;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实例管理Service业务层处理
 *
 * @author ruoyi
 * @date 2025-10-28
 */
@Service
@RequiredArgsConstructor
public class NodeMinecraftServerServiceImpl implements INodeMinecraftServerService {

    private final NodeMinecraftServerMapper nodeMinecraftServerMapper;
    private final NodeServerMapper nodeServerMapper;

    /**
     * 查询实例管理
     *
     * @param id 实例管理主键
     * @return 实例管理
     */
    @Override
    public NodeMinecraftServer selectNodeMinecraftServerById(Long id) {
        return nodeMinecraftServerMapper.selectNodeMinecraftServerById(id);
    }

    /**
     * 查询实例管理列表
     *
     * @param nodeMinecraftServer 实例管理
     * @return 实例管理
     */
    @Override
    public List<NodeMinecraftServer> selectNodeMinecraftServerList(NodeMinecraftServer nodeMinecraftServer) {
        return nodeMinecraftServerMapper.selectNodeMinecraftServerList(nodeMinecraftServer);
    }

    /**
     * 新增实例管理
     *
     * @param nodeMinecraftServer 实例管理
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertNodeMinecraftServer(NodeMinecraftServer nodeMinecraftServer) {
        nodeMinecraftServer.setCreateTime(DateUtils.getNowDate());

        // 创建远程实例
        ServerInstances instances = new ServerInstances();
        instances.setInstanceName(nodeMinecraftServer.getName());
        instances.setCoreType(nodeMinecraftServer.getCoreType());
        instances.setFilePath(nodeMinecraftServer.getServerPath());
        instances.setVersion(nodeMinecraftServer.getVersion());
        instances.setMemoryMb(Integer.valueOf(nodeMinecraftServer.getJvmXmx()));

        String args = "-Xms" + nodeMinecraftServer.getJvmXms() + "M " +
                "-Xmx" + nodeMinecraftServer.getJvmXmx() + "M " +
                nodeMinecraftServer.getJvmArgs();

        instances.setJvmArgs(args);
        final NodeServer node = NodeCache.get(nodeMinecraftServer.getNodeId());
        final HttpResponse execute = NodeHttpUtil.createPost(node, ApiUtil.getCreateInstanceApi(node))
                .body(JSONObject.toJSONString(instances))
                .execute();

        if (execute.isOk()) {
            final JSONObject body = JSONObject.parseObject(execute.body(), JSONObject.class);

            if (Boolean.TRUE.equals(body.getBoolean("success"))) {
                final Integer serverId = body.getInteger("serverId");
                nodeMinecraftServer.setNodeInstancesId(serverId);

                return nodeMinecraftServerMapper.insertNodeMinecraftServer(nodeMinecraftServer);
            }
        }
        return 0;
    }

    /**
     * 修改实例管理
     *
     * @param nodeMinecraftServer 实例管理
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateNodeMinecraftServer(NodeMinecraftServer nodeMinecraftServer) {
        nodeMinecraftServer.setUpdateTime(DateUtils.getNowDate());

        // 如果有节点实例ID，同步更新节点端实例
        if (nodeMinecraftServer.getNodeInstancesId() != null && nodeMinecraftServer.getNodeId() != null) {
            try {
                NodeServer node = getNode(nodeMinecraftServer.getNodeId());
                if (node != null) {
                    // 构建更新数据
                    ServerInstances instances = new ServerInstances();
                    instances.setId(nodeMinecraftServer.getNodeInstancesId());
                    instances.setInstanceName(nodeMinecraftServer.getName());
                    instances.setCoreType(nodeMinecraftServer.getCoreType());
                    instances.setFilePath(nodeMinecraftServer.getServerPath());
                    instances.setVersion(nodeMinecraftServer.getVersion());
                    instances.setMemoryMb(Integer.valueOf(nodeMinecraftServer.getJvmXmx()));

                    String args = "-Xms" + nodeMinecraftServer.getJvmXms() + "M " +
                            "-Xmx" + nodeMinecraftServer.getJvmXmx() + "M " +
                            nodeMinecraftServer.getJvmArgs();
                    instances.setJvmArgs(args);

                    // 调用节点API更新实例
                    HttpResponse execute = NodeHttpUtil.createPut(node,
                                    ApiUtil.getUpdateInstanceApi(node, nodeMinecraftServer.getNodeInstancesId()))
                            .body(JSONObject.toJSONString(instances))
                            .execute();

                    if (!execute.isOk()) {
                        throw new RuntimeException("更新节点实例失败: " + execute.body());
                    }

                    JSONObject body = JSONObject.parseObject(execute.body(), JSONObject.class);
                    if (!Boolean.TRUE.equals(body.getBoolean("success"))) {
                        String error = body.getString("error");
                        throw new RuntimeException("更新节点实例失败: " + (error != null ? error : "未知错误"));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("同步更新节点实例失败: " + e.getMessage(), e);
            }
        }
        
        return nodeMinecraftServerMapper.updateNodeMinecraftServer(nodeMinecraftServer);
    }

    /**
     * 批量删除实例管理
     *
     * @param ids 需要删除的实例管理主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteNodeMinecraftServerByIds(Long[] ids) {
        // 先删除节点端实例，再删除数据库记录
        for (Long id : ids) {
            NodeMinecraftServer server = nodeMinecraftServerMapper.selectNodeMinecraftServerById(id);
            if (server != null && server.getNodeInstancesId() != null && server.getNodeId() != null) {
                try {
                    NodeServer node = getNode(server.getNodeId());
                    if (node != null) {
                        // 调用节点API删除实例
                        HttpResponse execute = NodeHttpUtil.createDelete(node,
                                        ApiUtil.getDeleteInstanceApi(node, server.getNodeInstancesId()))
                                .execute();

                        if (execute.isOk()) {
                            JSONObject body = JSONObject.parseObject(execute.body(), JSONObject.class);
                            if (!Boolean.TRUE.equals(body.getBoolean("success"))) {
                                String error = body.getString("error");
                                throw new RuntimeException("删除节点实例失败: " + (error != null ? error : "未知错误"));
                            }
                        } else {
                            throw new RuntimeException("删除节点实例失败: " + execute.body());
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("删除服务器 " + id + " 的节点实例失败: " + e.getMessage(), e);
                }
            }
        }
        
        return nodeMinecraftServerMapper.deleteNodeMinecraftServerByIds(ids);
    }

    /**
     * 删除实例管理信息
     *
     * @param id 实例管理主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteNodeMinecraftServerById(Long id) {
        // 先删除节点端实例，再删除数据库记录
        NodeMinecraftServer server = nodeMinecraftServerMapper.selectNodeMinecraftServerById(id);
        if (server != null && server.getNodeInstancesId() != null && server.getNodeId() != null) {
            try {
                NodeServer node = getNode(server.getNodeId());
                if (node != null) {
                    // 调用节点API删除实例
                    HttpResponse execute = NodeHttpUtil.createDelete(node,
                                    ApiUtil.getDeleteInstanceApi(node, server.getNodeInstancesId()))
                            .execute();

                    if (execute.isOk()) {
                        JSONObject body = JSONObject.parseObject(execute.body(), JSONObject.class);
                        if (!Boolean.TRUE.equals(body.getBoolean("success"))) {
                            String error = body.getString("error");
                            throw new RuntimeException("删除节点实例失败: " + (error != null ? error : "未知错误"));
                        }
                    } else {
                        throw new RuntimeException("删除节点实例失败: " + execute.body());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("删除服务器 " + id + " 的节点实例失败: " + e.getMessage(), e);
            }
        }
        
        return nodeMinecraftServerMapper.deleteNodeMinecraftServerById(id);
    }

    // —— 辅助：获取节点信息（带缓存） ——
    private NodeServer getNode(Long id) {
        return NodeCache.getOrLoad(id, nodeServerMapper::selectNodeServerById);
    }

    /**
     * 准备启动脚本：获取默认脚本并处理JVM参数
     *
     * @param providedScript 用户提供的脚本（可能为null或空）
     * @param server         服务器实例信息
     * @return 处理后的启动脚本
     */
    private String prepareStartScript(String providedScript, NodeMinecraftServer server) {
        return StartScriptBuilder.prepareStartScript(providedScript, server);
    }

    // —— 节点端实例操控
    @Override
    public AjaxResult listInstances(Long nodeId) {
        NodeServer nodeServer = getNode(nodeId);
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }
        try {
            HttpResponse execute = NodeHttpUtil.createGet(nodeServer, ApiUtil.getListInstancesApi(nodeServer))
                    .execute();
            if (!execute.isOk()) {
                return AjaxResult.error("获取实例列表失败: " + execute.body());
            }
            JSONObject json = JSONObject.parseObject(execute.body());
            return AjaxResult.success(json);
        } catch (Exception e) {
            return AjaxResult.error("获取实例列表失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult createInstance(Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("server")) {
            return AjaxResult.error("缺少必要参数");
        }
        Integer id = (Integer) params.get("id");
        Object serverBody = params.get("server");

        NodeServer nodeServer = getNode(id.longValue());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }
        try {
            String body = JSONObject.toJSONString(serverBody);
            HttpResponse execute = NodeHttpUtil.createPost(nodeServer, ApiUtil.getCreateInstanceApi(nodeServer))
                    .body(body)
                    .execute();
            if (!execute.isOk()) {
                return AjaxResult.error("创建实例失败: " + execute.body());
            }
            JSONObject json = JSONObject.parseObject(execute.body());
            if (Boolean.TRUE.equals(json.getBoolean("success"))) {
                return AjaxResult.success(json);
            }
            return AjaxResult.error(json.getString("error") != null ? json.getString("error") : json.getString("message"));
        } catch (Exception e) {
            return AjaxResult.error("创建实例失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult startInstance(Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("serverId")) {
            return AjaxResult.error("缺少必要参数");
        }
        Integer id = (Integer) params.get("id");
        Integer serverId = (Integer) params.get("serverId");
        String script = params.get("script") == null ? null : String.valueOf(params.get("script"));

        final NodeMinecraftServer nodeMinecraftServer = selectNodeMinecraftServerById(serverId.longValue());

        // 准备启动脚本
        script = prepareStartScript(script, nodeMinecraftServer);

        NodeServer nodeServer = getNode(id.longValue());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }
        try {
            HttpRequest request = NodeHttpUtil.createPost(nodeServer, ApiUtil.getStartInstanceApi(nodeServer, nodeMinecraftServer.getNodeInstancesId()));
            if (script != null) {
                JSONObject body = new JSONObject();
                body.put("script", script);
                request.body(body.toJSONString());
            }
            HttpResponse execute = request.execute();
            if (!execute.isOk()) {
                return AjaxResult.error("启动实例失败: " + execute.body());
            }
            JSONObject json = JSONObject.parseObject(execute.body());
            if (Boolean.TRUE.equals(json.getBoolean("success"))) {
                // 更新最后启动时间
                nodeMinecraftServer.setLastStartTime(DateUtils.getNowDate());
                nodeMinecraftServer.setStatus("1");
                nodeMinecraftServerMapper.updateNodeMinecraftServer(nodeMinecraftServer);
                return AjaxResult.success(json);
            }
            return AjaxResult.error(json.getString("error") != null ? json.getString("error") : json.getString("message"));
        } catch (Exception e) {
            return AjaxResult.error("启动实例失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult stopInstance(Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("serverId")) {
            return AjaxResult.error("缺少必要参数");
        }
        Integer id = (Integer) params.get("id");
        Integer serverId = (Integer) params.get("serverId");
        String script = params.get("script") == null ? null : String.valueOf(params.get("script"));
        final NodeMinecraftServer nodeMinecraftServer = selectNodeMinecraftServerById(serverId.longValue());
        if (script == null || script.isEmpty()) {
            script = "stop";
        }

        NodeServer nodeServer = getNode(id.longValue());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }
        try {
            HttpRequest request = NodeHttpUtil.createPost(nodeServer, ApiUtil.getStopInstanceApi(nodeServer, nodeMinecraftServer.getNodeInstancesId()));
            JSONObject body = new JSONObject();
            body.put("script", script);
            request.body(body.toJSONString());
            HttpResponse execute = request.execute();
            if (!execute.isOk()) {
                return AjaxResult.error("停止实例失败: " + execute.body());
            }
            JSONObject json = JSONObject.parseObject(execute.body());
            if (Boolean.TRUE.equals(json.getBoolean("success"))) {
                // 更新最后停止时间
                nodeMinecraftServer.setLastStopTime(DateUtils.getNowDate());
                nodeMinecraftServer.setStatus("2");
                nodeMinecraftServerMapper.updateNodeMinecraftServer(nodeMinecraftServer);
                return AjaxResult.success(json);
            }
            return AjaxResult.error(json.getString("error") != null ? json.getString("error") : json.getString("message"));
        } catch (Exception e) {
            return AjaxResult.error("停止实例失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult restartInstance(Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("serverId")) {
            return AjaxResult.error("缺少必要参数");
        }
        Integer id = (Integer) params.get("id");
        Integer serverId = (Integer) params.get("serverId");
        String stopScript = params.get("stopScript") == null ? null : String.valueOf(params.get("stopScript"));
        String startScript = params.get("startScript") == null ? null : String.valueOf(params.get("startScript"));

        final NodeMinecraftServer nodeMinecraftServer = selectNodeMinecraftServerById(serverId.longValue());

        if (StringUtils.isEmpty(stopScript)) {
            stopScript = "stop"; // 默认停止脚本
        }

        // 准备启动脚本
        startScript = prepareStartScript(startScript, nodeMinecraftServer);

        NodeServer nodeServer = getNode(id.longValue());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }
        try {
            HttpRequest request = NodeHttpUtil.createPost(nodeServer, ApiUtil.getRestartInstanceApi(nodeServer, nodeMinecraftServer.getNodeInstancesId()))
                    .timeout(200000); // 重启操作可能较慢，适当增加超时时间
            if (stopScript != null || startScript != null) {
                JSONObject body = new JSONObject();
                if (stopScript != null) body.put("stopScript", stopScript);
                if (startScript != null) body.put("startScript", startScript);
                request.body(body.toJSONString());
            }
            HttpResponse execute = request.execute();
            if (!execute.isOk()) {
                return AjaxResult.error("重启实例失败: " + execute.body());
            }
            JSONObject json = JSONObject.parseObject(execute.body());
            if (Boolean.TRUE.equals(json.getBoolean("success"))) {
                // 更新最后启动时间
                nodeMinecraftServer.setLastStartTime(DateUtils.getNowDate());
                nodeMinecraftServerMapper.updateNodeMinecraftServer(nodeMinecraftServer);
                return AjaxResult.success(json);
            }
            return AjaxResult.error(json.getString("error") != null ? json.getString("error") : json.getString("message"));
        } catch (Exception e) {
            return AjaxResult.error("重启实例失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult killInstance(Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("serverId")) {
            return AjaxResult.error("缺少必要参数");
        }
        Integer id = (Integer) params.get("id");
        Integer serverId = (Integer) params.get("serverId");

        NodeServer nodeServer = getNode(id.longValue());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }

        final NodeMinecraftServer nodeMinecraftServer = selectNodeMinecraftServerById(serverId.longValue());
        try {
            HttpResponse execute = NodeHttpUtil.createPost(nodeServer, ApiUtil.getKillInstanceApi(nodeServer, nodeMinecraftServer.getNodeInstancesId()))
                    .execute();
            if (!execute.isOk()) {
                return AjaxResult.error("强制终止实例失败: " + execute.body());
            }
            JSONObject json = JSONObject.parseObject(execute.body());
            if (Boolean.TRUE.equals(json.getBoolean("success"))) {
                return AjaxResult.success(json);
            }
            return AjaxResult.error(json.getString("error") != null ? json.getString("error") : json.getString("message"));
        } catch (Exception e) {
            return AjaxResult.error("强制终止实例失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult deleteInstance(Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("serverId")) {
            return AjaxResult.error("缺少必要参数");
        }
        Integer id = (Integer) params.get("id");
        Integer serverId = (Integer) params.get("serverId");

        NodeServer nodeServer = getNode(id.longValue());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }
        final NodeMinecraftServer nodeMinecraftServer = selectNodeMinecraftServerById(serverId.longValue());
        try {
            HttpResponse execute = NodeHttpUtil.createDelete(nodeServer, ApiUtil.getDeleteInstanceApi(nodeServer, nodeMinecraftServer.getNodeInstancesId()))
                    .execute();
            if (!execute.isOk()) {
                return AjaxResult.error("删除实例失败: " + execute.body());
            }
            JSONObject json = JSONObject.parseObject(execute.body());
            if (Boolean.TRUE.equals(json.getBoolean("success"))) {
                return AjaxResult.success(json);
            }
            return AjaxResult.error(json.getString("error") != null ? json.getString("error") : json.getString("message"));
        } catch (Exception e) {
            return AjaxResult.error("删除实例失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult getConsole(Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("serverId")) {
            return AjaxResult.error("缺少必要参数");
        }
        Integer id = (Integer) params.get("id");
        Integer serverId = (Integer) params.get("serverId");

        NodeServer nodeServer = getNode(id.longValue());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }
        // 拼接WebSocket地址
        Map<String, String> data = new HashMap<>();
        data.put("wsUrl", ApiUtil.getWebSocketUrl(nodeServer));
        data.put("subscribe", ApiUtil.WEBSOCKET_SUBSCRIBE);
        data.put("console", ApiUtil.WEBSOCKET_CONSOLE);
        data.put("token", nodeServer.getToken());

        return AjaxResult.success(data);
    }

    @Override
    public AjaxResult getConsoleHistory(Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("serverId")) {
            return AjaxResult.error("缺少必要参数");
        }
        Integer id = (Integer) params.get("id");
        Integer serverId = (Integer) params.get("serverId");

        NodeServer nodeServer = getNode(id.longValue());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }
        final NodeMinecraftServer nodeMinecraftServer = selectNodeMinecraftServerById(serverId.longValue());
        if (nodeMinecraftServer == null) {
            return AjaxResult.error("服务器实例不存在");
        }
        try {
            HttpResponse execute = NodeHttpUtil.createGet(nodeServer, ApiUtil.getConsoleHistoryInstanceApi(nodeServer, nodeMinecraftServer.getNodeInstancesId()))
                    .execute();
            if (!execute.isOk()) {
                return AjaxResult.error("获取控制台历史日志失败: " + execute.body());
            }
            JSONObject json = JSONObject.parseObject(execute.body());
            return AjaxResult.success(json);
        } catch (Exception e) {
            return AjaxResult.error("获取控制台历史日志失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult sendCommand(Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("serverId") || !params.containsKey("command")) {
            return AjaxResult.error("缺少必要参数");
        }
        Integer id = (Integer) params.get("id");
        Integer serverId = (Integer) params.get("serverId");
        String command = String.valueOf(params.get("command"));

        NodeServer nodeServer = getNode(id.longValue());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }
        final NodeMinecraftServer nodeMinecraftServer = selectNodeMinecraftServerById(serverId.longValue());
        try {
            JSONObject body = new JSONObject();
            body.put("command", command);
            HttpResponse execute = NodeHttpUtil.createPost(nodeServer, ApiUtil.getCommandInstanceApi(nodeServer, nodeMinecraftServer.getNodeInstancesId()))
                    .body(body.toJSONString())
                    .execute();
            if (!execute.isOk()) {
                return AjaxResult.error("发送命令失败: " + execute.body());
            }
            JSONObject json = JSONObject.parseObject(execute.body());
            if (Boolean.TRUE.equals(json.getBoolean("success"))) {
                return AjaxResult.success(json);
            }
            return AjaxResult.error(json.getString("error") != null ? json.getString("error") : json.getString("message"));
        } catch (Exception e) {
            return AjaxResult.error("发送命令失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult getStatus(Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("serverId")) {
            return AjaxResult.error("缺少必要参数");
        }
        Integer id = (Integer) params.get("id");
        Integer serverId = (Integer) params.get("serverId");

        NodeServer nodeServer = getNode(id.longValue());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }
        final NodeMinecraftServer nodeMinecraftServer = selectNodeMinecraftServerById(serverId.longValue());
        if (nodeMinecraftServer == null) {
            return AjaxResult.error("服务器实例不存在");
        }
        try {
            HttpResponse execute = NodeHttpUtil.createGet(nodeServer, ApiUtil.getStatusInstanceApi(nodeServer, nodeMinecraftServer.getNodeInstancesId()))
                    .execute();
            if (!execute.isOk()) {
                return AjaxResult.error("获取服务器状态失败: " + execute.body());
            }
            JSONObject json = JSONObject.parseObject(execute.body());
            return AjaxResult.success(json);
        } catch (Exception e) {
            return AjaxResult.error("获取服务器状态失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult getServerPlayers(Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("serverId")) {
            return AjaxResult.error("缺少必要参数");
        }
        Integer id = (Integer) params.get("id");
        Integer serverId = (Integer) params.get("serverId");

        final NodeMinecraftServer nodeMinecraftServer = selectNodeMinecraftServerById(serverId.longValue());
        if (nodeMinecraftServer == null) {
            return AjaxResult.error("服务器实例不存在");
        }

        NodeServer nodeServer = getNode(id.longValue());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }

        try {
            HttpResponse execute = NodeHttpUtil.createGet(nodeServer,
                            ApiUtil.getServerPlayersApi(nodeServer, nodeMinecraftServer.getNodeInstancesId()))
                    .execute();
            if (!execute.isOk()) {
                return AjaxResult.error("获取玩家信息失败: " + execute.body());
            }
            JSONObject json = JSONObject.parseObject(execute.body());
            if (Boolean.TRUE.equals(json.getBoolean("success"))) {
                return AjaxResult.success(json);
            }
            return AjaxResult.error(json.getString("error") != null ? json.getString("error") : json.getString("message"));
        } catch (Exception e) {
            return AjaxResult.error("获取玩家信息失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult playerAction(Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("serverId") || !params.containsKey("playerName")) {
            return AjaxResult.error("缺少必要参数");
        }
        Integer id = (Integer) params.get("id");
        Integer serverId = (Integer) params.get("serverId");
        String playerName = (String) params.get("playerName");

        final NodeMinecraftServer nodeMinecraftServer = selectNodeMinecraftServerById(serverId.longValue());
        if (nodeMinecraftServer == null) {
            return AjaxResult.error("服务器实例不存在");
        }

        NodeServer nodeServer = getNode(id.longValue());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }

        try {
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("action", params.get("action"));
            requestBody.put("reason", params.get("reason"));

            HttpResponse execute = NodeHttpUtil.createPost(nodeServer,
                            ApiUtil.getPlayerActionApi(nodeServer, nodeMinecraftServer.getNodeInstancesId(), playerName))
                    .body(requestBody.toJSONString())
                    .execute();
            if (!execute.isOk()) {
                return AjaxResult.error("执行玩家操作失败: " + execute.body());
            }
            JSONObject json = JSONObject.parseObject(execute.body());
            if (Boolean.TRUE.equals(json.getBoolean("success"))) {
                return AjaxResult.success(json);
            }
            return AjaxResult.error(json.getString("error") != null ? json.getString("error") : json.getString("message"));
        } catch (Exception e) {
            return AjaxResult.error("执行玩家操作失败: " + e.getMessage());
        }
    }

    @Override
    public AjaxResult queryDiagnostic(Map<String, Object> params) {
        if (!params.containsKey("id") || !params.containsKey("serverId")) {
            return AjaxResult.error("缺少必要参数");
        }
        Integer id = (Integer) params.get("id");
        Integer serverId = (Integer) params.get("serverId");

        final NodeMinecraftServer nodeMinecraftServer = selectNodeMinecraftServerById(serverId.longValue());
        if (nodeMinecraftServer == null) {
            return AjaxResult.error("服务器实例不存在");
        }

        NodeServer nodeServer = getNode(id.longValue());
        if (nodeServer == null) {
            return AjaxResult.error("节点服务器不存在");
        }

        try {
            HttpResponse execute = NodeHttpUtil.createGet(nodeServer,
                            ApiUtil.getQueryDiagnosticApi(nodeServer, nodeMinecraftServer.getNodeInstancesId()))
                    .execute();
            if (!execute.isOk()) {
                return AjaxResult.error("Query诊断失败: " + execute.body());
            }
            JSONObject json = JSONObject.parseObject(execute.body());
            return AjaxResult.success(json);
        } catch (Exception e) {
            return AjaxResult.error("Query诊断失败: " + e.getMessage());
        }
    }

    /**
     * 仅更新服务器状态（不触发节点API同步）
     * 用于定时任务同步状态
     *
     * @param id     服务器ID
     * @param status 新状态
     * @return 结果
     */
    @Override
    public int updateServerStatusOnly(Long id, String status) {
        NodeMinecraftServer server = new NodeMinecraftServer();
        server.setId(id);
        server.setStatus(status);
        server.setUpdateTime(DateUtils.getNowDate());
        return nodeMinecraftServerMapper.updateNodeMinecraftServer(server);
    }
}
