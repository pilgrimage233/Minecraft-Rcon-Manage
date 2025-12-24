package cc.endmc.system.domain;

import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;

/**
 * 用户反馈记录表 sys_feedback_record
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysFeedbackRecord extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 反馈UUID（关联远程反馈系统）
     */
    private String uuid;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 反馈类型：1-Bug反馈 2-功能建议 3-使用问题 4-其他
     */
    private Integer feedbackType;

    /**
     * 反馈标题
     */
    private String title;

    /**
     * 状态：0-待处理 1-处理中 2-已解决 3-已关闭
     */
    private Integer status;

}
