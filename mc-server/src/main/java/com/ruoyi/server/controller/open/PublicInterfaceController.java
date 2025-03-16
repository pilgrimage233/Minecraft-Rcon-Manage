package com.ruoyi.server.controller.open;

import com.google.common.util.concurrent.RateLimiter;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.server.common.MapCache;
import com.ruoyi.server.common.constant.CacheKey;
import com.ruoyi.server.domain.permission.WhitelistInfo;
import com.ruoyi.server.service.permission.IWhitelistInfoService;
import com.ruoyi.server.service.server.IServerInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 公共接口
 * 用于提供一些公共的接口
 * 例如: 聚合查询, 获取白名单列表等
 */
@RestController
@RequestMapping("/api/v1")
public class PublicInterfaceController extends BaseController {

    private final RateLimiter rateLimiter = RateLimiter.create(10.0); // 每秒最多10个请求

    @Autowired
    private IServerInfoService serverInfoService;

    @Autowired
    private IWhitelistInfoService whitelistInfoService;

    @Autowired
    private RedisCache redisCache;

    /**
     * 聚合查询
     *
     * @return AjaxResult
     */
    @GetMapping("/aggregateQuery")
    public AjaxResult aggregateQuery() {
        final Map<String, Object> result = serverInfoService.aggregateQuery();
        if (!result.isEmpty()) {
            return success(result);
        } else {
            return error("服务器为空");
        }

    }

    @GetMapping("getWhiteListForServer")
    public AjaxResult getWhiteListForServer() {
        // 限流检查
        if (!rateLimiter.tryAcquire()) {
            return error("服务器繁忙,请稍后再试");
        }

        try {
            // 检查缓存
            if (redisCache.hasKey(CacheKey.WHITE_LIST_KEY) && redisCache.getCacheObject(CacheKey.WHITE_LIST_KEY) != null) {
                final Map<String, String> cacheObject = redisCache.getCacheObject(CacheKey.WHITE_LIST_KEY);
                cacheObject.remove("@type");
                // logger.info("获取白名单列表缓存");
                return success(cacheObject);
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
                redisCache.setCacheObject(CacheKey.WHITE_LIST_KEY, map, 5, TimeUnit.MINUTES);
            }
            return success(map);

        } catch (Exception e) {
            logger.error("获取白名单列表发生异常", e);
            return error("系统繁忙,请稍后重试");
        }
    }

    // 查询服务器在线人数
    @GetMapping("/getOnlinePlayer")
    public AjaxResult getOnlinePlayer() {
        return success(serverInfoService.getOnlinePlayer());
    }

    /**
     * 从数据库获取白名单列表
     *
     * @return AjaxResult
     */
    @GetMapping("/getWhiteList")
    public AjaxResult getWhiteList() {
        // 限流检查
        if (!rateLimiter.tryAcquire()) {
            return error("服务器繁忙,请稍后再试");
        }
        Map<String, String> result = new HashMap<>();

        try {
            // 检查缓存
            if (redisCache.hasKey(CacheKey.WHITE_LIST_KEY) && redisCache.getCacheObject(CacheKey.WHITE_LIST_KEY) != null) {
                final Map<String, String> cacheObject = redisCache.getCacheObject(CacheKey.WHITE_LIST_KEY);
                cacheObject.remove("@type");
                return success(cacheObject);
            }

            // 查询已通过审核且已添加的白名单用户
            WhitelistInfo query = new WhitelistInfo();
            query.setStatus("1"); // 已审核通过
            query.setAddState("1"); // 已添加
            final List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(query);

            if (whitelistInfos.isEmpty()) {
                return error("服务器白名单为空");
            }
            Map<String, Set<String>> cache = new HashMap<>();

            List<String> all = new ArrayList<>();
            // 遍历白名单列表
            for (WhitelistInfo whitelistInfo : whitelistInfos) {
                if (whitelistInfo.getServers().contains("all")) {
                    all.add(whitelistInfo.getUserName());
                } else {
                    for (String s : whitelistInfo.getServers().split(",")) {
                        if (cache.containsKey(s)) {
                            cache.get(s).add(whitelistInfo.getUserName());
                        } else {
                            Set<String> set = new HashSet<>();
                            set.add(whitelistInfo.getUserName());
                            cache.put(s, set);
                        }
                    }
                }
            }

            // 汇聚
            result.put("全部成员", Arrays.toString(all.toArray()));

            if (cache.isEmpty()) {
                return success(result);
            }

            cache.forEach((String k, Set<String> v) -> {
                if (MapCache.containsKey(k)) {  // 只查询活跃服务器
                    final String nameTag = serverInfoService.selectServerInfoById(Long.valueOf(k)).getNameTag();
                    result.put(nameTag, Arrays.toString(v.toArray()));
                }
            });

        } catch (Exception e) {
            logger.error("获取白名单列表发生异常", e);
            return error("系统繁忙,请稍后重试");
        }
        return success(result);
    }
}


