package com.ruoyi.server.common;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.server.common.constant.RconMsg;
import com.ruoyi.server.domain.ServerInfo;
import com.ruoyi.server.service.IServerCommandInfoService;
import com.ruoyi.server.service.IServerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
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
    public void afterPropertiesSet() throws ExecutionException, InterruptedException {
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

        redisCache.setCacheObject("serverInfo", serverInfos, 3, TimeUnit.DAYS);

        // 服务器信息缓存更新时间
        redisCache.setCacheObject("serverInfoUpdateTime", DateUtils.getNowDate());

        // 初始化Rcon连接
        ServerInfo info = new ServerInfo();
        info.setStatus(1L);
        for (ServerInfo serverInfo : serverInfoService.selectServerInfoList(info)) {
            rconService.init(serverInfo);
        }
        log.debug("InitializingBean afterPropertiesSet end...");

        // 发送广播
        rconService.sendCommand("all", "say Rcon ready! Time: " + DateUtils.getNowDate(), false);

        // 初始化缓存服务器指令
        commandInfoService.initServerCommandInfo();

    }


}
