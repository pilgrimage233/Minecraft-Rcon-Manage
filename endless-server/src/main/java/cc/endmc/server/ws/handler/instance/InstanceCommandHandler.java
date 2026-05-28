package cc.endmc.server.ws.handler.instance;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.node.domain.NodeMinecraftServer;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.service.INodeMinecraftServerService;
import cc.endmc.node.service.INodeServerService;
import cc.endmc.server.ws.BotClient;
import cc.endmc.server.ws.QQMessage;
import cc.endmc.server.ws.handler.BaseCommandHandler;
import cc.endmc.server.ws.handler.CommandRegistry;
import cc.endmc.server.ws.helper.BotMessageHelper;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实例管理命令处理器
 * 处理游戏服务器实例相关的命令
 */
@Slf4j
public class InstanceCommandHandler extends BaseCommandHandler {

    private final INodeMinecraftServerService nodeMinecraftServerService;
    private final INodeServerService nodeServerService;

    public InstanceCommandHandler(BotClient botClient, RedisCache redisCache,
                                  INodeMinecraftServerService nodeMinecraftServerService,
                                  INodeServerService nodeServerService) {
        super(botClient, redisCache);
        this.nodeMinecraftServerService = nodeMinecraftServerService;
        this.nodeServerService = nodeServerService;
    }

    /**
     * 注册实例管理命令到命令注册器
     */
    public void registerCommands(CommandRegistry registry) {
        registry.register("实例列表", this::handleInstanceList, "instances", "inst");
        registry.register("启动实例", this::handleStartInstance, "start", "run");
        registry.register("停止实例", this::handleStopInstance, "stop", "kill");
        registry.register("重启实例", this::handleRestartInstance, "restart", "reboot");
        registry.register("实例状态", this::handleInstanceStatus, "inststatus", "is");
        registry.register("实例日志", this::handleInstanceLogs, "logs", "log");
        registry.register("实例命令", this::handleInstanceCommand, "instcmd", "ic");
        registry.register("节点状态", this::handleNodeStatus, "nodestatus", "ns");
    }

    /**
     * 处理实例列表查询命令
     */
    public void handleInstanceList(QQMessage message) {
        executeWithPermissionCheck(message, false, () -> {
            String base = getAtPrefix(message);

            // 查询所有实例
            NodeMinecraftServer query = new NodeMinecraftServer();
            List<NodeMinecraftServer> instances = nodeMinecraftServerService.selectNodeMinecraftServerList(query);

            if (instances.isEmpty()) {
                sendMessage(message, base + " 当前没有任何游戏服务器实例。");
                return;
            }

            // 构建返回消息
            StringBuilder response = new StringBuilder(base + " 游戏服务器实例列表：\n\n");

            for (NodeMinecraftServer instance : instances) {
                response.append("ID: ").append(instance.getId()).append("\n");
                response.append("名称: ").append(instance.getName()).append("\n");
                response.append("版本: ").append(instance.getVersion()).append("\n");
                response.append("核心: ").append(instance.getCoreType()).append("\n");
                response.append("节点ID: ").append(instance.getNodeId()).append("\n");
                response.append("节点实例ID: ").append(instance.getNodeInstancesId()).append("\n\n");
            }

            sendMessage(message, response.toString());
        });
    }

    /**
     * 处理启动实例命令
     */
    public void handleStartInstance(QQMessage message) {
        executeWithPermissionCheck(message, false, () -> {
            String base = getAtPrefix(message);
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：启动实例 <实例ID>");
                return;
            }

            try {
                Long instanceId = Long.parseLong(parts[1]);
                NodeMinecraftServer instance = nodeMinecraftServerService.selectNodeMinecraftServerById(instanceId);

                if (instance == null) {
                    sendMessage(message, base + " 未找到ID为 " + instanceId + " 的实例。");
                    return;
                }

                // 调用启动接口
                Map<String, Object> params = new HashMap<>();
                params.put("id", instance.getNodeId().intValue());
                params.put("serverId", instanceId.intValue());

                AjaxResult result = nodeMinecraftServerService.startInstance(params);

                if (result.get("code").equals(200)) {
                    sendMessage(message, base + " 实例 " + instance.getName() + " 启动成功！");
                } else {
                    sendMessage(message, base + " 实例 " + instance.getName() + " 启动失败：" + result.get("msg"));
                }
            } catch (NumberFormatException e) {
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "实例ID格式错误，必须是数字。"));
            }
        });
    }

    /**
     * 处理停止实例命令
     */
    public void handleStopInstance(QQMessage message) {
        executeWithPermissionCheck(message, false, () -> {
            String base = getAtPrefix(message);
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：停止实例 <实例ID>");
                return;
            }

            try {
                Long instanceId = Long.parseLong(parts[1]);
                NodeMinecraftServer instance = nodeMinecraftServerService.selectNodeMinecraftServerById(instanceId);

                if (instance == null) {
                    sendMessage(message, base + " 未找到ID为 " + instanceId + " 的实例。");
                    return;
                }

                // 调用停止接口
                Map<String, Object> params = new HashMap<>();
                params.put("id", instance.getNodeId().intValue());
                params.put("serverId", instanceId.intValue());

                AjaxResult result = nodeMinecraftServerService.stopInstance(params);

                if (result.get("code").equals(200)) {
                    sendMessage(message, base + " 实例 " + instance.getName() + " 停止成功！");
                } else {
                    sendMessage(message, base + " 实例 " + instance.getName() + " 停止失败：" + result.get("msg"));
                }
            } catch (NumberFormatException e) {
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "实例ID格式错误，必须是数字。"));
            }
        });
    }

    /**
     * 处理重启实例命令
     */
    public void handleRestartInstance(QQMessage message) {
        executeWithPermissionCheck(message, false, () -> {
            String base = getAtPrefix(message);
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：重启实例 <实例ID>");
                return;
            }

            try {
                Long instanceId = Long.parseLong(parts[1]);
                NodeMinecraftServer instance = nodeMinecraftServerService.selectNodeMinecraftServerById(instanceId);

                if (instance == null) {
                    sendMessage(message, base + " 未找到ID为 " + instanceId + " 的实例。");
                    return;
                }

                // 调用重启接口
                Map<String, Object> params = new HashMap<>();
                params.put("id", instance.getNodeId().intValue());
                params.put("serverId", instanceId.intValue());

                AjaxResult result = nodeMinecraftServerService.restartInstance(params);

                if (result.get("code").equals(200)) {
                    sendMessage(message, base + " 实例 " + instance.getName() + " 重启成功！");
                } else {
                    sendMessage(message, base + " 实例 " + instance.getName() + " 重启失败：" + result.get("msg"));
                }
            } catch (NumberFormatException e) {
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "实例ID格式错误，必须是数字。"));
            }
        });
    }

    /**
     * 处理实例状态查询命令
     */
    public void handleInstanceStatus(QQMessage message) {
        executeWithPermissionCheck(message, false, () -> {
            String base = getAtPrefix(message);
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：实例状态 <实例ID>");
                return;
            }

            try {
                Long instanceId = Long.parseLong(parts[1]);
                NodeMinecraftServer instance = nodeMinecraftServerService.selectNodeMinecraftServerById(instanceId);

                if (instance == null) {
                    sendMessage(message, base + " 未找到ID为 " + instanceId + " 的实例。");
                    return;
                }

                // 调用状态查询接口
                Map<String, Object> params = new HashMap<>();
                params.put("id", instance.getNodeId().intValue());
                params.put("serverId", instanceId.intValue());

                AjaxResult result = nodeMinecraftServerService.getStatus(params);

                if (result.get("code").equals(200)) {
                    JSONObject data = (JSONObject) result.get("data");
                    StringBuilder response = new StringBuilder(base + " 实例 " + instance.getName() + " 状态信息：\n\n");

                    // 基本信息
                    response.append("━━━━ 基本信息 ━━━━\n");
                    if (data.containsKey("instanceName")) {
                        response.append("实例名称: ").append(data.getString("instanceName")).append("\n");
                    }
                    if (data.containsKey("serverId")) {
                        response.append("实例ID: ").append(data.get("serverId")).append("\n");
                    }
                    if (data.containsKey("status")) {
                        response.append("状态: ").append(data.getString("status")).append("\n");
                    }
                    if (data.containsKey("isRunning")) {
                        response.append("运行中: ").append(data.getBoolean("isRunning") ? "是" : "否").append("\n");
                    }
                    response.append("\n");

                    // 配置信息
                    if (data.containsKey("config")) {
                        JSONObject config = data.getJSONObject("config");
                        response.append("━━━━ 配置信息 ━━━━\n");
                        if (config.containsKey("version")) {
                            response.append("游戏版本: ").append(config.getString("version")).append("\n");
                        }
                        if (config.containsKey("coreType")) {
                            response.append("核心类型: ").append(config.getString("coreType")).append("\n");
                        }
                        if (config.containsKey("port")) {
                            response.append("端口: ").append(config.get("port")).append("\n");
                        }
                        if (config.containsKey("memoryMb")) {
                            response.append("内存: ").append(config.get("memoryMb")).append("MB\n");
                        }
                        if (config.containsKey("filePath")) {
                            response.append("文件路径: ").append(config.getString("filePath")).append("\n");
                        }
                        response.append("\n");
                    }

                    // 运行时信息
                    if (data.containsKey("runtime")) {
                        JSONObject runtime = data.getJSONObject("runtime");
                        response.append("━━━━ 运行时信息 ━━━━\n");
                        if (runtime.containsKey("runtimeFormatted")) {
                            response.append("运行时长: ").append(runtime.getString("runtimeFormatted")).append("\n");
                        }
                        if (runtime.containsKey("startTime")) {
                            response.append("启动时间: ").append(runtime.getString("startTime")).append("\n");
                        }
                        if (instance.getJavaPath() != null && !instance.getJavaPath().isEmpty()) {
                            response.append("使用Java: ").append(instance.getJavaPath()).append("\n");
                        }
                        response.append("\n");
                    }

                    // 进程信息
                    if (data.containsKey("processInfo")) {
                        JSONObject processInfo = data.getJSONObject("processInfo");
                        response.append("━━━━ 进程信息 ━━━━\n");
                        if (processInfo.containsKey("pid")) {
                            response.append("进程ID: ").append(processInfo.get("pid")).append("\n");
                        }
                        if (processInfo.containsKey("isAlive")) {
                            response.append("进程存活: ").append(processInfo.getBoolean("isAlive") ? "是" : "否").append("\n");
                        }
                        if (processInfo.containsKey("cpuUsage")) {
                            response.append("CPU使用率: ").append(processInfo.get("cpuUsage")).append("%\n");
                        }
                        if (processInfo.containsKey("memoryUsage")) {
                            response.append("内存使用: ").append(processInfo.get("memoryUsage")).append("MB\n");
                        }
                        response.append("\n");
                    }

                    // 时间戳信息
                    if (data.containsKey("timestamps")) {
                        JSONObject timestamps = data.getJSONObject("timestamps");
                        response.append("━━━━ 时间信息 ━━━━\n");
                        if (timestamps.containsKey("createdAt")) {
                            response.append("创建时间: ").append(timestamps.getString("createdAt")).append("\n");
                        }
                        if (timestamps.containsKey("updatedAt")) {
                            response.append("更新时间: ").append(timestamps.getString("updatedAt")).append("\n");
                        }
                    }

                    sendMessage(message, response.toString());
                } else {
                    sendMessage(message, base + " 查询实例状态失败：" + result.get("msg"));
                }
            } catch (NumberFormatException e) {
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "实例ID格式错误，必须是数字。"));
            }
        });
    }

    /**
     * 处理实例日志查询命令
     */
    public void handleInstanceLogs(QQMessage message) {
        executeWithPermissionCheck(message, false, () -> {
            String base = getAtPrefix(message);
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：实例日志 <实例ID> [行数]");
                return;
            }

            try {
                Long instanceId = Long.parseLong(parts[1]);
                int lines = 20; // 默认显示20行

                if (parts.length > 2) {
                    try {
                        lines = Integer.parseInt(parts[2]);
                        if (lines > 100) {
                            lines = 100; // 最多显示100行
                            sendMessage(message, base + " 最多只能显示100行日志，已自动调整。");
                        }
                    } catch (NumberFormatException e) {
                        sendMessage(message, base + " 行数格式错误，使用默认值20行。");
                    }
                }

                NodeMinecraftServer instance = nodeMinecraftServerService.selectNodeMinecraftServerById(instanceId);

                if (instance == null) {
                    sendMessage(message, base + " 未找到ID为 " + instanceId + " 的实例。");
                    return;
                }

                // 调用历史日志接口
                Map<String, Object> params = new HashMap<>();
                params.put("id", instance.getNodeId().intValue());
                params.put("serverId", instanceId.intValue());

                AjaxResult result = nodeMinecraftServerService.getConsoleHistory(params);

                if (result.get("code").equals(200)) {
                    JSONObject data = (JSONObject) result.get("data");
                    JSONArray logs = data.getJSONArray("logs");

                    if (logs == null || logs.isEmpty()) {
                        sendMessage(message, base + " 实例 " + instance.getName() + " 暂无日志。");
                        return;
                    }
                    StringBuilder response = new StringBuilder(base + " 实例 " + instance.getName() + " 最近 " + lines + " 行日志：\n\n");
                    int start = Math.max(0, logs.size() - lines);
                    for (int i = start; i < logs.size(); i++) {
                        response.append(logs.getString(i)).append("\n");
                    }
                    sendMessage(message, response.toString());
                } else {
                    sendMessage(message, base + " 获取实例日志失败：" + result.get("msg"));
                }
            } catch (NumberFormatException e) {
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "参数格式错误。"));
            }
        });
    }

    /**
     * 处理实例命令发送
     */
    public void handleInstanceCommand(QQMessage message) {
        executeWithPermissionCheck(message, false, () -> {
            String base = getAtPrefix(message);
            String[] parts = message.getMessage().trim().split("\\s+", 3);

            if (parts.length < 3) {
                sendMessage(message, base + " 格式错误，正确格式：实例命令 <实例ID> <命令>");
                return;
            }

            try {
                Long instanceId = Long.parseLong(parts[1]);
                String command = parts[2];

                NodeMinecraftServer instance = nodeMinecraftServerService.selectNodeMinecraftServerById(instanceId);

                if (instance == null) {
                    sendMessage(message, base + " 未找到ID为 " + instanceId + " 的实例。");
                    return;
                }

                // 调用发送命令接口
                Map<String, Object> params = new HashMap<>();
                params.put("id", instance.getNodeId().intValue());
                params.put("serverId", instanceId.intValue());
                params.put("command", command);

                AjaxResult result = nodeMinecraftServerService.sendCommand(params);

                if (result.get("code").equals(200)) {
                    sendMessage(message, base + " 命令已发送到实例 " + instance.getName());
                } else {
                    sendMessage(message, base + " 发送命令失败：" + result.get("msg"));
                }
            } catch (NumberFormatException e) {
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "实例ID格式错误，必须是数字。"));
            }
        });
    }

    /**
     * 处理节点状态查询命令
     */
    public void handleNodeStatus(QQMessage message) {
        executeWithPermissionCheck(message, false, () -> {
            String base = getAtPrefix(message);
            String[] parts = message.getMessage().trim().split("\\s+");

            // 获取所有节点服务器
            NodeServer queryParam = new NodeServer();
            List<NodeServer> nodeServers = nodeServerService.selectNodeServerList(queryParam);

            if (nodeServers.isEmpty()) {
                sendMessage(message, base + " 当前没有配置任何节点服务器。");
                return;
            }

            // 如果指定了节点ID，只查询该节点
            if (parts.length > 1) {
                try {
                    Long nodeId = Long.parseLong(parts[1]);
                    NodeServer targetNode = null;
                    for (NodeServer node : nodeServers) {
                        if (node.getId().equals(nodeId)) {
                            targetNode = node;
                            break;
                        }
                    }

                    if (targetNode == null) {
                        sendMessage(message, base + " 未找到ID为 " + nodeId + " 的节点服务器。");
                        return;
                    }

                    // 查询单个节点的详细信息
                    displayNodeDetails(message, base, targetNode);
                } catch (NumberFormatException e) {
                    sendMessage(message, base + " 节点ID格式错误，请输入数字。");
                }
                return;
            }

            // 显示所有节点的概览信息
            StringBuilder response = new StringBuilder(base + " 节点服务器状态概览：\n\n");

            for (NodeServer node : nodeServers) {
                response.append("━━━━━━━━━━━━━━━━━━━━\n");
                response.append("节点ID: ").append(node.getId()).append("\n");
                response.append("节点名称: ").append(node.getName()).append("\n");
                response.append("节点地址: ").append(node.getProtocol()).append("://")
                        .append(node.getIp()).append(":").append(node.getPort()).append("\n");
                response.append("节点版本: ").append(node.getVersion() != null ? node.getVersion() : "未知").append("\n");
                response.append("操作系统: ").append(node.getOsType() != null ? node.getOsType() : "未知").append("\n");

                // 尝试获取心跳信息
                try {
                    AjaxResult heartbeatResult = nodeServerService.getHeartbeat(node.getId());
                    if (heartbeatResult != null && heartbeatResult.get("code").equals(200)) {
                        JSONObject data = (JSONObject) heartbeatResult.get("data");
                        response.append("状态: ✅ 在线\n");
                        response.append("运行时间: ").append(BotMessageHelper.formatUptime(data.getLong("uptime"))).append("\n");

                        // 获取系统负载信息
                        if (data.containsKey("systemLoad")) {
                            JSONObject systemLoad = data.getJSONObject("systemLoad");
                            if (systemLoad.containsKey("cpu")) {
                                JSONObject cpu = systemLoad.getJSONObject("cpu");
                                response.append("CPU使用率: ").append(String.format("%.2f", cpu.getDouble("load"))).append("%\n");
                            }
                            if (systemLoad.containsKey("memoryLoad")) {
                                response.append("内存使用率: ").append(String.format("%.2f", systemLoad.getDouble("memoryLoad"))).append("%\n");
                            }
                        }

                        // 获取服务器实例统计
                        if (data.containsKey("serverStats")) {
                            JSONObject serverStats = data.getJSONObject("serverStats");
                            response.append("实例总数: ").append(serverStats.getInteger("totalInstances")).append("\n");
                            response.append("运行中: ").append(serverStats.getInteger("runningInstances")).append("\n");
                            response.append("已停止: ").append(serverStats.getInteger("stoppedInstances")).append("\n");
                        }
                    } else {
                        response.append("状态: ❌ 离线或无响应\n");
                    }
                } catch (Exception e) {
                    response.append("状态: ❌ 查询失败\n");
                    log.warn("查询节点 {} 心跳信息失败: {}", node.getId(), e.getMessage());
                }

                response.append("\n");
            }

            response.append("━━━━━━━━━━━━━━━━━━━━\n");
            response.append("💡 使用 节点状态 <节点ID> 查看详细信息");

            sendMessage(message, response.toString());
        });
    }

    /**
     * 显示单个节点的详细信息
     */
    private void displayNodeDetails(QQMessage message, String base, NodeServer node) {
        try {
            AjaxResult heartbeatResult = nodeServerService.getHeartbeat(node.getId());

            if (heartbeatResult == null || !heartbeatResult.get("code").equals(200)) {
                sendMessage(message, base + " 节点 " + node.getName() + " 离线或无响应。");
                return;
            }

            JSONObject data = (JSONObject) heartbeatResult.get("data");
            StringBuilder response = new StringBuilder(base + " 节点详细信息：\n\n");

            // 基本信息
            response.append("━━━━━━━━━━━━━━━━━━━━\n");
            response.append("📋 基本信息\n");
            response.append("━━━━━━━━━━━━━━━━━━━━\n");
            response.append("节点ID: ").append(node.getId()).append("\n");
            response.append("节点名称: ").append(node.getName()).append("\n");
            response.append("节点地址: ").append(node.getProtocol()).append("://")
                    .append(node.getIp()).append(":").append(node.getPort()).append("\n");
            response.append("节点版本: ").append(data.getString("version")).append("\n");
            response.append("协议版本: ").append(data.getString("protocolVersion")).append("\n");
            response.append("运行时间: ").append(BotMessageHelper.formatUptime(data.getLong("uptime"))).append("\n\n");

            // 系统信息
            if (data.containsKey("systemInfo")) {
                JSONObject systemInfo = data.getJSONObject("systemInfo");
                response.append("━━━━━━━━━━━━━━━━━━━━\n");
                response.append("💻 系统信息\n");
                response.append("━━━━━━━━━━━━━━━━━━━━\n");
                response.append("操作系统: ").append(systemInfo.getString("osName")).append("\n");
                response.append("系统版本: ").append(systemInfo.getString("osVersion")).append("\n");
                response.append("系统架构: ").append(systemInfo.getString("architecture")).append("\n");
                response.append("Java版本: ").append(systemInfo.getString("javaVersion")).append("\n");
                response.append("处理器数: ").append(systemInfo.getInteger("availableProcessors")).append("\n");
                response.append("总内存: ").append(BotMessageHelper.formatBytes(systemInfo.getLong("totalMemory"))).append("\n");
                response.append("可用内存: ").append(BotMessageHelper.formatBytes(systemInfo.getLong("freeMemory"))).append("\n");
                response.append("最大内存: ").append(BotMessageHelper.formatBytes(systemInfo.getLong("maxMemory"))).append("\n\n");
            }

            // 系统负载
            if (data.containsKey("systemLoad")) {
                JSONObject systemLoad = data.getJSONObject("systemLoad");
                response.append("━━━━━━━━━━━━━━━━━━━━\n");
                response.append("📊 系统负载\n");
                response.append("━━━━━━━━━━━━━━━━━━━━\n");

                if (systemLoad.containsKey("cpu")) {
                    JSONObject cpu = systemLoad.getJSONObject("cpu");
                    response.append("CPU使用率: ").append(String.format("%.2f", cpu.getDouble("load"))).append("%\n");

                    if (cpu.containsKey("loadDetail")) {
                        JSONObject loadDetail = cpu.getJSONObject("loadDetail");
                        response.append("  用户: ").append(String.format("%.2f", loadDetail.getDouble("user"))).append("%\n");
                        response.append("  系统: ").append(String.format("%.2f", loadDetail.getDouble("system"))).append("%\n");
                        response.append("  空闲: ").append(String.format("%.2f", loadDetail.getDouble("idle"))).append("%\n");
                    }
                }

                if (systemLoad.containsKey("memoryLoad")) {
                    response.append("内存使用率: ").append(String.format("%.2f", systemLoad.getDouble("memoryLoad"))).append("%\n");
                }

                if (systemLoad.containsKey("network")) {
                    JSONObject network = systemLoad.getJSONObject("network");
                    response.append("网络接收: ").append(BotMessageHelper.formatBytes(network.getLong("bytesRecvPerSec"))).append("/s\n");
                    response.append("网络发送: ").append(BotMessageHelper.formatBytes(network.getLong("bytesSentPerSec"))).append("/s\n");
                }
                response.append("\n");
            }

            // 服务器实例统计
            if (data.containsKey("serverStats")) {
                JSONObject serverStats = data.getJSONObject("serverStats");
                response.append("━━━━━━━━━━━━━━━━━━━━\n");
                response.append("🎮 服务器实例统计\n");
                response.append("━━━━━━━━━━━━━━━━━━━━\n");
                response.append("实例总数: ").append(serverStats.getInteger("totalInstances")).append("\n");
                response.append("运行中: ").append(serverStats.getInteger("runningInstances")).append("\n");
                response.append("已停止: ").append(serverStats.getInteger("stoppedInstances")).append("\n");
                response.append("总分配内存: ").append(serverStats.getInteger("totalAllocatedMemory")).append(" MB\n");

                // 显示实例列表
                if (serverStats.containsKey("instances") && !serverStats.getJSONArray("instances").isEmpty()) {
                    response.append("\n实例列表:\n");
                    JSONArray instances = serverStats.getJSONArray("instances");
                    for (int i = 0; i < Math.min(instances.size(), 5); i++) {
                        JSONObject instance = instances.getJSONObject(i);
                        response.append("  ▫️ ").append(instance.getString("name"))
                                .append(" (").append(instance.getString("status")).append(")")
                                .append(" - ").append(instance.getString("coreType"))
                                .append(" ").append(instance.getString("version"))
                                .append(" - ").append(instance.getInteger("memoryMb")).append("MB\n");
                    }
                    if (instances.size() > 5) {
                        response.append("  ... 还有 ").append(instances.size() - 5).append(" 个实例\n");
                    }
                }
            }

            response.append("\n━━━━━━━━━━━━━━━━━━━━");

            sendMessage(message, response.toString());

        } catch (Exception e) {
            log.error("显示节点详细信息失败: {}", e.getMessage(), e);
            sendMessage(message, base + " 获取节点详细信息失败。");
        }
    }
}
