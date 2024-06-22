package com.ruoyi.server.common;

import com.github.t9t.minecraftrconclient.RconClient;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.server.domain.ServerCommandInfo;
import com.ruoyi.server.domain.ServerInfo;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rcon发送命令工具类
 * 作者：Memory
 */

public class RconUtil {
    // 创建LOG对象
    private static final Log log = LogFactory.getLog(RconUtil.class);

    public static Map<String, ServerCommandInfo> COMMAND_INFO = new HashMap<>();

    private static final RedisCache redisCache = new RedisCache();

    /**
     * 获取指令信息
     */
    // static {
    //     // 从Redis缓存读取指令信息
    //     COMMAND_INFO = ObjectCache.getCommandInfo();
    // }

    /**
     * 发送Rcon命令
     *
     * @param key
     * @param command
     */
    public static void sendCommand(String key, String command) {

        if (key == null || command == null) {
            log.error("发送Rcon命令失败：key或command为空");
            return;
        }

        // 冗余策略：Map缓存
        if (MapCache.isEmpty()) {
            // 从Redis缓存读取服务器信息
            List<ServerInfo> serverInfo = redisCache.getCacheObject("serverInfo");
            // 初始化Rcon连接
            for (ServerInfo info : serverInfo) {
                if (info.getStatus() != 1L) {
                    continue;
                }
                init(info, log);
            }
        }

        if (!key.equals("all")) {
            // 判断是否为EasyAuth指令
            // System.err.println(COMMAND_INFO.get(key).toString());
            if (command.contains("addToForcedOffline") && !COMMAND_INFO.get(key).getEasyauth().equals("1")) {
                log.error("服务器" + key + "发送Rcon命令失败：该服务器未开启EasyAuth");
                return;
            }

            // 发送Rcon命令
            try {
                // 从Map缓存中获取RconClient
                RconClient client = MapCache.get(key);
                client.sendCommand(command);
                log.debug("服务器" + key + "发送Rcon命令：" + command);
            } catch (Exception e) {
                log.error("发送Rcon命令失败：" + command);
                log.error("失败原因：" + e.getMessage());
                log.error("尝试重连Rcon：" + key);
                reconnect(key);
            }
        } else {
            // 发送Rcon命令给所有服务器
            MapCache.getMap().values().forEach(client -> client.sendCommand(command));
        }
    }

    /**
     * 初始化Rcon连接
     *
     * @param info
     * @param log
     */
    public static void init(ServerInfo info, Log log) {
        try {
            log.debug("初始化Rcon连接：" + info.getNameTag());
            MapCache.put(info.getId().toString(), RconClient.open(DomainToIp.domainToIp(info.getIp()), info.getRconPort().intValue(), info.getRconPassword()));
            log.debug("初始化Rcon连接成功：" + info.getNameTag());
        } catch (Exception e) {
            log.error("初始化Rcon连接失败：" + info.getNameTag() + " " + info.getIp() + " " + info.getRconPort() + " " + info.getRconPassword());
            log.error("失败原因：" + e.getMessage());
        }
    }

    /**
     * 重连Rcon
     *
     * @param key
     */
    public static void reconnect(String key) {
        if (key == null) {
            log.error("重连Rcon失败：key为空");
            return;
        }
        // 从Redis缓存读取服务器信息
        List<ServerInfo> serverInfo = redisCache.getCacheObject("serverInfo");
        // 重连Rcon
        for (ServerInfo info : serverInfo) {
            if (info.getId().toString().equals(key)) {
                try {
                    log.debug("重连Rcon：" + info.getNameTag());
                    MapCache.put(info.getId().toString(), RconClient.open(DomainToIp.domainToIp(info.getIp()), info.getRconPort().intValue(), info.getRconPassword()));
                    log.debug("重连Rcon成功：" + info.getNameTag());
                } catch (Exception e) {
                    log.error("重连Rcon失败：" + info.getNameTag() + " " + info.getIp() + " " + info.getRconPort() + " " + info.getRconPassword());
                    log.error("失败原因：" + e.getMessage());
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
            log.error("关闭Rcon失败：key为空");
            return;
        }
        // 从Map缓存中获取RconClient
        RconClient client = MapCache.get(key);
        if (client != null) {
            client.close();
            MapCache.remove(key);
            log.debug("关闭Rcon：" + key);
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

        if (command == null) {
            log.error("替换Rcon命令失败：command为空");
            return key;
        }

        if (COMMAND_INFO != null && COMMAND_INFO.containsKey(key)) {
            // 从缓存中获取指令信息
            ServerCommandInfo info = COMMAND_INFO.get(key);
            // System.err.println(COMMAND_INFO);

            // 替换Rcon命令
            if (command.startsWith("white_add")) {
                command = onlineFlag ? info.getOnlineAddWhitelistCommand().replace("{player}", command.substring(10))
                        : info.getOfflineAddWhitelistCommand().replace("{player}", command.substring(10));
            } else if (command.startsWith("white_remove")) {
                command = onlineFlag ? info.getOnlineRmWhitelistCommand().replace("{player}", command.substring(13))
                        : info.getOfflineRmWhitelistCommand().replace("{player}", command.substring(13));
            } else if (command.startsWith("ban_add")) {
                command = onlineFlag ? info.getOnlineAddBanCommand().replace("{player}", command.substring(8))
                        : info.getOfflineAddBanCommand().replace("{player}", command.substring(8));
            } else if (command.startsWith("ban_remove")) {
                command = onlineFlag ? info.getOnlineRmBanCommand().replace("{player}", command.substring(11))
                        : info.getOfflineRmBanCommand().replace("{player}", command.substring(11));
            }

            log.debug("替换Rcon命令成功：" + key + " " + command);
        } else {
            log.error("替换Rcon命令失败：指令信息为空");
            throw new RuntimeException("指令信息为空");
        }
        return command;
    }
}


