package com.ruoyi.quartz.task;

import com.github.t9t.minecraftrconclient.RconClient;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.server.common.MapCache;
import com.ruoyi.server.common.RconService;
import com.ruoyi.server.common.constant.WhiteListCommand;
import com.ruoyi.server.domain.ServerInfo;
import com.ruoyi.server.service.IServerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 定时任务调度
 * 作者：Memory
 */
@Slf4j
@Component("rconTask")
public class RconTask {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IServerInfoService serverInfoService;

    @Autowired
    private RconService rconService;

    @Value("${server.port}")
    private String port;


    // 定时刷新Redis缓存
    public void refreshRedisCache() {
        redisCache.deleteObject("serverInfo");
        // 服务器信息缓存
        redisCache.setCacheObject("serverInfo", serverInfoService.selectServerInfoList(new ServerInfo()), 3, TimeUnit.DAYS);
        // 服务器信息缓存更新时间
        redisCache.setCacheObject("serverInfoUpdateTime", DateUtils.getNowDate());
    }

    // 定时刷新Map缓存
    public void refreshMapCache() {
        //关闭所有Rcon连接并清除Map缓存
        for (RconClient rconClient : MapCache.getMap().values()) {
            rconClient.close();
        }
        MapCache.clear();
        // 初始化Rcon连接
        ServerInfo info = new ServerInfo();
        info.setStatus(1L);

        for (ServerInfo serverInfo : serverInfoService.selectServerInfoList(info)) {
            rconService.init(serverInfo);
        }

        // 发送广播
        rconService.sendCommand("all", "say Rcon Connect Refresh " + DateUtils.getTime(), false);
    }

    // 心跳检测
    public void heartBeat() {
        if (MapCache.getMap().isEmpty()) {
            return;
        }
        if (!redisCache.hasKey("serverInfo")) {
            refreshRedisCache();
        }

        MapCache.getMap().forEach((k, v) -> {
            try {
                final String list = v.sendCommand("ping");
                if (StringUtils.isEmpty(list)) {
                    throw new Exception("Rcon连接异常");
                }
            } catch (Exception e) {
                log.error("Rcon连接异常：{}...尝试重连", e.getMessage());
                v.close();
                MapCache.remove(k);
                final ServerInfo serverInfo = serverInfoService.selectServerInfoById(Long.parseLong(k));

                // 重连单个服务器
                rconService.init(serverInfo);

                // 重连广播
                rconService.sendCommand(k, "say Rcon Reconnect " + serverInfo.getNameTag() + " " + DateUtils.getTime(), false);
            }
        });
    }
}
