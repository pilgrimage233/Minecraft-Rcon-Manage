package com.ruoyi.server.common.service;

import com.github.t9t.minecraftrconclient.RconClient;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.server.common.DomainToIp;
import com.ruoyi.server.common.MapCache;
import com.ruoyi.server.common.constant.Command;
import com.ruoyi.server.common.constant.RconMsg;
import com.ruoyi.server.domain.server.ServerCommandInfo;
import com.ruoyi.server.domain.server.ServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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

    /**
     * 发送Rcon命令
     *
     * @param key
     * @param command
     * @param onlineFlag
     */
    public void sendCommand(String key, String command, boolean onlineFlag) {
        int maxRetries = 5;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                if (key.contains("all")) {
                    MapCache.getMap().forEach((k, client) -> {
                        final String replaced = replaceCommand(k, command, onlineFlag);
                        CompletableFuture.runAsync(() -> {
                            client.sendCommand(replaced);
                        });
                    });
                } else {
                    if (MapCache.get(key) == null) {
                        throw new RuntimeException("RconClient not found for key: " + key);
                    }
                    final String replaced = replaceCommand(key, command, onlineFlag);
                    CompletableFuture.runAsync(() -> {
                        MapCache.get(key).sendCommand(replaced);
                    }).get(3, TimeUnit.SECONDS);
                }
                log.debug(RconMsg.SEND_COMMAND + "{}", command);
                return;
            } catch (Exception e) {
                retryCount++;
                log.warn("发送命令失败，第{}次重试: {}", retryCount, e.getMessage());
                // e.printStackTrace();
                if (retryCount >= maxRetries) {
                    log.error("发送命令最终失败: {}", e.getMessage());
                    // 重连并回调
                    if (reconnect(key)) {
                        log.debug("重连成功，重新发送命令: {}", command);
                        this.sendCommand(key, command, onlineFlag);
                    } else {
                        log.error("重连失败，无法发送命令: {}", command);
                        Map<String, Object> cache = new HashMap<>();
                        // 命令缓存
                        if (redisCache.hasKey("commandCache")) {
                            cache = redisCache.getCacheObject("commandCache");
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
                        redisCache.setCacheObject("commandCache", cache);
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
        AtomicReference<RconClient> client = new AtomicReference<>();
        try {
            // 使用异步线程初始化Rcon连接，超时时间为5秒
            CompletableFuture.runAsync(() -> {
                try {
                    client.set(RconClient.open(DomainToIp.domainToIp(info.getIp()), info.getRconPort().intValue(), info.getRconPassword()));
                    log.debug(RconMsg.INIT_RCON + "{}", info.getNameTag());
                } catch (Exception e) {
                    log.error(RconMsg.CONNECT_ERROR + "{} {} {} {}", info.getNameTag(), info.getIp(), info.getRconPort(), info.getRconPassword());
                    log.error(RconMsg.ERROR_MSG + "{}", e.getMessage());
                }
            }).get(3, TimeUnit.SECONDS);

            if (client.get() == null) {
                throw new RuntimeException("RconClient is null");
            }

            MapCache.put(info.getId().toString(), client.get());
            log.debug(RconMsg.CONNECT_SUCCESS + "{}", info.getNameTag());

            // 清除错误次数
            if (redisCache.hasKey("errorCount")) {
                redisCache.deleteObject("errorCount");
            }
            return true;
        } catch (Exception e) {
            // 记录错误次数
            if (redisCache.hasKey("errorCount")) {
                final Integer errorCount = redisCache.getCacheObject("errorCount");
                redisCache.setCacheObject("errorCount", errorCount + 1);
            } else {
                redisCache.setCacheObject("errorCount", 1);
            }
            if ((Integer) redisCache.getCacheObject("errorCount") >= 10 && (Integer) redisCache.getCacheObject("errorCount") % 10 == 0) {
                try {
                    emailService.push(ADMIN_EMAIL, "服务器异常", "服务器异常，请检查服务器连接状态，告警时间：" + DateUtils.getTime() + "\n错误次数：" + redisCache.getCacheObject("errorCount"));
                } catch (ExecutionException | InterruptedException ex) {
                    log.error("邮件发送失败: {}", ex.getMessage());
                }
            }
            log.error(RconMsg.CONNECT_ERROR + "{} {} {} {}", info.getNameTag(), info.getIp(), info.getRconPort(), info.getRconPassword());
            log.error(RconMsg.ERROR_MSG + "{}", e.getMessage());
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
            serverInfo = redisCache.getCacheObject("serverInfo");
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
    public String replaceCommand(String key, String command, boolean onlineFlag) {
        if (StringUtils.isEmpty(command)) {
            log.error("替换命令失败：command为空");
            return key;
        }

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

        boolean isMatch = false;
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

    @FunctionalInterface
    interface CommandReplacer {
        String replace(String command);
    }
}


