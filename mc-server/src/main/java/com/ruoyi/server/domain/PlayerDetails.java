package com.ruoyi.server.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 玩家详情对象 player_details
 *
 * @author Memory
 * @date 2024-12-31
 */
public class PlayerDetails extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private Long id;

    /**
     * 玩家昵称
     */
    @Excel(name = "玩家昵称")
    private String userName;

    /**
     * QQ号
     */
    @Excel(name = "QQ号")
    private String qq;

    /**
     * 身份
     */
    @Excel(name = "身份")
    private String identity;
    /**
     * 最后在线时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后在线时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastOnlineTime;

    /**
     * 最后离线时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后离线时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastOfflineTime;

    /**
     * 省份
     */
    @Excel(name = "省份")
    private String province;

    /**
     * 地市
     */
    @Excel(name = "地市")
    private String city;

    /**
     * 白名单ID
     */
    private Long whitelistId;

    /**
     * 封禁ID
     */
    private Long banlistId;

    /**
     * 其他参数
     */
    private String parameters;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Date getLastOnlineTime() {
        return lastOnlineTime;
    }

    public void setLastOnlineTime(Date lastOnlineTime) {
        this.lastOnlineTime = lastOnlineTime;
    }

    public Date getLastOfflineTime() {
        return lastOfflineTime;
    }

    public void setLastOfflineTime(Date lastOfflineTime) {
        this.lastOfflineTime = lastOfflineTime;
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

    public Long getWhitelistId() {
        return whitelistId;
    }

    public void setWhitelistId(Long whitelistId) {
        this.whitelistId = whitelistId;
    }

    public Long getBanlistId() {
        return banlistId;
    }

    public void setBanlistId(Long banlistId) {
        this.banlistId = banlistId;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("userName", getUserName())
                .append("qq", getQq())
                .append("identity", getIdentity())
                .append("lastOnlineTime", getLastOnlineTime())
                .append("lastOfflineTime", getLastOfflineTime())
                .append("province", getProvince())
                .append("city", getCity())
                .append("whitelistId", getWhitelistId())
                .append("banlistId", getBanlistId())
                .append("parameters", getParameters())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .append("createBy", getCreateBy())
                .append("updateBy", getUpdateBy())
                .append("remark", getRemark())
                .toString();
    }
}
