package cc.endmc.quartz.task;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.common.MapCache;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.rconclient.RconClient;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.service.server.IServerInfoService;
import lombok.extern.slf4j.Slf4j;
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
        redisCache.deleteObject(CacheKey.SERVER_INFO_KEY);
        // 服务器信息缓存
        redisCache.setCacheObject(CacheKey.SERVER_INFO_KEY, serverInfoService.selectServerInfoList(new ServerInfo()), 3, TimeUnit.DAYS);
        // 服务器信息缓存更新时间
        redisCache.setCacheObject(CacheKey.SERVER_INFO_UPDATE_TIME_KEY, DateUtils.getNowDate());
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
        if (!redisCache.hasKey(CacheKey.SERVER_INFO_KEY)) {
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
