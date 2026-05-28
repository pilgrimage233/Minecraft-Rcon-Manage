package cc.endmc.server.ws.handler.admin;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.framework.web.domain.Server;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.rconclient.RconClient;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.bot.QqBotManager;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.server.ServerCommandInfo;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.server.IServerInfoService;
import cc.endmc.server.utils.CommandUtil;
import cc.endmc.server.ws.BotClient;
import cc.endmc.server.ws.QQMessage;
import cc.endmc.server.ws.handler.BaseCommandHandler;
import cc.endmc.server.ws.handler.CommandRegistry;
import cc.endmc.server.ws.helper.BotMessageHelper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 管理员命令处理器
 * 处理管理员可用的命令
 */
@Slf4j
public class AdminCommandHandler extends BaseCommandHandler {

    private final IWhitelistInfoService whitelistInfoService;
    private final IServerInfoService serverInfoService;
    private final RconService rconService;
    private final Environment env;

    public AdminCommandHandler(BotClient botClient, RedisCache redisCache,
                               IWhitelistInfoService whitelistInfoService,
                               IServerInfoService serverInfoService,
                               RconService rconService, Environment env) {
        super(botClient, redisCache);
        this.whitelistInfoService = whitelistInfoService;
        this.serverInfoService = serverInfoService;
        this.rconService = rconService;
        this.env = env;
    }

    /**
     * 注册管理员命令到命令注册器
     */
    public void registerCommands(CommandRegistry registry) {
        registry.register("过审", this::handleWhitelistReview, "approve", "pass", "通过");
        registry.register("拒审", this::handleWhitelistReview, "reject", "deny");
        registry.register("封禁", this::handleBanOperation, "ban");
        registry.register("解封", this::handleBanOperation, "unban");
        registry.register("发送指令", msg -> handleRconCommand(msg, false), "cmd", "rcon");
        registry.register("运行状态", this::handleHostStatus, "status", "sys");
        registry.register("刷新连接", this::handleRefreshConnection, "refresh", "reload");
        registry.register("测试连接", this::handleTestConnection, "testconn", "tc");
    }

    /**
     * 处理白名单审核请求
     */
    public void handleWhitelistReview(QQMessage message) {
        executeWithPermissionCheck(message, false, () -> {
            log.info("开始处理白名单审核请求");

            String[] parts = message.getMessage().trim().split("\\s+");
            if (parts.length < 2) {
                log.info("命令格式错误: {}", message.getMessage());
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "格式错误，正确格式：过审/拒审 玩家ID"));
                return;
            }

            String command = parts[0];
            String playerId = parts[1];

            log.info("处理白名单审核 - 命令: {}, 玩家ID: {}", command, playerId);

            if (command.equals("通过")) {
                final WhitelistInfo whitelistInfo = redisCache.getCacheObject(CacheKey.PASS_KEY + playerId);
                if (whitelistInfo == null) {
                    log.info("未找到玩家 {} 的白名单申请信息", playerId);
                    sendMessage(message, BotMessageHelper.buildErrorMessage(message, "未找到玩家 " + playerId + " 的白名单申请。"));
                } else {
                    log.info("获取到玩家 {} 的白名单信息: {}", playerId, whitelistInfo);
                    // 设置审核状态
                    whitelistInfo.setStatus("1"); // 通过
                    whitelistInfo.setAddState("1");
                    whitelistInfo.setServers("all"); // 默认添加到所有服务器
                    whitelistInfo.setAddTime(new Date());

                    // 更新白名单信息
                    log.info("开始更新白名单信息");
                    int result = whitelistInfoService.updateWhitelistInfo(whitelistInfo, message.getSender().getUserId().toString());
                    log.info("更新结果: {}", result);

                    if (result > 0) {
                        log.info("白名单审核成功: 通过");
                        sendMessage(message, BotMessageHelper.buildErrorMessage(message, "已通过玩家 " + whitelistInfo.getUserName() + " 的白名单申请。"));
                    } else {
                        log.warn("白名单审核失败: 更新数据库返回 {}", result);
                        sendMessage(message, BotMessageHelper.buildErrorMessage(message, "审核操作失败，请稍后重试。"));
                    }

                    // 删除缓存中的白名单申请信息
                    redisCache.deleteObject(CacheKey.PASS_KEY + playerId);
                    log.info("已删除缓存中的白名单申请信息: {}", CacheKey.PASS_KEY + playerId);
                }
            } else {
                // 查询白名单信息
                WhitelistInfo whitelistInfo = new WhitelistInfo();
                whitelistInfo.setUserName(playerId);
                log.info("开始查询玩家 {} 的白名单信息", playerId);
                List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);
                log.info("查询结果: 找到 {} 条记录", whitelistInfos.size());

                if (whitelistInfos.isEmpty()) {
                    sendMessage(message, BotMessageHelper.buildErrorMessage(message, "未找到玩家 " + playerId + " 的白名单申请。"));
                    return;
                }

                whitelistInfo = whitelistInfos.get(0);
                log.info("获取到玩家 {} 的白名单信息: {}", playerId, whitelistInfo);

                // 设置审核状态
                if (command.equals("过审")) {
                    log.info("执行过审操作");
                    whitelistInfo.setStatus("1"); // 通过
                    whitelistInfo.setAddState("1");
                    whitelistInfo.setServers("all"); // 默认添加到所有服务器
                } else {
                    log.info("执行拒审操作");
                    whitelistInfo.setStatus("2"); // 拒绝
                    whitelistInfo.setAddState("2");
                    whitelistInfo.setRemoveReason("管理员拒绝");
                }
                whitelistInfo.setAddTime(new Date());

                // 更新白名单信息
                log.info("开始更新白名单信息");
                int result = whitelistInfoService.updateWhitelistInfo(whitelistInfo, message.getSender().getUserId().toString());
                log.info("更新结果: {}", result);

                if (result > 0) {
                    String status = command.equals("过审") ? "通过" : "拒绝";
                    log.info("白名单审核成功: {}", status);
                    sendMessage(message, BotMessageHelper.buildErrorMessage(message, "已" + status + "玩家 " + playerId + " 的白名单申请。"));
                } else {
                    log.warn("白名单审核失败: 更新数据库返回 {}", result);
                    sendMessage(message, BotMessageHelper.buildErrorMessage(message, "审核操作失败，请稍后重试。"));
                }
            }
        });
    }

    /**
     * 处理封禁和解封操作
     */
    public void handleBanOperation(QQMessage message) {
        executeWithPermissionCheck(message, false, () -> {
            String[] parts = message.getMessage().trim().split("\\s+", 3);
            String command = parts[0];

            if (command.equals("封禁") && parts.length < 3) {
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "格式错误，正确格式：封禁 玩家ID 封禁原因"));
                return;
            } else if (command.equals("解封") && parts.length < 2) {
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "格式错误，正确格式：解封 玩家ID"));
                return;
            }

            String playerId = parts[1];
            String banReason = command.equals("封禁") ? parts[2] : null;

            // 查询白名单信息
            WhitelistInfo whitelistInfo = new WhitelistInfo();
            whitelistInfo.setUserName(playerId);
            List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);

            if (whitelistInfos.isEmpty()) {
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "未找到玩家 " + playerId + " 的白名单信息。"));
                return;
            }

            whitelistInfo = whitelistInfos.get(0);

            // 设置封禁/解封状态
            if (command.equals("封禁")) {
                whitelistInfo.setBanFlag("true");
                whitelistInfo.setBannedReason(banReason);
            } else {
                whitelistInfo.setBanFlag("false");
            }

            // 更新白名单信息
            int result = whitelistInfoService.updateWhitelistInfo(whitelistInfo, message.getSender().getUserId().toString());

            if (result > 0) {
                String status = command.equals("封禁") ? "封禁" : "解封";
                String msg = BotMessageHelper.buildErrorMessage(message, "已" + status + "玩家 " + playerId);
                if (command.equals("封禁")) {
                    msg += "，原因：" + banReason;
                }
                sendMessage(message, msg);
            } else {
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "操作失败，请稍后重试。"));
            }
        });
    }

    /**
     * 处理RCON指令发送
     */
    public void handleRconCommand(QQMessage message, boolean lastUsed) {
        executeWithPermissionCheck(message, false, () -> {
            // 检查超级管理员权限
            List<QqBotManager> managers = getManagers(message);
            if (managers.isEmpty() || managers.get(0).getPermissionType() != 0) {
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "权限不足！"));
                return;
            }

            String serverId;
            String command;
            if (!lastUsed) {
                String[] parts = message.getMessage().trim().split("\\s+", 3);
                if (parts.length < 3) {
                    sendMessage(message, BotMessageHelper.buildErrorMessage(message, "格式错误，正确格式：发送指令 服务器ID/all 指令内容"));
                    return;
                }
                serverId = parts[1];
                command = parts[2];

                // 清除用户最后使用的服务器ID缓存
                String lastServerKey = CacheKey.LAST_USED_SERVER_KEY + message.getSender().getUserId();
                if (redisCache.hasKey(lastServerKey)) {
                    redisCache.deleteObject(lastServerKey);
                }
            } else {
                // 使用最后使用的服务器ID
                command = message.getMessage().trim();
                if (command.isEmpty()) {
                    sendMessage(message, BotMessageHelper.buildErrorMessage(message, "格式错误，正确格式：/指令内容"));
                    return;
                }
                // 获取用户最后使用的服务器ID
                serverId = redisCache.getCacheObject(CacheKey.LAST_USED_SERVER_KEY + message.getSender().getUserId());
                if (serverId == null) {
                    sendMessage(message, BotMessageHelper.buildErrorMessage(message, "未找到您上次使用的服务器ID，请使用完整格式发送指令。"));
                    return;
                }
            }

            if (!serverId.contains("all")) {
                if (!RconCache.containsKey(serverId)) {
                    sendMessage(message, BotMessageHelper.buildErrorMessage(message, "未找到服务器 " + serverId));
                    return;
                }
            }

            // 判断是否为高危命令
            if (CommandUtil.isHighRiskCommand(command)) {
                // 获取确认状态
                String confirmKey = CacheKey.COMMAND_USE_KEY + "confirm:" + message.getSender().getUserId() + ":" + serverId + ":" + command;
                Integer confirmCount = redisCache.getCacheObject(confirmKey);

                if (confirmCount == null) {
                    confirmCount = 0;
                }

                confirmCount++;

                if (confirmCount < 3) {
                    // 更新确认次数
                    redisCache.setCacheObject(confirmKey, confirmCount, 5, TimeUnit.MINUTES);

                    // 获取服务器信息
                    Map<String, Object> serverInfoMap = redisCache.getCacheObject(CacheKey.SERVER_INFO_MAP_KEY);
                    String serverDisplay = serverId;
                    if (!"all".equals(serverId)) {
                        Object serverObj = serverInfoMap.get(serverId);
                        if (serverObj != null) {
                            try {
                                ServerInfo serverInfo = JSON.parseObject(JSON.toJSONString(serverObj), ServerInfo.class);
                                serverDisplay = serverInfo.getNameTag() + " (" + serverId + ")";
                            } catch (Exception e) {
                                log.warn("服务器信息转换失败: {}", e.getMessage());
                            }
                        }
                    } else {
                        serverDisplay = "所有在线服务器";
                    }

                    // 发送确认消息
                    StringBuilder warningMsg = new StringBuilder();
                    warningMsg.append(BotMessageHelper.getAtPrefix(message)).append(" ");
                    warningMsg.append("⚠️ 高危命令警告 ⚠️\n\n");
                    warningMsg.append("您正在尝试执行高危命令：").append(command).append("\n");
                    warningMsg.append("该命令可能对服务器 ").append(serverDisplay).append(" 造成严重影响！\n\n");
                    warningMsg.append("确认状态：").append(confirmCount).append("/3\n");
                    warningMsg.append("请再次发送相同指令以确认执行（5分钟内有效）");

                    sendMessage(message, warningMsg.toString());
                    return;
                } else {
                    // 清除确认状态
                    redisCache.deleteObject(confirmKey);
                }
            }

            try {
                // 获取服务器信息
                Map<String, Object> serverInfoMap = redisCache.getCacheObject(CacheKey.SERVER_INFO_MAP_KEY);

                // 发送RCON指令并获取结果
                String result = rconService.sendCommand(serverId, command, true);
                StringBuilder response = new StringBuilder();
                response.append(BotMessageHelper.getAtPrefix(message)).append(" ");

                if ("all".equals(serverId)) {
                    response.append("指令已发送至所有在线服务器\n");
                } else {
                    Object serverObj = serverInfoMap.get(serverId);
                    if (serverObj != null) {
                        ServerInfo serverInfo = null;
                        if (serverObj instanceof ServerInfo) {
                            serverInfo = (ServerInfo) serverObj;
                        } else {
                            try {
                                serverInfo = JSON.parseObject(JSON.toJSONString(serverObj), ServerInfo.class);
                            } catch (Exception e) {
                                log.warn("服务器信息转换失败: {}", e.getMessage());
                            }
                        }

                        if (serverInfo != null) {
                            response.append("指令已发送至服务器: ").append(serverInfo.getNameTag())
                                    .append(" (").append(serverId).append(")")
                                    .append(" [").append(serverInfo.getServerVersion()).append("]")
                                    .append("\n");
                        } else {
                            response.append("指令已发送至服务器: ").append(serverId).append("\n");
                        }
                    } else {
                        response.append("指令已发送至服务器: ").append(serverId).append("\n");
                    }
                }

                if (!result.trim().isEmpty()) {
                    response.append("执行结果：\n").append(result);
                } else {
                    response.append("指令已执行，无返回结果。");
                }
                if (!lastUsed) {
                    // 缓存用户最后使用的服务器ID
                    String lastServerKey = CacheKey.LAST_USED_SERVER_KEY + message.getSender().getUserId();
                    redisCache.setCacheObject(lastServerKey, serverId, 1, TimeUnit.DAYS);
                    response.append("\n(已记录您最后使用的服务器ID: ").append(serverId).append("，24小时内再次发送指令时将默认使用)");
                }
                sendMessage(message, response.toString());
            } catch (Exception e) {
                log.error("发送RCON指令失败: {}", e.getMessage());
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "指令发送失败：" + e.getMessage()));
            }
        });
    }

    /**
     * 处理主机状态查询请求
     */
    public void handleHostStatus(QQMessage message) {
        executeWithPermissionCheck(message, false, () -> {
            String base = getAtPrefix(message);

            try {
                Server server = new Server();
                server.copyTo();

                // 构建返回消息
                StringBuilder response = new StringBuilder(base + " 主机运行状态如下：\n\n");

                // CPU信息
                response.append("CPU状态：\n");
                response.append("核心数：").append(server.getCpu().getCpuNum()).append("\n");
                response.append("系统使用率：").append(server.getCpu().getSys()).append("%\n");
                response.append("用户使用率：").append(server.getCpu().getUsed()).append("%\n");
                response.append("当前等待率：").append(server.getCpu().getWait()).append("%\n");
                response.append("当前空闲率：").append(server.getCpu().getFree()).append("%\n\n");

                // 内存信息
                response.append("内存状态：\n");
                response.append("总内存：").append(server.getMem().getTotal()).append("G\n");
                response.append("已用内存：").append(server.getMem().getUsed()).append("G\n");
                response.append("剩余内存：").append(server.getMem().getFree()).append("G\n");
                response.append("内存使用率：").append(server.getMem().getUsage()).append("%\n\n");

                // JVM信息
                response.append("JVM状态：\n");
                response.append("总内存：").append(server.getJvm().getTotal()).append("M\n");
                response.append("已用内存：").append(server.getJvm().getUsed()).append("M\n");
                response.append("剩余内存：").append(server.getJvm().getFree()).append("M\n");
                response.append("内存使用率：").append(server.getJvm().getUsage()).append("%\n");
                response.append("JDK版本：").append(server.getJvm().getVersion()).append("\n\n");

                // 系统信息
                response.append("系统信息：\n");
                response.append("服务器名称：").append(server.getSys().getComputerName()).append("\n");
                response.append("操作系统：").append(server.getSys().getOsName()).append("\n");
                response.append("系统架构：").append(server.getSys().getOsArch()).append("\n\n");

                response.append("Endless-Manager：\n");
                response.append("版本：").append(env.getProperty("endless.version")).append("\n");

                // 发送消息
                sendMessage(message, response.toString());
            } catch (Exception e) {
                log.error("获取主机状态信息失败: {}", e.getMessage(), e);
                sendMessage(message, base + " 获取主机状态信息失败，请稍后重试。");
            }
        });
    }

    /**
     * 处理刷新连接命令
     */
    public void handleRefreshConnection(QQMessage message) {
        executeWithPermissionCheck(message, false, () -> {
            // 检查超级管理员权限
            List<QqBotManager> managers = getManagers(message);
            if (managers.isEmpty() || managers.get(0).getPermissionType() != 0) {
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "权限不足！"));
                return;
            }

            String[] parts = message.getMessage().trim().split("\\s+");
            String serverId = "all";

            // 如果指定了服务器ID
            if (parts.length > 1) {
                serverId = parts[1];
                if (!serverId.equals("all") && !RconCache.containsKey(serverId)) {
                    sendMessage(message, BotMessageHelper.buildErrorMessage(message, "未找到服务器 " + serverId));
                    return;
                }
            }

            if (serverId.equals("all")) {
                // 关闭所有Rcon连接并清除Map缓存
                for (RconClient rconClient : RconCache.getMap().values()) {
                    rconClient.close();
                }
                RconCache.clear();

                // 初始化Rcon连接
                ServerInfo info = new ServerInfo();
                info.setStatus(1L);

                for (ServerInfo serverInfo : serverInfoService.selectServerInfoList(info)) {
                    rconService.init(serverInfo);
                }

                // 发送消息
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "已成功刷新所有服务器的RCON连接。"));
            } else {
                // 获取服务器信息
                Map<String, Object> serverInfoMap = redisCache.getCacheObject(CacheKey.SERVER_INFO_MAP_KEY);
                String serverDisplay = serverId;
                ServerInfo serverInfo = null;

                Object serverObj = serverInfoMap.get(serverId);
                if (serverObj != null) {
                    try {
                        serverInfo = JSON.parseObject(JSON.toJSONString(serverObj), ServerInfo.class);
                        serverDisplay = serverInfo.getNameTag() + " (" + serverId + ")";
                    } catch (Exception e) {
                        log.warn("服务器信息转换失败: {}", e.getMessage());
                    }
                }

                // 关闭指定的Rcon连接
                if (RconCache.containsKey(serverId)) {
                    RconClient rconClient = RconCache.get(serverId);
                    if (rconClient != null) {
                        rconClient.close();
                        RconCache.remove(serverId);
                    }
                }

                // 重新初始化指定的Rcon连接
                if (serverInfo != null) {
                    boolean success = rconService.init(serverInfo);
                    if (success) {
                        sendMessage(message, BotMessageHelper.buildErrorMessage(message, "已成功刷新服务器 " + serverDisplay + " 的RCON连接。"));
                    } else {
                        sendMessage(message, BotMessageHelper.buildErrorMessage(message, "刷新服务器 " + serverDisplay + " 的RCON连接失败，请检查服务器状态。"));
                    }
                } else {
                    // 如果从Redis缓存获取失败，尝试从数据库获取
                    ServerInfo dbServerInfo = serverInfoService.selectServerInfoById(Long.parseLong(serverId));
                    if (dbServerInfo != null) {
                        boolean success = rconService.init(dbServerInfo);
                        if (success) {
                            sendMessage(message, BotMessageHelper.buildErrorMessage(message, "已成功刷新服务器 " + serverId + " 的RCON连接。"));
                        } else {
                            sendMessage(message, BotMessageHelper.buildErrorMessage(message, "刷新服务器 " + serverId + " 的RCON连接失败，请检查服务器状态。"));
                        }
                    } else {
                        sendMessage(message, BotMessageHelper.buildErrorMessage(message, "未找到服务器 " + serverId));
                    }
                }
            }
        });
    }

    /**
     * 处理测试连接命令
     */
    public void handleTestConnection(QQMessage message) {
        executeWithPermissionCheck(message, false, () -> {
            // 检查超级管理员权限
            List<QqBotManager> managers = getManagers(message);
            if (managers.isEmpty() || managers.get(0).getPermissionType() != 0) {
                sendMessage(message, BotMessageHelper.buildErrorMessage(message, "权限不足！"));
                return;
            }

            String[] parts = message.getMessage().trim().split("\\s+");
            String serverId = "all";

            // 如果指定了服务器ID
            if (parts.length > 1) {
                serverId = parts[1];
                if (!serverId.equals("all") && !RconCache.containsKey(serverId)) {
                    sendMessage(message, BotMessageHelper.buildErrorMessage(message, "未找到服务器 " + serverId));
                    return;
                }
            }

            StringBuilder response = new StringBuilder();
            response.append(BotMessageHelper.getAtPrefix(message)).append(" 测试连接结果：\n\n");

            if (serverId.equals("all")) {
                // 测试所有服务器
                if (RconCache.isEmpty()) {
                    sendMessage(message, BotMessageHelper.buildErrorMessage(message, "当前没有RCON连接。"));
                    return;
                }

                Map<String, Object> serverInfoMap = redisCache.getCacheObject(CacheKey.SERVER_INFO_MAP_KEY);
                for (Map.Entry<String, RconClient> entry : RconCache.getMap().entrySet()) {
                    String id = entry.getKey();
                    RconClient client = entry.getValue();
                    ServerInfo serverInfo = null;

                    // 获取服务器信息并处理类型转换
                    Object serverObj = serverInfoMap.get(id);
                    if (serverObj != null) {
                        try {
                            serverInfo = JSON.parseObject(JSON.toJSONString(serverObj), ServerInfo.class);
                        } catch (Exception e) {
                            log.warn("服务器信息转换失败: {}", e.getMessage());
                        }
                    }

                    if (serverInfo != null) {
                        response.append("服务器: ").append(serverInfo.getNameTag())
                                .append(" (ID: ").append(id).append(")")
                                .append(" [").append(serverInfo.getServerVersion()).append("]")
                                .append("\n");
                    } else {
                        response.append("服务器: ").append(id).append("\n");
                    }

                    try {
                        String result = client.sendCommand("seed");
                        if (result != null && !result.trim().isEmpty()) {
                            response.append("✅ 连接正常: ").append(result.trim()).append("\n\n");
                        } else {
                            response.append("⚠️ 连接异常: 命令返回为空\n\n");
                        }
                    } catch (Exception e) {
                        response.append("❌ 连接错误: ").append(e.getMessage()).append("\n\n");
                    }
                }
            } else {
                // 测试指定服务器
                RconClient client = RconCache.get(serverId);
                Map<String, Object> serverInfoMap = redisCache.getCacheObject(CacheKey.SERVER_INFO_MAP_KEY);
                ServerInfo serverInfo = null;

                // 获取服务器信息并处理类型转换
                Object serverObj = serverInfoMap.get(serverId);
                if (serverObj != null) {
                    try {
                        serverInfo = JSON.parseObject(JSON.toJSONString(serverObj), ServerInfo.class);
                    } catch (Exception e) {
                        log.warn("服务器信息转换失败: {}", e.getMessage());
                    }
                }

                if (serverInfo != null) {
                    response.append("服务器: ").append(serverInfo.getNameTag())
                            .append(" (ID: ").append(serverId).append(")")
                            .append(" [").append(serverInfo.getServerVersion()).append("]")
                            .append("\n");
                } else {
                    response.append("服务器: ").append(serverId).append("\n");
                }

                try {
                    String result = client.sendCommand("seed");
                    if (result != null && !result.trim().isEmpty()) {
                        response.append("✅ 连接正常: ").append(result.trim()).append("\n");
                    } else {
                        response.append("⚠️ 连接异常: 命令返回为空\n");
                    }
                } catch (Exception e) {
                    response.append("❌ 连接错误: ").append(e.getMessage()).append("\n");
                }
            }

            sendMessage(message, response.toString());
        });
    }

    /**
     * 添加管理员
     */
    public void handleAddManager(QQMessage message) {
        // 实现添加管理员逻辑
        sendMessage(message, getAtPrefix(message) + " 添加管理员功能开发中...");
    }

    /**
     * 添加超级管理员
     */
    public void handleAddSuperManager(QQMessage message) {
        // 实现添加超级管理员逻辑
        sendMessage(message, getAtPrefix(message) + " 添加超级管理员功能开发中...");
    }
}
