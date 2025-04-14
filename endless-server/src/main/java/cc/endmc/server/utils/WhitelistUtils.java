package cc.endmc.server.utils;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.utils.http.HttpUtils;
import cc.endmc.server.domain.other.IpLimitInfo;
import cc.endmc.server.sdk.SearchHttpAK;
import cc.endmc.server.service.other.IIpLimitInfoService;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 白名单工具类
 */
@Slf4j
@Component
public class WhitelistUtils {

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

            // 获取地理位置
            updateIpLocation(ipLimitInfo);

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
     * 获取IP地理位置信息
     *
     * @param ip IP地址
     * @return 地理位置信息 [省份, 城市, 经度, 纬度]
     */
    public static String[] getIpLocation(String ip) {
        String[] location = new String[4]; // [省份, 城市, 经度, 纬度]

        // 主要接口：百度地图API
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
                    location[2] = point.getString("x"); // 经度
                    location[3] = point.getString("y"); // 纬度
                }
            }
        } catch (Exception e) {
            log.error("百度地图API获取IP地理位置失败", e);
        }

        // 备用接口1：ip-api.com
        if ((location[0] == null || location[0].isEmpty()) &&
                (location[1] == null || location[1].isEmpty())) {
            try {
                String result = HttpUtils.sendGet("http://ip-api.com/json/" + ip);
                JSONObject json = JSONObject.parseObject(result);
                if (json != null) {
                    if (json.containsKey("regionName")) {
                        location[0] = json.getString("regionName");
                    }
                    if (json.containsKey("city")) {
                        location[1] = json.getString("city");
                    }
                    if (json.containsKey("lon")) {
                        location[2] = json.getString("lon");
                    }
                    if (json.containsKey("lat")) {
                        location[3] = json.getString("lat");
                    }
                }
            } catch (Exception e) {
                log.error("ip-api.com获取IP地理位置失败", e);
            }
        }

        // 备用接口2：ipapi.co
        if ((location[0] == null || location[0].isEmpty()) &&
                (location[1] == null || location[1].isEmpty())) {
            try {
                String result = HttpUtils.sendGet("https://ipapi.co/" + ip + "/json/");
                JSONObject json = JSONObject.parseObject(result);
                if (json != null) {
                    if (json.containsKey("region")) {
                        location[0] = json.getString("region");
                    }
                    if (json.containsKey("city")) {
                        location[1] = json.getString("city");
                    }
                    if (json.containsKey("longitude")) {
                        location[2] = json.getString("longitude");
                    }
                    if (json.containsKey("latitude")) {
                        location[3] = json.getString("latitude");
                    }
                }
            } catch (Exception e) {
                log.error("ipapi.co获取IP地理位置失败", e);
            }
        }

        // 备用接口3：ipinfo.io
        if ((location[0] == null || location[0].isEmpty()) &&
                (location[1] == null || location[1].isEmpty())) {
            try {
                String result = HttpUtils.sendGet("https://ipinfo.io/" + ip + "/json");
                JSONObject json = JSONObject.parseObject(result);
                if (json != null) {
                    if (json.containsKey("region")) {
                        location[0] = json.getString("region");
                    }
                    if (json.containsKey("city")) {
                        location[1] = json.getString("city");
                    }
                    if (json.containsKey("loc")) {
                        String[] coords = json.getString("loc").split(",");
                        if (coords.length == 2) {
                            location[2] = coords[1]; // 经度
                            location[3] = coords[0]; // 纬度
                        }
                    }
                }
            } catch (Exception e) {
                log.error("ipinfo.io获取IP地理位置失败", e);
            }
        }

        return location;
    }

    /**
     * 更新IP限流信息中的地理位置信息
     *
     * @param ipLimitInfo IP限流信息
     */
    private static void updateIpLocation(IpLimitInfo ipLimitInfo) {
        String[] location = getIpLocation(ipLimitInfo.getIp());
        if (location[0] != null) {
            ipLimitInfo.setProvince(location[0]);
        }
        if (location[1] != null) {
            ipLimitInfo.setCity(location[1]);
        }
        if (location[2] != null) {
            ipLimitInfo.setLongitude(location[2]);
        }
        if (location[3] != null) {
            ipLimitInfo.setLatitude(location[3]);
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