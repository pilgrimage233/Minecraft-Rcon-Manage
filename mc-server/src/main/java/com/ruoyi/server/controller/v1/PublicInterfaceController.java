package com.ruoyi.server.controller.v1;

import com.google.common.util.concurrent.RateLimiter;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.server.common.MapCache;
import com.ruoyi.server.service.IServerInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 公共接口
 * 用于提供一些公共的接口
 * 例如: 聚合查询, 获取白名单列表等
 */
@RestController
@RequestMapping("/api/v1")
public class PublicInterfaceController extends BaseController {

    private static final long CACHE_DURATION = 300 * 1000; // 缓存时间5分钟

    private final RateLimiter rateLimiter = RateLimiter.create(10.0); // 每秒最多10个请求

    private final Map<String, CachedWhitelist> whitelistCache = new ConcurrentHashMap<>();

    @Autowired
    private IServerInfoService serverInfoService;

    /**
     * 聚合查询
     *
     * @return AjaxResult
     */
    @GetMapping("/aggregateQuery")
    public AjaxResult aggregateQuery() {
        return success(serverInfoService.aggregateQuery());
    }

    @GetMapping("getWhiteList")
    @CrossOrigin(origins = "https://app.yousb.sbs", maxAge = 3600)
    public AjaxResult getWhiteList() {
        // 限流检查
        if (!rateLimiter.tryAcquire()) {
            return error("服务器繁忙,请稍后再试");
        }

        try {
            // 检查缓存
            CachedWhitelist cached = whitelistCache.get("whitelist");
            if (cached != null && !cached.isExpired()) {
                return success(cached.data);
            }

            Map<String, String> map = new HashMap<>();
            MapCache.getMap().forEach((k, v) -> {
                final String nameTag = serverInfoService.selectServerInfoById(Long.valueOf(k)).getNameTag();
                try {
                    final String list = v.sendCommand("whitelist list");
                    String[] split = new String[0];
                    if (StringUtils.isNotEmpty(list) && list.contains("There are")) {
                        split = list.split("whitelisted player\\(s\\):")[1].trim().split(", ");
                    }
                    map.put(nameTag, Arrays.toString(split));
                } catch (Exception e) {
                    logger.error("获取白名单列表失败, serverId: {}", k, e);
                    // map.put(nameTag, "获取失败"); // 不要因为单个服务器失败影响整体
                }
            });

            // 更新缓存
            if (!map.isEmpty()) {
                logger.info("更新白名单列表缓存");
                whitelistCache.put("whitelist", new CachedWhitelist(map));
            }
            return success(map);

        } catch (Exception e) {
            logger.error("获取白名单列表发生异常", e);
            return error("系统繁忙,请稍后重试");
        }
    }


    // 添加缓存对象类
    private static class CachedWhitelist {
        private final Map<String, String> data;
        private final long timestamp;

        public CachedWhitelist(Map<String, String> data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }

}
