package com.ruoyi.server.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 指令管理对象 server_command_info
 *
 * @author ruoyi
 * @date 2024-04-16
 */
public class ServerCommandInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 服务器ID
     */
    @Excel(name = "服务器ID")
    private String serverId;

    /**
     * 在线模式白名单添加指令
     */
    @Excel(name = "在线模式白名单添加指令")
    private String onlineAddWhitelistCommand;

    /**
     * 离线模式白名单添加指令
     */
    @Excel(name = "离线模式白名单添加指令")
    private String offlineAddWhitelistCommand;

    /**
     * 在线模式白名单移除指令
     */
    @Excel(name = "在线模式白名单移除指令")
    private String onlineRmWhitelistCommand;

    /**
     * 离线模式白名单添加指令
     */
    @Excel(name = "离线模式白名单添加指令")
    private String offlineRmWhitelistCommand;

    /**
     * 在线模式添加封禁指令
     */
    @Excel(name = "在线模式添加封禁指令")
    private String onlineAddBanCommand;

    /**
     * 离线模式添加封禁指令
     */
    @Excel(name = "离线模式添加封禁指令")
    private String offlineAddBanCommand;

    /**
     * 在线模式移除封禁指令
     */
    @Excel(name = "在线模式移除封禁指令")
    private String onlineRmBanCommand;

    /**
     * 离线模式移除封禁指令
     */
    @Excel(name = "离线模式移除封禁指令")
    private String offlineRmBanCommand;

    /**
     * 是否启用EasyAuthMod
     */
    @Excel(name = "是否启用EasyAuthMod")
    private String easyauth;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getOnlineAddWhitelistCommand() {
        return onlineAddWhitelistCommand;
    }

    public void setOnlineAddWhitelistCommand(String onlineAddWhitelistCommand) {
        this.onlineAddWhitelistCommand = onlineAddWhitelistCommand;
    }

    public String getOfflineAddWhitelistCommand() {
        return offlineAddWhitelistCommand;
    }

    public void setOfflineAddWhitelistCommand(String offlineAddWhitelistCommand) {
        this.offlineAddWhitelistCommand = offlineAddWhitelistCommand;
    }

    public String getOnlineRmWhitelistCommand() {
        return onlineRmWhitelistCommand;
    }

    public void setOnlineRmWhitelistCommand(String onlineRmWhitelistCommand) {
        this.onlineRmWhitelistCommand = onlineRmWhitelistCommand;
    }

    public String getOfflineRmWhitelistCommand() {
        return offlineRmWhitelistCommand;
    }

    public void setOfflineRmWhitelistCommand(String offlineRmWhitelistCommand) {
        this.offlineRmWhitelistCommand = offlineRmWhitelistCommand;
    }

    public String getOnlineAddBanCommand() {
        return onlineAddBanCommand;
    }

    public void setOnlineAddBanCommand(String onlineAddBanCommand) {
        this.onlineAddBanCommand = onlineAddBanCommand;
    }

    public String getOfflineAddBanCommand() {
        return offlineAddBanCommand;
    }

    public void setOfflineAddBanCommand(String offlineAddBanCommand) {
        this.offlineAddBanCommand = offlineAddBanCommand;
    }

    public String getOnlineRmBanCommand() {
        return onlineRmBanCommand;
    }

    public void setOnlineRmBanCommand(String onlineRmBanCommand) {
        this.onlineRmBanCommand = onlineRmBanCommand;
    }

    public String getOfflineRmBanCommand() {
        return offlineRmBanCommand;
    }

    public void setOfflineRmBanCommand(String offlineRmBanCommand) {
        this.offlineRmBanCommand = offlineRmBanCommand;
    }

    public String getEasyauth() {
        return easyauth;
    }

    public void setEasyauth(String easyauth) {
        this.easyauth = easyauth;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("serverId", getServerId())
                .append("createTime", getCreateTime())
                .append("createBy", getCreateBy())
                .append("onlineAddWhitelistCommand", getOnlineAddWhitelistCommand())
                .append("offlineAddWhitelistCommand", getOfflineAddWhitelistCommand())
                .append("onlineRmWhitelistCommand", getOnlineRmWhitelistCommand())
                .append("offlineRmWhitelistCommand", getOfflineRmWhitelistCommand())
                .append("onlineAddBanCommand", getOnlineAddBanCommand())
                .append("offlineAddBanCommand", getOfflineAddBanCommand())
                .append("onlineRmBanCommand", getOnlineRmBanCommand())
                .append("offlineRmBanCommand", getOfflineRmBanCommand())
                .append("easyauth", getEasyauth())
                .append("updateTime", getUpdateTime())
                .append("updateBy", getUpdateBy())
                .append("remark", getRemark())
                .toString();
    }
}
