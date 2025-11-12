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
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 实例管理Service业务层处理
 *
 * @author ruoyi
 * @date 2025-10-28
 */
@Service
public class NodeMinecraftServerServiceImpl implements INodeMinecraftServerService {
    @Autowired
    private NodeMinecraftServerMapper nodeMinecraftServerMapper;

    @Autowired
    private NodeServerMapper nodeServerMapper;

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
    public int insertNodeMinecraftServer(NodeMinecraftServer nodeMinecraftServer) {
        nodeMinecraftServer.setCreateTime(DateUtils.getNowDate());

        // 创建远程实例
        ServerInstances instances = new ServerInstances();
        instances.setInstanceName(nodeMinecraftServer.getName());
        instances.setCoreType(nodeMinecraftServer.getCoreType());
        instances.setFilePath(nodeMinecraftServer.getServerPath());
        instances.setVersion(nodeMinecraftServer.getVersion());

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
    public int updateNodeMinecraftServer(NodeMinecraftServer nodeMinecraftServer) {
        nodeMinecraftServer.setUpdateTime(DateUtils.getNowDate());
        return nodeMinecraftServerMapper.updateNodeMinecraftServer(nodeMinecraftServer);
    }

    /**
     * 批量删除实例管理
     *
     * @param ids 需要删除的实例管理主键
     * @return 结果
     */
    @Override
    public int deleteNodeMinecraftServerByIds(Long[] ids) {
        return nodeMinecraftServerMapper.deleteNodeMinecraftServerByIds(ids);
    }

    /**
     * 删除实例管理信息
     *
     * @param id 实例管理主键
     * @return 结果
     */
    @Override
    public int deleteNodeMinecraftServerById(Long id) {
        return nodeMinecraftServerMapper.deleteNodeMinecraftServerById(id);
    }

    // —— 辅助：获取节点信息（带缓存） ——
    private NodeServer getNode(Long id) {
        if (NodeCache.containsKey(id)) {
            return NodeCache.get(id);
        } else {
            NodeServer nodeServer = nodeServerMapper.selectNodeServerById(id);
            if (nodeServer != null) {
                NodeCache.put(id, nodeServer);
                return nodeServer;
            } else {
                return null;
            }
        }
    }

    /**
     * 准备启动脚本：获取默认脚本并处理JVM参数
     *
     * @param providedScript 用户提供的脚本（可能为null或空）
     * @param server         服务器实例信息
     * @return 处理后的启动脚本
     */
    private String prepareStartScript(String providedScript, NodeMinecraftServer server) {
        String script = providedScript;
        if (StringUtils.isEmpty(script)) {
            script = server.getStartStr();
        }
        return processJvmArgs(script, server);
    }

    /**
     * 处理JVM参数：强制替换或添加Xms、Xmx和其他JVM参数
     *
     * @param script 原始启动脚本
     * @param server 服务器实例信息
     * @return 处理后的启动脚本
     */
    private String processJvmArgs(String script, NodeMinecraftServer server) {
        if (script == null || script.isEmpty()) {
            return script;
        }

        // 构建新的JVM参数
        String newXms = "-Xms" + server.getJvmXms() + "M";
        String newXmx = "-Xmx" + server.getJvmXmx() + "M";
        String otherArgs = server.getJvmArgs();

        // 移除脚本中已存在的 -Xms 和 -Xmx 参数
        script = script.replaceAll("-Xms\\d+[MmGgKk]?\\s*", "");
        script = script.replaceAll("-Xmx\\d+[MmGgKk]?\\s*", "");

        // 如果有其他JVM参数，也需要处理（避免重复添加）
        if (StringUtils.isNotEmpty(otherArgs)) {
            // 移除可能重复的其他JVM参数（简单处理：移除常见的JVM参数）
            String[] commonJvmArgs = {
                    "-XX:\\+UseG1GC", "-XX:\\+ParallelRefProcEnabled", "-XX:MaxGCPauseMillis=",
                    "-XX:G1HeapRegionSize=", "-XX:\\+UnlockExperimentalVMOptions", "-XX:\\+DisableExplicitGC",
                    "-XX:-OmitStackTraceInFastThrow", "-XX:G1NewSizePercent=", "-XX:G1MaxNewSizePercent=",
                    "-XX:G1HeapWastePercent=", "-XX:G1MixedGCCountTarget=", "-XX:InitiatingHeapOccupancyPercent=",
                    "-XX:G1MixedGCLiveThresholdPercent=", "-XX:G1RSetUpdatingPauseTimePercent=",
                    "-XX:SurvivorRatio=", "-XX:PerfDisableSharedMem", "-XX:MaxTenuringThreshold=",
                    "-Dusing.aikars.flags=", "-Daikars.new.flags="
            };

            for (String arg : commonJvmArgs) {
                if (arg.endsWith("=")) {
                    // 带值的参数，使用正则移除
                    script = script.replaceAll(arg.replace("+", "\\+").replace(".", "\\.") + "\\d+\\s*", "");
                } else {
                    // 不带值的参数，直接移除
                    script = script.replace(arg + " ", "").replace(arg, "");
                }
            }
        }

        // 找到 java 命令的位置（支持完整路径、引号和 .exe 后缀）
        // 匹配模式：可能包含引号、路径分隔符（/ 或 \）和可选的 .exe 后缀
        int javaIndex = -1;
        int insertPos = -1;

        // 使用正则表达式查找 java 或 java.exe（可能带完整路径和引号）
        // 匹配: java 或 java.exe 或 /path/to/java 或 'C:\path\to\java.exe' 或 "/path/to/java"
        Pattern pattern = Pattern.compile("(['\"]?)([^\\s'\"]*[/\\\\])?java(\\.exe)?\\1(?=\\s|$)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(script);

        if (matcher.find()) {
            javaIndex = matcher.start();
            insertPos = matcher.end();

            // 跳过 java 命令后面的空格
            while (insertPos < script.length() && script.charAt(insertPos) == ' ') {
                insertPos++;
            }

            StringBuilder sb = new StringBuilder(script);
            String jvmParams = " " + newXms + " " + newXmx;
            if (StringUtils.isNotEmpty(otherArgs)) {
                jvmParams += " " + otherArgs.trim();
            }
            jvmParams += " ";

            sb.insert(insertPos, jvmParams);
            script = sb.toString();
        } else {
            // 如果没有找到 java 命令，直接在开头添加（不太可能出现这种情况）
            String jvmParams = newXms + " " + newXmx;
            if (StringUtils.isNotEmpty(otherArgs)) {
                jvmParams += " " + otherArgs.trim();
            }
            script = jvmParams + " " + script;
        }

        // 清理多余的空格
        script = script.replaceAll("\\s+", " ").trim();

        return script;
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
}
