package com.ruoyi.quartz.task;

import com.github.t9t.minecraftrconclient.RconClient;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.server.common.DomainToIp;
import com.ruoyi.server.common.MapCache;
import com.ruoyi.server.common.RconUtil;
import com.ruoyi.server.domain.ServerInfo;
import com.ruoyi.server.service.IServerInfoService;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 定时任务调度
 * 作者：Memory
 */
@Component("rconTask")
public class RconTask {
    private static final Log log = LogFactory.getLog(RconTask.class);
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private IServerInfoService serverInfoService;

    // 定时刷新Redis缓存
    public void refreshRedisCache() {
        redisCache.deleteObject("serverInfo");
        // 服务器信息缓存
        redisCache.setCacheObject("serverInfo", serverInfoService.selectServerInfoList(new ServerInfo()), 1, TimeUnit.DAYS);
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
            try {
                log.debug("初始化Rcon连接：" + serverInfo.getNameTag());
                MapCache.put(serverInfo.getId().toString(), RconClient.open(DomainToIp.domainToIp(serverInfo.getIp()), serverInfo.getRconPort().intValue(), serverInfo.getRconPassword()));
                // System.out.println(MapCache.getMap().toString());
                log.debug("初始化Rcon连接成功：" + serverInfo.getNameTag());
            } catch (Exception e) {
                log.error("初始化Rcon连接失败：" + serverInfo.getNameTag() + " " + serverInfo.getIp() + " " + serverInfo.getRconPort() + " " + serverInfo.getRconPassword());
                log.error("失败原因：" + e.getMessage());
            }
            // System.out.println(MapCache.getMap());
        }
        // 发送广播
        RconUtil.sendCommand("all", "say Rcon Connect Refresh " + DateUtils.getTime());
    }
}
