package cc.endmc.server.domain.quiz;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 题库配置对象 whitelist_quiz_config
 *
 * @author ruoyi
 * @date 2025-03-21
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WhitelistQuizConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 配置键
     */
    @Excel(name = "配置键")
    private String configKey;

    /**
     * 配置值
     */
    @Excel(name = "配置值")
    private String configValue;

    /**
     * 配置描述
     */
    @Excel(name = "配置描述")
    private String description;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("configKey", getConfigKey())
                .append("configValue", getConfigValue())
                .append("description", getDescription())
                .append("remark", getRemark())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
