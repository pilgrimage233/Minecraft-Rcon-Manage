package com.ruoyi.server.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 白名单对象 whitelist_info
 *
 * @author ruoyi
 * @date 2023-12-26
 */
public class WhitelistInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "申请时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    /**
     * 游戏名称
     */
    @Excel(name = "游戏名称")
    private String userName;

    /**
     * $column.columnComment
     */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String userUuid;

    /**
     * 正版标识
     */
    @Excel(name = "正版标识")
    private Long onlineFlag;

    /**
     * 用户QQ号
     */
    @Excel(name = "用户QQ号")
    private String qqNum;

    /**
     * 审核用户
     */
    @Excel(name = "审核用户")
    private String reviewUsers;

    /**
     * 审核状态
     */
    @Excel(name = "审核状态")
    private String status;

    /**
     * 添加状态
     */
    @Excel(name = "添加状态")
    private String addState;

    /**
     * 添加时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "添加时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date addTime;

    /**
     * 移除原因
     */
    @Excel(name = "移除原因")
    private String removeReason;

    /**
     * 移除时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "移除时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date removeTime;

    @Excel(name = "服务器ID")
    private String servers;

    /**
     * 是否被封禁
     */
    private String banFlag;

    /**
     * 封禁原因
     */
    private String bannedReason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public Long getOnlineFlag() {
        return onlineFlag;
    }

    public void setOnlineFlag(Long onlineFlag) {
        this.onlineFlag = onlineFlag;
    }

    public String getQqNum() {
        return qqNum;
    }

    public void setQqNum(String qqNum) {
        this.qqNum = qqNum;
    }

    public String getReviewUsers() {
        return reviewUsers;
    }

    public void setReviewUsers(String reviewUsers) {
        this.reviewUsers = reviewUsers;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddState() {
        return addState;
    }

    public void setAddState(String addState) {
        this.addState = addState;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public String getRemoveReason() {
        return removeReason;
    }

    public void setRemoveReason(String removeReason) {
        this.removeReason = removeReason;
    }

    public Date getRemoveTime() {
        return removeTime;
    }

    public void setRemoveTime(Date removeTime) {
        this.removeTime = removeTime;
    }

    public String getServers() {
        return servers;
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public String getBanFlag() {
        return banFlag;
    }

    public void setBanFlag(String banFlag) {
        this.banFlag = banFlag;
    }

    public String getBannedReason() {
        return bannedReason;
    }

    public void setBannedReason(String bannedReason) {
        this.bannedReason = bannedReason;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("time", getTime())
                .append("userName", getUserName())
                .append("userUuid", getUserUuid())
                .append("onlineFlag", getOnlineFlag())
                .append("qqNum", getQqNum())
                .append("reviewUsers", getReviewUsers())
                .append("status", getStatus())
                .append("addState", getAddState())
                .append("addTime", getAddTime())
                .append("removeReason", getRemoveReason())
                .append("removeTime", getRemoveTime())
                .append("servers", getServers())
                .toString();
    }
}
