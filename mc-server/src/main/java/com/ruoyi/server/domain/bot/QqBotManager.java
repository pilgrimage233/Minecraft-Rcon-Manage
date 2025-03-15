package com.ruoyi.server.domain.bot;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

/**
 * QQ机器人管理员对象 qq_bot_manager
 *
 * @author Memory
 * @date 2025-03-13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QqBotManager extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 关联的机器人ID
     */
    private Long botId;

    /**
     * 管理员名称
     */
    @Excel(name = "管理员名称")
    private String managerName;

    /**
     * QQ
     */
    @Excel(name = "QQ")
    private String managerQq;

    /**
     * 权限类型
     */
    @Excel(name = "权限类型")
    private Long permissionType;

    /**
     * 最后活动时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后活动时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastActiveTime;

    /**
     * 状态
     */
    @Excel(name = "状态")
    private Long status;

    /**
     * QQ机器人管理员群组关联信息
     */
    private List<QqBotManagerGroup> qqBotManagerGroupList;


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("botId", getBotId())
                .append("managerName", getManagerName())
                .append("managerQq", getManagerQq())
                .append("permissionType", getPermissionType())
                .append("lastActiveTime", getLastActiveTime())
                .append("status", getStatus())
                .append("createTime", getCreateTime())
                .append("createBy", getCreateBy())
                .append("updateTime", getUpdateTime())
                .append("updateBy", getUpdateBy())
                .append("remark", getRemark())
                .append("qqBotManagerGroupList", getQqBotManagerGroupList())
                .toString();
    }
}
