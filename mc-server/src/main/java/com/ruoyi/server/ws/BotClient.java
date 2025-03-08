package com.ruoyi.server.ws;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.server.common.EmailTemplates;
import com.ruoyi.server.common.MapCache;
import com.ruoyi.server.common.constant.BotApi;
import com.ruoyi.server.common.constant.CacheKey;
import com.ruoyi.server.common.service.EmailService;
import com.ruoyi.server.common.service.RconService;
import com.ruoyi.server.domain.permission.WhitelistInfo;
import com.ruoyi.server.service.permission.IWhitelistInfoService;
import com.ruoyi.server.service.server.IServerInfoService;
import com.ruoyi.server.ws.config.QQBotProperties;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * QQ机器人WebSocket客户端
 * 用于与QQ机器人服务器建立长连接，实时接收消息
 * 只有在配置文件中启用（qq.bot.enable=true）时才会创建此客户端
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "qq.bot", name = "enable", havingValue = "true")
public class BotClient extends WebSocketClient {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final QQBotProperties properties;
    private ScheduledFuture<?> reconnectTask;
    private volatile boolean isShuttingDown = false;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IWhitelistInfoService whitelistInfoService;

    @Autowired
    private IServerInfoService serverInfoService;

    @Autowired
    private RconService rconService;

    @Value("${app-url}")
    private String appUrl;

    /**
     * 构造函数
     * 初始化WebSocket客户端，设置认证token
     *
     * @param properties QQ机器人配置属性
     */
    public BotClient(QQBotProperties properties) {
        super(URI.create("ws://localhost"), new HashMap<String, String>() {{
            put("Authorization", "Bearer " + properties.getToken());
        }});
        this.properties = properties;
    }

    /**
     * Spring Bean初始化时调用
     * 使用配置的URL创建WebSocket连接
     */
    @PostConstruct
    public void init() {
        try {
            this.uri = URI.create(properties.getWs().getUrl());
            connect();
            log.info("QQ机器人WebSocket客户端已初始化，连接地址: {}", properties.getWs().getUrl());
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
            try {
                close();
            } catch (Exception e) {
                log.error("关闭WebSocket连接时发生错误: {}", e.getMessage());
            }

            log.info("QQ机器人WebSocket客户端已关闭");
        } catch (Exception e) {
            log.error("关闭QQ机器人WebSocket客户端时发生错误: {}", e.getMessage());
        }
    }

    /**
     * WebSocket连接成功时的回调方法
     * 当与服务器建立连接后调用
     *
     * @param handshakedata 握手信息
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("已成功连接到QQ机器人WebSocket服务器");
        // 连接成功后取消重连任务
        if (reconnectTask != null) {
            reconnectTask.cancel(false);
            reconnectTask = null;
        }
    }

    /**
     * 接收到WebSocket消息时的回调方法
     * 当服务器发送消息时调用，负责解析消息并处理
     *
     * @param message 收到的消息内容
     */
    @Override
    public void onMessage(String message) {
        try {
            log.info("收到消息: {}", message);
            QQMessage qqMessage = JSON.parseObject(message, QQMessage.class);
            handleMessage(qqMessage);
        } catch (Exception e) {
            log.error("消息解析失败: {}", e.getMessage());
        }
    }

    /**
     * WebSocket连接关闭时的回调方法
     * 当连接断开时调用，会触发重连机制
     *
     * @param code   关闭代码
     * @param reason 关闭原因
     * @param remote 是否由服务器端关闭
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket连接已关闭 - 关闭方: {}, 状态码: {}, 原因: {}",
                (remote ? "服务器" : "客户端"), code, reason);
        if (!isShuttingDown) {
            scheduleReconnect();
        }
    }

    /**
     * WebSocket发生错误时的回调方法
     * 当连接出现异常时调用，会触发重连机制
     *
     * @param ex 异常信息
     */
    @Override
    public void onError(Exception ex) {
        log.error("WebSocket连接发生错误: {}", ex.getMessage());
        if (!isShuttingDown) {
            scheduleReconnect();
        }
    }

    /**
     * 安排重连任务
     * 在连接断开或发生错误时，每30秒尝试重新连接一次
     * 直到连接成功为止
     */
    private void scheduleReconnect() {
        if (isShuttingDown) {
            return;
        }
        
        if (reconnectTask == null || reconnectTask.isDone()) {
            try {
                reconnectTask = scheduler.scheduleAtFixedRate(() -> {
                    try {
                        if (!isOpen() && !isShuttingDown) {
                            log.info("正在尝试重新连接...");
                            reconnect();
                        }
                    } catch (Exception e) {
                        log.error("重连失败: {}", e.getMessage());
                    }
                }, 0, 30, TimeUnit.SECONDS);
            } catch (RejectedExecutionException e) {
                log.warn("调度器已关闭，无法创建重连任务");
            }
        }
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
                properties.getGroupIds().contains(message.getGroupId())) {
            log.info("收到QQ群[{}]消息 - 发送者: {}, 内容: {}",
                    message.getGroupId(),
                    message.getSender().getUserId(),
                    message.getMessage());

            String msg = message.getMessage().trim();
            String base = "[CQ:at,qq=" + message.getSender().getUserId() + "]";

            if (msg.startsWith("白名单申请")) {
                handleWhitelistApplication(message);
            } else if (msg.startsWith("查询白名单")) {
                handleWhitelistQuery(message);
            } else if (msg.startsWith("过审") || msg.startsWith("拒审")) {
                handleWhitelistReview(message);
            } else if (msg.startsWith("封禁") || msg.startsWith("解封")) {
                handleBanOperation(message);
            } else if (msg.startsWith("发送指令")) {
                handleRconCommand(message);
            } else if (msg.startsWith("查询玩家")) {
                handlePlayerQuery(message);
            } else if (msg.startsWith("查询在线")) {
                handleOnlineQuery(message);
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
        // Map<String, String> body = new HashMap<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("group_id", message.getGroupId().toString());
        jsonObject.put("message", msg);
        final QQBotProperties.Http http = properties.getHttp();

        // 设置Authorization头
        final HttpResponse response = HttpUtil.createPost(http.getUrl() + BotApi.SEND_GROUP_MSG)
                // .header("Authorization", "Bearer " + properties.getToken())
                .body(jsonObject.toJSONString())
                .execute();
        log.info("发送消息结果: {}", response);
    }

    /**
     * 处理白名单审核请求
     * 管理员可以通过发送"过审 ID"或"拒审 ID"来审核白名单
     *
     * @param message QQ消息对象
     */
    private void handleWhitelistReview(QQMessage message) {
        try {
            // 检查是否是管理员
            if (!properties.getManagers().contains(message.getSender().getUserId())) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
                return;
            }

            String[] parts = message.getMessage().trim().split("\\s+");
            if (parts.length < 2) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 格式错误，正确格式：过审/拒审 玩家ID");
                return;
            }

            String command = parts[0];
            String playerId = parts[1];

            // 查询白名单信息
            WhitelistInfo whitelistInfo = new WhitelistInfo();
            whitelistInfo.setUserName(playerId);
            List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);

            if (whitelistInfos.isEmpty()) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 未找到玩家 " + playerId + " 的白名单申请。");
                return;
            }

            whitelistInfo = whitelistInfos.get(0);

            // 设置审核状态
            if (command.equals("过审")) {
                whitelistInfo.setStatus("1"); // 通过
                whitelistInfo.setAddState("1");
                whitelistInfo.setServers("all"); // 默认添加到所有服务器
            } else {
                whitelistInfo.setStatus("2"); // 拒绝
                whitelistInfo.setAddState("2");
                whitelistInfo.setRemoveReason("管理员拒绝");
            }

            // 更新白名单信息
            int result = whitelistInfoService.updateWhitelistInfo(whitelistInfo, message.getSender().getUserId().toString());

            if (result > 0) {
                String status = command.equals("过审") ? "通过" : "拒绝";
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 已" + status + "玩家 " + playerId + " 的白名单申请。");
            } else {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 审核操作失败，请稍后重试。");
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("处理白名单审核失败: {}", e.getMessage());
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
            if (!properties.getManagers().contains(message.getSender().getUserId())) {
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
            if (!properties.getManagers().contains(message.getSender().getUserId())) {
                sendMessage(message, "[CQ:at,qq=" + message.getSender().getUserId() + "] 您没有权限执行此操作。");
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
}
