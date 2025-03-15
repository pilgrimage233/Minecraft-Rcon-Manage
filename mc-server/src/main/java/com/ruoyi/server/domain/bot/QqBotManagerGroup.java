package com.ruoyi.server.domain.bot;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * QQ机器人管理员群组关联对象 qq_bot_manager_group
 *
 * @author Memory
 * @date 2025-03-13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QqBotManagerGroup extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 管理员ID
     */
    @Excel(name = "管理员ID")
    private Long managerId;

    /**
     * 群号
     */
    @Excel(name = "群号")
    private String groupId;

    /**
     * 状态：0=禁用，1=启用
     */
    @Excel(name = "状态：0=禁用，1=启用")
    private Long status;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("managerId", getManagerId())
                .append("groupId", getGroupId())
                .append("status", getStatus())
                .append("createTime", getCreateTime())
                .append("createBy", getCreateBy())
                .append("updateTime", getUpdateTime())
                .append("updateBy", getUpdateBy())
                .append("remark", getRemark())
                .toString();
    }
}
