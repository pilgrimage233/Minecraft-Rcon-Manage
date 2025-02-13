package com.ruoyi.server.domain.server;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 指令管理对象 server_command_info
 *
 * @author ruoyi
 * @date 2024-04-16
 */
@EqualsAndHashCode(callSuper = true)
@Data
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
