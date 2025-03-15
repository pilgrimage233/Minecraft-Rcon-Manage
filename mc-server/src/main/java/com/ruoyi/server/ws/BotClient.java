package com.ruoyi.server.ws;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.domain.Server;
import com.ruoyi.framework.web.domain.server.SysFile;
import com.ruoyi.server.common.EmailTemplates;
import com.ruoyi.server.common.MapCache;
import com.ruoyi.server.common.constant.BotApi;
import com.ruoyi.server.common.constant.CacheKey;
import com.ruoyi.server.common.service.EmailService;
import com.ruoyi.server.common.service.RconService;
import com.ruoyi.server.domain.bot.QqBotConfig;
import com.ruoyi.server.domain.bot.QqBotManager;
import com.ruoyi.server.domain.bot.QqBotManagerGroup;
import com.ruoyi.server.domain.permission.WhitelistInfo;
import com.ruoyi.server.service.bot.IQqBotConfigService;
import com.ruoyi.server.service.bot.IQqBotManagerService;
import com.ruoyi.server.service.permission.IWhitelistInfoService;
import com.ruoyi.server.service.server.IServerInfoService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
    private ScheduledFuture<?> reconnectTask;
    private volatile boolean isShuttingDown = false;
    private final EmailService emailService;
    private final IWhitelistInfoService whitelistInfoService;
    private final IServerInfoService serverInfoService;
    private final RconService rconService;
    private final IQqBotConfigService qqBotConfigService;
    private final IQqBotManagerService qqBotManagerService;
    private final String appUrl;
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

            // 处理其他命令
            if (command.startsWith("白名单申请")) {
                handleWhitelistApplication(message);
            } else if (command.startsWith("查询白名单")) {
                handleWhitelistQuery(message);
            } else if (command.startsWith("过审") || command.startsWith("拒审")) {
                handleWhitelistReview(message);
            } else if (command.startsWith("封禁") || command.startsWith("解封")) {
                handleBanOperation(message);
            } else if (command.startsWith("发送指令")) {
                handleRconCommand(message);
            } else if (command.startsWith("查询玩家")) {
                handlePlayerQuery(message);
            } else if (command.startsWith("查询在线")) {
                handleOnlineQuery(message);
            } else if (command.startsWith("运行状态")) {
                handleHostStatus(message);
            } else if (command.startsWith("添加管理")) {
                handleAddManager(message);
            } else if (command.startsWith("添加超管")) {
                handleAddSuperManager(message);
            }
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
        help.append(prefix).append("查询在线 - 查询所有服务器在线玩家\n\n");

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
        // 使用QQ号生成验证码
        String code;
        try {
            // 基于QQ号生成固定验证码
            // 改为1800秒(30分钟)来匹配缓存过期时间
            String rawKey = whitelistInfo.getQqNum() + "_" + System.currentTimeMillis() / 1000 / 1800;
            // 使用MD5加密并取前8位作为验证码
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            code = sb.substring(0, 8);

            // 检查是否已存在该验证码
            if (redisCache.hasKey(CacheKey.VERIFY_FOR_BOT_KEY + code)) {
                result.put("status", "NO");
                result.put("msg", "请勿重复提交！否则可能将无法通过验证！");
                return result;
            }
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            result.put("status", "NO");
            result.put("msg", "验证码生成失败，请联系管理员！");
            return result;
        }

        redisCache.setCacheObject(CacheKey.VERIFY_FOR_BOT_KEY + code, whitelistInfo, 30, TimeUnit.MINUTES);
        result.put("status", "YES");
        result.put("msg", "验证码生成成功");
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
            log.info("发送消息结果: {}", response);
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

            try {
                // 发送RCON指令并获取结果
                String result = rconService.sendCommand(serverId, command, true);
                StringBuilder response = new StringBuilder();
                response.append("[CQ:at,qq=").append(message.getSender().getUserId()).append("] ");
                response.append("指令已发送至服务器 ").append(serverId).append("\n");
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

            String[] parts = message.getMessage().trim().split("\\s+");
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
            Map<String, Object> params = new HashMap<>();
            params.put("user_id", targetQQ);
            final String response = HttpUtil.post(config.getHttpUrl() + BotApi.GET_STRANGER_INFO, params);
            final JSONObject jsonObject = JSON.parseObject(response);
            if (jsonObject.getInteger("retcode") != 0) {
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
            newManager.setManagerName(managerName);
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

            String[] parts = message.getMessage().trim().split("\\s+");
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
            Map<String, Object> params = new HashMap<>();
            params.put("user_id", targetQQ);
            final String response = HttpUtil.post(config.getHttpUrl() + BotApi.GET_STRANGER_INFO, params);
            final JSONObject jsonObject = JSON.parseObject(response);
            if (jsonObject.getInteger("retcode") != 0) {
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
            newManager.setManagerName(managerName);
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


}
