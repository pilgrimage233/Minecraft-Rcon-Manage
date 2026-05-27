package cc.endmc.server.common;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.rconclient.RconClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

@Slf4j
@Component
public class RunScript {

    @Autowired
    private RedisCache redisCache;

    @PreDestroy
    public void end() {
        // 程序关闭时断开所有Rcon连接
        RconCache.getMap().forEach((k, v) -> {
            try {
                v.close();
            } catch (Exception e) {
                log.error("Rcon连接关闭失败");
            }
        });
        // 关闭RconClient共享线程池
        RconClient.shutdownSharedExecutor();

        // 清除缓存
        if (redisCache.deleteObject(CacheKey.SERVER_INFO_KEY)) {
            log.info("清除服务器信息缓存成功");
        } else {
            log.error("清除服务器信息缓存失败");
        }

        if (redisCache.deleteObject(CacheKey.SERVER_INFO_UPDATE_TIME_KEY)) {
            log.info("清除服务器信息缓存更新时间成功");
        } else {
            log.error("清除服务器信息缓存更新时间失败");
        }
    }
}
