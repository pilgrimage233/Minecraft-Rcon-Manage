package com.ruoyi.server.common;

import com.github.t9t.minecraftrconclient.RconClient;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.server.common.constant.RconMsg;
import com.ruoyi.server.common.constant.WhiteListCommand;
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

        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(command)) {
            log.error(RconMsg.MAIN_INFO_EMPTY);
            return;
        }

        // 冗余策略：Map缓存
        if (MapCache.isEmpty()) {
            List<ServerInfo> serverInfo = redisCache.getCacheObject("serverInfo");
            for (ServerInfo info : serverInfo) {
                if (info.getStatus() != 1L) {
                    continue;
                }
                init(info);
            }
        }

        if (!key.equalsIgnoreCase("all")) {
            // 判断是否为EasyAuth指令
            if (command.contains("addToForcedOffline")) {
                if (COMMAND_INFO.get(key).getEasyauth() == null || !COMMAND_INFO.get(key).getEasyauth().equals("1")) {
                    log.error(RconMsg.NO_EASY_AUTH_MOD);
                    return;
                }
            }

            // 发送Rcon命令
            try {
                // 从Map缓存中获取RconClient
                RconClient client = MapCache.get(key);
                client.sendCommand(command);
                log.debug(RconMsg.SEND_COMMAND + command);
            } catch (Exception e) {
                log.error(RconMsg.ERROR + command);
                log.error(RconMsg.ERROR_MSG + e.getMessage());
                log.error(RconMsg.TRY_RECONNECT + key);
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
        // 从Map缓存中获取RconClient
        RconClient client = MapCache.get(key);
        if (client != null) {
            client.close();
            MapCache.remove(key);
            log.debug(RconMsg.TURN_OFF_RCON + key);
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
            if (command.startsWith(WhiteListCommand.WHITELIST_ADD_COMMAND)) {
                command = onlineFlag ? info.getOnlineAddWhitelistCommand().replace("{player}", command.substring(14))
                        : info.getOfflineAddWhitelistCommand().replace("{player}", command.substring(14));
            } else if (command.startsWith(WhiteListCommand.WHITELIST_REMOVE_COMMAND)) {
                command = onlineFlag ? info.getOnlineRmWhitelistCommand().replace("{player}", command.substring(17))
                        : info.getOfflineRmWhitelistCommand().replace("{player}", command.substring(17));
            } else if (command.startsWith(WhiteListCommand.BAN_ADD_COMMAND)) {
                command = onlineFlag ? info.getOnlineAddBanCommand().replace("{player}", command.substring(4))
                        : info.getOfflineAddBanCommand().replace("{player}", command.substring(4));
            } else if (command.startsWith(WhiteListCommand.BAN_REMOVE_COMMAND)) {
                command = onlineFlag ? info.getOnlineRmBanCommand().replace("{player}", command.substring(7))
                        : info.getOfflineRmBanCommand().replace("{player}", command.substring(7));
            }

            log.debug("替换Rcon命令成功：" + key + " " + command);
        } else {
            log.error("替换Rcon命令失败：指令信息为空");
            throw new RuntimeException("指令信息为空");
        }
        return command;
    }
}


