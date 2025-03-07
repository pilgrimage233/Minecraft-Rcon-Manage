package com.ruoyi.server.utils;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.server.domain.other.IpLimitInfo;
import com.ruoyi.server.sdk.SearchHttpAK;
import com.ruoyi.server.service.other.IIpLimitInfoService;
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
                "x-real-ip",
                "x-forwarded-for",
                "proxy-client-ip",
                "wl-proxy-client-ip",
                "http_client_ip",
                "http_x_forwarded_for"
        };

        for (String headerName : ipHeaders) {
            String ip = header.get(headerName);
            if (ip != null && !ip.isEmpty()) {
                return ip;
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

        // 如果主要接口失败，使用备用接口：ip-api.com
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
} 