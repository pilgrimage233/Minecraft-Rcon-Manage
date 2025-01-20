package com.ruoyi.server.common;

import com.github.t9t.minecraftrconclient.RconClient;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.server.common.constant.Command;
import com.ruoyi.server.common.constant.RconMsg;
import com.ruoyi.server.domain.ServerCommandInfo;
import com.ruoyi.server.domain.ServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Rcon发送命令工具类
 * 作者：Memory
 */
@Slf4j
@Component
public class RconService {

    public Map<String, ServerCommandInfo> COMMAND_INFO = ObjectCache.getCommandInfo();
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
                        CompletableFuture.runAsync(() -> {
                            client.sendCommand(replaceCommand(k, command, onlineFlag));
                        });
                    });
                } else {
                    if (MapCache.get(key) == null) {
                        throw new RuntimeException("RconClient not found for key: " + key);
                    }
                    CompletableFuture.runAsync(() -> {
                        MapCache.get(key).sendCommand(replaceCommand(key, command, onlineFlag));
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
    public void init(ServerInfo info) {
        try {
            log.debug(RconMsg.INIT_RCON + "{}", info.getNameTag());
            MapCache.put(info.getId().toString(), RconClient.open(DomainToIp.domainToIp(info.getIp()), info.getRconPort().intValue(), info.getRconPassword()));
            log.debug(RconMsg.RECONNECT_SUCCESS + "{}", info.getNameTag());

            // 清除错误次数
            if (redisCache.hasKey("errorCount")) {
                redisCache.deleteObject("errorCount");
            }
        } catch (Exception e) {
            // 记录错误次数
            if (redisCache.hasKey("errorCount")) {
                final Integer errorCount = redisCache.getCacheObject("errorCount");
                redisCache.setCacheObject("errorCount", errorCount + 1);
            } else {
                redisCache.setCacheObject("errorCount", 1);
            }
            if ((Integer) redisCache.getCacheObject("errorCount") >= 10 && (Integer) redisCache.getCacheObject("errorCount") % 10 == 0) {
                // 创建反射调用EmailService
                try {
                    emailService.push(ADMIN_EMAIL, "服务器异常", "服务器异常，请检查服务器连接状态，告警时间：" + DateUtils.getTime() + "\n错误次数：" + redisCache.getCacheObject("errorCount"));
                } catch (ExecutionException | InterruptedException ex) {
                    log.error("邮件发送失败: {}", ex.getMessage());
                }
            }
            log.error(RconMsg.RECONNECT_ERROR + "{} {} {} {}", info.getNameTag(), info.getIp(), info.getRconPort(), info.getRconPassword());
            log.error(RconMsg.ERROR_MSG + "{}", e.getMessage());
        }
    }

    /**
     * 重连Rcon
     *
     * @param key
     */
    public void reconnect(String key) {
        if (key == null) {
            log.error(RconMsg.RECONNECT_ERROR);
            return;
        }
        List<ServerInfo> serverInfo = null;

        try {
            // 从Redis缓存读取服务器信息
            serverInfo = redisCache.getCacheObject("serverInfo");
        } catch (Exception e) {
            log.error(RconMsg.ERROR_MSG + "{}", e.getMessage());
            return;
        }

        // 重连Rcon
        for (ServerInfo info : serverInfo) {
            if (info.getId().toString().equals(key)) {
                try {
                    log.debug(RconMsg.TRY_RECONNECT + "{}", info.getNameTag());
                    MapCache.put(info.getId().toString(), RconClient.open(DomainToIp.domainToIp(info.getIp()), info.getRconPort().intValue(), info.getRconPassword()));
                    log.debug(RconMsg.RECONNECT_SUCCESS + "{}", info.getNameTag());
                } catch (Exception e) {
                    log.error(RconMsg.RECONNECT_ERROR + "{} {} {} {}", info.getNameTag(), info.getIp(), info.getRconPort(), info.getRconPassword());
                    log.error(RconMsg.ERROR_MSG + "{}", e.getMessage());
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
    public String replaceCommand(String key, String command, boolean onlineFlag) {
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
        commandMap.put(Command.WHITELIST_ADD_COMMAND,
                (cmd) -> onlineFlag ? info.getOnlineAddWhitelistCommand() : info.getOfflineAddWhitelistCommand());
        commandMap.put(Command.WHITELIST_REMOVE_COMMAND,
                (cmd) -> onlineFlag ? info.getOnlineRmWhitelistCommand() : info.getOfflineRmWhitelistCommand());
        commandMap.put(Command.BAN_ADD_COMMAND,
                (cmd) -> onlineFlag ? info.getOnlineAddBanCommand() : info.getOfflineRmBanCommand());
        commandMap.put(Command.BAN_REMOVE_COMMAND,
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


