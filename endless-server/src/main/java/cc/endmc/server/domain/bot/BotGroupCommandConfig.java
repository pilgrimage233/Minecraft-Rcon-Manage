package cc.endmc.server.domain.bot;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 群组指令功能配置对象 bot_group_command_config
 *
 * @author Memory
 * @date 2025-12-10
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BotGroupCommandConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 群组ID
     */
    @Excel(name = "群组ID")
    private String groupId;

    /**
     * 指令关键字（主命令名称）
     */
    @Excel(name = "指令关键字", readConverterExp = "主=命令名称")
    private String commandKey;

    /**
     * 指令显示名称
     */
    @Excel(name = "指令显示名称")
    private String commandName;

    /**
     * 指令分类（user/admin/super）
     */
    @Excel(name = "指令分类", readConverterExp = "u=ser/admin/super")
    private String commandCategory;

    /**
     * 是否启用（0=禁用，1=启用）
     */
    @Excel(name = "是否启用", readConverterExp = "0==禁用，1=启用")
    private Integer isEnabled;

    /**
     * 禁用时的提示消息
     */
    @Excel(name = "禁用时的提示消息")
    private String disabledMessage;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("groupId", getGroupId())
                .append("commandKey", getCommandKey())
                .append("commandName", getCommandName())
                .append("commandCategory", getCommandCategory())
                .append("isEnabled", getIsEnabled())
                .append("disabledMessage", getDisabledMessage())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .append("createBy", getCreateBy())
                .append("updateBy", getUpdateBy())
                .append("remark", getRemark())
                .toString();
    }
}
