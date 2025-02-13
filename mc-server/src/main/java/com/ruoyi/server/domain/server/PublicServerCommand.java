package com.ruoyi.server.domain.server;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 公开命令对象 public_server_command
 *
 * @author ruoyi
 * @date 2024-05-08
 */
public class PublicServerCommand extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 服务器ID
     */
    @Excel(name = "服务器ID")
    private Long serverId;

    /**
     * 指令
     */
    @Excel(name = "指令")
    private String command;

    /**
     * 启用状态
     */
    @Excel(name = "启用状态")
    private Long status;

    /**
     * 模糊匹配
     */
    @Excel(name = "模糊匹配")
    private Long vagueMatching;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Long getVagueMatching() {
        return vagueMatching;
    }

    public void setVagueMatching(Long vagueMatching) {
        this.vagueMatching = vagueMatching;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("serverId", getServerId())
                .append("command", getCommand())
                .append("status", getStatus())
                .append("vagueMatching", getVagueMatching())
                .append("createTime", getCreateTime())
                .append("createBy", getCreateBy())
                .append("updateTime", getUpdateTime())
                .append("updateBy", getUpdateBy())
                .append("remark", getRemark())
                .toString();
    }
}
