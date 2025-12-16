package cc.endmc.server.ws;

import cc.endmc.common.constant.Constants;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.framework.manager.AsyncManager;
import cc.endmc.framework.web.domain.Server;
import cc.endmc.node.domain.NodeMinecraftServer;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.service.INodeMinecraftServerService;
import cc.endmc.node.service.INodeServerService;
import cc.endmc.server.annotation.BotCommand;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.EmailTemplates;
import cc.endmc.server.common.constant.BotApi;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.rconclient.RconClient;
import cc.endmc.server.common.service.EmailService;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.bot.*;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.service.bot.IBotGroupCommandConfigService;
import cc.endmc.server.service.bot.IQqBotConfigService;
import cc.endmc.server.service.bot.IQqBotLogService;
import cc.endmc.server.service.bot.IQqBotManagerService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.server.IServerInfoService;
import cc.endmc.server.utils.CodeUtil;
import cc.endmc.server.utils.CommandUtil;
import cc.endmc.server.utils.HtmlUtils;
import cc.endmc.server.utils.IPUtils;
import cc.endmc.server.ws.handler.CommandHandler;
import cc.endmc.server.ws.handler.CommandRegistry;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * QQ机器人WebSocket客户端
 * 用于与QQ机器人服务器建立长连接，实时接收消息
 */
@Lazy
@Slf4j
@Component
@RequiredArgsConstructor
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BotClient {

    @Value("${app-url}")
    private String appUrl;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AsyncManager asyncExecutor = AsyncManager.me();
    private final IWhitelistInfoService whitelistInfoService;
    private final IServerInfoService serverInfoService;
    private final IQqBotConfigService qqBotConfigService;
    private final IQqBotManagerService qqBotManagerService;
    private final IQqBotLogService qqBotLogService;
    private final INodeMinecraftServerService nodeMinecraftServerService;
    private final INodeServerService nodeServerService;
    private final IBotGroupCommandConfigService commandConfigService;
    private ScheduledFuture<?> reconnectTask;
    private final Environment env;
    private final RedisCache redisCache;
    private final EmailService emailService;
    private final RconService rconService;
    private final BotManager botManager;
    private volatile boolean isShuttingDown = false;
    /**
     * 命令注册器
     */
    private final CommandRegistry commandRegistry = new CommandRegistry();
    /**
     * -- GETTER --
     * 获取机器人配置
     *
     * @return 机器人配置
     */
    @Getter
    private QqBotConfig config;
    private WebSocketClient wsClient;

    @PostConstruct
    public void init() {
        log.info("BotClient 实例已创建，依赖注入完成");

        // 初始化命令注册器
        initCommandRegistry();
    }

    /**
     * 初始化命令注册器
     * 注册所有命令及其处理器
     */
    private void initCommandRegistry() {
        // 普通用户命令
        commandRegistry.register("help", this::handleHelpCommand, "h");
        commandRegistry.register("白名单申请", this::handleWhitelistApplication, "apply", "wl");
        commandRegistry.register("查询白名单", this::handleWhitelistQuery, "check", "wlcheck");
        commandRegistry.register("查询玩家", this::handlePlayerQuery, "player", "p");
        commandRegistry.register("查询在线", this::handleOnlineQuery, "online", "list");
        commandRegistry.register("查询服务器", this::handleServerList, "servers", "sv");
        commandRegistry.register("test", this::handleTestCommand, "ping");

        // 管理员命令
        commandRegistry.register("过审", this::handleWhitelistReview, "approve", "pass", "通过");
        commandRegistry.register("拒审", this::handleWhitelistReview, "reject", "deny");
        commandRegistry.register("封禁", this::handleBanOperation, "ban");
        commandRegistry.register("解封", this::handleBanOperation, "unban");
        commandRegistry.register("发送指令", msg -> handleRconCommand(msg, false), "cmd", "rcon");
        commandRegistry.register("运行状态", this::handleHostStatus, "status", "sys");
        commandRegistry.register("刷新连接", this::handleRefreshConnection, "refresh", "reload");
        commandRegistry.register("测试连接", this::handleTestConnection, "testconn", "tc");
        commandRegistry.register("添加管理", this::handleAddManager, "addadmin", "aa");
        commandRegistry.register("添加超管", this::handleAddSuperManager, "addsuper", "as");

        // 实例管理命令
        commandRegistry.register("实例列表", this::handleInstanceList, "instances", "inst");
        commandRegistry.register("启动实例", this::handleStartInstance, "start", "run");
        commandRegistry.register("停止实例", this::handleStopInstance, "stop", "kill");
        commandRegistry.register("重启实例", this::handleRestartInstance, "restart", "reboot");
        commandRegistry.register("实例状态", this::handleInstanceStatus, "inststatus", "is");
        commandRegistry.register("实例日志", this::handleInstanceLogs, "logs", "log");
        commandRegistry.register("实例命令", this::handleInstanceCommand, "instcmd", "ic");
        commandRegistry.register("节点状态", this::handleNodeStatus, "nodestatus", "ns");

        // 功能开关命令（管理员）
        commandRegistry.register("关闭", this::handleDisableCommand, "disable", "off");
        commandRegistry.register("开启", this::handleEnableCommand, "enable", "on");
        commandRegistry.register("功能列表", this::handleCommandList, "cmdlist", "cl");

        log.info("命令注册器初始化完成，共注册 {} 个命令", commandRegistry.getAllCommands().size());
    }

    /**
     * 初始化机器人客户端
     * 使用配置的URL创建WebSocket连接
     *
     * @param config 机器人配置
     */
    public void init(QqBotConfig config) {
        this.config = config;
        final String httpUrl = config.getHttpUrl();
        final String wsUrl = config.getWsUrl();
        log.info("初始化机器人客户端，配置ID: {}", config.getId());
        // logSystemEvent("init", String.format("初始化机器人客户端，配置ID: %d", config.getId()));

        // 关闭现有的WebSocket连接
        if (wsClient != null) {
            wsClient.close();
        }

        // 检查URL格式
        if (!wsUrl.startsWith("ws://")) {
            config.setWsUrl(Constants.WS + config.getWsUrl());
        }
        if (!HttpUtil.isHttp(httpUrl) && !HttpUtil.isHttps(httpUrl)) {
            config.setHttpUrl(Constants.HTTP + config.getHttpUrl());
        }

        // 创建新的WebSocket连接
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + config.getToken());
            wsClient = new WebSocketClient(new URI(config.getWsUrl()), headers) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    BotClient.this.onOpen(handshakedata);
                }

                @Override
                public void onMessage(String message) {
                    BotClient.this.onMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    BotClient.this.onClose(code, reason, remote);
                }

                @Override
                public void onError(Exception ex) {
                    BotClient.this.onError(ex);
                }
            };

            // 设置连接超时
            wsClient.setConnectionLostTimeout(30);

            // 连接WebSocket服务器
            wsClient.connect();
            log.info("WebSocket连接已启动，URL: {}", config.getWsUrl());
        } catch (Exception e) {
            log.error("初始化WebSocket连接失败: {}", e.getMessage());
            // logError("init", e.getMessage(), e.getStackTrace().toString());
        }
    }

    /**
     * Spring Bean销毁时调用
     * 清理资源，关闭连接和定时任务
     */
    @PreDestroy
    public void destroy() {
        log.info("正在关闭机器人客户端...");
        // logSystemEvent("destroy", "正在关闭机器人客户端");

        isShuttingDown = true;

        // 取消重连任务
        if (reconnectTask != null) {
            reconnectTask.cancel(true);
            reconnectTask = null;
        }

        // 关闭WebSocket连接
        if (wsClient != null) {
            try {
                wsClient.close();
                log.info("WebSocket连接已关闭");
            } catch (Exception e) {
                log.error("关闭WebSocket连接时发生错误: {}", e.getMessage());
                logError("destroy", e.getMessage(), Arrays.toString(e.getStackTrace()));
            }
        }

        // 关闭调度器
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            log.info("调度器已关闭");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            log.error("关闭调度器时发生错误: {}", e.getMessage());
            logError("destroy", e.getMessage(), Arrays.toString(e.getStackTrace()));
        }

        log.info("机器人客户端已关闭");
    }

    /**
     * WebSocket连接打开时的回调
     */
    public void onOpen(ServerHandshake handshakedata) {
        log.info("WebSocket连接已建立");
        logSystemEvent("onOpen", "WebSocket连接已建立");
    }

    /**
     * 接收到WebSocket消息时的回调
     */
    public void onMessage(String message) {
        try {
            // log.debug("收到消息: {}", message);
            QQMessage qqMessage = JSON.parseObject(message, QQMessage.class);

            // 记录接收到的消息
            if (qqMessage != null && qqMessage.getMessageType() != null) {
                String senderId = qqMessage.getUserId() != null ? qqMessage.getUserId().toString() : null;
                String senderType = "user";
                String receiverId = qqMessage.getGroupId() != null ? qqMessage.getGroupId().toString() : null;
                String receiverType = "group";
                String messageId = qqMessage.getMessageId() != null ? qqMessage.getMessageId().toString() : null;

                logReceivedMessage(
                        messageId,
                        senderId,
                        senderType,
                        receiverId,
                        receiverType,
                        qqMessage.getMessage(),
                        qqMessage.getMessageType()
                );
            }

            handleMessage(qqMessage);
        } catch (Exception e) {
            log.error("处理WebSocket消息时发生错误: {}", e.getMessage());
            logError("onMessage", e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * WebSocket连接关闭时的回调
     */
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket连接已关闭，代码: {}，原因: {}，远程关闭: {}", code, reason, remote);
        logSystemEvent("onClose", String.format("WebSocket连接已关闭，代码: %d，原因: %s，远程关闭: %b", code, reason, remote));

        if (!isShuttingDown) {
            scheduleReconnect();
        }
    }

    /**
     * WebSocket连接发生错误时的回调
     */
    public void onError(Exception ex) {
        log.error("WebSocket连接发生错误: {}", ex.getMessage());
        logError("onError", ex.getMessage(), Arrays.toString(ex.getStackTrace()));
    }

    /**
     * 检查WebSocket连接是否打开
     */
    public boolean isOpen() {
        return wsClient != null && wsClient.isOpen();
    }

    /**
     * 重新连接WebSocket
     */
    public void reconnect() {
        if (wsClient != null) {
            try {
                wsClient.reconnect();
            } catch (Exception e) {
                log.error("重新连接失败: {}", e.getMessage());
                scheduleReconnect();
            }
        } else {
            init(config);
        }
    }

    /**
     * 安排重新连接任务
     */
    private void scheduleReconnect() {
        if (isShuttingDown) {
            return;
        }

        if (reconnectTask != null && !reconnectTask.isDone()) {
            return;
        }

        reconnectTask = scheduler.schedule(() -> {
            try {
                log.info("尝试重新连接WebSocket...");
                // reconnect();
                botManager.reconnectBot(config.getId());
            } catch (Exception e) {
                log.error("重新连接失败: {}", e.getMessage());
            }
        }, 5, TimeUnit.SECONDS);
    }

    /**
     * 获取命令前缀
     * 如果配置文件中未设置或为空，则返回默认值"/"
     */
    private String getCommandPrefix() {
        return StringUtils.isNotEmpty(config.getCommandPrefix()) ? config.getCommandPrefix() : "/";
    }

    /**
     * 检查消息是否是命令
     *
     * @param message 消息内容
     * @return 如果是命令则返回去除前缀的内容，否则返回null
     */
    private String parseCommand(String message) {
        String prefix = getCommandPrefix();
        if (message.startsWith(prefix)) {
            return message.substring(prefix.length()).trim();
        }
        return null;
    }

    /**
     * 处理接收到的QQ消息
     * 使用策略模式路由命令到对应的处理器
     *
     * @param message QQ消息对象
     */
    public void handleMessage(QQMessage message) {
        try {
            // 检查是否是群消息且在配置的群组中
            if (!"group".equals(message.getMessageType()) ||
                    message.getGroupId() == null ||
                    config.getGroupIds() == null ||
                    !config.getGroupIds().contains(message.getGroupId().toString())) {
                return;
            }

            // 解析命令
            String command = parseCommand(message.getMessage());
            if (command == null) {
                return;
            }

            message.setMessage(command);

            // 提取命令关键字（第一个单词）
            String commandKey = command.split("\\s+")[0].toLowerCase();

            // 查找命令处理器
            CommandHandler handler = commandRegistry.getHandler(commandKey);

            if (handler != null) {
                // 获取主命令名称（用于检查配置）
                String mainCommand = commandRegistry.getMainCommand(commandKey);

                // 检查命令是否在该群组启用（关闭/开启/功能列表命令不受限制）
                if (!isCommandControlCommand(mainCommand)) {
                    BotGroupCommandConfig cmdConfig = commandConfigService.checkCommandEnabled(
                            message.getGroupId().toString(), mainCommand);
                    if (cmdConfig != null && cmdConfig.getIsEnabled() != null && cmdConfig.getIsEnabled() == 0) {
                        // 命令已被禁用
                        String disabledMsg = StringUtils.isNotEmpty(cmdConfig.getDisabledMessage())
                                ? cmdConfig.getDisabledMessage()
                                : "该功能已在本群禁用";
                        sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] " + disabledMsg);
                        return;
                    }
                }

                handler.handle(message);
            } else {
                // 未找到命令处理器，检查是否有上次使用的服务器（用于快捷RCON命令）
                if (redisCache.hasKey(CacheKey.LAST_USED_SERVER_KEY + message.getSender().getUserId())) {
                    this.handleRconCommand(message, true);
                } else {
                    // 未知命令
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() +
                            "] 未知命令，请使用 " + getCommandPrefix() + "help 查看可用命令。");
                }
            }
        } catch (Exception e) {
            // 记录错误信息
            log.error("处理消息时发生错误: {}", e.getMessage(), e);
            logError("handleMessage", e.getMessage(), getStackTraceAsString(e));

            // 发送错误消息给用户
            try {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() +
                        "] 处理命令时发生错误，请稍后重试。");
            } catch (Exception ex) {
                log.error("发送错误消息失败: {}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * 处理test命令
     * 根据参数判断是测试Minecraft服务器还是HTTP/HTTPS服务器
     *
     * @param message QQ消息对象
     */
    private void handleTestCommand(QQMessage message) {
        String[] parts = message.getMessage().split("\\s+");
        if (parts.length > 1 && (parts[1].startsWith("http") || parts[1].startsWith("https"))) {
            testHttp(message);
        } else {
            testServer(message);
        }
    }

    /**
     * 判断是否是功能控制命令（这些命令不受开关限制）
     */
    private boolean isCommandControlCommand(String command) {
        return "关闭".equals(command) || "开启".equals(command) || "功能列表".equals(command) || "help".equals(command);
    }

    /**
     * 处理关闭功能命令
     * 格式：关闭 <功能名称>
     */
    @BotCommand(description = "关闭指定功能", permissionLevel = 1)
    private void handleDisableCommand(QQMessage message) {
        handleToggleCommand(message, false);
    }

    /**
     * 处理开启功能命令
     * 格式：开启 <功能名称>
     */
    @BotCommand(description = "开启指定功能", permissionLevel = 1)
    private void handleEnableCommand(QQMessage message) {
        handleToggleCommand(message, true);
    }

    /**
     * 处理功能开关切换
     */
    private void handleToggleCommand(QQMessage message, boolean enable) {
        String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";

        // 检查管理员权限
        List<QqBotManager> managers = config.selectManagerForThisGroup(message.getGroupId(), message.getUserId());
        if (managers.isEmpty()) {
            sendMessage(message, base + " 您没有权限执行此操作，需要管理员权限。");
            return;
        }

        String[] parts = message.getMessage().split("\\s+");
        if (parts.length < 2) {
            String action = enable ? "开启" : "关闭";
            sendMessage(message, base + " 格式错误，正确格式：" + action + " <功能名称>\n使用 /功能列表 查看所有可用功能。");
            return;
        }

        String commandKey = parts[1];

        // 不允许关闭功能控制命令本身
        if (isCommandControlCommand(commandKey)) {
            sendMessage(message, base + " 该功能不允许被关闭。");
            return;
        }

        // 获取主命令名称（如果是注册的命令别名，则转换为主命令）
        String mainCommand = commandKey;
        if (commandRegistry.hasCommand(commandKey)) {
            mainCommand = commandRegistry.getMainCommand(commandKey);
        } else {
            // 检查是否是系统功能（非指令类功能，如玩家上下线通知）
            BotGroupCommandConfig systemConfig = commandConfigService.checkCommandEnabled("default", commandKey);
            if (systemConfig == null) {
                sendMessage(message, base + " 未找到功能：" + commandKey + "\n使用 /功能列表 查看所有可用功能。");
                return;
            }
        }

        // 执行切换
        int result = commandConfigService.toggleCommandStatus(
                message.getGroupId().toString(),
                mainCommand,
                enable,
                message.getSender().getUserId().toString()
        );

        if (result > 0) {
            String action = enable ? "开启" : "关闭";
            sendMessage(message, base + " 已成功" + action + "功能：" + mainCommand);
        } else if (result == -1) {
            sendMessage(message, base + " 功能配置不存在：" + mainCommand);
        } else {
            sendMessage(message, base + " 操作失败，请稍后重试。");
        }
    }

    /**
     * 处理功能列表命令
     * 显示所有可用功能及其在当前群的启用状态
     */
    @BotCommand(description = "查看功能列表", permissionLevel = 0)
    private void handleCommandList(QQMessage message) {
        String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";
        String groupId = message.getGroupId().toString();

        StringBuilder sb = new StringBuilder();
        sb.append(base).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("📋 功能列表\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━\n\n");

        // 获取所有默认配置的命令
        BotGroupCommandConfig query = new BotGroupCommandConfig();
        query.setGroupId("default");
        List<BotGroupCommandConfig> defaultConfigs = commandConfigService.selectBotGroupCommandConfigList(query);

        // 按分类分组
        Map<String, List<BotGroupCommandConfig>> categoryMap = defaultConfigs.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCommandCategory() != null ? c.getCommandCategory() : "other"
                ));

        String[] categories = {"user", "admin", "super", "system"};
        String[] categoryNames = {"👥 普通用户功能", "👮 管理员功能", "⭐ 超级管理员功能", "🔔 系统通知功能"};

        for (int i = 0; i < categories.length; i++) {
            List<BotGroupCommandConfig> configs = categoryMap.get(categories[i]);
            if (configs == null || configs.isEmpty()) continue;

            sb.append(categoryNames[i]).append("\n");
            sb.append("────────────────────\n");

            for (BotGroupCommandConfig cfg : configs) {
                // 检查该群组的实际状态
                BotGroupCommandConfig actualConfig = commandConfigService.checkCommandEnabled(groupId, cfg.getCommandKey());
                boolean enabled = actualConfig == null || actualConfig.getIsEnabled() == null || actualConfig.getIsEnabled() == 1;
                String status = enabled ? "✅" : "❌";
                sb.append(status).append(" ").append(cfg.getCommandKey());
                if (StringUtils.isNotEmpty(cfg.getCommandName())) {
                    sb.append(" (").append(cfg.getCommandName()).append(")");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("💡 管理员可使用 /关闭 <功能> 或 /开启 <功能> 来控制");

        sendMessage(message, sb.toString());
    }

    /**
     * 处理服务器列表查询命令
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "查询服务器列表", permissionLevel = 0)
    public void handleServerList(QQMessage message) {
        try {
            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";

            // 默认只查询在线
            String[] parts = message.getMessage().split("\\s+");

            // 获取所有服务器信息
            final List<ServerInfo> serverInfos = serverInfoService.selectServerInfoList(new ServerInfo());
            List<ServerInfo> servers;

            if (!(parts.length > 1)) {
                // 只获取在线的服务器
                servers = new ArrayList<>();
                serverInfos.forEach(serverInfo -> {
                    if (RconCache.containsKey(String.valueOf(serverInfo.getId()))) {
                        servers.add(serverInfo);
                    }
                });

                if (servers.isEmpty()) {
                    sendMessage(message, base + " 当前没有在线的服务器。");
                    return;
                }
            } else if ("全部".equals(parts[1])) {
                // 获取所有服务器，包括离线的
                servers = serverInfos;

                if (servers.isEmpty()) {
                    sendMessage(message, base + " 当前没有任何服务器。");
                    return;
                }
            } else if (parts[1].startsWith("%") && parts[1].length() > 1) {
                final String replace = parts[1].replace("%", "");
                // 获取指定服务器
                servers = new ArrayList<>();
                for (ServerInfo server : serverInfos) {
                    if (server.getNameTag().contains(replace)) {
                        servers.add(server);
                    }
                }

                if (servers.isEmpty()) {
                    sendMessage(message, base + " 未找到名称包含 " + replace + " 的服务器。");
                    return;
                }
            } else {
                sendMessage(message, base + " 格式错误，正确格式：查询服务器 [全部]/[%模糊匹配]");
                return;
            }

            // 构建返回消息
            StringBuilder response = new StringBuilder(base + " 服务器列表：\n\n");

            // 遍历服务器信息
            for (ServerInfo server : servers) {
                boolean isOnline = RconCache.containsKey(String.valueOf(server.getId()));

                response.append("ID: ").append(server.getId()).append("\n");
                response.append("名称: ").append(server.getNameTag()).append("\n");
                response.append("状态: ").append(isOnline ? "在线" : "离线").append("\n");
                response.append("版本: ").append(server.getServerVersion()).append("\n");
                response.append("核心: ").append(server.getServerCore()).append("\n");
                response.append("地址: ").append(server.getPlayAddress()).append("\n");
                response.append("端口: ").append(server.getPlayAddressPort()).append("\n\n");
            }

            // 发送消息
            sendMessage(message, response.toString());

        } catch (Exception e) {
            log.error("处理服务器列表查询失败: {}", e.getMessage());
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 查询失败，请稍后重试。");
        }
    }

    /**
     * 处理help命令
     * 显示所有可用的命令及其用法
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "显示帮助信息", permissionLevel = 0)
    public void handleHelpCommand(QQMessage message) {
        String prefix = getCommandPrefix();
        StringBuilder help = new StringBuilder();
        help.append("[CQ:at,qq=").append(message.getSender().getUserId()).append("]\n");
        help.append("━━━━━━━━━━━━━━━━━━━━\n");
        help.append("📖 机器人命令帮助\n");
        help.append("━━━━━━━━━━━━━━━━━━━━\n\n");

        // 所有用户可用的命令
        help.append("👥 普通用户命令\n");
        help.append("━━━━━━━━━━━━━━━━━━━━\n");
        help.append("▫️ ").append(prefix).append("help (h)\n");
        help.append("   显示此帮助信息\n\n");
        help.append("▫️ ").append(prefix).append("白名单申请 (apply/wl)\n");
        help.append("   <玩家ID> <正版/离线>\n");
        help.append("   申请白名单\n\n");
        help.append("▫️ ").append(prefix).append("查询白名单 (check/wlcheck)\n");
        help.append("   查询自己的白名单状态\n\n");
        help.append("▫️ ").append(prefix).append("查询玩家 (player/p)\n");
        help.append("   <玩家ID>\n");
        help.append("   查询指定玩家信息\n\n");
        help.append("▫️ ").append(prefix).append("查询在线 (online/list)\n");
        help.append("   查询所有服务器在线玩家\n\n");
        help.append("▫️ ").append(prefix).append("查询服务器 (servers/sv)\n");
        help.append("   [全部]/[%模糊匹配]\n");
        help.append("   查询服务器列表\n\n");
        help.append("▫️ ").append(prefix).append("test (ping)\n");
        help.append("   <IP[:端口]> 或 <http(s)://url>\n");
        help.append("   测试服务器连通性\n\n");

        // 管理员命令
        List<QqBotManager> managers = config.selectManagerForThisGroup(message.getGroupId(), message.getUserId());
        if (!managers.isEmpty() && managers.get(0).getPermissionType() == 0) {
            help.append("━━━━━━━━━━━━━━━━━━━━\n");
            help.append("👮 管理员命令\n");
            help.append("━━━━━━━━━━━━━━━━━━━━\n");
            help.append("▫️ ").append(prefix).append("过审 (approve/pass)\n");
            help.append("   <玩家ID> - 通过白名单申请\n\n");
            help.append("▫️ ").append(prefix).append("拒审 (reject/deny)\n");
            help.append("   <玩家ID> - 拒绝白名单申请\n\n");
            help.append("▫️ ").append(prefix).append("封禁 (ban)\n");
            help.append("   <玩家ID> <原因> - 封禁玩家\n\n");
            help.append("▫️ ").append(prefix).append("解封 (unban)\n");
            help.append("   <玩家ID> - 解除玩家封禁\n\n");
            help.append("▫️ ").append(prefix).append("发送指令 (cmd/rcon)\n");
            help.append("   <服务器ID/all> <指令>\n");
            help.append("   发送RCON指令\n\n");
            help.append("▫️ ").append(prefix).append("运行状态 (status/sys)\n");
            help.append("   查看主机运行状态\n\n");
            help.append("▫️ ").append(prefix).append("刷新连接 (refresh/reload)\n");
            help.append("   [服务器ID] - 刷新RCON连接\n\n");
            help.append("▫️ ").append(prefix).append("测试连接 (testconn/tc)\n");
            help.append("   [服务器ID] - 测试RCON连接\n\n");
            help.append("▫️ ").append(prefix).append("实例列表 (instances/inst)\n");
            help.append("   查看游戏服务器实例\n\n");
            help.append("▫️ ").append(prefix).append("启动实例 (start/run)\n");
            help.append("   <实例ID> - 启动实例\n\n");
            help.append("▫️ ").append(prefix).append("停止实例 (stop/kill)\n");
            help.append("   <实例ID> - 停止实例\n\n");
            help.append("▫️ ").append(prefix).append("重启实例 (restart/reboot)\n");
            help.append("   <实例ID> - 重启实例\n\n");
            help.append("▫️ ").append(prefix).append("实例状态 (inststatus/is)\n");
            help.append("   <实例ID> - 查看实例状态\n\n");
            help.append("▫️ ").append(prefix).append("实例日志 (logs/log)\n");
            help.append("   <实例ID> [行数] - 查看实例日志\n\n");
            help.append("▫️ ").append(prefix).append("实例命令 (instcmd/ic)\n");
            help.append("   <实例ID> <命令>\n");
            help.append("   发送实例命令\n\n");
            help.append("▫️ ").append(prefix).append("节点状态 (nodestatus/ns)\n");
            help.append("   [节点ID] - 查看节点服务器状态\n\n");

            // 超级管理员命令
            if (managers.get(0).getPermissionType() == 0) {
                help.append("━━━━━━━━━━━━━━━━━━━━\n");
                help.append("⭐ 超级管理员命令\n");
                help.append("━━━━━━━━━━━━━━━━━━━━\n");
                help.append("▫️ ").append(prefix).append("添加管理 (addadmin/aa)\n");
                help.append("   <QQ号> [群号]\n");
                help.append("   添加普通管理员\n\n");
                help.append("▫️ ").append(prefix).append("添加超管 (addsuper/as)\n");
                help.append("   <QQ号> [群号]\n");
                help.append("   添加超级管理员\n\n");
            }

            // 功能开关命令
            help.append("━━━━━━━━━━━━━━━━━━━━\n");
            help.append("🔧 功能开关命令\n");
            help.append("━━━━━━━━━━━━━━━━━━━━\n");
            help.append("▫️ ").append(prefix).append("关闭 (disable/off)\n");
            help.append("   <功能名称> - 关闭指定功能\n\n");
            help.append("▫️ ").append(prefix).append("开启 (enable/on)\n");
            help.append("   <功能名称> - 开启指定功能\n\n");
            help.append("▫️ ").append(prefix).append("功能列表 (cmdlist/cl)\n");
            help.append("   查看所有功能及状态\n\n");
        }

        help.append("━━━━━━━━━━━━━━━━━━━━\n");
        help.append("💡 提示：括号内为英文简写命令");

        sendMessage(message, help.toString());
    }

    /**
     * 处理群成员减少通知
     * 当用户退群时，自动移除用户白名单，并发送通知
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "处理群退事件", permissionLevel = 0)
    public void handleGroupDecrease(QQMessage message) {
        if (config.getGroupIdList().contains(message.getGroupId())) {
            log.info("QQ群[{}]有用户退群 - 用户: {}", message.getGroupId(), message.getUserId());
            // 退群用户的QQ号
            Long userId = message.getUserId();
            // 查询白名单信息
            WhitelistInfo whitelistInfo = new WhitelistInfo();
            whitelistInfo.setQqNum(String.valueOf(userId));
            List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);
            if (whitelistInfos.isEmpty()) {
                return;
            }
            whitelistInfo = whitelistInfos.get(0);
            // 设置退群状态
            whitelistInfo.setAddState("true");
            whitelistInfo.setRemoveReason("用户退群-主动");
            // 更新白名单信息
            int result = whitelistInfoService.updateWhitelistInfo(whitelistInfo, message.getUserId().toString());
            if (result > 0) {
                log.info("用户 {} 退群，已更新白名单信息", userId);
                StringBuilder warningMsg = new StringBuilder();
                warningMsg.append("⚠️ 警告：玩家退群通知 ⚠️\n")
                        .append("━━━━━━━━━━━━━━━\n")
                        .append("👤 玩家信息：\n")
                        .append("▫️ 游戏ID：").append(whitelistInfo.getUserName()).append("\n")
                        .append("▫️ QQ号：").append(userId).append("\n")
                        .append("━━━━━━━━━━━━━━━\n")
                        .append("❗ 该玩家已主动退出群聊\n")
                        .append("❗ 白名单已自动移除\n")
                        .append("❗ 如需恢复白名单，请重新申请\n")
                        .append("━━━━━━━━━━━━━━━");
                sendMessage(message, warningMsg.toString());
            } else {
                log.error("用户 {} 退群，更新白名单信息失败", userId);
                sendMessage(message, "⚠️ 系统提示：玩家 " + userId + " 退群处理失败，请管理员手动处理！");
            }
        }
    }

    /**
     * 处理白名单查询请求
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "查询自己的白名单状态", permissionLevel = 0)
    public void handleWhitelistQuery(QQMessage message) {
        try {
            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";

            // 准备查询参数
            Map<String, String> params = new HashMap<>();
            params.put("qq", String.valueOf(message.getSender().getUserId()));

            // 调用服务查询白名单信息
            Map<String, Object> result = whitelistInfoService.check(params);

            if (result.isEmpty()) {
                sendMessage(message, base + " 未查询到您的白名单信息。");
                return;
            }

            // 构建返回消息
            StringBuilder response = new StringBuilder(base + " 您的白名单信息如下：\n");

            // 按固定顺序添加信息
            appendIfExists(response, result, "游戏ID");
            appendIfExists(response, result, "QQ号");
            appendIfExists(response, result, "账号类型");
            appendIfExists(response, result, "审核状态");

            if (result.containsKey("审核状态")) {
                String status = (String) result.get("审核状态");
                switch (status) {
                    case "已通过":
                        appendIfExists(response, result, "审核时间");
                        appendIfExists(response, result, "审核人");
                        appendIfExists(response, result, "最后上线时间");
                        appendIfExists(response, result, "游戏时间");
                        break;
                    case "未通过/已移除":
                        appendIfExists(response, result, "移除时间");
                        appendIfExists(response, result, "移除原因");
                        break;
                    case "已封禁":
                        appendIfExists(response, result, "封禁时间");
                        appendIfExists(response, result, "封禁原因");
                        break;
                    case "待审核":
                        appendIfExists(response, result, "UUID");
                        break;
                }
            }

            appendIfExists(response, result, "城市");

            // 发送消息
            sendMessage(message, response.toString());

        } catch (Exception e) {
            log.error("处理白名单查询失败: {}", e.getMessage());
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 查询失败，请稍后重试。");
        }
    }

    /**
     * 辅助方法：如果存在指定键值，则添加到响应消息中
     */
    private void appendIfExists(StringBuilder response, Map<String, Object> data, String key) {
        if (data.containsKey(key)) {
            response.append(key).append(": ").append(data.get(key)).append("\n");
        }
    }

    /**
     * 处理白名单申请请求
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "申请白名单", permissionLevel = 0)
    public void handleWhitelistApplication(QQMessage message) {
        try {
            // 解析消息内容
            String[] parts = message.getMessage().split("\\s+");
            if (parts.length < 3) {
                log.warn("白名单申请格式错误，正确格式：白名单申请 玩家ID 正版/离线");
                return;
            }

            // 提取玩家信息
            String playerId = parts[1];
            boolean isPremium = "正版".equals(parts[2]);
            int accountType = isPremium ? 1 : 0;

            log.info("收到白名单申请 - 玩家ID: {}, 账号类型: {}",
                    playerId,
                    isPremium ? "正版" : "离线");

            // 处理白名单申请
            handleWhitelistApplication(message.getGroupId(),
                    message.getSender().getUserId(),
                    playerId,
                    accountType, message);

        } catch (Exception e) {
            log.error("处理白名单申请失败: {}", e.getMessage());
        }
    }

    /**
     * 处理白名单申请
     *
     * @param groupId     QQ群号
     * @param userId      申请人QQ号
     * @param playerId    玩家ID
     * @param accountType 账号类型（1:正版, 0:离线）
     */
    private void handleWhitelistApplication(Long groupId, Long userId, String playerId, int accountType, QQMessage message) throws ExecutionException, InterruptedException {
        log.info("正在处理白名单申请 - 群号: {}, 申请人: {}, 玩家ID: {}, 账号类型: {}",
                groupId, userId, playerId, accountType == 1 ? "正版" : "离线");
        String base = "[CQ:at,qq=" + userId + "]";
        WhitelistInfo whitelistInfo = new WhitelistInfo();
        whitelistInfo.setQqNum(String.valueOf(userId));
        // 查询是否已存在该QQ号的申请
        final List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);
        if (!whitelistInfos.isEmpty()) {
            sendMessage(message, base + "您已提交过申请，请勿重复提交！");
            return;
        }

        whitelistInfo.setUserName(playerId);
        // whitelistInfo.setRemark(message.toString());
        whitelistInfo.setOnlineFlag((long) accountType);

        // 调用内部方法
        final Map<String, Object> result = this.applyForBot(whitelistInfo);

        if (result == null) {
            sendMessage(message, base + "申请失败，请稍后再试。");
            return;
        }

        if (result.get("status").equals("NO")) {
            sendMessage(message, base + result.get("msg"));
            return;
        }

        String code = (String) result.get("code");
        // 验证码生成成功
        if (StringUtils.isNotEmpty(code)) {
            // 发送消息
            String msg = base + "验证邮箱已发送到您此QQ，有效期为30分钟。";

            sendMessage(message, msg);

            String url = appUrl + "/#/verify?code=" + code;

            // 发送邮件通知
            emailService.push(whitelistInfo.getQqNum() + EmailTemplates.QQ_EMAIL,
                    EmailTemplates.EMAIL_VERIFY_TITLE, EmailTemplates.getEmailVerifyTemplate(url));
        } else {
            // 发送消息
            String msg = "[CQ:at,qq=" + userId + "] 申请失败，请稍后再试。";
            this.sendMessage(message, msg);
        }
    }

    /**
     * 私有化方法,用于程序内部机器人申请白名单
     *
     * @param whitelistInfo 白名单信息
     * @return Map
     */
    public Map<String, Object> applyForBot(WhitelistInfo whitelistInfo) {

        if (whitelistInfo == null || whitelistInfo.getUserName() == null ||
                whitelistInfo.getQqNum() == null || whitelistInfo.getOnlineFlag() == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        final String code = CodeUtil.generateCode(whitelistInfo.getQqNum(), CacheKey.VERIFY_FOR_BOT_KEY);

        if (StringUtils.isEmpty(code)) {
            result.put("status", "NO");
            result.put("msg", "验证码申请失败，请稍后再试。");
            return result;
        } else if (code != null && code.equals("isExist")) {
            result.put("status", "NO");
            result.put("msg", "请勿重复提交！否则可能将无法通过验证！");
            return result;
        } else {
            result.put("status", "YES");
            result.put("msg", "验证码申请成功，请查看邮箱。");
        }
        redisCache.setCacheObject(CacheKey.VERIFY_FOR_BOT_KEY + code, whitelistInfo, 30, TimeUnit.MINUTES);
        result.put("code", code);

        return result;
    }

    public void sendMessage(QQMessage message, String msg) {
        log.info("message: {}", message);
        // 发送消息
        try {
            if (config == null) {
                log.error("无法发送消息：机器人配置为空");
                return;
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("group_id", message.getGroupId().toString());
            jsonObject.put("message", msg);

            final HttpResponse response = HttpUtil.createPost(config.getHttpUrl() + BotApi.SEND_GROUP_MSG)
                    // 设置Authorization头
                    .header("Authorization", "Bearer " + config.getToken())
                    .body(jsonObject.toJSONString())
                    .execute();
            log.info("发送消息结果: {}", response.body());

            // 记录发送的消息
            String senderId = config.getBotQq();
            String senderType = "bot";
            String receiverId = message.getGroupId() != null ? message.getGroupId().toString() : null;
            String receiverType = "group";

            // 从响应中获取消息ID
            String messageId = null;
            try {
                JSONObject responseJson = JSON.parseObject(response.body());
                if (responseJson != null && responseJson.containsKey("data")) {
                    JSONObject data = responseJson.getJSONObject("data");
                    if (data != null && data.containsKey("message_id")) {
                        messageId = data.getString("message_id");
                    }
                }
            } catch (Exception e) {
                log.warn("解析消息ID失败: {}", e.getMessage());
            }

            logSentMessage(
                    messageId,
                    senderId,
                    senderType,
                    receiverId,
                    receiverType,
                    msg,
                    "text"
            );
        } catch (Exception e) {
            log.debug(e.toString());
            log.error("发送消息失败: {}", e.getMessage());
            logError("sendMessage", e.getMessage(), e.getStackTrace().toString());
        }
    }

    /**
     * 处理白名单审核请求
     * 管理员可以通过发送"过审 ID"或"拒审 ID"来审核白名单
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "审核白名单申请", permissionLevel = 1)
    public void handleWhitelistReview(QQMessage message) {
        try {
            log.info("开始处理白名单审核请求");

            // 检查是否是管理员
            List<QqBotManager> managers = config.selectManagerForThisGroup(message.getGroupId(), message.getUserId());
            if (managers.isEmpty()) {
                log.info("用户 {} 不是群 {} 的管理员", message.getUserId(), message.getGroupId());
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
                return;
            }

            log.info("用户 {} 是群 {} 的管理员，权限验证通过", message.getUserId(), message.getGroupId());

            String[] parts = message.getMessage().trim().split("\\s+");
            if (parts.length < 2) {
                log.info("命令格式错误: {}", message.getMessage());
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 格式错误，正确格式：过审/拒审 玩家ID");
                return;
            }

            String command = parts[0];
            String playerId = parts[1];

            log.info("处理白名单审核 - 命令: {}, 玩家ID: {}", command, playerId);

            if (command.equals("通过")) {
                final WhitelistInfo whitelistInfo = redisCache.getCacheObject(CacheKey.PASS_KEY + playerId);
                if (whitelistInfo == null) {
                    log.info("未找到玩家 {} 的白名单申请信息", playerId);
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 未找到玩家 " + playerId + " 的白名单申请。");
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
                        sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 已通过玩家 " + whitelistInfo.getUserName() + " 的白名单申请。");
                    } else {
                        log.warn("白名单审核失败: 更新数据库返回 {}", result);
                        sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 审核操作失败，请稍后重试。");
                    }

                    // 更新管理员最后活跃时间
                    updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());
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
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 未找到玩家 " + playerId + " 的白名单申请。");
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
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 已" + status + "玩家 " + playerId + " 的白名单申请。");
                } else {
                    log.warn("白名单审核失败: 更新数据库返回 {}", result);
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 审核操作失败，请稍后重试。");
                }

                // 更新管理员最后活跃时间
                updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());
            }

        } catch (Exception e) {
            log.debug(e.toString());
            log.error("处理白名单审核失败: {}", e.getMessage(), e);
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 审核失败，请稍后重试。");
        }
    }

    /**
     * 处理封禁和解封操作
     * 管理员可以通过发送"封禁 ID 原因"或"解封 ID"来操作
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "封禁/解封玩家", permissionLevel = 1)
    public void handleBanOperation(QQMessage message) {
        try {
            // 检查是否是管理员
            if (config.selectManagerForThisGroup(message.getGroupId(), message.getUserId()).isEmpty()) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
                return;
            }

            String[] parts = message.getMessage().trim().split("\\s+", 3);
            String command = parts[0];

            if (command.equals("封禁") && parts.length < 3) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 格式错误，正确格式：封禁 玩家ID 封禁原因");
                return;
            } else if (command.equals("解封") && parts.length < 2) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 格式错误，正确格式：解封 玩家ID");
                return;
            }

            String playerId = parts[1];
            String banReason = command.equals("封禁") ? parts[2] : null;

            // 查询白名单信息
            WhitelistInfo whitelistInfo = new WhitelistInfo();
            whitelistInfo.setUserName(playerId);
            List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);

            if (whitelistInfos.isEmpty()) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 未找到玩家 " + playerId + " 的白名单信息。");
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
                String msg = "[CQ:at,qq=" + message.getSender().getUserId() + "] 已" + status + "玩家 " + playerId;
                if (command.equals("封禁")) {
                    msg += "，原因：" + banReason;
                }
                sendMessage(message, msg);
            } else {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 操作失败，请稍后重试。");
            }

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());

        } catch (Exception e) {
            log.debug(e.toString());
            log.error("处理封禁/解封操作失败: {}", e.getMessage());
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 操作失败，请稍后重试。");
        }
    }

    /**
     * 处理RCON指令发送
     * 管理员可以通过发送"发送指令 服务器ID 指令内容"来执行服务器指令
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "发送RCON指令", permissionLevel = 1)
    public void handleRconCommand(QQMessage message, boolean lastUsed) {
        try {
            // 检查是否是管理员
            if (config.selectManagerForThisGroup(message.getGroupId(), message.getUserId()).isEmpty()) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
                return;
            }
            final List<QqBotManager> qqBotManagers = config.selectManagerForThisGroup(message.getGroupId(), message.getUserId());
            final QqBotManager qqBotManager = qqBotManagers.get(0);
            if (qqBotManager.getPermissionType() != 0) {
                // 权限不足
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 权限不足！");
                return;
            }

            String serverId;
            String command;
            if (!lastUsed) {
                String[] parts = message.getMessage().trim().split("\\s+", 3);
                if (parts.length < 3) {
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 格式错误，正确格式：发送指令 服务器ID/all 指令内容");
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
                // 直接使用整个消息内容作为命令（已经去除了前缀）
                command = message.getMessage().trim();
                if (command.isEmpty()) {
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 格式错误，正确格式：/指令内容");
                    return;
                }
                // 获取用户最后使用的服务器ID
                serverId = redisCache.getCacheObject(CacheKey.LAST_USED_SERVER_KEY + message.getSender().getUserId());
                if (serverId == null) {
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 未找到您上次使用的服务器ID，请使用完整格式发送指令。");
                    return;
                }
            }

            if (!serverId.contains("all")) {
                if (!RconCache.containsKey(serverId)) {
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 未找到服务器 " + serverId);
                    return;
                }
            }

            // 判断是否为高危命令
            if (CommandUtil.isHighRiskCommand(command)) {
                // 获取确认状态
                String confirmKey = CacheKey.COMMAND_USE_KEY + "confirm:" + message.getSender().getUserId() + ":" + serverId + ":" + command;
                Integer confirmCount = redisCache.getCacheObject(confirmKey);

                // 如果未确认过，或者确认次数不足
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
                                // 使用JSON转换
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
                    warningMsg.append("[CQ:at,qq=").append(message.getSender().getUserId()).append("] ");
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
                response.append("[CQ:at,qq=").append(message.getSender().getUserId()).append("] ");

                if ("all".equals(serverId)) {
                    response.append("指令已发送至所有在线服务器\n");
                } else {
                    Object serverObj = serverInfoMap.get(serverId);
                    if (serverObj != null) {
                        // 将JSON对象转换为ServerInfo对象
                        ServerInfo serverInfo = null;
                        if (serverObj instanceof ServerInfo) {
                            serverInfo = (ServerInfo) serverObj;
                        } else {
                            try {
                                // 使用JSON转换
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
                    // 缓存用户最后使用的服务器ID，以便下次默认使用
                    String lastServerKey = CacheKey.LAST_USED_SERVER_KEY + message.getSender().getUserId();
                    redisCache.setCacheObject(lastServerKey, serverId, 1, TimeUnit.DAYS);
                    response.append("\n(已记录您最后使用的服务器ID: ").append(serverId).append("，24小时内再次发送指令时将默认使用)");
                }
                sendMessage(message, response.toString());
            } catch (Exception e) {
                log.error("发送RCON指令失败: {}", e.getMessage());
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 指令发送失败：" + e.getMessage());
            }

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());

        } catch (Exception e) {
            log.debug(e.toString());
            log.error("处理RCON指令失败: {}", e.getMessage());
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 操作失败，请稍后重试。");
        }
    }

    /**
     * 处理玩家信息查询请求
     * 玩家可以通过发送"查询玩家 玩家ID"来查询任意玩家的信息
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "查询指定玩家的详细信息")
    public void handlePlayerQuery(QQMessage message) {
        try {
            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：查询玩家 玩家ID");
                return;
            }

            String playerId = parts[1];

            // 准备查询参数
            Map<String, String> params = new HashMap<>();
            params.put("id", playerId);

            // 调用服务查询白名单信息
            Map<String, Object> result = whitelistInfoService.check(params);

            if (result.isEmpty()) {
                sendMessage(message, base + " 未查询到玩家 " + playerId + " 的信息。");
                return;
            }

            // 构建返回消息
            StringBuilder response = new StringBuilder(base + " 玩家 " + playerId + " 的信息如下：\n");

            // 按固定顺序添加信息
            appendIfExists(response, result, "游戏ID");
            appendIfExists(response, result, "QQ号");
            appendIfExists(response, result, "账号类型");
            appendIfExists(response, result, "审核状态");

            if (result.containsKey("审核状态")) {
                String status = (String) result.get("审核状态");
                switch (status) {
                    case "已通过":
                        appendIfExists(response, result, "审核时间");
                        appendIfExists(response, result, "审核人");
                        appendIfExists(response, result, "最后上线时间");
                        appendIfExists(response, result, "游戏时间");
                        break;
                    case "未通过/已移除":
                        appendIfExists(response, result, "移除时间");
                        appendIfExists(response, result, "移除原因");
                        break;
                    case "已封禁":
                        appendIfExists(response, result, "封禁时间");
                        appendIfExists(response, result, "封禁原因");
                        break;
                    case "待审核":
                        appendIfExists(response, result, "UUID");
                        break;
                }
            }

            appendIfExists(response, result, "城市");
            if (result.containsKey("历史名称")) {
                response.append("历史名称: ").append(result.get("历史名称")).append("\n");
            }

            // 发送消息
            sendMessage(message, response.toString());

        } catch (Exception e) {
            log.error("处理玩家查询失败: {}", e.getMessage());
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 查询失败，请稍后重试。");
        }
    }

    /**
     * 处理在线玩家查询请求
     * 查询所有服务器的在线玩家信息
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "查询所有服务器的在线玩家信息")
    public void handleOnlineQuery(QQMessage message) {
        try {
            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";

            // 获取在线玩家信息
            Map<String, Object> result = serverInfoService.getOnlinePlayer(false);

            if (result.isEmpty()) {
                sendMessage(message, base + " 当前没有服务器在线。");
                return;
            }

            // 构建返回消息
            StringBuilder response = new StringBuilder(base + " 当前在线情况如下：\n");

            // 遍历每个服务器的信息
            for (Map.Entry<String, Object> entry : result.entrySet()) {
                if (entry.getKey().equals("查询时间")) {
                    response.append("\n查询时间: ").append(entry.getValue());
                    continue;
                }

                response.append("\n服务器: ").append(entry.getKey()).append("\n");

                if (entry.getValue() instanceof String) {
                    response.append(entry.getValue()).append("\n");
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> serverInfo = (Map<String, Object>) entry.getValue();
                response.append("在线人数: ").append(serverInfo.get("在线人数")).append("\n");
                if ((int) serverInfo.get("在线人数") > 0) {
                    response.append("在线玩家: ").append(serverInfo.get("在线玩家")).append("\n");
                }
            }

            // 发送消息
            sendMessage(message, response.toString());

        } catch (Exception e) {
            log.error("处理在线查询失败: {}", e.getMessage());
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 查询失败，请稍后重试。");
        }
    }

    /**
     * 处理主机状态查询请求
     * 查询运行该项目的服务器主机状态，包括系统信息、CPU、内存、JVM等
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "查询主机状态", permissionLevel = 1)
    public void handleHostStatus(QQMessage message) {
        // 检查是否是管理员
        if (!config.getManagerIdList().contains(message.getSender().getUserId())) {
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
            return;
        }

        try {
            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";

            Server server = new Server();
            server.copyTo();

            // 构建返回消息
            StringBuilder response = new StringBuilder(base + " 主机运行状态如下：\n\n");

            // CPU信息
            response.append("CPU状态：\n");
            response.append("核心数：").append(server.getCpu().getCpuNum()).append("\n");
            // response.append("CPU总使用率：").append(server.getCpu().getTotal()).append("%\n");
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
            response.append("版本：").append(env.getProperty("ruoyi.version")).append("\n");

            // 磁盘信息
            // response.append("\n磁盘状态：\n");
            // for (SysFile sysFile : server.getSysFiles()) {
            //     response.append(sysFile.getDirName()).append("（").append(sysFile.getTypeName()).append("）：\n");
            //     response.append("总大小：").append(sysFile.getTotal()).append("GB\n");
            //     response.append("已用大小：").append(sysFile.getUsed()).append("GB\n");
            //     response.append("剩余大小：").append(sysFile.getFree()).append("GB\n");
            //     response.append("使用率：").append(sysFile.getUsage()).append("%\n");
            // }

            // 发送消息
            sendMessage(message, response.toString());

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());

        } catch (Exception e) {
            log.error("处理主机状态查询失败:{} ", e.getMessage(), e);
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 查询失败，请稍后重试。");
        }
    }

    /**
     * 更新机器人配置中的管理员信息
     * 在添加或修改管理员后调用此方法以刷新配置
     */
    public void updateManagerConfig() {
        try {
            if (config == null) {
                log.error("无法更新管理员配置：机器人配置为空");
                return;
            }

            // 从数据库重新获取最新的机器人配置
            QqBotConfig latestConfig = qqBotConfigService.selectQqBotConfigById(config.getId());
            if (latestConfig == null) {
                log.error("无法获取机器人配置：ID {} 不存在", config.getId());
                return;
            }

            // 更新当前配置
            this.config = latestConfig;
            log.info("机器人 {} 的管理员配置已更新", config.getId());
        } catch (Exception e) {
            log.error("更新管理员配置失败: {}", e.getMessage());
        }
    }

    /**
     * 处理添加管理员命令
     * 超级管理员可以通过发送"添加管理 QQ号 [群号]"来添加普通管理员
     * 如果不指定群号，则默认为当前群
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "添加管理员", permissionLevel = 2)
    public void handleAddManager(QQMessage message) {
        try {
            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";

            // 检查是否是超级管理员
            List<QqBotManager> managers = config.selectManagerForThisGroup(message.getGroupId(), message.getUserId());
            if (managers.isEmpty() || managers.get(0).getPermissionType() != 0) {
                sendMessage(message, base + " 您没有权限执行此操作，此操作仅限超级管理员使用。");
                return;
            }

            String[] parts = message.getMessage().replace(getCommandPrefix(), "").trim().split("\\s+");
            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：添加管理 QQ号 [群号]，不填群号默认为当前群");
                return;
            }

            String targetQQ = parts[1];
            // 如果没有指定群号，使用当前群号
            String groupId = parts.length > 2 ? parts[2] : String.valueOf(message.getGroupId());

            // 查询是否已存在该QQ号的管理员
            QqBotManager manager = new QqBotManager();
            manager.setManagerQq(targetQQ);
            manager.setPermissionType(1L);
            List<QqBotManager> managers1 = qqBotManagerService.selectQqBotManagerList(manager);
            if (!managers1.isEmpty()) {
                sendMessage(message, base + " 该QQ号已是管理员，无需重复添加。");
                return;
            }

            // 调用API查询QQ号信息
            JSONObject body = new JSONObject();
            body.put("user_id", targetQQ);
            final HttpResponse response = HttpUtil
                    .createPost(config.getHttpUrl() + BotApi.GET_STRANGER_INFO)
                    .header("Authorization", "Bearer " + config.getToken())
                    .body(body.toJSONString())
                    .execute();

            if (!response.isOk()) {
                sendMessage(message, base + " 查询QQ号信息失败，请稍后重试。");
                log.error("查询QQ号信息失败: {}", response);
                return;
            }

            final JSONObject jsonObject = JSON.parseObject(response.body());
            if (jsonObject.containsKey("retcode") && jsonObject.getInteger("retcode") != 0 || jsonObject.getJSONObject("data") == null) {
                sendMessage(message, base + " 未查询到该QQ号的信息，请检查QQ号是否正确。");
                return;
            }
            // 设置管理员名称
            String managerName = jsonObject.getJSONObject("data").getString("nick");

            // 创建新的管理员对象
            QqBotManager newManager = new QqBotManager();
            newManager.setBotId(config.getId());
            newManager.setManagerQq(targetQQ);
            newManager.setPermissionType(1L); // 1表示普通管理员
            newManager.setManagerName(managerName == null ? "未知" : managerName);
            newManager.setStatus(1L); // 1表示启用状态

            // 创建群组关联
            QqBotManagerGroup group = new QqBotManagerGroup();
            group.setGroupId(groupId);
            group.setStatus(1L);

            // 设置群组列表
            List<QqBotManagerGroup> groups = new ArrayList<>();
            groups.add(group);
            newManager.setQqBotManagerGroupList(groups);

            // 调用服务添加管理员
            int result = qqBotManagerService.insertQqBotManager(newManager);

            if (result > 0) {
                // 更新管理员配置
                updateManagerConfig();
                sendMessage(message, base + " 已成功添加管理员，QQ：" + targetQQ + "，群号：" + groupId);
            } else {
                sendMessage(message, base + " 添加管理员失败，请稍后重试。");
            }

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());

        } catch (Exception e) {
            log.debug(e.toString());
            log.error("处理添加管理员失败: {}", e.getMessage());
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 添加管理员失败，请稍后重试。");
        }
    }

    /**
     * 处理添加超级管理员命令
     * 超级管理员可以通过发送"添加超管 QQ号 [群号]"来添加其他超级管理员
     * 如果不指定群号，则默认为当前群
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "添加超级管理员", permissionLevel = 2)
    public void handleAddSuperManager(QQMessage message) {
        try {
            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";

            // 检查是否是超级管理员
            List<QqBotManager> managers = config.selectManagerForThisGroup(message.getGroupId(), message.getUserId());
            if (managers.isEmpty() || managers.get(0).getPermissionType() != 0) {
                sendMessage(message, base + " 您没有权限执行此操作，此操作仅限超级管理员使用。");
                return;
            }

            String[] parts = message.getMessage().replace(getCommandPrefix(), "").trim().split("\\s+");
            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：添加超管 QQ号 [群号]，不填群号默认为当前群");
                return;
            }

            String targetQQ = parts[1];
            // 如果没有指定群号，使用当前群号
            String groupId = parts.length > 2 ? parts[2] : String.valueOf(message.getGroupId());

            // 查询是否已存在该QQ号的超级管理员
            QqBotManager manager = new QqBotManager();
            manager.setManagerQq(targetQQ);
            manager.setPermissionType(0L);
            List<QqBotManager> superManagers = qqBotManagerService.selectQqBotManagerList(manager);
            if (!superManagers.isEmpty()) {
                sendMessage(message, base + " 该QQ号已是超级管理员，无需重复添加。");
                return;
            }

            // 调用API查询QQ号信息
            JSONObject body = new JSONObject();
            body.put("user_id", targetQQ);
            final HttpResponse response = HttpUtil
                    .createPost(config.getHttpUrl() + BotApi.GET_STRANGER_INFO)
                    .header("Authorization", "Bearer " + config.getToken())
                    .body(body.toJSONString())
                    .execute();

            if (!response.isOk()) {
                sendMessage(message, base + " 查询QQ号信息失败，请稍后重试。");
                log.error("查询QQ号信息失败: {}", response);
                return;
            }

            final JSONObject jsonObject = JSON.parseObject(response.body());
            if (jsonObject.containsKey("retcode") && jsonObject.getInteger("retcode") != 0 || jsonObject.getJSONObject("data") == null) {
                sendMessage(message, base + " 未查询到该QQ号的信息，请检查QQ号是否正确。");
                return;
            }

            // 设置管理员名称
            String managerName = jsonObject.getJSONObject("data").getString("nick");

            // 创建新的超级管理员对象
            QqBotManager newManager = new QqBotManager();
            newManager.setBotId(config.getId());
            newManager.setManagerQq(targetQQ);
            newManager.setPermissionType(0L); // 0表示超级管理员
            newManager.setManagerName(managerName == null ? "未知" : managerName);
            newManager.setStatus(1L); // 1表示启用状态

            // 创建群组关联
            QqBotManagerGroup group = new QqBotManagerGroup();
            group.setGroupId(groupId);
            group.setStatus(1L);

            // 设置群组列表
            List<QqBotManagerGroup> groups = new ArrayList<>();
            groups.add(group);
            newManager.setQqBotManagerGroupList(groups);

            // 调用服务添加超级管理员
            int result = qqBotManagerService.insertQqBotManager(newManager);

            if (result > 0) {
                // 更新管理员配置
                updateManagerConfig();
                sendMessage(message, base + " 已成功添加超级管理员，QQ：" + targetQQ + "，群号：" + groupId);
            } else {
                sendMessage(message, base + " 添加超级管理员失败，请稍后重试。");
            }

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());

        } catch (Exception e) {
            log.error("处理添加超级管理员失败: {}", e.getMessage());
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 添加超级管理员失败，请稍后重试。");
        }
    }

    private void updateQqBotManagerLastActiveTime(Long userId, Long botId) {
        if (botId == null || userId == null) {
            log.info("更新管理员最后活跃时间失败：参数为空");
            return;
        }

        QqBotManager manager = new QqBotManager();
        manager.setManagerQq(userId.toString());
        manager.setBotId(botId);
        manager.setLastActiveTime(new Date());

        final int i = qqBotManagerService.updateQqBotManagerLastActiveTime(manager);

        if (i > 0) {
            log.info("更新管理员 {} 最后活跃时间成功", userId);
        } else {
            log.info("更新管理员 {} 最后活跃时间失败", userId);
        }
    }

    /**
     * 测试Minecraft服务器通断
     * 用户可以通过发送"test IP[:端口]"来测试服务器连通性
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "测试Minecraft服务器连通性")
    public void testServer(QQMessage message) {
        try {
            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：test <服务器地址>[:端口]，默认端口25565");
                return;
            }

            // 检查是否是管理员，非管理员有使用次数限制
            boolean isAdmin = !config.selectManagerForThisGroup(message.getGroupId(), message.getUserId()).isEmpty();

            // 如果不是管理员，检查使用次数限制
            if (!isAdmin) {
                String userId = message.getSender().getUserId().toString();
                String usageKey = CacheKey.COMMAND_USE_KEY + "test:" + userId;

                // 获取今日使用次数
                Integer usageCount = redisCache.getCacheObject(usageKey);

                // 如果缓存中没有，初始化为0
                if (usageCount == null) {
                    usageCount = 0;
                }

                // 检查是否超过每日限制(10次)
                if (usageCount >= 10) {
                    sendMessage(message, base + " 您今日的测试次数已用完，每位用户每天限制使用10次。");
                    return;
                }

                // 增加使用次数并更新缓存，设置过期时间为当天结束
                redisCache.setCacheObject(usageKey, usageCount + 1, getSecondsUntilEndOfDay(), TimeUnit.SECONDS);

                // 显示剩余使用次数
                sendMessage(message, base + " 您今天还能使用 " + (10 - (usageCount + 1)) + " 次Minecraft服务器测试指令。");
            }

            String serverAddress = parts[1];
            String ip;
            final int port; // 声明为final，因为在lambda表达式中使用

            // 解析IP和端口
            if (serverAddress.contains(":")) {
                String[] addressParts = serverAddress.split(":");
                ip = addressParts[0];
                try {
                    port = Integer.parseInt(addressParts[1]);
                } catch (NumberFormatException e) {
                    sendMessage(message, base + " 端口格式错误，必须是数字");
                    return;
                }
            } else {
                ip = serverAddress;
                port = 25565; // 默认端口
            }

            // 验证是否为有效的IP地址或域名
            if (!IPUtils.isValidIpOrDomain(ip)) {
                sendMessage(message, base + " 无效的IP地址或域名格式，请检查输入");
                return;
            }

            // 发送检测中的提示消息
            sendMessage(message, base + " 正在检测服务器 " + ip + ":" + port + " 的连通性，请稍候...");

            // 异步执行服务器测试
            asyncExecutor.execute(new TimerTask() {
                @Override
                public void run() {
                    performServerTest(message, base, serverAddress, ip, port);
                }
            });

        } catch (Exception e) {
            log.error("测试服务器通断失败: {}", e.getMessage());
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 测试失败，请稍后重试。");
        }
    }

    /**
     * 异步执行服务器连通性测试
     */
    private void performServerTest(QQMessage message, String base, String serverAddress, String ip, int port) {
        try {
            // 尝试解析SRV记录
            boolean hasSrv = false;
            try {
                // 检查是否有SRV记录
                String srvLookup = "_minecraft._tcp." + ip;
                log.info("尝试解析SRV记录: {}", srvLookup);

                // InetAddress.getAllByName(ip); // 检查域名是否有效

                DirContext dirContext = new InitialDirContext();
                Attributes attributes = dirContext.getAttributes("dns:/" + srvLookup, new String[]{"SRV"});
                Attribute attribute = attributes.get("SRV");

                if (attribute != null) {
                    // 解析SRV记录
                    String srvRecord = attribute.get().toString();
                    log.info("找到SRV记录: {}", srvRecord);

                    // SRV记录格式: 优先级 权重 端口 目标主机
                    String[] srvParts = srvRecord.split(" ");
                    if (srvParts.length >= 4) {
                        // 获取目标主机和端口
                        String target = srvParts[3];
                        // 如果主机名以点结尾，去掉结尾的点
                        if (target.endsWith(".")) {
                            target = target.substring(0, target.length() - 1);
                        }
                        int srvPort = Integer.parseInt(srvParts[2]);

                        // 更新连接信息
                        log.info("SRV解析: {} -> {}:{}", ip, target, srvPort);
                        sendMessage(message, base + " 发现SRV记录，重定向至 " + target + ":" + srvPort);

                        ip = target;
                        port = srvPort;
                        hasSrv = true;
                    }
                }
            } catch (Exception e) {
                // SRV记录解析失败，继续使用原始IP和端口
                log.info("SRV记录解析失败或不存在: {}", e.getMessage());
                if (hasSrv) {
                    // 只有在确认有SRV但解析失败时才发送消息
                    sendMessage(message, base + " SRV记录解析失败，将使用原始地址");
                }
            }

            // 开始时间
            long startTime = System.currentTimeMillis();

            // 使用Java Socket尝试连接
            try (Socket socket = new Socket()) {
                // 设置连接超时时间为5秒
                socket.connect(new InetSocketAddress(ip, port), 5000);

                // 计算连接耗时
                long connectTime = System.currentTimeMillis() - startTime;

                // 连接成功
                StringBuilder response = new StringBuilder();
                response.append(base).append(" Minecraft服务器连通性测试结果：\n\n");
                response.append("✅ 服务器 ").append(serverAddress).append(" 可以连接\n");

                // 获取服务器IP地址
                String ipAddress = null;
                try {
                    InetAddress inetAddress = InetAddress.getByName(ip);
                    ipAddress = inetAddress.getHostAddress();
                } catch (Exception e) {
                    log.warn("获取IP地址失败: {}", e.getMessage());
                }
                if (ipAddress != null) {
                    response.append("IP地址: ").append(ipAddress).append("\n");
                }

                response.append("连接耗时: ").append(connectTime).append("ms\n\n");

                // 尝试获取服务器信息 (Minecraft Server List Ping)
                try {
                    // 创建新连接用于Server List Ping
                    Socket pingSocket = new Socket();
                    pingSocket.connect(new InetSocketAddress(ip, port), 5000);

                    OutputStream out = pingSocket.getOutputStream();
                    DataOutputStream dataOut = new DataOutputStream(out);

                    InputStream in = pingSocket.getInputStream();
                    DataInputStream dataIn = new DataInputStream(in);

                    // 发送握手包和状态请求
                    // 构造握手包: 包长度 + 包ID(0x00) + 协议版本 + 服务器地址长度 + 服务器地址 + 端口 + 下一状态(1表示状态)
                    ByteArrayOutputStream handshakeBytes = new ByteArrayOutputStream();
                    DataOutputStream handshake = new DataOutputStream(handshakeBytes);

                    handshake.writeByte(0x00);         // 握手包ID
                    writeVarInt(handshake, 47);    // 协议版本 (1.8+)
                    writeString(handshake, ip);         // 服务器地址
                    handshake.writeShort(port);        // 端口
                    writeVarInt(handshake, 1);  // 下一状态 (1 = 状态)

                    // 发送握手包
                    writeVarInt(dataOut, handshakeBytes.size());
                    dataOut.write(handshakeBytes.toByteArray());

                    // 发送状态请求
                    writeVarInt(dataOut, 1); // 包长度
                    writeVarInt(dataOut, 0); // 包ID (0x00)

                    // 读取响应
                    readVarInt(dataIn);
                    int packetId = readVarInt(dataIn);

                    if (packetId == 0x00) {
                        String jsonResponse = readString(dataIn);
                        log.info("Server responded with JSON: {}", jsonResponse);

                        // 解析JSON响应
                        JSONObject serverData = JSON.parseObject(jsonResponse);

                        // 添加服务器信息到响应中
                        if (serverData.containsKey("version")) {
                            JSONObject version = serverData.getJSONObject("version");
                            response.append("服务器版本: ").append(version.getString("name")).append("\n");
                        }

                        if (serverData.containsKey("players")) {
                            JSONObject players = serverData.getJSONObject("players");
                            response.append("在线人数: ").append(players.getInteger("online"))
                                    .append("/").append(players.getInteger("max")).append("\n");
                        }

                        if (serverData.containsKey("description")) {
                            Object description = serverData.get("description");
                            String motd = extractMotdFromJson(description);

                            // 清除Minecraft颜色代码
                            motd = stripMinecraftColorCodes(motd);
                            response.append("服务器描述: ").append(motd.trim()).append("\n");
                        }
                    }

                    pingSocket.close();
                } catch (Exception e) {
                    log.warn("获取服务器信息失败: {}", e.getMessage());
                    response.append("无法获取详细服务器信息，但服务器可连接\n");
                }

                sendMessage(message, response.toString());
            } catch (UnknownHostException e) {
                // 域名解析失败
                sendMessage(message, base + " ❌ 服务器连接失败：无法解析域名 " + ip);
            } catch (ConnectException e) {
                // 连接被拒绝
                sendMessage(message, base + " ❌ 服务器连接失败：连接被拒绝，服务器可能未启动或端口未开放");
            } catch (SocketTimeoutException e) {
                // 连接超时
                sendMessage(message, base + " ❌ 服务器连接失败：连接超时，服务器响应时间过长或不可达");
            } catch (Exception e) {
                // 其他连接错误
                sendMessage(message, base + " ❌ 服务器连接失败：" + e.getMessage());
            }
        } catch (Exception e) {
            log.error("异步测试服务器通断失败: {}", e.getMessage());
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 测试失败，请稍后重试。");
        }
    }

    /**
     * 从JSON中提取MOTD文本
     * 处理Minecraft服务器返回的复杂JSON描述格式
     *
     * @param description JSON描述对象
     * @return 提取出的纯文本MOTD
     */
    private String extractMotdFromJson(Object description) {
        StringBuilder result = new StringBuilder();

        try {
            if (description instanceof String) {
                // 简单字符串格式
                return (String) description;
            } else if (description instanceof JSONObject) {
                JSONObject jsonObj = (JSONObject) description;

                // 处理基本text字段
                if (jsonObj.containsKey("text")) {
                    result.append(jsonObj.getString("text"));
                }

                // 处理extra数组（包含额外文本元素）
                if (jsonObj.containsKey("extra") && jsonObj.get("extra") instanceof List) {
                    List<Object> extraList = (List<Object>) jsonObj.get("extra");
                    for (Object extraItem : extraList) {
                        // 递归处理每个元素
                        result.append(extractMotdFromJson(extraItem));
                    }
                }
            } else if (description instanceof List) {
                // 处理数组格式
                List<Object> list = (List<Object>) description;
                for (Object item : list) {
                    result.append(extractMotdFromJson(item));
                }
            } else {
                // 其他类型，转为字符串
                result.append(description.toString());
            }
        } catch (Exception e) {
            log.warn("解析服务器描述失败: {}", e.getMessage());
            return description.toString();
        }

        return result.toString();
    }

    /**
     * 清除Minecraft颜色代码
     * Minecraft使用§加颜色代码来表示颜色，如§a表示绿色，§c表示红色等
     *
     * @param text 包含颜色代码的文本
     * @return 清除颜色代码后的文本
     */
    private String stripMinecraftColorCodes(String text) {
        if (text == null) {
            return "";
        }
        // 使用正则表达式去除所有Minecraft颜色代码 (§ 后跟一个字符)
        return text.replaceAll("§[0-9a-fk-or]", "");
    }

    /**
     * 写入VarInt类型到流
     */
    private void writeVarInt(DataOutputStream out, int value) throws IOException {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.writeByte(value);
                return;
            }

            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
    }

    /**
     * 读取VarInt类型
     */
    private int readVarInt(DataInputStream in) throws IOException {
        int value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = in.readByte();
            value |= (currentByte & 0x7F) << position;

            if ((currentByte & 0x80) == 0) break;

            position += 7;
            if (position >= 32) throw new RuntimeException("VarInt is too big");
        }

        return value;
    }

    /**
     * 写入字符串到流
     */
    private void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    /**
     * 读取字符串
     */
    private String readString(DataInputStream in) throws IOException {
        int length = readVarInt(in);
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 计算到今天结束还剩多少秒
     *
     * @return 剩余秒数
     */
    private Integer getSecondsUntilEndOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
        long seconds = (calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000;
        return (int) seconds;
    }

    /**
     * 处理刷新连接命令
     * 管理员可以刷新指定服务器或所有服务器的RCON连接
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "刷新RCON连接", permissionLevel = 1)
    public void handleRefreshConnection(QQMessage message) {
        try {
            // 检查是否是管理员
            if (config.selectManagerForThisGroup(message.getGroupId(), message.getUserId()).isEmpty()) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
                return;
            }
            final List<QqBotManager> qqBotManagers = config.selectManagerForThisGroup(message.getGroupId(), message.getUserId());
            final QqBotManager qqBotManager = qqBotManagers.get(0);
            if (qqBotManager.getPermissionType() != 0) {
                // 权限不足
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 权限不足！");
                return;
            }

            String[] parts = message.getMessage().trim().split("\\s+");
            String serverId = "all";

            // 如果指定了服务器ID
            if (parts.length > 1) {
                serverId = parts[1];
                if (!serverId.equals("all") && !RconCache.containsKey(serverId)) {
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 未找到服务器 " + serverId);
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
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 已成功刷新所有服务器的RCON连接。");
            } else {
                // 获取服务器信息
                Map<String, Object> serverInfoMap = redisCache.getCacheObject(CacheKey.SERVER_INFO_MAP_KEY);
                String serverDisplay = serverId;
                ServerInfo serverInfo = null;

                Object serverObj = serverInfoMap.get(serverId);
                if (serverObj != null) {
                    try {
                        // 使用JSON转换
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
                        sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 已成功刷新服务器 " + serverDisplay + " 的RCON连接。");
                    } else {
                        sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 刷新服务器 " + serverDisplay + " 的RCON连接失败，请检查服务器状态。");
                    }
                } else {
                    // 如果从Redis缓存获取失败，尝试从数据库获取
                    ServerInfo dbServerInfo = serverInfoService.selectServerInfoById(Long.parseLong(serverId));
                    if (dbServerInfo != null) {
                        boolean success = rconService.init(dbServerInfo);
                        if (success) {
                            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 已成功刷新服务器 " + serverId + " 的RCON连接。");
                        } else {
                            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 刷新服务器 " + serverId + " 的RCON连接失败，请检查服务器状态。");
                        }
                    } else {
                        sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 未找到服务器 " + serverId);
                    }
                }
            }

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());
        } catch (Exception e) {
            log.error("刷新RCON连接失败: {}", e.getMessage());
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 刷新RCON连接失败：" + e.getMessage());
        }
    }

    /**
     * 处理测试连接命令
     * 管理员可以测试指定服务器或所有服务器的RCON连接
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "测试RCON连接", permissionLevel = 1)
    public void handleTestConnection(QQMessage message) {
        try {
            // 检查是否是管理员
            if (config.selectManagerForThisGroup(message.getGroupId(), message.getUserId()).isEmpty()) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
                return;
            }
            final List<QqBotManager> qqBotManagers = config.selectManagerForThisGroup(message.getGroupId(), message.getUserId());
            final QqBotManager qqBotManager = qqBotManagers.get(0);
            if (qqBotManager.getPermissionType() != 0) {
                // 权限不足
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 权限不足！");
                return;
            }

            String[] parts = message.getMessage().trim().split("\\s+");
            String serverId = "all";

            // 如果指定了服务器ID
            if (parts.length > 1) {
                serverId = parts[1];
                if (!serverId.equals("all") && !RconCache.containsKey(serverId)) {
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 未找到服务器 " + serverId);
                    return;
                }
            }

            StringBuilder response = new StringBuilder();
            response.append("[CQ:at,qq=").append(message.getSender().getUserId()).append("] 测试连接结果：\n\n");

            if (serverId.equals("all")) {
                // 测试所有服务器
                if (RconCache.isEmpty()) {
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 当前没有RCON连接。");
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
                            // 使用JSON转换
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
                        // 使用JSON转换
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

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());
        } catch (Exception e) {
            log.error("测试RCON连接失败: {}", e.getMessage());
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 测试RCON连接失败：" + e.getMessage());
        }
    }

    /**
     * 测试HTTP/HTTPS服务器通断
     * 用户可以通过发送"test http://example.com[:port]"来测试HTTP/HTTPS服务器连通性
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "测试HTTP/HTTPS服务器的连通性")
    public void testHttp(QQMessage message) {
        try {
            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：test http://example.com[:port] 或 test https://example.com[:port]");
                return;
            }

            // 检查是否是管理员，非管理员有使用次数限制
            boolean isAdmin = !config.selectManagerForThisGroup(message.getGroupId(), message.getUserId()).isEmpty();

            // 如果不是管理员，检查使用次数限制
            if (!isAdmin) {
                String userId = message.getSender().getUserId().toString();
                String usageKey = CacheKey.COMMAND_USE_KEY + "testhttp:" + userId;

                // 获取今日使用次数
                Integer usageCount = redisCache.getCacheObject(usageKey);

                // 如果缓存中没有，初始化为0
                if (usageCount == null) {
                    usageCount = 0;
                }

                // 检查是否超过每日限制(10次)
                if (usageCount >= 10) {
                    sendMessage(message, base + " 您今日的测试次数已用完，每位用户每天限制使用10次。");
                    return;
                }

                // 增加使用次数并更新缓存，设置过期时间为当天结束
                redisCache.setCacheObject(usageKey, usageCount + 1, getSecondsUntilEndOfDay(), TimeUnit.SECONDS);

                // 显示剩余使用次数
                sendMessage(message, base + " 您今天还能使用 " + (10 - (usageCount + 1)) + " 次Web服务器测试指令。");
            }

            String urlString = parts[1];

            // 验证URL格式
            if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                sendMessage(message, base + " 无效的URL格式，请使用 http:// 或 https:// 开头");
                return;
            }

            // 发送检测中的提示消息
            sendMessage(message, base + " 正在检测网站 " + urlString + " 的连通性，请稍候...");

            // 开始时间
            long startTime = System.currentTimeMillis();

            try {
                // 使用Hutool的HttpUtil发送请求
                HttpRequest request = HttpUtil.createGet(urlString)
                        .timeout(5000) // 设置超时时间为5秒
                        .setFollowRedirects(true); // 允许重定向

                // 设置用户代理
                request.header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

                // 执行请求
                HttpResponse httpResponse = request.execute();

                // 计算连接耗时
                long connectTime = System.currentTimeMillis() - startTime;

                // 获取响应码
                int responseCode = httpResponse.getStatus();

                // 获取网站IP地址
                String ipAddress = null;
                try {
                    InetAddress inetAddress = InetAddress.getByName(new URL(urlString).getHost());
                    ipAddress = inetAddress.getHostAddress();
                } catch (Exception e) {
                    log.warn("获取IP地址失败: {}", e.getMessage());
                }

                // 构建响应消息
                StringBuilder response = new StringBuilder();
                response.append(base).append(" HTTP/HTTPS服务器连通性测试结果：\n\n");
                response.append("✅ 服务器 ").append(urlString).append(" 可以连接\n");
                if (ipAddress != null) {
                    response.append("IP地址: ").append(ipAddress).append("\n");
                }
                response.append("连接耗时: ").append(connectTime).append("ms\n");
                response.append("响应码: ").append(responseCode).append("\n");

                // 获取服务器信息
                String server = httpResponse.header("Server");
                if (server != null) {
                    response.append("服务器类型: ").append(server).append("\n");
                }

                // 获取内容类型
                String contentType = httpResponse.header(HttpHeaders.CONTENT_TYPE);
                if (contentType != null) {
                    response.append("内容类型: ").append(contentType).append("\n");
                }

                // 获取SSL/TLS信息（如果是HTTPS）
                if (urlString.startsWith("https://")) {
                    try {
                        // 使用SSLSocket直接连接
                        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                        URL url = new URL(urlString);
                        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(url.getHost(), url.getPort() > 0 ? url.getPort() : 443);
                        sslSocket.startHandshake();

                        SSLSession sslSession = sslSocket.getSession();
                        response.append("\nSSL/TLS信息:\n");
                        response.append("协议: ").append(sslSession.getProtocol()).append("\n");
                        response.append("加密套件: ").append(sslSession.getCipherSuite()).append("\n");

                        // 获取证书信息
                        Certificate[] certificates = sslSession.getPeerCertificates();
                        if (certificates.length > 0) {
                            X509Certificate cert = (X509Certificate) certificates[0];
                            response.append("证书颁发者: ").append(cert.getIssuerDN()).append("\n");
                            response.append("证书有效期至: ").append(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, cert.getNotAfter())).append("\n");
                        }

                        sslSocket.close();
                    } catch (Exception e) {
                        response.append("\nSSL/TLS信息获取失败: ").append(e.getMessage()).append("\n");
                    }
                }

                // 获取网页内容并提取标题等信息
                if (responseCode == 200) {
                    try {
                        // 获取响应内容
                        String htmlContent = httpResponse.body();

                        // 提取标题
                        String title = HtmlUtils.extractTitle(htmlContent);
                        if (title != null && !title.isEmpty()) {
                            response.append("\n网页信息:\n");
                            response.append("标题: ").append(title).append("\n");
                        }

                        // 提取描述
                        String description = HtmlUtils.extractMetaDescription(htmlContent);
                        if (description != null && !description.isEmpty()) {
                            response.append("描述: ").append(description).append("\n");
                        }

                        // 提取关键词
                        String keywords = HtmlUtils.extractMetaKeywords(htmlContent);
                        if (keywords != null && !keywords.isEmpty()) {
                            response.append("关键词: ").append(keywords).append("\n");
                        }

                        // 提取字符集
                        String charset = HtmlUtils.extractCharset(htmlContent, httpResponse);
                        if (charset != null && !charset.isEmpty()) {
                            response.append("字符集: ").append(charset).append("\n");
                        }

                        // 提取网站图标
                        String favicon = HtmlUtils.extractFavicon(htmlContent, new URL(urlString));
                        if (favicon != null && !favicon.isEmpty()) {
                            response.append("图标: ").append(favicon).append("\n");
                        }
                    } catch (Exception e) {
                        response.append("\n获取网页内容失败: ").append(e.getMessage()).append("\n");
                    }
                }

                // 发送消息
                sendMessage(message, response.toString());

            } catch (cn.hutool.http.HttpException e) {
                // Hutool的HTTP异常处理
                String errorMessage = e.getMessage();
                if (errorMessage.contains("UnknownHostException")) {
                    sendMessage(message, base + " ❌ 网站连接失败：无法解析域名 " + urlString);
                } else if (errorMessage.contains("ConnectException")) {
                    sendMessage(message, base + " ❌ 网站连接失败：连接被拒绝，服务器可能未启动或端口未开放");
                } else if (errorMessage.contains("SocketTimeoutException")) {
                    sendMessage(message, base + " ❌ 网站连接失败：连接超时，网站响应时间过长或不可达");
                } else if (errorMessage.contains("SSLHandshakeException")) {
                    sendMessage(message, base + " ❌ SSL连接失败：" + e.getMessage());
                } else {
                    sendMessage(message, base + " ❌ 网站连接失败：" + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("测试HTTP/HTTPS服务器通断失败: {}", e.getMessage());
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 测试失败，请稍后重试。");
        }
    }


    /**
     * 异步记录机器人日志
     *
     * @param logType        日志类型：1=接收消息，2=发送消息，3=方法调用，4=系统事件
     * @param messageId      消息ID
     * @param senderId       发送者ID
     * @param senderType     发送者类型：user=用户，group=群组
     * @param receiverId     接收者ID
     * @param receiverType   接收者类型：user=用户，group=群组
     * @param messageContent 消息内容
     * @param messageType    消息类型：text=文本，image=图片，voice=语音，file=文件等
     * @param methodName     调用的方法名称
     * @param methodParams   方法参数(JSON格式)
     * @param methodResult   方法执行结果
     * @param executionTime  方法执行时间(毫秒)
     * @param errorMessage   错误信息
     * @param stackTrace     错误堆栈信息
     */
    private void logAsync(Long logType, String messageId, String senderId, String senderType,
                          String receiverId, String receiverType, String messageContent,
                          String messageType, String methodName, String methodParams,
                          String methodResult, Long executionTime, String errorMessage,
                          String stackTrace) {
        if (config == null || config.getId() == null) {
            log.warn("无法记录日志：机器人配置未初始化");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                QqBotLog qqBotLog = new QqBotLog();
                qqBotLog.setBotId(config.getId());
                qqBotLog.setLogType(logType);
                qqBotLog.setMessageId(messageId);
                qqBotLog.setSenderId(senderId);
                qqBotLog.setSenderType(senderType);
                qqBotLog.setReceiverId(receiverId);
                qqBotLog.setReceiverType(receiverType);
                qqBotLog.setMessageContent(messageContent);
                qqBotLog.setMessageType(messageType);
                qqBotLog.setMethodName(methodName);
                qqBotLog.setMethodParams(methodParams);
                qqBotLog.setMethodResult(methodResult);
                qqBotLog.setExecutionTime(executionTime);
                qqBotLog.setErrorMessage(errorMessage);
                qqBotLog.setStackTrace(stackTrace);

                qqBotLogService.insertQqBotLog(qqBotLog);
            } catch (Exception e) {
                log.error("记录机器人日志失败: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 记录接收到的消息
     *
     * @param messageId      消息ID
     * @param senderId       发送者ID
     * @param senderType     发送者类型
     * @param receiverId     接收者ID
     * @param receiverType   接收者类型
     * @param messageContent 消息内容
     * @param messageType    消息类型
     */
    private void logReceivedMessage(String messageId, String senderId, String senderType,
                                    String receiverId, String receiverType, String messageContent,
                                    String messageType) {
        logAsync(1L, messageId, senderId, senderType, receiverId, receiverType,
                messageContent, messageType, null, null, null, null, null, null);
    }

    /**
     * 记录发送的消息
     *
     * @param messageId      消息ID
     * @param senderId       发送者ID
     * @param senderType     发送者类型
     * @param receiverId     接收者ID
     * @param receiverType   接收者类型
     * @param messageContent 消息内容
     * @param messageType    消息类型
     */
    private void logSentMessage(String messageId, String senderId, String senderType,
                                String receiverId, String receiverType, String messageContent,
                                String messageType) {
        logAsync(2L, messageId, senderId, senderType, receiverId, receiverType,
                messageContent, messageType, null, null, null, null, null, null);
    }

    /**
     * 记录方法调用
     *
     * @param methodName    方法名称
     * @param methodParams  方法参数
     * @param methodResult  方法结果
     * @param executionTime 执行时间
     */
    private void logMethodCall(String methodName, String methodParams, String methodResult, Long executionTime, String msg) {
        logAsync(3L, null, null, null, null, null, msg, null,
                methodName, methodParams, methodResult, executionTime, null, null);
    }

    /**
     * 记录系统事件
     *
     * @param eventName    事件名称
     * @param eventDetails 事件详情
     */
    private void logSystemEvent(String eventName, String eventDetails) {
        logAsync(4L, null, null, null, null, null, eventDetails, null,
                eventName, null, null, null, null, null);
    }

    /**
     * 记录错误
     *
     * @param methodName   方法名称
     * @param errorMessage 错误信息
     * @param stackTrace   堆栈信息
     */
    private void logError(String methodName, String errorMessage, String stackTrace) {
        logAsync(3L, null, null, null, null, null, null, null,
                methodName, null, null, null, errorMessage, stackTrace);
    }

    /**
     * 将异常堆栈转换为字符串
     *
     * @param e 异常对象
     * @return 堆栈跟踪字符串
     */
    private String getStackTraceAsString(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 记录命令执行日志
     * 由BotCommandAspect调用，统一处理命令执行日志
     *
     * @param methodName    方法名称
     * @param methodParams  方法参数
     * @param methodResult  方法结果
     * @param executionTime 执行时间
     * @param errorMessage  错误信息
     * @param stackTrace    堆栈跟踪
     * @param message       QQ消息对象
     */
    public void logCommandExecution(String methodName, String methodParams, String methodResult,
                                    long executionTime, String errorMessage, String stackTrace,
                                    QQMessage message) {
        // 记录方法调用
        logMethodCall(methodName, methodParams, methodResult, executionTime, message.getMessage());

        // 如果有错误信息，也记录错误
        if (errorMessage != null && !errorMessage.isEmpty()) {
            logError(methodName, errorMessage, stackTrace);
        }
    }

    /**
     * 处理实例列表查询命令
     * 管理员可以查看所有游戏服务器实例
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "查看游戏服务器实例列表", permissionLevel = 1)
    public void handleInstanceList(QQMessage message) {
        try {
            // 检查是否是管理员
            if (config.selectManagerForThisGroup(message.getGroupId(), message.getUserId()).isEmpty()) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
                return;
            }

            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";

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

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());

        } catch (Exception e) {
            log.error("处理实例列表查询失败: {}", e.getMessage(), e);
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 查询失败，请稍后重试。");
        }
    }

    /**
     * 处理启动实例命令
     * 管理员可以启动指定的游戏服务器实例
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "启动游戏服务器实例", permissionLevel = 1)
    public void handleStartInstance(QQMessage message) {
        try {
            // 检查是否是管理员
            if (config.selectManagerForThisGroup(message.getGroupId(), message.getUserId()).isEmpty()) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
                return;
            }

            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：启动实例 <实例ID>");
                return;
            }

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

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());

        } catch (NumberFormatException e) {
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 实例ID格式错误，必须是数字。");
        } catch (Exception e) {
            log.error("处理启动实例失败: {}", e.getMessage(), e);
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 启动失败，请稍后重试。");
        }
    }

    /**
     * 处理停止实例命令
     * 管理员可以停止指定的游戏服务器实例
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "停止游戏服务器实例", permissionLevel = 1)
    public void handleStopInstance(QQMessage message) {
        try {
            // 检查是否是管理员
            if (config.selectManagerForThisGroup(message.getGroupId(), message.getUserId()).isEmpty()) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
                return;
            }

            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：停止实例 <实例ID>");
                return;
            }

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

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());

        } catch (NumberFormatException e) {
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 实例ID格式错误，必须是数字。");
        } catch (Exception e) {
            log.error("处理停止实例失败: {}", e.getMessage(), e);
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 停止失败，请稍后重试。");
        }
    }

    /**
     * 处理重启实例命令
     * 管理员可以重启指定的游戏服务器实例
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "重启游戏服务器实例", permissionLevel = 1)
    public void handleRestartInstance(QQMessage message) {
        try {
            // 检查是否是管理员
            if (config.selectManagerForThisGroup(message.getGroupId(), message.getUserId()).isEmpty()) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
                return;
            }

            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：重启实例 <实例ID>");
                return;
            }

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

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());

        } catch (NumberFormatException e) {
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 实例ID格式错误，必须是数字。");
        } catch (Exception e) {
            log.error("处理重启实例失败: {}", e.getMessage(), e);
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 重启失败，请稍后重试。");
        }
    }

    /**
     * 处理实例状态查询命令
     * 管理员可以查看指定实例的运行状态
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "查看实例运行状态", permissionLevel = 1)
    public void handleInstanceStatus(QQMessage message) {
        try {
            // 检查是否是管理员
            if (config.selectManagerForThisGroup(message.getGroupId(), message.getUserId()).isEmpty()) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
                return;
            }

            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：实例状态 <实例ID>");
                return;
            }

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

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());

        } catch (NumberFormatException e) {
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 实例ID格式错误，必须是数字。");
        } catch (Exception e) {
            log.error("处理实例状态查询失败: {}", e.getMessage(), e);
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 查询失败，请稍后重试。");
        }
    }

    /**
     * 处理实例日志查询命令
     * 管理员可以查看指定实例的控制台日志
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "查看实例控制台日志", permissionLevel = 1)
    public void handleInstanceLogs(QQMessage message) {
        try {
            // 检查是否是管理员
            if (config.selectManagerForThisGroup(message.getGroupId(), message.getUserId()).isEmpty()) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
                return;
            }

            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：实例日志 <实例ID> [行数]");
                return;
            }

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

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());

        } catch (NumberFormatException e) {
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 参数格式错误。");
        } catch (Exception e) {
            log.error("处理实例日志查询失败: {}", e.getMessage(), e);
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 查询失败，请稍后重试。");
        }
    }

    /**
     * 处理实例命令发送
     * 管理员可以向指定实例发送控制台命令
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "向实例发送控制台命令", permissionLevel = 1)
    public void handleInstanceCommand(QQMessage message) {
        try {
            // 检查是否是管理员
            if (config.selectManagerForThisGroup(message.getGroupId(), message.getUserId()).isEmpty()) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
                return;
            }

            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";
            String[] parts = message.getMessage().trim().split("\\s+", 3);

            if (parts.length < 3) {
                sendMessage(message, base + " 格式错误，正确格式：实例命令 <实例ID> <命令>");
                return;
            }

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

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());

        } catch (NumberFormatException e) {
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 实例ID格式错误，必须是数字。");
        } catch (Exception e) {
            log.error("处理实例命令发送失败: {}", e.getMessage(), e);
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 发送失败，请稍后重试。");
        }
    }

    /**
     * 处理节点状态查询命令
     * 管理员可以查询节点服务器的详细状态信息
     *
     * @param message QQ消息对象
     */
    @BotCommand(description = "查询节点服务器状态", permissionLevel = 1)
    public void handleNodeStatus(QQMessage message) {
        try {
            // 检查是否是管理员
            if (config.selectManagerForThisGroup(message.getGroupId(), message.getUserId()).isEmpty()) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
                return;
            }

            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";
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
                        response.append("运行时间: ").append(formatUptime(data.getLong("uptime"))).append("\n");

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

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());

        } catch (Exception e) {
            log.error("处理节点状态查询失败: {}", e.getMessage(), e);
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 查询失败，请稍后重试。");
        }
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
            response.append("运行时间: ").append(formatUptime(data.getLong("uptime"))).append("\n\n");

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
                response.append("总内存: ").append(formatBytes(systemInfo.getLong("totalMemory"))).append("\n");
                response.append("可用内存: ").append(formatBytes(systemInfo.getLong("freeMemory"))).append("\n");
                response.append("最大内存: ").append(formatBytes(systemInfo.getLong("maxMemory"))).append("\n\n");
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
                    response.append("网络接收: ").append(formatBytes(network.getLong("bytesRecvPerSec"))).append("/s\n");
                    response.append("网络发送: ").append(formatBytes(network.getLong("bytesSentPerSec"))).append("/s\n");
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
                if (serverStats.containsKey("instances") && serverStats.getJSONArray("instances").size() > 0) {
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

    /**
     * 格式化运行时间
     */
    private String formatUptime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        if (days > 0) {
            return String.format("%d天%d小时%d分钟", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes);
        } else {
            return String.format("%d分钟", minutes);
        }
    }

    /**
     * 格式化字节大小
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}

