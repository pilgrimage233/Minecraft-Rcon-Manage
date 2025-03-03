package com.ruoyi.server.common;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.server.common.constant.CacheKey;
import com.ruoyi.server.common.constant.RconMsg;
import com.ruoyi.server.common.service.RconService;
import com.ruoyi.server.domain.server.ServerInfo;
import com.ruoyi.server.service.server.IServerCommandInfoService;
import com.ruoyi.server.service.server.IServerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class InitializingBeanExamplebBean implements InitializingBean {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IServerInfoService serverInfoService;

    @Autowired
    private IServerCommandInfoService commandInfoService;

    @Autowired
    private RconService rconService;

    /**
     * InitializingBean afterPropertiesSet
     * 在bean初始化后执行一些操作
     * 例如：初始化服务器信息缓存、Rcon连接
     * 作者：Memory
     */
    @Override
    public void afterPropertiesSet() {
        log.debug("InitializingBean afterPropertiesSet begin...");
        // 判断Redis缓存是否存在
        // if (redisCache.hasKey("serverInfo") && redisCache.hasKey("serverInfoUpdateTime")) {
        //     // 判断上次缓存时间是否超过一天
        //     if (DateUtils.getNowDate().getTime() - ((Date) redisCache.getCacheObject("serverInfoUpdateTime")).getTime() < 86400000) {
        //         log.debug("服务器信息缓存存在且未过期");
        //         return;
        //     }
        // }

        // 服务器信息缓存
        final List<ServerInfo> serverInfos = serverInfoService.selectServerInfoList(new ServerInfo());
        if (serverInfos == null || serverInfos.isEmpty()) {
            log.error(RconMsg.SERVER_EMPTY);
        }

        redisCache.setCacheObject(CacheKey.SERVER_INFO_KEY, serverInfos, 3, TimeUnit.DAYS);

        // 服务器信息缓存更新时间
        redisCache.setCacheObject(CacheKey.SERVER_INFO_UPDATE_TIME_KEY, DateUtils.getNowDate());

        // 初始化缓存服务器指令
        commandInfoService.initServerCommandInfo();

        RconService.COMMAND_INFO = ObjectCache.getCommandInfo();

        // 初始化Rcon连接
        ServerInfo info = new ServerInfo();
        info.setStatus(1L);
        for (ServerInfo serverInfo : serverInfoService.selectServerInfoList(info)) {
            rconService.init(serverInfo);
        }

        // Thread.sleep(5000);

        // 发送广播
        rconService.sendCommand("all", "say Rcon ready! Time: " + DateUtils.getNowDate(), false);

        log.debug("InitializingBean afterPropertiesSet end...");

    }
}
