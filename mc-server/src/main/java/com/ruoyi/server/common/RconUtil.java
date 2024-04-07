package com.ruoyi.server.common;

import com.github.t9t.minecraftrconclient.RconClient;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.server.domain.ServerInfo;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.util.List;

/**
 * Rcon发送命令工具类
 * 作者：Memory
 */

public class RconUtil {
    // 创建LOG对象
    private static final Log log = LogFactory.getLog(RconUtil.class);

    private static final RedisCache redisCache = new RedisCache();

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
                try {
                    log.debug("初始化Rcon连接：" + info.getNameTag());
                    MapCache.put(info.getId().toString(), RconClient.open(DomainToIp.domainToIp(info.getIp()), info.getRconPort().intValue(), info.getRconPassword()));
                    log.debug("初始化Rcon连接成功：" + info.getNameTag());
                } catch (Exception e) {
                    log.error("初始化Rcon连接失败：" + info.getNameTag() + " " + info.getIp() + " " + info.getRconPort() + " " + info.getRconPassword());
                    log.error("失败原因：" + e.getMessage());
                }
            }
        }
        if (!key.equals("all")) {
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
            for (RconClient client : MapCache.getMap().values()) {
                try {
                    client.sendCommand(command);
                    log.debug("服务器" + key + "发送Rcon命令：" + command);
                } catch (Exception e) {
                    log.error("发送Rcon命令失败：" + command);
                    log.error("失败原因：" + e.getMessage());
                    log.error("尝试重连Rcon：" + key);
                    reconnect(key);
                }
            }
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

}


