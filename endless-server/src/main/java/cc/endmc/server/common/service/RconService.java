package cc.endmc.server.common.service;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.EmailTemplates;
import cc.endmc.server.common.PasswordManager;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.constant.Command;
import cc.endmc.server.common.constant.RconMsg;
import cc.endmc.server.common.rconclient.RconClient;
import cc.endmc.server.common.rconclient.RconClientException;
import cc.endmc.server.domain.server.ServerCommandInfo;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.mapper.server.ServerCommandInfoMapper;
import cc.endmc.server.mapper.server.ServerInfoMapper;
import cc.endmc.server.utils.IPUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Rcon发送命令工具类
 * 作者：Memory
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RconService {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_BASE_MS = 1000L;
    private static final int CONNECTION_TIMEOUT_SECONDS = 10;
    private static final int ERROR_EMAIL_THRESHOLD = 10;
    private static final ReentrantLock INIT_LOCK = new ReentrantLock(true);
    public static Map<String, ServerCommandInfo> COMMAND_INFO = new ConcurrentHashMap<>();
    private final PasswordManager passwordManager;

    private final EmailService emailService;
    private final RedisCache redisCache;
    private final ServerCommandInfoMapper serverCommandInfoMapper;
    private final ServerInfoMapper serverInfoMapper;
    @Qualifier("threadPoolTaskExecutor")
    private final Executor taskExecutor;
    @Value("${whitelist.email}")
    private String adminEmail;

    /**
     * 关闭Rcon
     *
     * @param key
     */
    public static void close(String key) {
        if (key == null) {
            log.error(RconMsg.KEY_EMPTY);
            return;
        }

        try {
            RconCache.remove(key);
            log.debug(RconMsg.TURN_OFF_RCON + "{}", key);
        } catch (Exception e) {
            log.error("关闭 Rcon 连接失败: {}", e.getMessage());
        }
    }

    /**
     * 发送Rcon命令
     *
     * @param key     服务器ID
     * @param command 命令
     */
    public String sendCommand(String key, String command) {
        return this.sendCommand(key, command, false, null);
    }

    /**
     * 异步发送Rcon命令（不等待返回结果）
     * 适用于不需要关心执行结果的场景，如批量删除白名单
     *
     * @param key     服务器ID
     * @param command 命令
     */
    public void sendCommandAsync(String key, String command) {
        sendCommandAsync(key, command, false, null);
    }

    /**
     * 异步发送Rcon命令（不等待返回结果）
     *
     * @param key        服务器ID
     * @param command    命令
     * @param onlineFlag 是否在线
     */
    public void sendCommandAsync(String key, String command, boolean onlineFlag) {
        sendCommandAsync(key, command, onlineFlag, null);
    }

    /**
     * 异步发送Rcon命令（不等待返回结果）
     * 此方法立即返回，不阻塞调用线程
     * 命令执行失败只记录日志，不抛出异常
     *
     * @param key        服务器ID
     * @param command    命令
     * @param onlineFlag 是否在线
     * @param reason     封禁原因
     */
    public void sendCommandAsync(String key, String command, boolean onlineFlag, String reason) {
        CompletableFuture.runAsync(() -> {
            try {
                if (key.contains("all")) {
                    sendCommandToAllServersAsync(command, onlineFlag, reason);
                } else {
                    sendCommandToSingleServerAsync(key, command, onlineFlag, reason);
                }
            } catch (Exception e) {
                log.error("异步发送命令失败 [key={}, command={}]: {}", key, command, e.getMessage());
            }
        }, taskExecutor).exceptionally(ex -> {
            log.error("异步任务执行异常 [key={}, command={}]: {}", key, command, ex.getMessage());
            return null;
        });
    }

    /**
     * 异步发送命令到所有服务器（不等待结果）
     */
    private void sendCommandToAllServersAsync(String command, boolean onlineFlag, String reason) {
        RconCache.getMap().forEach((k, client) -> {
            CompletableFuture.runAsync(() -> {
                try {
                    final String replaced = replaceCommand(k, command, onlineFlag, reason);
                    client.sendCommand(replaced);
                    log.debug("异步发送命令成功到服务器 {}: {}", k, command);
                } catch (Exception e) {
                    log.error("异步发送命令失败到服务器 {} [command={}]: {}", k, command, e.getMessage());
                }
            }, taskExecutor);
        });
    }

    /**
     * 异步发送命令到单个服务器（不等待结果）
     */
    private void sendCommandToSingleServerAsync(String key, String command, boolean onlineFlag, String reason) {
        RconClient client = RconCache.get(key);
        if (client == null) {
            log.error("RconClient not found for key: {}", key);
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                final String replaced = replaceCommand(key, command, onlineFlag, reason);
                client.sendCommand(replaced);
                log.debug("异步发送命令成功到服务器 {}: {}", key, command);
            } catch (Exception e) {
                log.error("异步发送命令失败到服务器 {} [command={}]: {}", key, command, e.getMessage());
            }
        }, taskExecutor);
    }


    /**
     * 发送Rcon命令
     *
     * @param key        服务器ID
     * @param command    命令
     * @param onlineFlag 是否在线
     */
    public String sendCommand(String key, String command, boolean onlineFlag) {
        return this.sendCommand(key, command, onlineFlag, null);
    }

    /**
     * 发送Rcon命令
     *
     * @param key        服务器ID
     * @param command    命令
     * @param onlineFlag 是否在线
     * @param reason     封禁原因
     */
    public String sendCommand(String key, String command, boolean onlineFlag, String reason) {
        StringBuilder result = new StringBuilder();

        for (int retryCount = 0; retryCount < MAX_RETRIES; retryCount++) {
            try {
                if (key.contains("all")) {
                    return sendCommandToAllServers(command, onlineFlag, reason);
                } else {
                    return sendCommandToSingleServer(key, command, onlineFlag, reason);
                }
            } catch (Exception e) {
                if (handleRetryLogic(retryCount, key, command, onlineFlag, reason, e)) {
                    continue;
                } else {
                    break;
                }
            }
        }
        return null;
    }

    private String sendCommandToAllServers(String command, boolean onlineFlag, String reason) throws ExecutionException, InterruptedException {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        StringBuilder result = new StringBuilder();

        RconCache.getMap().forEach((k, client) -> {
            final String replaced = replaceCommand(k, command, onlineFlag, reason);
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return stripMinecraftColorCodes(client.sendCommand(replaced));
                } catch (Exception e) {
                    log.error("发送命令失败到服务器 {}: {}", k, e.getMessage());
                    return "Error: " + e.getMessage();
                }
            }, taskExecutor);
            futures.add(future);
        });

        // 等待所有命令执行完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 收集所有结果
        for (CompletableFuture<String> future : futures) {
            result.append(future.get()).append("\n");
        }

        log.debug("发送命令成功到所有服务器: {}", command);
        return result.toString();
    }

    private String sendCommandToSingleServer(String key, String command, boolean onlineFlag, String reason) {
        RconClient client = RconCache.get(key);
        if (client == null) {
            throw new RuntimeException("RconClient not found for key: " + key);
        }

        final String replaced = replaceCommand(key, command, onlineFlag, reason);
        String result = stripMinecraftColorCodes(client.sendCommand(replaced));

        log.debug("发送命令成功到服务器 {}: {}", key, command);
        return result;
    }

    private boolean handleRetryLogic(int retryCount, String key, String command, boolean onlineFlag, String reason, Exception e) {
        log.warn("发送命令失败，第{}次重试: {}", retryCount + 1, e.getMessage());

        if (retryCount >= MAX_RETRIES - 1) {
            log.error("发送命令最终失败: {}", e.getMessage());
            // 重连并回调
            if (reconnect(key)) {
                log.debug("重连成功，重新发送命令: {}", command);
                sendCommand(key, command, onlineFlag, reason);
                return false; // 不需要继续重试
            } else {
                log.error("重连失败，无法发送命令: {}", command);
                handleCommandError(key, command);
                return false;
            }
        }

        try {
            Thread.sleep(RETRY_DELAY_BASE_MS * (retryCount + 1));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;
        }

        return true; // 继续重试
    }

    /**
     * 处理命令错误，记录到缓存
     *
     * @param key     服务器ID
     * @param command 命令
     */
    private void handleCommandError(String key, String command) {
        Map<String, Object> cache = redisCache.hasKey(CacheKey.ERROR_COMMAND_CACHE_KEY)
                ? redisCache.getCacheObject(CacheKey.ERROR_COMMAND_CACHE_KEY)
                : new ConcurrentHashMap<>();

        cache.computeIfAbsent(key, k -> new HashSet<>());
        ((Set<String>) cache.get(key)).add(command);
        
        redisCache.setCacheObject(CacheKey.ERROR_COMMAND_CACHE_KEY, cache);
    }

    /**
     * 初始化Rcon连接
     *
     * @param info 服务器信息
     * @return 连接是否成功
     */
    public boolean init(ServerInfo info) {
        if (info == null) {
            log.error(RconMsg.MAIN_INFO_EMPTY);
            return false;
        }

        INIT_LOCK.lock();
        try {
            // 关闭已存在的连接
            closeExistingConnection(info.getId().toString());

            try {
                String decryptedPassword = decryptPassword(info);
                RconClient client = createRconConnection(info, decryptedPassword);

                if (client != null && client.isSocketChannelOpen()) {
                    COMMAND_INFO.put(info.getId().toString(), createServerCommandInfo(info));
                    RconCache.put(info.getId().toString(), client);
                    clearErrorCount();
                    log.debug(RconMsg.CONNECT_SUCCESS + "{}", info.getNameTag());
                    return true;
                } else {
                    log.error("RCON连接失败，Socket通道未打开: {} ({}:{})",
                            info.getNameTag(), info.getIp(), info.getRconPort());
                    return false;
                }

            } catch (Exception e) {
                handleConnectionError(info, e);
                return false;
            }
        } finally {
            INIT_LOCK.unlock();
        }
    }

    private void closeExistingConnection(String serverId) {
        if (!RconCache.isEmpty() && RconCache.containsKey(serverId)) {
            RconCache.remove(serverId);
        }
    }

    private String decryptPassword(ServerInfo info) {
        try {
            return passwordManager.decrypt(info.getRconPassword());
        } catch (NullPointerException e) {
            log.warn("环境变量未初始化，使用原始密码: {}", info.getNameTag());
            return info.getRconPassword();
        } catch (Exception e) {
            log.error("密码解密失败: {} - {}", info.getNameTag(), e.getMessage());
            log.warn("尝试使用原始密码连接: {}", info.getNameTag());
            return info.getRconPassword();
        }
    }

    private RconClient createRconConnection(ServerInfo info, String password) {
        String serverIp = IPUtils.domainToIp(info.getIp());
        int port = info.getRconPort().intValue();
        int timeoutMs = CONNECTION_TIMEOUT_SECONDS * 1000;

        log.info("正在连接RCON服务器: {}:{} (解析IP: {})", info.getIp(), port, serverIp);
        try {
            return RconClient.open(serverIp, port, password, timeoutMs);
        } catch (RconClientException e) {
            log.error("连接失败: {} ({}:{}) - {}", info.getNameTag(), serverIp, port, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("连接异常: {} ({}:{})", info.getNameTag(), serverIp, port, e);
            return null;
        }
    }

    private ServerCommandInfo createServerCommandInfo(ServerInfo info) {
        final ServerCommandInfo query = new ServerCommandInfo();
        query.setServerId(String.valueOf(info.getId()));
        ServerCommandInfo commandInfo = serverCommandInfoMapper.selectServerCommandInfoList(query).getFirst();
        if (commandInfo != null) {
            return commandInfo;
        }
        return new ServerCommandInfo();
    }

    private void clearErrorCount() {
        String errorCountKey = CacheKey.ERROR_COUNT_KEY;
        if (redisCache.hasKey(errorCountKey)) {
            redisCache.deleteObject(errorCountKey);
        }
    }

    private void handleConnectionError(ServerInfo info, Exception e) {
        incrementErrorCount();
        Integer currentErrorCount = redisCache.getCacheObject(CacheKey.ERROR_COUNT_KEY);

        if (currentErrorCount >= ERROR_EMAIL_THRESHOLD && currentErrorCount % ERROR_EMAIL_THRESHOLD == 0) {
            sendErrorNotificationEmail(info, e, currentErrorCount);
        }

        log.error("连接失败:{} {} {} {}", info.getNameTag(), info.getIp(), info.getRconPort(), "******");
        log.error("连接失败详细信息: ", e);
    }

    private void incrementErrorCount() {
        String errorCountKey = CacheKey.ERROR_COUNT_KEY;
        Integer errorCount = redisCache.hasKey(errorCountKey)
                ? redisCache.getCacheObject(errorCountKey)
                : 0;
        redisCache.setCacheObject(errorCountKey, errorCount + 1);
    }

    private void sendErrorNotificationEmail(ServerInfo info, Exception e, Integer errorCount) {
        try {
            String errorType = e.getMessage().contains("Authentication") ? "认证失败" : "连接异常";
            String emailContent = EmailTemplates.getAlertNotification(
                    DateUtils.getTime(),
                    errorCount,
                    errorType,
                    info.getNameTag(),
                    info.getIp() + ":" + info.getRconPort()
            );
            emailService.push(adminEmail, EmailTemplates.ALERT_TITLE, emailContent);
        } catch (Exception ex) {
            log.error("邮件发送失败: {}", ex.getMessage());
        }
    }

    /**
     * 重连Rcon
     *
     * @param key 服务器ID
     * @return 重连是否成功
     */
    public boolean reconnect(String key) {
        if (StringUtils.isEmpty(key)) {
            log.error(RconMsg.CONNECT_ERROR);
            return false;
        }

        try {
            List<ServerInfo> serverInfos = redisCache.getCacheObject(CacheKey.SERVER_INFO_KEY);
            if (serverInfos == null || serverInfos.isEmpty()) {
                log.error("服务器信息缓存为空，无法重连");
                return false;
            }

            return serverInfos.stream()
                    .filter(info -> info.getId().toString().equals(key))
                    .findFirst()
                    .map(info -> {
                        close(key);
                        log.debug(RconMsg.TRY_RECONNECT + "{}", key);
                        return init(info);
                    })
                    .orElse(false);

        } catch (Exception e) {
            log.error(RconMsg.ERROR_MSG + "{}", e.getMessage());
            return false;
        }
    }

    /**
     * 替换Rcon命令
     *
     * @param key        服务器ID
     * @param command    命令
     * @param onlineFlag 是否在线
     * @return 替换后的Rcon命令
     */
    public String replaceCommand(String key, String command, boolean onlineFlag) {
        return replaceCommand(key, command, onlineFlag, null);
    }

    /**
     * 替换Rcon命令
     *
     * @param key        服务器ID
     * @param command    命令
     * @param onlineFlag 是否在线
     * @param reason     封禁原因
     * @return 替换后的Rcon命令
     */
    public String replaceCommand(String key, String command, boolean onlineFlag, String reason) {
        if (StringUtils.isEmpty(command)) {
            log.error("替换命令失败：command为空");
            return command;
        }

        // 检查命令是否匹配
        if (!isCommandMatched(command)) {
            return command;
        }

        ServerCommandInfo info = COMMAND_INFO.get(key);
        if (info == null) {
            log.error("替换命令失败：指令信息为空，服务器ID: {}", key);
            throw new RuntimeException("指令信息为空");
        }

        return processCommandReplacement(command, info, onlineFlag, reason, key);
    }

    private boolean isCommandMatched(String command) {
        return Arrays.stream(Command.MATCH_COMMAND)
                .anyMatch(command::startsWith);
    }

    /**
     * 处理命令替换逻辑
     *
     * @param command    原始命令
     * @param info       服务器指令信息
     * @param onlineFlag 是否在线
     * @param reason     封禁原因
     * @param key        服务器ID
     * @return 替换后的命令
     */
    private String processCommandReplacement(String command, ServerCommandInfo info, boolean onlineFlag, String reason, String key) {
        Map<String, CommandReplacer> commandMap = createCommandMap(info, onlineFlag);

        for (Map.Entry<String, CommandReplacer> entry : commandMap.entrySet()) {
            if (command.startsWith(entry.getKey())) {
                String player = command.substring(entry.getKey().length()).trim();
                String template = entry.getValue().replace(command);
                String replacedCommand = template.replace("{player}", player);

                // 替换封禁原因
                if (reason != null && replacedCommand.contains("{reason}")) {
                    replacedCommand = replacedCommand.replace("{reason}", reason);
                }

                log.info("替换命令成功：{} -> {}", key, replacedCommand);
                return replacedCommand;
            }
        }

        log.info("替换命令失败，未匹配模板：{} -> {}", key, command);
        return command;
    }

    private Map<String, CommandReplacer> createCommandMap(ServerCommandInfo info, boolean onlineFlag) {
        Map<String, CommandReplacer> commandMap = new HashMap<>();
        commandMap.put(Command.WHITELIST_ADD_COMMAND,
                cmd -> onlineFlag ? info.getOnlineAddWhitelistCommand() : info.getOfflineAddWhitelistCommand());
        commandMap.put(Command.WHITELIST_REMOVE_COMMAND,
                cmd -> onlineFlag ? info.getOnlineRmWhitelistCommand() : info.getOfflineRmWhitelistCommand());
        commandMap.put(Command.BAN_ADD_COMMAND,
                cmd -> onlineFlag ? info.getOnlineAddBanCommand() : info.getOfflineAddBanCommand());
        commandMap.put(Command.BAN_REMOVE_COMMAND,
                cmd -> onlineFlag ? info.getOnlineRmBanCommand() : info.getOfflineRmBanCommand());
        return commandMap;
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
     * 重建缓存
     * 服务器信息缓存
     */
    public void reBuildCache() {
        try {
            List<ServerInfo> serverInfos = serverInfoMapper.selectServerInfoList(new ServerInfo());

            if (serverInfos == null || serverInfos.isEmpty()) {
                log.error(RconMsg.SERVER_EMPTY);
                return;
            }

            // 构建服务器信息映射
            Map<String, ServerInfo> serverInfoMap = serverInfos.stream()
                    .collect(Collectors.toMap(
                            info -> info.getId().toString(),
                            info -> info
                    ));

            // 更新缓存
            redisCache.setCacheObject(CacheKey.SERVER_INFO_MAP_KEY, serverInfoMap);
            redisCache.setCacheObject(CacheKey.SERVER_INFO_KEY, serverInfos, 3, TimeUnit.DAYS);
            redisCache.setCacheObject(CacheKey.SERVER_INFO_UPDATE_TIME_KEY, DateUtils.getNowDate());

            log.info("服务器信息缓存重建完成，共{}个服务器", serverInfos.size());

        } catch (Exception e) {
            log.error("重建缓存失败: {}", e.getMessage(), e);
        }
    }

    @FunctionalInterface
    interface CommandReplacer {
        String replace(String command);
    }
}


