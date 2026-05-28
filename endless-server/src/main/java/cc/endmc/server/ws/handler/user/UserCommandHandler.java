package cc.endmc.server.ws.handler.user;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.framework.manager.AsyncManager;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.EmailTemplates;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.common.email.EmailService;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.server.IServerInfoService;
import cc.endmc.server.utils.HtmlUtils;
import cc.endmc.server.utils.IPUtils;
import cc.endmc.server.utils.SecureCodeUtil;
import cc.endmc.server.ws.BotClient;
import cc.endmc.server.ws.QQMessage;
import cc.endmc.server.ws.handler.BaseCommandHandler;
import cc.endmc.server.ws.handler.CommandRegistry;
import cc.endmc.server.ws.helper.BotMessageHelper;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

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
import java.util.concurrent.TimeUnit;

/**
 * 用户命令处理器
 * 处理普通用户可用的命令
 */
@Slf4j
public class UserCommandHandler extends BaseCommandHandler {

    private final IWhitelistInfoService whitelistInfoService;
    private final IServerInfoService serverInfoService;
    private final EmailService emailService;
    private final String appUrl;
    private final AsyncManager asyncExecutor = AsyncManager.me();

    public UserCommandHandler(BotClient botClient, RedisCache redisCache,
                              IWhitelistInfoService whitelistInfoService,
                              IServerInfoService serverInfoService,
                              EmailService emailService, String appUrl) {
        super(botClient, redisCache);
        this.whitelistInfoService = whitelistInfoService;
        this.serverInfoService = serverInfoService;
        this.emailService = emailService;
        this.appUrl = appUrl;
    }

    /**
     * 注册用户命令到命令注册器
     */
    public void registerCommands(CommandRegistry registry) {
        registry.register("help", this::handleHelpCommand, "h");
        registry.register("白名单申请", this::handleWhitelistApplication, "apply", "wl");
        registry.register("查询白名单", this::handleWhitelistQuery, "check", "wlcheck");
        registry.register("查询玩家", this::handlePlayerQuery, "player", "p");
        registry.register("查询在线", this::handleOnlineQuery, "online", "list");
        registry.register("查询服务器", this::handleServerList, "servers", "sv");
        registry.register("test", this::handleTestCommand, "ping");
    }

    /**
     * 处理help命令
     */
    public void handleHelpCommand(QQMessage message) {
        String prefix = getConfig().getCommandPrefix();
        if (StringUtils.isEmpty(prefix)) {
            prefix = "/";
        }

        StringBuilder help = new StringBuilder();
        help.append(BotMessageHelper.getAtPrefix(message)).append("\n");
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
        if (isAdmin(message)) {
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
            if (isSuperAdmin(message)) {
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
     * 处理白名单查询请求
     */
    public void handleWhitelistQuery(QQMessage message) {
        try {
            String base = getAtPrefix(message);

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
            BotMessageHelper.appendIfExists(response, result, "游戏ID");
            BotMessageHelper.appendIfExists(response, result, "QQ号");
            BotMessageHelper.appendIfExists(response, result, "账号类型");
            BotMessageHelper.appendIfExists(response, result, "审核状态");

            if (result.containsKey("审核状态")) {
                String status = (String) result.get("审核状态");
                switch (status) {
                    case "已通过":
                        BotMessageHelper.appendIfExists(response, result, "审核时间");
                        BotMessageHelper.appendIfExists(response, result, "审核人");
                        BotMessageHelper.appendIfExists(response, result, "最后上线时间");
                        BotMessageHelper.appendIfExists(response, result, "游戏时间");
                        break;
                    case "未通过/已移除":
                        BotMessageHelper.appendIfExists(response, result, "移除时间");
                        BotMessageHelper.appendIfExists(response, result, "移除原因");
                        break;
                    case "已封禁":
                        BotMessageHelper.appendIfExists(response, result, "封禁时间");
                        BotMessageHelper.appendIfExists(response, result, "封禁原因");
                        break;
                    case "待审核":
                        BotMessageHelper.appendIfExists(response, result, "UUID");
                        break;
                }
            }

            BotMessageHelper.appendIfExists(response, result, "城市");

            // 发送消息
            sendMessage(message, response.toString());

        } catch (Exception e) {
            log.error("处理白名单查询失败: {}", e.getMessage());
            sendMessage(message, BotMessageHelper.buildErrorMessage(message, "查询失败，请稍后重试。"));
        }
    }

    /**
     * 处理白名单申请请求
     */
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

            log.info("收到白名单申请 - 玩家ID: {}, 账号类型: {}", playerId, isPremium ? "正版" : "离线");

            // 处理白名单申请
            handleWhitelistApplicationInternal(message.getGroupId(),
                    message.getSender().getUserId(),
                    playerId,
                    accountType, message);

        } catch (Exception e) {
            log.error("处理白名单申请失败: {}", e.getMessage());
        }
    }

    /**
     * 处理白名单申请内部方法
     */
    private void handleWhitelistApplicationInternal(Long groupId, Long userId, String playerId, int accountType, QQMessage message) {
        log.info("正在处理白名单申请 - 群号: {}, 申请人: {}, 玩家ID: {}, 账号类型: {}",
                groupId, userId, playerId, accountType == 1 ? "正版" : "离线");
        String base = getAtPrefix(message);
        WhitelistInfo whitelistInfo = new WhitelistInfo();
        whitelistInfo.setQqNum(String.valueOf(userId));
        // 查询是否已存在该QQ号的申请
        final List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);
        if (!whitelistInfos.isEmpty()) {
            sendMessage(message, base + "您已提交过申请，请勿重复提交！");
            return;
        }

        whitelistInfo.setUserName(playerId);
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
            try {
                emailService.push(whitelistInfo.getQqNum() + EmailTemplates.QQ_EMAIL,
                        EmailTemplates.EMAIL_VERIFY_TITLE, EmailTemplates.getEmailVerifyTemplate(url));
            } catch (Exception e) {
                log.error("发送邮件通知失败: {}", e.getMessage(), e);
            }
        } else {
            // 发送消息
            String msg = "[CQ:at,qq=" + userId + "] 申请失败，请稍后再试。";
            sendMessage(message, msg);
        }
    }

    /**
     * 私有化方法,用于程序内部机器人申请白名单
     */
    public Map<String, Object> applyForBot(WhitelistInfo whitelistInfo) {
        if (whitelistInfo == null || whitelistInfo.getUserName() == null ||
                whitelistInfo.getQqNum() == null || whitelistInfo.getOnlineFlag() == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();

        // 检查是否有活跃的验证码
        if (SecureCodeUtil.hasActiveCode(whitelistInfo.getQqNum(), CacheKey.VERIFY_FOR_BOT_KEY)) {
            result.put("status", "NO");
            result.put("msg", "请勿重复提交！否则可能将无法通过验证！");
            return result;
        }

        // 生成安全的验证码（8位字母数字组合）
        final String code = SecureCodeUtil.generateSecureCode(
                whitelistInfo.getQqNum(),
                CacheKey.VERIFY_FOR_BOT_KEY,
                8,
                30
        );

        if (StringUtils.isEmpty(code)) {
            result.put("status", "NO");
            result.put("msg", "验证码申请失败，请稍后再试。");
            return result;
        }

        // 缓存白名单信息
        redisCache.setCacheObject(CacheKey.VERIFY_FOR_BOT_KEY + code, whitelistInfo, 30, TimeUnit.MINUTES);

        // 标记活跃验证码
        SecureCodeUtil.markActiveCode(whitelistInfo.getQqNum(), CacheKey.VERIFY_FOR_BOT_KEY, 30);

        result.put("status", "YES");
        result.put("msg", "验证码申请成功，请查看邮箱。");
        result.put("code", code);

        return result;
    }

    /**
     * 处理玩家信息查询请求
     */
    public void handlePlayerQuery(QQMessage message) {
        try {
            String base = getAtPrefix(message);
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
            BotMessageHelper.appendIfExists(response, result, "游戏ID");
            BotMessageHelper.appendIfExists(response, result, "QQ号");
            BotMessageHelper.appendIfExists(response, result, "账号类型");
            BotMessageHelper.appendIfExists(response, result, "审核状态");

            if (result.containsKey("审核状态")) {
                String status = (String) result.get("审核状态");
                switch (status) {
                    case "已通过":
                        BotMessageHelper.appendIfExists(response, result, "审核时间");
                        BotMessageHelper.appendIfExists(response, result, "审核人");
                        BotMessageHelper.appendIfExists(response, result, "最后上线时间");
                        BotMessageHelper.appendIfExists(response, result, "游戏时间");
                        break;
                    case "未通过/已移除":
                        BotMessageHelper.appendIfExists(response, result, "移除时间");
                        BotMessageHelper.appendIfExists(response, result, "移除原因");
                        break;
                    case "已封禁":
                        BotMessageHelper.appendIfExists(response, result, "封禁时间");
                        BotMessageHelper.appendIfExists(response, result, "封禁原因");
                        break;
                    case "待审核":
                        BotMessageHelper.appendIfExists(response, result, "UUID");
                        break;
                }
            }

            BotMessageHelper.appendIfExists(response, result, "城市");
            if (result.containsKey("历史名称")) {
                response.append("历史名称: ").append(result.get("历史名称")).append("\n");
            }

            // 发送消息
            sendMessage(message, response.toString());

        } catch (Exception e) {
            log.error("处理玩家查询失败: {}", e.getMessage());
            sendMessage(message, BotMessageHelper.buildErrorMessage(message, "查询失败，请稍后重试。"));
        }
    }

    /**
     * 处理在线玩家查询请求
     */
    public void handleOnlineQuery(QQMessage message) {
        try {
            String base = getAtPrefix(message);

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
            sendMessage(message, BotMessageHelper.buildErrorMessage(message, "查询失败，请稍后重试。"));
        }
    }

    /**
     * 处理服务器列表查询命令
     */
    public void handleServerList(QQMessage message) {
        try {
            String base = getAtPrefix(message);

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
            sendMessage(message, BotMessageHelper.buildErrorMessage(message, "查询失败，请稍后重试。"));
        }
    }

    /**
     * 处理test命令
     */
    public void handleTestCommand(QQMessage message) {
        String[] parts = message.getMessage().split("\\s+");
        if (parts.length > 1 && (parts[1].startsWith("http") || parts[1].startsWith("https"))) {
            testHttp(message);
        } else {
            testServer(message);
        }
    }

    /**
     * 测试Minecraft服务器通断
     */
    private void testServer(QQMessage message) {
        try {
            String base = getAtPrefix(message);
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：test <服务器地址>[:端口]，默认端口25565");
                return;
            }

            // 检查是否是管理员，非管理员有使用次数限制
            boolean isAdmin = isAdmin(message);

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
            sendMessage(message, BotMessageHelper.buildErrorMessage(message, "测试失败，请稍后重试。"));
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
                            motd = BotMessageHelper.stripMinecraftColorCodes(motd);
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
            sendMessage(message, BotMessageHelper.buildErrorMessage(message, "测试失败，请稍后重试。"));
        }
    }

    /**
     * 测试HTTP/HTTPS服务器通断
     */
    private void testHttp(QQMessage message) {
        try {
            String base = getAtPrefix(message);
            String[] parts = message.getMessage().trim().split("\\s+");

            if (parts.length < 2) {
                sendMessage(message, base + " 格式错误，正确格式：test http://example.com[:port] 或 test https://example.com[:port]");
                return;
            }

            // 检查是否是管理员，非管理员有使用次数限制
            boolean isAdmin = isAdmin(message);

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
            sendMessage(message, BotMessageHelper.buildErrorMessage(message, "测试失败，请稍后重试。"));
        }
    }

    /**
     * 从JSON中提取MOTD文本
     * 处理Minecraft服务器返回的复杂JSON描述格式
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
     * 处理群成员减少通知
     */
    public void handleGroupDecrease(QQMessage message) {
        if (getConfig().getGroupIdList().contains(message.getGroupId())) {
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
            int result = whitelistInfoService.updateWhitelistInfo(whitelistInfo, userId.toString());
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
}
