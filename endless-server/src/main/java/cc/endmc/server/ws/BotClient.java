package cc.endmc.server.ws;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.framework.web.domain.Server;
import cc.endmc.framework.web.domain.server.SysFile;
import cc.endmc.server.common.EmailTemplates;
import cc.endmc.server.common.MapCache;
import cc.endmc.server.common.constant.BotApi;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.rconclient.RconClient;
import cc.endmc.server.common.service.EmailService;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.bot.QqBotConfig;
import cc.endmc.server.domain.bot.QqBotManager;
import cc.endmc.server.domain.bot.QqBotManagerGroup;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.service.bot.IQqBotConfigService;
import cc.endmc.server.service.bot.IQqBotManagerService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.server.IServerInfoService;
import cc.endmc.server.utils.CodeUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * QQ机器人WebSocket客户端
 * 用于与QQ机器人服务器建立长连接，实时接收消息
 */
@Slf4j
@Component
@Scope("prototype")
public class BotClient {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final RedisCache redisCache;
    private final EmailService emailService;
    private final IWhitelistInfoService whitelistInfoService;
    private final IServerInfoService serverInfoService;
    private final RconService rconService;
    private final IQqBotConfigService qqBotConfigService;
    private final IQqBotManagerService qqBotManagerService;
    private final String appUrl;
    private ScheduledFuture<?> reconnectTask;
    private volatile boolean isShuttingDown = false;
    /**
     * -- GETTER --
     * 获取机器人配置
     *
     * @return 机器人配置
     */
    @Getter
    private QqBotConfig config;
    private WebSocketClient wsClient;

    /**
     * 构造函数
     * 初始化依赖
     */
    @Autowired
    public BotClient(RedisCache redisCache,
                     EmailService emailService,
                     IWhitelistInfoService whitelistInfoService,
                     IServerInfoService serverInfoService,
                     RconService rconService,
                     IQqBotConfigService qqBotConfigService,
                     IQqBotManagerService qqBotManagerService,
                     @Value("${app-url}") String appUrl) {
        this.redisCache = redisCache;
        this.emailService = emailService;
        this.whitelistInfoService = whitelistInfoService;
        this.serverInfoService = serverInfoService;
        this.rconService = rconService;
        this.qqBotConfigService = qqBotConfigService;
        this.qqBotManagerService = qqBotManagerService;
        this.appUrl = appUrl;

        log.info("BotClient 实例已创建，依赖注入完成");
    }

    /**
     * 初始化机器人客户端
     * 使用配置的URL创建WebSocket连接
     *
     * @param config 机器人配置
     */
    public void init(QqBotConfig config) {
        try {
            this.config = config;
            if (config == null) {
                log.error("机器人配置为空");
                return;
            }

            // 如果已存在连接，先关闭
            if (wsClient != null) {
                try {
                    wsClient.close();
                } catch (Exception e) {
                    log.error("关闭旧WebSocket连接时发生错误: {}", e.getMessage());
                }
            }

            // 创建新的WebSocket连接
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + config.getToken());

            wsClient = new WebSocketClient(URI.create(config.getWsUrl()), headers) {
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

            wsClient.connect();
            log.info("QQ机器人WebSocket客户端已初始化，连接地址: {}", config.getWsUrl());
        } catch (Exception e) {
            log.error("QQ机器人WebSocket客户端初始化失败: {}", e.getMessage());
            scheduleReconnect();
        }
    }

    /**
     * Spring Bean销毁时调用
     * 清理资源，关闭连接和定时任务
     */
    @PreDestroy
    public void destroy() {
        try {
            isShuttingDown = true;
            log.info("正在关闭QQ机器人WebSocket客户端...");

            // 取消重连任务
            if (reconnectTask != null) {
                reconnectTask.cancel(true);
                reconnectTask = null;
            }

            // 关闭调度器
            try {
                scheduler.shutdown();
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }

            // 关闭WebSocket连接
            if (wsClient != null) {
                try {
                    wsClient.close();
                } catch (Exception e) {
                    log.error("关闭WebSocket连接时发生错误: {}", e.getMessage());
                }
            }

            log.info("QQ机器人WebSocket客户端已关闭");
        } catch (Exception e) {
            log.error("关闭QQ机器人WebSocket客户端时发生错误: {}", e.getMessage());
        }
    }

    /**
     * WebSocket连接打开时的回调
     */
    public void onOpen(ServerHandshake handshakedata) {
        log.info("WebSocket连接已建立");
    }

    /**
     * 接收到WebSocket消息时的回调
     */
    public void onMessage(String message) {
        try {
            log.debug("收到消息: {}", message);
            QQMessage qqMessage = JSON.parseObject(message, QQMessage.class);
            handleMessage(qqMessage);
        } catch (Exception e) {
            log.error("处理WebSocket消息时发生错误: {}", e.getMessage());
        }
    }

    /**
     * WebSocket连接关闭时的回调
     */
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket连接已关闭: code={}, reason={}, remote={}", code, reason, remote);
        if (!isShuttingDown) {
            scheduleReconnect();
        }
    }

    /**
     * WebSocket发生错误时的回调
     */
    public void onError(Exception ex) {
        log.error("WebSocket发生错误: {}", ex.getMessage());
        if (!isShuttingDown) {
            scheduleReconnect();
        }
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
                reconnect();
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
     * 可以在这里添加自定义的消息处理逻辑
     *
     * @param message QQ消息对象
     */
    private void handleMessage(QQMessage message) {
        // 处理消息的具体逻辑
        if ("group".equals(message.getMessageType()) &&
                config.getGroupIdList().contains(message.getGroupId())) {
            log.info("收到QQ群[{}]消息 - 发送者: {}, 内容: {}",
                    message.getGroupId(),
                    message.getSender().getUserId(),
                    message.getMessage());

            String msg = message.getMessage().trim();
            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";

            // 优先处理退群消息
            if (message.getNoticeType() != null && message.getNoticeType().startsWith("group")) {
                if (message.getNoticeType().equals("group_decrease")) {
                    handleGroupDecrease(message);
                }
                return;
            }

            // 解析命令
            String command = parseCommand(msg);
            message.setMessage(command);
            if (StringUtils.isEmpty(command)) {
                return;
            }

            // 处理help命令
            if (command.startsWith("help")) {
                handleHelpCommand(message);
                return;
            }

            // 普通用户命令
            if (command.startsWith("白名单申请")) {
                handleWhitelistApplication(message);
            } else if (command.startsWith("查询白名单")) {
                handleWhitelistQuery(message);
            } else if (command.startsWith("查询玩家")) {
                handlePlayerQuery(message);
            } else if (command.startsWith("查询在线")) {
                handleOnlineQuery(message);
            } else if (command.startsWith("查询服务器")) {
                handleServerList(message);
            } else if (command.startsWith("test")) {
                testServer(message);
            }

            // 管理员命令
            if (command.startsWith("过审") || command.startsWith("拒审")) {
                handleWhitelistReview(message);
            } else if (command.startsWith("封禁") || command.startsWith("解封")) {
                handleBanOperation(message);
            } else if (command.startsWith("发送指令")) {
                handleRconCommand(message);
            } else if (command.startsWith("运行状态")) {
                handleHostStatus(message);
            } else if (command.startsWith("刷新连接")) {
                handleRefreshConnection(message);
            } else if (command.startsWith("测试连接")) {
                handleTestConnection(message);
            }

            // 超管命令
            if (command.startsWith("添加管理")) {
                handleAddManager(message);
            } else if (command.startsWith("添加超管")) {
                handleAddSuperManager(message);
            }
        }
    }

    /**
     * 处理服务器列表查询命令
     *
     * @param message QQ消息对象
     */
    private void handleServerList(QQMessage message) {
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
                    if (MapCache.containsKey(String.valueOf(serverInfo.getId()))) {
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
                boolean isOnline = MapCache.containsKey(String.valueOf(server.getId()));

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
    private void handleHelpCommand(QQMessage message) {
        String prefix = getCommandPrefix();
        StringBuilder help = new StringBuilder();
        help.append("[CQ:at,qq=").append(message.getSender().getUserId()).append("] 可用命令列表：\n\n");

        // 所有用户可用的命令
        help.append("普通用户命令：\n");
        help.append(prefix).append("help - 显示此帮助信息\n");
        help.append(prefix).append("白名单申请 <玩家ID> <正版/离线> - 申请白名单\n");
        help.append(prefix).append("查询白名单 - 查询自己的白名单状态\n");
        help.append(prefix).append("查询玩家 <玩家ID> - 查询指定玩家信息\n");
        help.append(prefix).append("查询在线 - 查询所有服务器在线玩家\n");
        help.append(prefix).append("查询服务器 [全部]/[%模糊匹配] - 查询服务器列表，默认只显示在线服务器\n");
        help.append(prefix).append("test <IP[:端口]> - 测试指定Minecraft服务器的通断，默认端口25565\n\n");

        // 管理员命令
        List<QqBotManager> managers = config.selectManagerForThisGroup(message.getGroupId(), message.getUserId());
        if (!managers.isEmpty() && managers.get(0).getPermissionType() == 0) {
            help.append("管理员命令：\n");
            help.append(prefix).append("过审 <玩家ID> - 通过玩家的白名单申请\n");
            help.append(prefix).append("拒审 <玩家ID> - 拒绝玩家的白名单申请\n");
            help.append(prefix).append("封禁 <玩家ID> <原因> - 封禁玩家\n");
            help.append(prefix).append("解封 <玩家ID> - 解除玩家封禁\n");
            help.append(prefix).append("发送指令 <服务器ID/all> <指令内容> - 向服务器发送RCON指令\n");
            help.append(prefix).append("运行状态 - 查看服务器主机运行状态\n");
            help.append(prefix).append("刷新连接 [服务器ID] - 刷新服务器的RCON连接，不填服务器ID默认刷新所有服务器\n");
            help.append(prefix).append("测试连接 [服务器ID] - 测试服务器的RCON连接，不填服务器ID默认测试所有服务器\n");

            // 超级管理员命令
            if (!managers.isEmpty() && managers.get(0).getPermissionType() == 0) {
                help.append("\n超级管理员命令：\n");
                help.append(prefix).append("添加管理 <QQ号> [群号] - 添加普通管理员，不填群号默认为当前群\n");
                help.append(prefix).append("添加超管 <QQ号> [群号] - 添加超级管理员，不填群号默认为当前群\n");
            }
        }

        sendMessage(message, help.toString());
    }

    /**
     * 退群相关处理
     *
     * @param message
     */
    private void handleGroupDecrease(QQMessage message) {
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
    private void handleWhitelistQuery(QQMessage message) {
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

    private void handleWhitelistApplication(QQMessage message) {
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
        if (whitelistInfos.size() > 0) {
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
     * @param whitelistInfo
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

    private void sendMessage(QQMessage message, String msg) {
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
        } catch (Exception e) {
            e.printStackTrace();
            log.error("发送消息失败: {}", e.getMessage());
        }
    }

    /**
     * 处理白名单审核请求
     * 管理员可以通过发送"过审 ID"或"拒审 ID"来审核白名单
     *
     * @param message QQ消息对象
     */
    private void handleWhitelistReview(QQMessage message) {
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

        } catch (Exception e) {
            e.printStackTrace();
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
    private void handleBanOperation(QQMessage message) {
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
            e.printStackTrace();
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
    private void handleRconCommand(QQMessage message) {
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

            String[] parts = message.getMessage().trim().split("\\s+", 3);
            if (parts.length < 3) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 格式错误，正确格式：发送指令 服务器ID/all 指令内容");
                return;
            }

            String serverId = parts[1];
            String command = parts[2];

            if (!serverId.contains("all")) {
                if (!MapCache.containsKey(serverId)) {
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 未找到服务器 " + serverId);
                    return;
                }
            }

            // 判断是否为高危命令
            if (isHighRiskCommand(command)) {
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
                sendMessage(message, response.toString());
            } catch (Exception e) {
                log.error("发送RCON指令失败: {}", e.getMessage());
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 指令发送失败：" + e.getMessage());
            }

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());

        } catch (Exception e) {
            e.printStackTrace();
            log.error("处理RCON指令失败: {}", e.getMessage());
            sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 操作失败，请稍后重试。");
        }
    }

    /**
     * 判断是否为高危命令
     *
     * @param command 要执行的命令
     * @return 是否为高危命令
     */
    private boolean isHighRiskCommand(String command) {
        if (command == null || command.isEmpty()) {
            return false;
        }

        // 将命令转为小写并去除前导空格，以便更准确地匹配
        String cmdLower = command.trim().toLowerCase();

        // 高危命令列表
        String[] highRiskCommands = {
                // 服务器核心命令
                "stop",                // 关闭服务器
                "reload",              // 重载服务器
                "restart",             // 重启服务器
                "shutdown",            // 关闭服务器
                "whitelist off",       // 关闭白名单
                "op ",                 // 给予OP权限
                "deop ",               // 移除OP权限
                "ban-ip ",             // IP封禁
                "pardon-ip ",          // 解除IP封禁
                "ban ",                // 封禁玩家
                "pardon ",             // 解除玩家封禁
                "save-off",            // 关闭自动保存
                "kill @e",             // 杀死所有实体
                "difficulty ",         // 修改游戏难度
                "gamerule ",           // 修改游戏规则
                "defaultgamemode ",    // 修改默认游戏模式

                // 世界编辑命令
                "fill",                // 填充大量方块
                "setblock",            // 设置方块
                "worldedit",           // WorldEdit命令
                "we",                  // WorldEdit简写
                "/expand",             // WorldEdit扩展选区
                "/set",                // WorldEdit设置方块
                "/replace",            // WorldEdit替换方块

                // 多世界管理插件
                "mv delete",           // Multiverse删除世界
                "mv remove",           // Multiverse移除世界
                "mv unload",           // Multiverse卸载世界
                "mv modify",           // Multiverse修改世界设置
                "mvtp",                // Multiverse传送

                // 权限插件
                "pex group default set",  // PermissionsEx更改默认组权限
                "pex user * set",         // PermissionsEx设置所有用户权限
                "lp group default set",   // LuckPerms更改默认组权限
                "lp user * set",          // LuckPerms设置所有用户权限
                "lp group",               // LuckPerms组操作
                "permissions",            // 权限操作

                // Essentials插件危险命令
                "essentials.eco",         // 经济系统修改
                "eco give",               // 给予金钱
                "eco reset",              // 重置经济
                "eco set",                // 设置金钱
                "eco take",               // 移除金钱
                "ess reload",             // 重载Essentials
                "essentials reload",      // 重载Essentials
                "god",                    // 上帝模式
                "ext",                    // 灭火
                "ext all",                // 灭所有火
                "ext -a",                 // 灭所有火
                "kickall",                // 踢出所有玩家
                "killall",                // 杀死所有实体
                "spawnmob",               // 生成怪物
                "sudo",                   // 以他人身份执行命令
                "unlimited",              // 无限物品
                "nuke",                   // 核爆
                "essentials.gamemode",    // 修改游戏模式
                "tpall",                  // 传送所有人
                "antioch",                // 圣手雷
                "essentials.give",        // 给予物品
                "give",                   // 给予物品
                "item",                   // 给予物品
                "i",                      // 给予物品简写
                "more",                   // 更多物品
                "backup",                 // 备份服务器
                "fireball",               // 火球
                "lightning",              // 闪电
                "thunder",                // 雷暴
                "tempban",                // 临时封禁
                "banip",                  // IP封禁
                "unbanip",                // 解除IP封禁
                "mute",                   // 禁言
                "broadcast",              // 广播
                "essentials.clearinventory", // 清空背包
                "clear",                  // 清空背包
                "ci",                     // 清空背包
                "clearinventory",         // 清空背包
                "socialspy",              // 窥探私聊
                "tp",                     // 传送
                "tphere",                 // 传送到这里
                "tppos",                  // 传送到坐标
                "top",                    // 传送到顶部
                "tptoggle",               // 切换传送
                "vanish",                 // 隐身
                "v",                      // 隐身简写

                // 其他常见插件的危险命令
                "upc ",                   // UltraPermissions
                "lpc ",                   // LuckPerms
                "pex ",                   // PermissionsEx
                "nuker ",                 // 核爆插件
                "essentialsreload",       // Essentials重载
                "plugman",                // 插件管理
                "pl ",                    // 插件操作
                "plugin ",                // 插件操作
                "worldborder set",        // 设置世界边界
                "timings",                // 服务器性能分析
                "lag",                    // 卡顿分析
                "pstop",                  // 停止服务器
                "coreprotect ",           // CoreProtect核心保护
                "co rollback",            // CoreProtect回滚
                "co restore",             // CoreProtect恢复
                "co purge"                // CoreProtect清除数据
        };

        // 检查命令是否匹配任何高危命令
        for (String highRiskCmd : highRiskCommands) {
            if (cmdLower.startsWith(highRiskCmd.trim().toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 处理玩家信息查询请求
     * 玩家可以通过发送"查询玩家 玩家ID"来查询任意玩家的信息
     *
     * @param message QQ消息对象
     */
    private void handlePlayerQuery(QQMessage message) {
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
    private void handleOnlineQuery(QQMessage message) {
        try {
            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";

            // 获取在线玩家信息
            Map<String, Object> result = serverInfoService.getOnlinePlayer();

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
    private void handleHostStatus(QQMessage message) {
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
            response.append("系统架构：").append(server.getSys().getOsArch()).append("\n");

            // 磁盘信息
            response.append("\n磁盘状态：\n");
            for (SysFile sysFile : server.getSysFiles()) {
                response.append(sysFile.getDirName()).append("（").append(sysFile.getTypeName()).append("）：\n");
                response.append("总大小：").append(sysFile.getTotal()).append("GB\n");
                response.append("已用大小：").append(sysFile.getUsed()).append("GB\n");
                response.append("剩余大小：").append(sysFile.getFree()).append("GB\n");
                response.append("使用率：").append(sysFile.getUsage()).append("%\n");
            }

            // 发送消息
            sendMessage(message, response.toString());

            // 更新管理员最后活跃时间
            updateQqBotManagerLastActiveTime(message.getSender().getUserId(), config.getId());

        } catch (Exception e) {
            log.error("处理主机状态查询失败: " + e.getMessage(), e);
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
    private void handleAddManager(QQMessage message) {
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
            e.printStackTrace();
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
    private void handleAddSuperManager(QQMessage message) {
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
    private void testServer(QQMessage message) {
        try {
            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：test <IP[:端口]>，默认端口25565");
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
            }

            String serverAddress = parts[1];
            String ip;
            int port = 25565; // 默认端口

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
            }

            // 验证是否为有效的IP地址或域名
            if (!isValidIpOrDomain(ip)) {
                sendMessage(message, base + " 无效的IP地址或域名格式，请检查输入");
                return;
            }

            // 发送检测中的提示消息
            sendMessage(message, base + " 正在检测服务器 " + ip + ":" + port + " 的连通性，请稍候...");

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
                response.append(base).append(" 服务器连通性测试结果：\n\n");
                response.append("✅ 服务器 ").append(ip).append(":").append(port).append(" 可以连接\n");
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
                    int length = readVarInt(dataIn);
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
            log.error("测试服务器通断失败: {}", e.getMessage());
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
     * 验证字符串是否是有效的IP地址或域名
     *
     * @param input 需要验证的字符串
     * @return 是否有效
     */
    private boolean isValidIpOrDomain(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        // IPv4地址正则表达式
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        // IPv6地址正则表达式 (简化版)
        String ipv6Pattern = "^(([0-9a-fA-F]{1,4}:){7}([0-9a-fA-F]{1,4}|:))|(([0-9a-fA-F]{1,4}:){6}(:[0-9a-fA-F]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9a-fA-F]{1,4}:){5}(((:[0-9a-fA-F]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9a-fA-F]{1,4}:){4}(((:[0-9a-fA-F]{1,4}){1,3})|((:[0-9a-fA-F]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9a-fA-F]{1,4}:){3}(((:[0-9a-fA-F]{1,4}){1,4})|((:[0-9a-fA-F]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9a-fA-F]{1,4}:){2}(((:[0-9a-fA-F]{1,4}){1,5})|((:[0-9a-fA-F]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9a-fA-F]{1,4}:){1}(((:[0-9a-fA-F]{1,4}){1,6})|((:[0-9a-fA-F]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9a-fA-F]{1,4}){1,7})|((:[0-9a-fA-F]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))$";

        // 域名正则表达式
        String domainPattern = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,63}$";

        // 使用正则表达式验证
        return input.matches(ipv4Pattern) || input.matches(ipv6Pattern) || input.matches(domainPattern);
    }

    /**
     * 处理刷新连接命令
     * 管理员可以刷新指定服务器或所有服务器的RCON连接
     *
     * @param message QQ消息对象
     */
    private void handleRefreshConnection(QQMessage message) {
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
                if (!serverId.equals("all") && !MapCache.containsKey(serverId)) {
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 未找到服务器 " + serverId);
                    return;
                }
            }

            if (serverId.equals("all")) {
                // 关闭所有Rcon连接并清除Map缓存
                for (RconClient rconClient : MapCache.getMap().values()) {
                    rconClient.close();
                }
                MapCache.clear();

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
                if (MapCache.containsKey(serverId)) {
                    RconClient rconClient = MapCache.get(serverId);
                    if (rconClient != null) {
                        rconClient.close();
                        MapCache.remove(serverId);
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
    private void handleTestConnection(QQMessage message) {
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
                if (!serverId.equals("all") && !MapCache.containsKey(serverId)) {
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 未找到服务器 " + serverId);
                    return;
                }
            }

            StringBuilder response = new StringBuilder();
            response.append("[CQ:at,qq=").append(message.getSender().getUserId()).append("] 测试连接结果：\n\n");

            if (serverId.equals("all")) {
                // 测试所有服务器
                if (MapCache.isEmpty()) {
                    sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 当前没有RCON连接。");
                    return;
                }

                Map<String, Object> serverInfoMap = redisCache.getCacheObject(CacheKey.SERVER_INFO_MAP_KEY);
                for (Map.Entry<String, RconClient> entry : MapCache.getMap().entrySet()) {
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
                RconClient client = MapCache.get(serverId);
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

}
