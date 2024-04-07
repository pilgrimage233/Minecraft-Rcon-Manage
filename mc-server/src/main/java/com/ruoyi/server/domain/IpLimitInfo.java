package com.ruoyi.server.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * IP限流对象 ip_limit_info
 *
 * @author ruoyi
 * @date 2024-03-22
 */
public class IpLimitInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 随机UUID
     */
    @Excel(name = "随机UUID")
    private String uuid;

    /**
     * IP地址
     */
    @Excel(name = "IP地址")
    private String ip;

    /**
     * UA标识
     */
    @Excel(name = "UA标识")
    private String userAgent;

    /**
     * 请求次数
     */
    @Excel(name = "请求次数")
    private Long count;

    /**
     * 省
     */
    @Excel(name = "省")
    private String province;

    /**
     * 地市
     */
    @Excel(name = "地市")
    private String city;

    /**
     * 区县
     */
    @Excel(name = "区县")
    private String county;

    /**
     * 经度
     */
    @Excel(name = "经度")
    private String longitude;

    /**
     * 纬度
     */
    @Excel(name = "纬度")
    private String latitude;

    /**
     * 请求参数
     */
    @Excel(name = "请求参数")
    private String bodyParams;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getBodyParams() {
        return bodyParams;
    }

    public void setBodyParams(String bodyParams) {
        this.bodyParams = bodyParams;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("uuid", getUuid())
                .append("createTime", getCreateTime())
                .append("createBy", getCreateBy())
                .append("updateTime", getUpdateTime())
                .append("updateBy", getUpdateBy())
                .append("ip", getIp())
                .append("userAgent", getUserAgent())
                .append("count", getCount())
                .append("province", getProvince())
                .append("city", getCity())
                .append("county", getCounty())
                .append("longitude", getLongitude())
                .append("latitude", getLatitude())
                .append("bodyParams", getBodyParams())
                .append("remark", getRemark())
                .toString();
    }
}
