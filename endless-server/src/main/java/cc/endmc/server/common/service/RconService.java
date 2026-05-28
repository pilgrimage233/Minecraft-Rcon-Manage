package cc.endmc.server.common.service;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.email.EmailService;
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

    /**
     * Per-server 锁，避免全局锁导致不同服务器的 init 互相阻塞
     */
    private static final ConcurrentHashMap<String, ReentrantLock> SERVER_LOCKS = new ConcurrentHashMap<>();

    /**
     * 服务器命令信息缓存，使用 ConcurrentHashMap 保证线程安全
     */
    public static final ConcurrentHashMap<String, ServerCommandInfo> COMMAND_INFO = new ConcurrentHashMap<>();

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
        for (int retryCount = 0; retryCount < MAX_RETRIES; retryCount++) {
            try {
                if (key.contains("all")) {
                    return sendCommandToAllServers(command, onlineFlag, reason);
                } else {
                    return sendCommandToSingleServer(key, command, onlineFlag, reason);
                }
            } catch (Exception e) {
                String result = handleRetryLogic(retryCount, key, command, onlineFlag, reason, e);
                if (result != null) {
                    // 重连成功后得到了结果
                    return result;
                }
                // result == null 表示需要继续重试或已失败
                if (retryCount >= MAX_RETRIES - 1) {
                    break;
                }
            }
        }
        return null;
    }

    /**
     * 发送命令到所有服务器，带超时控制
     */
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

        // 等待所有命令执行完成，设置30秒超时
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("等待所有服务器响应超时（30秒），继续处理已完成的结果");
            // 超时后继续收集已完成的结果
        } catch (ExecutionException e) {
            log.error("执行命令异常: {}", e.getMessage());
        }

        // 收集所有结果（包括已完成的）
        for (CompletableFuture<String> future : futures) {
            if (future.isDone()) {
                try {
                    result.append(future.get()).append("\n");
                } catch (Exception e) {
                    result.append("Error: ").append(e.getMessage()).append("\n");
                }
            } else {
                result.append("Timeout: 服务器响应超时\n");
                future.cancel(true); // 取消未完成的任务
            }
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

    /**
     * 处理重试逻辑
     *
     * @return 重连成功后的命令结果，如果返回 null 表示需要继续重试或已失败
     */
    private String handleRetryLogic(int retryCount, String key, String command, boolean onlineFlag, String reason, Exception e) {
        log.warn("发送命令失败，第{}次重试: {}", retryCount + 1, e.getMessage());

        if (retryCount >= MAX_RETRIES - 1) {
            log.error("发送命令最终失败: {}", e.getMessage());
            // 重连并直接发送一次命令（不再递归进入重试循环）
            if (reconnect(key)) {
                log.debug("重连成功，重新发送命令: {}", command);
                try {
                    RconClient client = RconCache.get(key);
                    String replaced = replaceCommand(key, command, onlineFlag, reason);
                    String result = client.sendCommand(replaced);
                    return stripMinecraftColorCodes(result);
                } catch (Exception ex) {
                    log.error("重连后发送命令仍然失败: {}", ex.getMessage());
                    handleCommandError(key, command);
                }
            } else {
                log.error("重连失败，无法发送命令: {}", command);
                handleCommandError(key, command);
            }
            return null;
        }

        try {
            // 使用指数退避策略，重试间隔随次数增加
            // 注意：这里使用 Thread.sleep 是有意为之的阻塞操作，因为重试需要等待连接恢复
            long delay = Math.min(RETRY_DELAY_BASE_MS * (1L << retryCount), 10000); // 最大10秒
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null;
        }

        return null; // 继续重试
    }

    /**
     * 处理命令错误，记录到缓存
     *
     * @param key     服务器ID
     * @param command 命令
     */
    private void handleCommandError(String key, String command) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(command)) {
            return;
        }

        String cacheKey = CacheKey.ERROR_COMMAND_CACHE_KEY + ":" + key;
        try {
            redisCache.redisTemplate.opsForSet().add(cacheKey, command);
        } catch (Exception e) {
            log.error("记录错误命令失败 [key={}, command={}]: {}", key, command, e.getMessage());
        }
    }

    /**
     * 获取服务器级别的锁，避免全局锁导致不同服务器的 init 互相阻塞
     *
     * @param serverId 服务器ID
     * @return 该服务器对应的锁
     */
    private static ReentrantLock getServerLock(String serverId) {
        return SERVER_LOCKS.computeIfAbsent(serverId, k -> new ReentrantLock(true));
    }

    /**
     * 获取服务器命令信息
     *
     * @param serverId 服务器ID
     * @return 命令信息，如果不存在返回 null
     */
    public static ServerCommandInfo getCommandInfo(String serverId) {
        return COMMAND_INFO.get(serverId);
    }

    /**
     * 设置服务器命令信息
     *
     * @param serverId 服务器ID
     * @param info     命令信息
     */
    public static void putCommandInfo(String serverId, ServerCommandInfo info) {
        if (serverId != null && info != null) {
            COMMAND_INFO.put(serverId, info);
        }
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

        String serverId = info.getId().toString();
        ReentrantLock serverLock = getServerLock(serverId);
        serverLock.lock();
        try {
            // 关闭已存在的连接
            closeExistingConnection(serverId);

            try {
                String decryptedPassword = decryptPassword(info);
                RconClient client = createRconConnection(info, decryptedPassword);

                if (client != null && client.isSocketChannelOpen()) {
                    putCommandInfo(serverId, createServerCommandInfo(info));
                    RconCache.put(serverId, client);
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
            serverLock.unlock();
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
            log.error("加密密钥未配置，无法解密RCON密码 [{}]", info.getNameTag());
            throw new SecurityException("加密密钥未配置，请检查 encrypt.key 环境变量", e);
        } catch (Exception e) {
            log.error("密码解密失败 [{}]: {}", info.getNameTag(), e.getMessage());
            throw new SecurityException("RCON密码解密失败: " + info.getNameTag(), e);
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
        long currentErrorCount = incrementErrorCount();

        if (currentErrorCount >= ERROR_EMAIL_THRESHOLD && currentErrorCount % ERROR_EMAIL_THRESHOLD == 0) {
            sendErrorNotificationEmail(info, e, currentErrorCount);
        }

        log.error("连接失败:{} {} {} {}", info.getNameTag(), info.getIp(), info.getRconPort(), "******");
        log.error("连接失败详细信息: ", e);
    }

    private long incrementErrorCount() {
        String errorCountKey = CacheKey.ERROR_COUNT_KEY;
        Long current = redisCache.redisTemplate.opsForValue().increment(errorCountKey);
        return current != null ? current : 0L;
    }

    private void sendErrorNotificationEmail(ServerInfo info, Exception e, Long errorCount) {
        try {
            String errorType = e.getMessage().contains("Authentication") ? "认证失败" : "连接异常";
            int safeCount = errorCount != null ? errorCount.intValue() : 0;
            String emailContent = EmailTemplates.getAlertNotification(
                    DateUtils.getTime(),
                safeCount,
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
            // 优先从 Redis 缓存获取服务器信息
            List<ServerInfo> serverInfos = redisCache.getCacheObject(CacheKey.SERVER_INFO_KEY);

            // 如果缓存为空，从数据库查询作为 fallback
            if (serverInfos == null || serverInfos.isEmpty()) {
                log.warn("服务器信息缓存为空，尝试从数据库查询");
                try {
                    ServerInfo query = new ServerInfo();
                    query.setStatus(1L);
                    serverInfos = serverInfoMapper.selectServerInfoList(query);

                    if (serverInfos != null && !serverInfos.isEmpty()) {
                        // 重建缓存
                        redisCache.setCacheObject(CacheKey.SERVER_INFO_KEY, serverInfos, 3, TimeUnit.DAYS);
                        log.info("从数据库重建服务器信息缓存成功，共{}个服务器", serverInfos.size());
                    }
                } catch (Exception dbEx) {
                    log.error("从数据库查询服务器信息失败: {}", dbEx.getMessage());
                }
            }

            if (serverInfos == null || serverInfos.isEmpty()) {
                log.error("无法获取服务器信息（缓存和数据库均为空），无法重连");
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

        ServerCommandInfo info = getCommandInfo(key);
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


