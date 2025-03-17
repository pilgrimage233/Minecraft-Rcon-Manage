package cc.endmc.server.domain.permission;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 封禁管理对象 banlist_info
 *
 * @author ruoyi
 * @date 2024-03-28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BanlistInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 关联白名单ID
     */
    @Excel(name = "关联白名单ID")
    private Long whiteId;

    /**
     * 用户名称
     */
    @Excel(name = "用户名称")
    private String userName;

    /**
     * 封禁状态
     */
    @Excel(name = "封禁状态")
    private Long state;

    /**
     * 封禁原因
     */
    @Excel(name = "封禁原因")
    private String reason;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("whiteId", getWhiteId())
                .append("userName", getUserName())
                .append("state", getState())
                .append("reason", getReason())
                .append("remark", getRemark())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .append("createBy", getCreateBy())
                .append("updateBy", getUpdateBy())
                .toString();
    }
}
