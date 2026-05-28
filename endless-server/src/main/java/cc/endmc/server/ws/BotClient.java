package cc.endmc.server.ws;

import cc.endmc.common.constant.Constants;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.common.constant.BotApi;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.bot.*;
import cc.endmc.server.mapper.bot.QqBotConfigMapper;
import cc.endmc.server.service.bot.IBotGroupCommandConfigService;
import cc.endmc.server.service.bot.IQqBotLogService;
import cc.endmc.server.service.bot.IQqBotManagerService;
import cc.endmc.server.service.github.GitHubActionsService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.server.IServerInfoService;
import cc.endmc.server.ws.handler.CommandHandler;
import cc.endmc.server.ws.handler.CommandRegistry;
import cc.endmc.server.ws.handler.admin.AdminCommandHandler;
import cc.endmc.server.ws.handler.build.BuildCommandHandler;
import cc.endmc.server.ws.handler.instance.InstanceCommandHandler;
import cc.endmc.server.ws.handler.toggle.FeatureToggleCommandHandler;
import cc.endmc.server.ws.handler.user.UserCommandHandler;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import cc.endmc.node.service.INodeMinecraftServerService;
import cc.endmc.node.service.INodeServerService;
import cc.endmc.common.email.EmailService;
import cc.endmc.framework.manager.AsyncManager;

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

/**
 * QQ机器人WebSocket客户端
 * 用于与QQ机器人服务器建立长连接，实时接收消息
 * 
 * 重构说明：命令处理逻辑已拆分到各个CommandHandler中
 * - UserCommandHandler: 用户命令 (help, 白名单申请, 查询, test等)
 * - AdminCommandHandler: 管理员命令 (审核, 封禁, RCON指令等)
 * - InstanceCommandHandler: 实例管理命令
 * - BuildCommandHandler: 构建命令
 * - FeatureToggleCommandHandler: 功能开关命令
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
    private final QqBotConfigMapper qqBotConfigMapper;
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
    private final ObjectProvider<BotManager> botManagerProvider;
    private final GitHubActionsService gitHubActionsService;
    @Qualifier("threadPoolTaskExecutor")
    private final Executor taskExecutor;
    private volatile boolean isShuttingDown = false;

    /**
     * 命令注册器
     */
    private final CommandRegistry commandRegistry = new CommandRegistry();

    /**
     * 命令处理器
     */
    private UserCommandHandler userCommandHandler;
    private AdminCommandHandler adminCommandHandler;
    private InstanceCommandHandler instanceCommandHandler;
    private BuildCommandHandler buildCommandHandler;
    private FeatureToggleCommandHandler featureToggleCommandHandler;

    /**
     * 机器人配置
     */
    @Getter
    private QqBotConfig config;

    private WebSocketClient wsClient;

    @PostConstruct
    public void init() {
        log.info("BotClient 实例已创建，依赖注入完成");

        // 初始化命令处理器
        initCommandHandlers();

        // 初始化命令注册器
        initCommandRegistry();
    }

    /**
     * 初始化命令处理器
     */
    private void initCommandHandlers() {
        // 创建用户命令处理器
        userCommandHandler = new UserCommandHandler(this, redisCache, whitelistInfoService, serverInfoService, emailService, appUrl);

        // 创建管理员命令处理器
        adminCommandHandler = new AdminCommandHandler(this, redisCache, whitelistInfoService, serverInfoService, rconService, env);

        // 创建实例管理命令处理器
        instanceCommandHandler = new InstanceCommandHandler(this, redisCache, nodeMinecraftServerService, nodeServerService);

        // 创建构建命令处理器
        buildCommandHandler = new BuildCommandHandler(this, redisCache, gitHubActionsService);

        // 创建功能开关命令处理器
        featureToggleCommandHandler = new FeatureToggleCommandHandler(this, redisCache, commandRegistry, commandConfigService);

        log.info("命令处理器初始化完成");
    }

    /**
     * 初始化命令注册器
     * 注册所有命令及其处理器
     */
    private void initCommandRegistry() {
        // 注册用户命令
        userCommandHandler.registerCommands(commandRegistry);

        // 注册管理员命令
        adminCommandHandler.registerCommands(commandRegistry);

        // 注册实例管理命令
        instanceCommandHandler.registerCommands(commandRegistry);

        // 注册构建命令
        buildCommandHandler.registerCommands(commandRegistry);

        // 注册功能开关命令
        featureToggleCommandHandler.registerCommands(commandRegistry);

        // 添加管理/超管命令（特殊处理，因为需要更新配置）
        commandRegistry.register("添加管理", msg -> {
            adminCommandHandler.handleAddManager(msg);
            updateManagerConfig();
        }, "addadmin", "aa");
        commandRegistry.register("添加超管", msg -> {
            adminCommandHandler.handleAddSuperManager(msg);
            updateManagerConfig();
        }, "addsuper", "as");

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
        }
    }

    /**
     * Spring Bean销毁时调用
     * 清理资源，关闭连接和定时任务
     */
    @PreDestroy
    public void destroy() {
        log.info("正在关闭机器人客户端...");

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
            QQMessage qqMessage = JSON.parseObject(message, QQMessage.class);

            // 记录接收到的消息
            if (qqMessage != null && qqMessage.getMessageType() != null) {
                String senderId = qqMessage.getUserId() != null ? qqMessage.getUserId().toString() : null;
                String senderType = "user";
                String receiverId = qqMessage.getGroupId() != null ? qqMessage.getGroupId().toString() : null;
                String receiverType = "group";
                String messageId = qqMessage.getMessageId() != null ? qqMessage.getMessageId().toString() : null;

                logReceivedMessage(messageId, senderId, senderType, receiverId, receiverType,
                        qqMessage.getMessage(), qqMessage.getMessageType());
            }

            handleMessage(qqMessage);
        } catch (Exception e) {
            log.error("处理WebSocket消息时发生错误: {}", e.getMessage());
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
                BotManager botManager = botManagerProvider.getIfAvailable();
                if (botManager != null) {
                    botManager.reconnectBot(config.getId());
                }
            } catch (Exception e) {
                log.error("重新连接失败: {}", e.getMessage());
            }
        }, 5, TimeUnit.SECONDS);
    }

    /**
     * 获取命令前缀
     * 如果配置文件中未设置或为空，则返回默认值"/"
     */
    public String getCommandPrefix() {
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
                if (!featureToggleCommandHandler.isCommandControlCommand(mainCommand)) {
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
                    adminCommandHandler.handleRconCommand(message, true);
                } else {
                    // 未知命令
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() +
                            "] 未知命令，请使用 " + getCommandPrefix() + "help 查看可用命令。");
                }
            }
        } catch (Exception e) {
            // 记录错误信息
            log.error("处理消息时发生错误: {}", e.getMessage(), e);

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
     * 发送消息
     *
     * @param message QQ消息对象
     * @param msg     消息内容
     */
    public void sendMessage(QQMessage message, String msg) {
        log.info("message: {}", message);
        try {
            if (config == null) {
                log.error("无法发送消息：机器人配置为空");
                return;
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("group_id", message.getGroupId().toString());
            jsonObject.put("message", msg);

            final HttpResponse response = HttpUtil.createPost(config.getHttpUrl() + BotApi.SEND_GROUP_MSG)
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

            logSentMessage(messageId, senderId, senderType, receiverId, receiverType, msg, "text");
        } catch (Exception e) {
            log.debug(e.toString());
            log.error("发送消息失败: {}", e.getMessage());
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
            QqBotConfig latestConfig = qqBotConfigMapper.selectQqBotConfigById(config.getId());
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
     * 更新管理员最后活跃时间
     *
     * @param userId 用户ID
     * @param botId  机器人ID
     */
    public void updateQqBotManagerLastActiveTime(Long userId, Long botId) {
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
     * 异步记录机器人日志
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
        }, taskExecutor);
    }

    /**
     * 记录接收到的消息
     */
    private void logReceivedMessage(String messageId, String senderId, String senderType,
                                    String receiverId, String receiverType, String messageContent,
                                    String messageType) {
        logAsync(1L, messageId, senderId, senderType, receiverId, receiverType,
                messageContent, messageType, null, null, null, null, null, null);
    }

    /**
     * 记录发送的消息
     */
    private void logSentMessage(String messageId, String senderId, String senderType,
                                String receiverId, String receiverType, String messageContent,
                                String messageType) {
        logAsync(2L, messageId, senderId, senderType, receiverId, receiverType,
                messageContent, messageType, null, null, null, null, null, null);
    }

    /**
     * 记录系统事件
     */
    private void logSystemEvent(String eventName, String eventDetails) {
        logAsync(4L, null, null, null, null, null, eventDetails, null,
                eventName, null, null, null, null, null);
    }

    /**
     * 记录命令执行日志
     * 由BotCommandAspect调用，统一处理命令执行日志
     */
    public void logCommandExecution(String methodName, String methodParams, String methodResult,
                                    long executionTime, String errorMessage, String stackTrace,
                                    QQMessage message) {
        // 记录方法调用
        logAsync(3L, null, null, null, null, null, message.getMessage(), null,
                methodName, methodParams, methodResult, executionTime, null, null);

        // 如果有错误信息，也记录错误
        if (errorMessage != null && !errorMessage.isEmpty()) {
            logAsync(3L, null, null, null, null, null, null, null,
                    methodName, null, null, null, errorMessage, stackTrace);
        }
    }
}
