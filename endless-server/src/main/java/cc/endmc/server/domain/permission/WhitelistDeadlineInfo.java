package cc.endmc.server.domain.permission;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 时限管理对象 whitelist_deadline_info
 *
 * @author Memory
 * @date 2025-08-15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WhitelistDeadlineInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 白名单ID
     */
    @Excel(name = "白名单ID")
    private Long whitelistId;

    /**
     * 用户昵称
     */
    @Excel(name = "用户昵称")
    private String userName;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm")
    private Date startTime;

    /**
     * 截止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Excel(name = "截止时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm")
    private Date endTime;

    /**
     * 清除标识
     */
    private Long delFlag;

    /**
     * 更新者
     */
    @Excel(name = "更新者")
    private String updateBv;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("whitelistId", getWhitelistId())
                .append("userName", getUserName())
                .append("startTime", getStartTime())
                .append("endTime", getEndTime())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("updateBv", getUpdateBv())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
