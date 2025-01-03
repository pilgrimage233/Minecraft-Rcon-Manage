package com.ruoyi.server.common;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.server.async.AsyncManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Slf4j
@Component
public class RunScript {

    @Autowired
    private RedisCache redisCache;

    @PreDestroy
    public void end() {
        // 程序关闭时断开所有Rcon连接
        MapCache.getMap().forEach((k, v) -> {
            try {
                v.close();
            } catch (Exception e) {
                log.error("Rcon连接关闭失败");
            }
        });
        // 关闭线程池
        AsyncManager.getInstance().shutdown();

        // 清除缓存
        if (redisCache.deleteObject("serverInfo")) {
            log.info("清除服务器信息缓存成功");
        } else {
            log.error("清除服务器信息缓存失败");
        }

        if (redisCache.deleteObject("serverInfoUpdateTime")) {
            log.info("清除服务器信息缓存更新时间成功");
        } else {
            log.error("清除服务器信息缓存更新时间失败");
        }

      /*  if (redisCache.deleteObject("onlinePlayer")) {
            log.info("清除在线玩家缓存成功");
        } else {
            log.error("清除在线玩家缓存失败");
        }*/
    }
}
