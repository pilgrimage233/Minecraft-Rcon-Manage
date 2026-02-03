package cc.endmc.server.utils;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.utils.http.HttpUtils;
import cc.endmc.common.utils.ip.IpUtils;
import cc.endmc.server.domain.other.IpLimitInfo;
import cc.endmc.server.sdk.SearchHttpAK;
import cc.endmc.server.service.other.IIpLimitInfoService;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 白名单工具类
 */
@Slf4j
@Component
public class WhitelistUtils {

    // IP地理位置缓存（1小时过期）
    private static final Map<String, CachedLocation> LOCATION_CACHE = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRE_MS = 60 * 60 * 1000; // 1小时

    private static ThreadPoolTaskExecutor asyncExecutor;

    /**
     * 检查IP限流
     *
     * @param ip                 IP地址
     * @param ipLimitInfoService IP限流服务
     * @param iplimit            IP限流阈值
     * @param userName           用户名
     * @param userAgent          用户代理
     * @param bodyParams         请求参数
     * @return 如果限流返回错误结果，否则返回null
     */
    public static AjaxResult checkIpLimit(String ip, IIpLimitInfoService ipLimitInfoService,
                                          String iplimit, String userName, String userAgent, String bodyParams) {
        if (ip == null || ip.isEmpty()) {
            return AjaxResult.error("申请失败,请勿使用代理!");
        }

        IpLimitInfo ipLimitInfo = new IpLimitInfo();
        ipLimitInfo.setIp(ip);
        List<IpLimitInfo> ipLimitInfos = ipLimitInfoService.selectIpLimitInfoList(ipLimitInfo);

        if (ipLimitInfos.isEmpty()) {
            // 新IP，创建记录
            ipLimitInfo.setCreateTime(new Date());
            ipLimitInfo.setCreateBy("AUTO::apply::" + userName);
            ipLimitInfo.setCount(1L);
            ipLimitInfo.setUserAgent(userAgent);
            ipLimitInfo.setUuid(UUID.randomUUID().toString());
            ipLimitInfo.setBodyParams(bodyParams);

            // 异步获取地理位置
            updateIpLocationAsync(ipLimitInfo, ipLimitInfoService);

            ipLimitInfoService.insertIpLimitInfo(ipLimitInfo);
        } else {
            // 已存在的IP
            ipLimitInfo = ipLimitInfos.get(0);
            if (ipLimitInfo.getCount() >= Long.parseLong(iplimit)) {
                return AjaxResult.error("请求次数达到上限，请联系管理员!");
            }

            // 更新计数和信息
            if (ipLimitInfo.getCreateBy() == null || ipLimitInfo.getCreateBy().isEmpty()) {
                ipLimitInfo.setCreateBy("AUTO::apply::" + userName);
            } else if (ipLimitInfo.getCount() == 1) {
                ipLimitInfo.setUpdateBy(ipLimitInfo.getCreateBy() + "::" + userName);
            } else {
                ipLimitInfo.setUpdateBy(ipLimitInfo.getUpdateBy() + "::" + userName);
            }
            ipLimitInfo.setCount(ipLimitInfo.getCount() + 1);
            ipLimitInfo.setUpdateTime(new Date());

            ipLimitInfoService.updateIpLimitInfo(ipLimitInfo);
        }

        return null;
    }

    /**
     * 异步更新IP地理位置（不阻塞主流程）
     */
    private static void updateIpLocationAsync(IpLimitInfo ipLimitInfo, IIpLimitInfoService ipLimitInfoService) {
        if (asyncExecutor == null) {
            log.warn("线程池未初始化，跳过异步更新IP地理位置");
            return;
        }

        asyncExecutor.submit(() -> {
            try {
                String[] location = getIpLocationWithCache(ipLimitInfo.getIp());
                if (location[0] != null) ipLimitInfo.setProvince(location[0]);
                if (location[1] != null) ipLimitInfo.setCity(location[1]);
                if (location[2] != null) ipLimitInfo.setLongitude(location[2]);
                if (location[3] != null) ipLimitInfo.setLatitude(location[3]);

                // 如果有地理位置信息，更新到数据库
                if (location[0] != null || location[1] != null) {
                    ipLimitInfoService.updateIpLimitInfo(ipLimitInfo);
                }
            } catch (Exception e) {
                log.error("异步更新IP地理位置失败: {}", ipLimitInfo.getIp(), e);
            }
        });
    }

    /**
     * 从请求头中获取IP地址
     *
     * @param header 请求头
     * @return IP地址
     */
    public static String getIpFromHeader(Map<String, String> header) {
        String[] ipHeaders = {
                // 基础代理头
                "x-real-ip",
                "x-forwarded-for",
                "forwarded",               // 标准化的代理头(RFC7239)

                // 云服务商专用头
                "cf-connecting-ip",        // Cloudflare
                "fastly-client-ip",        // Fastly
                "true-client-ip",          // Akamai & Cloudflare(旧版)
                "x-cloudfront-viewer-ip",  // AWS CloudFront
                "x-azure-socketip",        // Azure
                "x-gcp-forwarding-rule-ip",// Google Cloud

                // 反向代理软件头
                "proxy-client-ip",
                "wl-proxy-client-ip",      // WebLogic
                "http_client_ip",
                "http_x_forwarded_for",
                "x-cluster-client-ip",     // 集群场景

                // 安全防护/CDN扩展头
                "x-original-forwarded-for",// 某些WAF添加
                "x-authress-client-ip",    // Authress身份服务
                "x-apigateway-api-id",     // API网关场景

                // 特殊网络设备头
                "x-bluecoat-via",          // Blue Coat代理
                "x-ivy-client-ip",         // F5 BIG-IP

                // 移动端专用头
                "x-mobile-client-ip",      // 移动运营商代理
                "x-nokia-msisdn",

                // 协议扩展头
                "x-envoy-external-ip",     // Envoy代理
                "x-nginx-proxy-ip",

                // 备用头（按需添加）
                "client-ip",               // 部分旧系统
                "remote-addr",             // 直接连接IP（需验证可信性）
                "x-host-ip"
        };

        for (String headerName : ipHeaders) {
            if (header.containsKey(headerName) && header.get(headerName) != null) {
                String ip = header.get(headerName);
                if (!ip.startsWith("192.168.") && !ip.startsWith("10.") && !ip.startsWith("172.")) {
                    ip = extractIpFromHeader(ip);
                    return ip;
                }
            }
        }
        return null;
    }

    /**
     * 带缓存的IP地理位置查询
     */
    public static String[] getIpLocationWithCache(String ip) {
        // 1. 检查是否为内网IP
        if (IpUtils.internalIp(ip)) {
            return getPrivateIpLocation();
        }

        // 2. 检查缓存
        CachedLocation cached = LOCATION_CACHE.get(ip);
        if (cached != null && !cached.isExpired()) {
            log.debug("从缓存获取IP地理位置: {}", ip);
            return cached.location;
        }

        // 3. 查询并缓存
        String[] location = getIpLocation(ip);
        LOCATION_CACHE.put(ip, new CachedLocation(location));

        // 4. 清理过期缓存（每100次查询清理一次）
        if (LOCATION_CACHE.size() > 100 && Math.random() < 0.01) {
            cleanExpiredCache();
        }

        return location;
    }

    /**
     * 返回内网IP的固定位置信息
     */
    private static String[] getPrivateIpLocation() {
        return new String[]{"内网", "局域网", "0", "0"};
    }

    /**
     * 清理过期缓存
     */
    private static void cleanExpiredCache() {
        if (asyncExecutor == null) {
            return;
        }

        asyncExecutor.submit(() -> {
            try {
                LOCATION_CACHE.entrySet().removeIf(entry -> entry.getValue().isExpired());
                log.debug("清理过期IP缓存，当前缓存数量: {}", LOCATION_CACHE.size());
            } catch (Exception e) {
                log.error("清理IP缓存失败", e);
            }
        });
    }

    /**
     * 获取IP地理位置信息（竞速模式）
     *
     * @param ip IP地址
     * @return 地理位置信息 [省份, 城市, 经度, 纬度]
     */
    public static String[] getIpLocation(String ip) {
        // 如果线程池未初始化，使用同步方式
        if (asyncExecutor == null) {
            log.warn("线程池未初始化，使用同步方式查询IP地理位置");
            return fetchFromBaidu(ip);
        }

        // 创建4个并行任务
        CompletableFuture<String[]> baiduFuture = CompletableFuture.supplyAsync(() -> fetchFromBaidu(ip), asyncExecutor);
        CompletableFuture<String[]> ipApiFuture = CompletableFuture.supplyAsync(() -> fetchFromIpApi(ip), asyncExecutor);
        CompletableFuture<String[]> ipapiCoFuture = CompletableFuture.supplyAsync(() -> fetchFromIpapiCo(ip), asyncExecutor);
        CompletableFuture<String[]> ipinfoFuture = CompletableFuture.supplyAsync(() -> fetchFromIpinfo(ip), asyncExecutor);

        // 竞速模式：任意一个成功返回完整数据就立即使用
        CompletableFuture<String[]> raceFuture = CompletableFuture.anyOf(
                baiduFuture, ipApiFuture, ipapiCoFuture, ipinfoFuture
        ).thenApply(result -> (String[]) result);

        try {
            // 等待第一个成功的结果
            String[] firstResult = raceFuture.get(2, TimeUnit.SECONDS);
            if (isValidLocation(firstResult)) {
                log.debug("快速获取到IP地理位置: {}", ip);
                return firstResult;
            }
        } catch (Exception e) {
            log.debug("竞速模式获取IP位置超时，尝试合并结果");
        }

        // 如果竞速失败，等待所有任务完成并合并结果
        try {
            CompletableFuture.allOf(baiduFuture, ipApiFuture, ipapiCoFuture, ipinfoFuture)
                    .get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.debug("部分IP地理位置API查询超时");
        }

        // 合并所有结果
        String[] result = new String[4];
        mergeLocation(result, getNow(baiduFuture));
        mergeLocation(result, getNow(ipApiFuture));
        mergeLocation(result, getNow(ipapiCoFuture));
        mergeLocation(result, getNow(ipinfoFuture));

        return result;
    }

    /**
     * 检查位置信息是否有效（至少有省份或城市）
     */
    private static boolean isValidLocation(String[] location) {
        return location != null &&
                ((location[0] != null && !location[0].isEmpty()) ||
                        (location[1] != null && !location[1].isEmpty()));
    }

    /**
     * 从百度地图API获取IP位置
     */
    private static String[] fetchFromBaidu(String ip) {
        String[] location = new String[4];
        try {
            SearchHttpAK searchHttpAK = new SearchHttpAK();
            Map<String, String> params = new HashMap<>();
            params.put("ip", ip);
            params.put("coor", "bd09ll");
            params.put("ak", SearchHttpAK.AK);

            JSONObject json = searchHttpAK.requestGetAK(SearchHttpAK.URL, params);
            if (json != null && json.containsKey("content")) {
                JSONObject content = json.getJSONObject("content");
                if (content.containsKey("address_detail")) {
                    JSONObject addressDetail = content.getJSONObject("address_detail");
                    location[0] = addressDetail.getString("province");
                    location[1] = addressDetail.getString("city");
                }
                if (content.containsKey("point")) {
                    JSONObject point = content.getJSONObject("point");
                    location[2] = point.getString("x");
                    location[3] = point.getString("y");
                }
            }
        } catch (Exception e) {
            log.debug("百度地图API查询失败: {}", e.getMessage());
        }
        return location;
    }

    /**
     * 从ip-api.com获取IP位置
     */
    private static String[] fetchFromIpApi(String ip) {
        String[] location = new String[4];
        try {
            String result = HttpUtils.sendGet("http://ip-api.com/json/" + ip + "?lang=zh-CN");
            JSONObject json = JSONObject.parseObject(result);
            if (json != null && "success".equals(json.getString("status"))) {
                location[0] = json.getString("regionName");
                location[1] = json.getString("city");
                location[2] = json.getString("lon");
                location[3] = json.getString("lat");
            }
        } catch (Exception e) {
            log.debug("ip-api.com查询失败: {}", e.getMessage());
        }
        return location;
    }

    /**
     * 从ipapi.co获取IP位置
     */
    private static String[] fetchFromIpapiCo(String ip) {
        String[] location = new String[4];
        try {
            String result = HttpUtils.sendGet("https://ipapi.co/" + ip + "/json/");
            JSONObject json = JSONObject.parseObject(result);
            if (json != null && !json.containsKey("error")) {
                location[0] = json.getString("region");
                location[1] = json.getString("city");
                location[2] = json.getString("longitude");
                location[3] = json.getString("latitude");
            }
        } catch (Exception e) {
            log.debug("ipapi.co查询失败: {}", e.getMessage());
        }
        return location;
    }

    /**
     * 从ipinfo.io获取IP位置（带超时控制）
     */
    private static String[] fetchFromIpinfo(String ip) {
        String[] location = new String[4];
        try {
            String result = HttpUtils.sendGet("https://ipinfo.io/" + ip + "/json");
            JSONObject json = JSONObject.parseObject(result);
            if (json != null && !json.containsKey("error")) {
                location[0] = json.getString("region");
                location[1] = json.getString("city");
                if (json.containsKey("loc")) {
                    String[] coords = json.getString("loc").split(",");
                    if (coords.length == 2) {
                        location[2] = coords[1]; // 经度
                        location[3] = coords[0]; // 纬度
                    }
                }
            }
        } catch (Exception e) {
            log.debug("ipinfo.io查询失败: {}", e.getMessage());
        }
        return location;
    }

    /**
     * 安全获取CompletableFuture结果
     */
    private static String[] getNow(CompletableFuture<String[]> future) {
        try {
            return future.getNow(new String[4]);
        } catch (Exception e) {
            return new String[4];
        }
    }

    /**
     * 合并位置信息（只填充空值）
     */
    private static void mergeLocation(String[] target, String[] source) {
        if (source == null) return;
        for (int i = 0; i < 4; i++) {
            if ((target[i] == null || target[i].isEmpty()) && source[i] != null && !source[i].isEmpty()) {
                target[i] = source[i];
            }
        }
    }

    @Autowired
    public void setAsyncExecutor(@Qualifier("threadPoolTaskExecutor") ThreadPoolTaskExecutor executor) {
        WhitelistUtils.asyncExecutor = executor;
    }

    /**
     * 缓存的位置信息
     */
    private static class CachedLocation {
        final String[] location;
        final long timestamp;

        CachedLocation(String[] location) {
            this.location = location;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRE_MS;
        }
    }

    // 提取第一个有效IP（处理X-Forwarded-For多IP场景）
    private static String extractIpFromHeader(String headerValue) {
        if (headerValue.contains(",")) {
            return headerValue.split(",")[0].trim();
        }
        return headerValue.trim();
    }

}