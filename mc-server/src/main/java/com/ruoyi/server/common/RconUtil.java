package com.ruoyi.server.common;

import com.github.t9t.minecraftrconclient.RconClient;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.server.common.constant.RconMsg;
import com.ruoyi.server.common.constant.WhiteListCommand;
import com.ruoyi.server.domain.ServerCommandInfo;
import com.ruoyi.server.domain.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Rcon发送命令工具类
 * 作者：Memory
 */

public class RconUtil {
    // 使用 SLF4J 的日志实现
    private static final Logger log = LoggerFactory.getLogger(RconUtil.class);

    public static Map<String, ServerCommandInfo> COMMAND_INFO = new ConcurrentHashMap<>();

    private static final RedisCache redisCache = new RedisCache();

    /**
     * 发送Rcon命令
     *
     * @param key
     * @param command
     */
    public static void sendCommand(String key, String command) {
        int maxRetries = 5;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                if (key.contains("all")) {
                    MapCache.getMap().forEach((k, client) -> {
                        CompletableFuture.runAsync(() -> {
                            client.sendCommand(command);
                        });
                    });
                } else {
                    if (MapCache.get(key) == null) {
                        throw new RuntimeException("RconClient not found for key: " + key);
                    }
                    CompletableFuture.runAsync(() -> {
                        MapCache.get(key).sendCommand(command);
                    }).get(5, TimeUnit.SECONDS);
                }

                log.debug(RconMsg.SEND_COMMAND + "{}", command);
                return;
            } catch (Exception e) {
                retryCount++;
                log.warn("发送命令失败，第{}次重试: {}", retryCount, e.getMessage());
                if (retryCount >= maxRetries) {
                    log.error("发送命令最终失败: {}", e.getMessage());
                    reconnect(key);
                }
                try {
                    Thread.sleep(1000L * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * 初始化Rcon连接
     *
     * @param info
     */
    public static void init(ServerInfo info) {
        try {
            log.debug(RconMsg.INIT_RCON + info.getNameTag());
            MapCache.put(info.getId().toString(), RconClient.open(DomainToIp.domainToIp(info.getIp()), info.getRconPort().intValue(), info.getRconPassword()));
            log.debug(RconMsg.RECONNECT_SUCCESS + info.getNameTag());
        } catch (Exception e) {
            log.error(RconMsg.RECONNECT_ERROR + info.getNameTag() + " " + info.getIp() + " " + info.getRconPort() + " " + info.getRconPassword());
            log.error(RconMsg.ERROR_MSG + e.getMessage());
        }
    }

    /**
     * 重连Rcon
     *
     * @param key
     */
    public static void reconnect(String key) {
        if (key == null) {
            log.error(RconMsg.RECONNECT_ERROR);
            return;
        }
        List<ServerInfo> serverInfo = null;

        try {
            // 从Redis缓存读取服务器信息
            serverInfo = redisCache.getCacheObject("serverInfo");
        } catch (Exception e) {
            log.error(RconMsg.ERROR_MSG + e.getMessage());
            return;
        }

        // 重连Rcon
        for (ServerInfo info : serverInfo) {
            if (info.getId().toString().equals(key)) {
                try {
                    log.debug(RconMsg.TRY_RECONNECT + info.getNameTag());
                    MapCache.put(info.getId().toString(), RconClient.open(DomainToIp.domainToIp(info.getIp()), info.getRconPort().intValue(), info.getRconPassword()));
                    log.debug(RconMsg.RECONNECT_SUCCESS + info.getNameTag());
                } catch (Exception e) {
                    log.error(RconMsg.RECONNECT_ERROR + info.getNameTag() + " " + info.getIp() + " " + info.getRconPort() + " " + info.getRconPassword());
                    log.error(RconMsg.ERROR_MSG + e.getMessage());
                }
            }
        }
    }

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

        try (RconClient client = MapCache.get(key)) {
            if (client != null) {
                MapCache.remove(key);
                log.debug(RconMsg.TURN_OFF_RCON + "{}", key);
            }
        } catch (Exception e) {
            log.error("关闭 Rcon 连接失败: {}", e.getMessage());
        }
    }

    /**
     * 替换Rcon命令
     *
     * @param key
     * @param command
     * @param onlineFlag
     * @return 替换后的Rcon命令
     */
    public static String replaceCommand(String key, String command, boolean onlineFlag) {
        if (StringUtils.isEmpty(command)) {
            log.error("替换Rcon命令失败：command为空");
            return key;
        }

        ServerCommandInfo info = COMMAND_INFO.get(key);
        if (info == null) {
            log.error("替换Rcon命令失败：指令信息为空");
            throw new RuntimeException("指令信息为空");
        }

        // 使用 Map 存储命令映射关系
        Map<String, CommandReplacer> commandMap = new HashMap<>();
        commandMap.put(WhiteListCommand.WHITELIST_ADD_COMMAND,
                (cmd) -> onlineFlag ? info.getOnlineAddWhitelistCommand() : info.getOfflineAddWhitelistCommand());
        commandMap.put(WhiteListCommand.WHITELIST_REMOVE_COMMAND,
                (cmd) -> onlineFlag ? info.getOnlineRmWhitelistCommand() : info.getOfflineRmWhitelistCommand());
        commandMap.put(WhiteListCommand.BAN_ADD_COMMAND,
                (cmd) -> onlineFlag ? info.getOnlineAddBanCommand() : info.getOfflineRmBanCommand());
        commandMap.put(WhiteListCommand.BAN_REMOVE_COMMAND,
                (cmd) -> onlineFlag ? info.getOnlineRmBanCommand() : info.getOfflineRmBanCommand());

        // 查找匹配的命令并替换
        for (Map.Entry<String, CommandReplacer> entry : commandMap.entrySet()) {
            if (command.startsWith(entry.getKey())) {
                String player = command.substring(entry.getKey().length());
                command = entry.getValue().replace(command).replace("{player}", player);
                break;
            }
        }

        log.debug("替换Rcon命令成功：{} {}", key, command);
        return command;
    }

    @FunctionalInterface
    interface CommandReplacer {
        String replace(String command);
    }
}


