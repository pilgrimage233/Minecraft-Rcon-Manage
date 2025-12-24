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
import cc.endmc.server.domain.server.ServerCommandInfo;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.mapper.server.ServerInfoMapper;
import cc.endmc.server.utils.IPUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Rcon发送命令工具类
 * 作者：Memory
 */
@Slf4j
@Component
public class RconService {

    public static Map<String, ServerCommandInfo> COMMAND_INFO = new HashMap<>();
    @Value("${whitelist.email}")
    private String ADMIN_EMAIL;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private PasswordManager PasswordManager;
    @Autowired
    private ServerInfoMapper serverInfoMapper;

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

        try (RconClient client = RconCache.get(key)) {
            if (client != null) {
                RconCache.remove(key);
                log.debug(RconMsg.TURN_OFF_RCON + "{}", key);
            }
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
        return this.sendCommand(key, command, false);
    }

    /**
     * 发送Rcon命令
     *
     * @param key        服务器ID
     * @param command    命令
     * @param onlineFlag 是否在线
     */
    public String sendCommand(String key, String command, boolean onlineFlag) {
        int maxRetries = 3;
        int retryCount = 0;
        StringBuilder result = new StringBuilder();

        while (retryCount < maxRetries) {
            try {
                if (key.contains("all")) {
                    // 使用 CompletableFuture.allOf 等待所有命令执行完成
                    List<CompletableFuture<String>> futures = new ArrayList<>();

                    RconCache.getMap().forEach((k, client) -> {
                        final String replaced = replaceCommand(k, command, onlineFlag);
                        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                            try {
                                return stripMinecraftColorCodes(client.sendCommand(replaced));
                            } catch (Exception e) {
                                log.error("发送命令失败: {}", e.getMessage());
                                return "Error: " + e.getMessage();
                            }
                        });
                        futures.add(future);
                    });

                    // 等待所有命令执行完成
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                    // 收集所有结果
                    for (CompletableFuture<String> future : futures) {
                        result.append(future.get()).append("\n");
                    }
                } else {
                    if (RconCache.get(key) == null) {
                        throw new RuntimeException("RconClient not found for key: " + key);
                    }

                    final String replaced = replaceCommand(key, command, onlineFlag);
                    final RconClient client = RconCache.get(key);

                    // 同步执行命令
                    result.append(stripMinecraftColorCodes(client.sendCommand(replaced)));
                }
                log.debug("发送命令成功: {}", command);
                return result.toString();

            } catch (Exception e) {
                retryCount++;
                log.warn("发送命令失败，第{}次重试: {}", retryCount, e.getMessage());

                if (retryCount >= maxRetries) {
                    log.error("发送命令最终失败: {}", e.getMessage());
                    // 重连并回调
                    if (reconnect(key)) {
                        log.debug("重连成功，重新发送命令: {}", command);
                        return this.sendCommand(key, command, onlineFlag);
                    } else {
                        log.error("重连失败，无法发送命令: {}", command);
                        handleCommandError(key, command);
                    }
                }

                try {
                    Thread.sleep(1000L * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return null;
    }

    // 处理命令错误的辅助方法
    private void handleCommandError(String key, String command) {
        Map<String, Object> cache = new HashMap<>();
        // 命令缓存
        if (redisCache.hasKey(CacheKey.ERROR_COMMAND_CACHE_KEY)) {
            cache = redisCache.getCacheObject(CacheKey.ERROR_COMMAND_CACHE_KEY);
            if (cache.containsKey(key)) {
                Set<String> set = (Set<String>) cache.get(key);
                set.add(command);
                cache.put(key, set);
            } else {
                Set<String> set = new HashSet<>();
                set.add(command);
                cache.put(key, set);
            }
        } else {
            Set<String> set = new HashSet<>();
            set.add(command);
            cache.put(key, set);
        }
        redisCache.setCacheObject(CacheKey.ERROR_COMMAND_CACHE_KEY, cache);
    }

    /**
     * 初始化Rcon连接
     *
     * @param info
     */
    public boolean init(ServerInfo info) {

        if (info == null) {
            log.error(RconMsg.MAIN_INFO_EMPTY);
            return false;
        }

        if ((!RconCache.isEmpty()) && RconCache.containsKey(info.getId().toString())) {
            RconCache.get(info.getId().toString()).close();
        }

        final String ERROR_COUNT_KEY = CacheKey.ERROR_COUNT_KEY;
        AtomicReference<RconClient> client = new AtomicReference<>();

        try {
            String decryptedPassword;
            try {
                try {
                    decryptedPassword = PasswordManager.decrypt(info.getRconPassword());
                    log.debug("密码解密成功: {}", info.getNameTag());
                } catch (NullPointerException e) {
                    // 环境变量未初始化，使用原始密码
                    log.warn("环境变量未初始化，使用原始密码: {}", info.getNameTag());
                    decryptedPassword = info.getRconPassword();
                }
            } catch (Exception e) {
                log.error("密码解密失败: {} - {}", info.getNameTag(), e.getMessage());
                // 尝试使用原始密码
                log.warn("尝试使用原始密码连接: {}", info.getNameTag());
                decryptedPassword = info.getRconPassword();
            }

            // 使用异步线程初始化Rcon连接，超时时间为5秒
            String finalDecryptedPassword = decryptedPassword;
            String serverIp = IPUtils.domainToIp(info.getIp());
            int port = info.getRconPort().intValue();

            log.info("正在连接RCON服务器: {}:{} (解析IP: {})", info.getIp(), port, serverIp);

            CompletableFuture<RconClient> rconFuture = CompletableFuture.supplyAsync(() -> {
                return RconClient.open(serverIp, port, finalDecryptedPassword);
            });

            try {
                // 等待连接完成，设置超时
                client.set(rconFuture.get(10, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                log.error("连接超时: {} ({}:{})", info.getNameTag(), serverIp, port);
            }

            if (client.get() == null) {
                log.error("RCON连接失败: {} ({}:{})", info.getNameTag(), serverIp, port);
            }

            if (client.get().isSocketChannelOpen()) {
                RconCache.put(info.getId().toString(), client.get());

                log.debug(RconMsg.CONNECT_SUCCESS + "{}", info.getNameTag());

                // 清除错误次数
                if (redisCache.hasKey(ERROR_COUNT_KEY)) {
                    redisCache.deleteObject(ERROR_COUNT_KEY);
                }
                return true;
            } else {
                log.error("RCON连接失败，Socket通道未打开: {} ({}:{})", info.getNameTag(), serverIp, port);
                return false;
            }

        } catch (Exception e) {
            // 记录错误次数
            if (redisCache.hasKey(ERROR_COUNT_KEY)) {
                final Integer errorCount = redisCache.getCacheObject(ERROR_COUNT_KEY);
                redisCache.setCacheObject(ERROR_COUNT_KEY, errorCount + 1);
            } else {
                redisCache.setCacheObject(ERROR_COUNT_KEY, 1);
            }

            // 获取当前错误次数
            Integer currentErrorCount = (Integer) redisCache.getCacheObject(ERROR_COUNT_KEY);

            // 每10次错误发送一次告警邮件
            if (currentErrorCount >= 10 && currentErrorCount % 10 == 0) {
                try {
                    String errorType;

                    // 区分异常类型
                    if (e.getMessage().contains("Authentication")) {
                        errorType = "认证失败";
                    } else {
                        errorType = "连接异常";
                    }

                    // 使用新的告警邮件模板
                    String emailContent = EmailTemplates.getAlertNotification(
                            DateUtils.getTime(),           // 异常时间
                            currentErrorCount,             // 异常次数
                            errorType,                     // 异常类型
                            info.getNameTag(),             // 服务器名称
                            info.getIp() + ":" + info.getRconPort()  // 服务器地址
                    );

                    emailService.push(ADMIN_EMAIL, EmailTemplates.ALERT_TITLE, emailContent);
                } catch (ExecutionException | InterruptedException ex) {
                    log.error("邮件发送失败: {}", ex.getMessage());
                }
            }

            log.error("连接失败:{} {} {} {}", info.getNameTag(), info.getIp(), info.getRconPort(), "******");
            log.error("连接失败详细信息: ", e);
            return false;
        }
    }

    /**
     * 重连Rcon
     *
     * @param key
     */
    public boolean reconnect(String key) {
        if (key == null) {
            log.error(RconMsg.CONNECT_ERROR);
            return false;
        }
        List<ServerInfo> serverInfo = null;

        try {
            // 从Redis缓存读取服务器信息
            serverInfo = redisCache.getCacheObject(CacheKey.SERVER_INFO_KEY);
        } catch (Exception e) {
            log.error(RconMsg.ERROR_MSG + "{}", e.getMessage());
            return false;
        }

        // 重连Rcon
        for (ServerInfo info : serverInfo) {
            if (info.getId().toString().equals(key)) {
                close(key);
                log.debug(RconMsg.TRY_RECONNECT + "{}", key);
                if (init(info)) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    /**
     * 替换Rcon命令
     *
     * @param key
     * @param command
     * @param onlineFlag
     * @return 替换后的Rcon命令
     */
    public String replaceCommand(String key, String command, boolean onlineFlag) {
        if (StringUtils.isEmpty(command)) {
            log.error("替换命令失败：command为空");
            return key;
        }

        // 未匹配直接返回
        boolean isMatch = false;
        String[] matchCommand = Command.MATCH_COMMAND;
        for (String s : matchCommand) {
            if (command.startsWith(s)) {
                isMatch = true;
                break;
            }
        }
        if (!isMatch) return command;

        ServerCommandInfo info = COMMAND_INFO.get(key);
        if (info == null) {
            log.error("替换命令失败：指令信息为空");
            throw new RuntimeException("指令信息为空");
        }

        // 使用 Map 存储命令映射关系
        Map<String, CommandReplacer> commandMap = new HashMap<>();
        commandMap.put(Command.WHITELIST_ADD_COMMAND,
                (cmd) -> onlineFlag ? info.getOnlineAddWhitelistCommand() : info.getOfflineAddWhitelistCommand());
        commandMap.put(Command.WHITELIST_REMOVE_COMMAND,
                (cmd) -> onlineFlag ? info.getOnlineRmWhitelistCommand() : info.getOfflineRmWhitelistCommand());
        commandMap.put(Command.BAN_ADD_COMMAND,
                (cmd) -> onlineFlag ? info.getOnlineAddBanCommand() : info.getOfflineRmBanCommand());
        commandMap.put(Command.BAN_REMOVE_COMMAND,
                (cmd) -> onlineFlag ? info.getOnlineRmBanCommand() : info.getOfflineRmBanCommand());

        isMatch = false;
        for (Map.Entry<String, CommandReplacer> entry : commandMap.entrySet()) {
            if (command.startsWith(entry.getKey())) {
                isMatch = true;
                String player = command.substring(entry.getKey().length()).trim();
                String template = entry.getValue().replace(command);
                command = template.replace("{player}", player);
                log.info("替换命令成功：{} -> {}", key, command);
                break;
            }
        }

        if (!isMatch) {
            log.info("替换命令失败，未匹配模板：{} -> {}", key, command);
        }

        return command;
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
        // 服务器信息缓存
        final List<ServerInfo> serverInfos = serverInfoMapper.selectServerInfoList(new ServerInfo());
        if (serverInfos == null || serverInfos.isEmpty()) {
            log.error(RconMsg.SERVER_EMPTY);
        }
        Map<String, ServerInfo> map = new HashMap<>();
        if (serverInfos != null) {
            for (ServerInfo serverInfo : serverInfos) {
                map.put(serverInfo.getId().toString(), serverInfo);
            }
        }

        redisCache.setCacheObject(CacheKey.SERVER_INFO_MAP_KEY, map);

        redisCache.setCacheObject(CacheKey.SERVER_INFO_KEY, serverInfos, 3, TimeUnit.DAYS);

        // 服务器信息缓存更新时间
        redisCache.setCacheObject(CacheKey.SERVER_INFO_UPDATE_TIME_KEY, DateUtils.getNowDate());
    }

    @FunctionalInterface
    interface CommandReplacer {
        String replace(String command);
    }
}


