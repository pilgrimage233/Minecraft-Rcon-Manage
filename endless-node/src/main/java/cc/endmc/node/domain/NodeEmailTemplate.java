package cc.endmc.node.domain;

import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 邮件模板对象 node_email_template
 *
 * @author Memory
 * @date 2026-05-29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NodeEmailTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;

    /** 模板标识 */
    private String templateKey;

    /** 模板名称 */
    private String templateName;

    /** 邮件主题模板 */
    private String subject;

    /** 邮件内容模板 (HTML) */
    private String content;

    /** 模板说明 */
    private String description;

    /** 删除标志 */
    private String delFlag;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("templateKey", getTemplateKey())
                .append("templateName", getTemplateName())
                .append("subject", getSubject())
                .append("description", getDescription())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
