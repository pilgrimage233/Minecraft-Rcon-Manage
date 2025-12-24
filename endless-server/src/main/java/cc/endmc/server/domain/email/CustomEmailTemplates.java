package cc.endmc.server.domain.email;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 自定义邮件通知模板对象 custom_email_templates
 *
 * @author memory
 * @date 2025-10-03
 */
@Setter
@Getter
public class CustomEmailTemplates extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 服务器ID
     */
    @Excel(name = "服务器ID")
    private Long serverId;

    /**
     * 审核
     */
    @Excel(name = "审核")
    private String reviewTemp;

    /**
     * 待审核
     */
    @Excel(name = "待审核")
    private String pendingTemp;

    /**
     * 通过
     */
    @Excel(name = "通过")
    private String passTemp;

    /**
     * 拒绝
     */
    @Excel(name = "拒绝")
    private String refuseTemp;

    /**
     * 移除
     */
    @Excel(name = "移除")
    private String removeTemp;

    /**
     * 封禁
     */
    @Excel(name = "封禁")
    private String banTemp;

    /**
     * 解禁
     */
    @Excel(name = "解禁")
    private String pardonTemp;

    /**
     * 邮箱验证
     */
    @Excel(name = "邮箱验证")
    private String verifyTemp;

    /**
     * 系统告警
     */
    @Excel(name = "系统告警")
    private String warningTemp;

    /**
     * 状态
     */
    @Excel(name = "状态")
    private Long status;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("createTime", getCreateTime())
                .append("createBy", getCreateBy())
                .append("serverId", getServerId())
                .append("reviewTemp", getReviewTemp())
                .append("pendingTemp", getPendingTemp())
                .append("passTemp", getPassTemp())
                .append("refuseTemp", getRefuseTemp())
                .append("removeTemp", getRemoveTemp())
                .append("banTemp", getBanTemp())
                .append("pardonTemp", getPardonTemp())
                .append("verifyTemp", getVerifyTemp())
                .append("warningTemp", getWarningTemp())
                .append("status", getStatus())
                .append("updateTime", getUpdateTime())
                .append("updateBy", getUpdateBy())
                .append("remark", getRemark())
                .toString();
    }
}
