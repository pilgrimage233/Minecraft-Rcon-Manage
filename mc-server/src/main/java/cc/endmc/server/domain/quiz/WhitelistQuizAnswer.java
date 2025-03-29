package cc.endmc.server.domain.quiz;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 白名单申请题目答案对象 whitelist_quiz_answer
 *
 * @author Memory
 * @date 2025-03-19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WhitelistQuizAnswer extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 关联的问题ID
     */
    @Excel(name = "关联的问题ID")
    private Long questionId;

    /**
     * 答案内容
     */
    @Excel(name = "答案内容")
    private String answerText;

    /**
     * 是否为正确答案：0-否，1-是
     */
    @Excel(name = "是否为正确答案：0-否，1-是")
    private Integer isCorrect;

    /**
     * 排序顺序（选择题选项排序）
     */
    @Excel(name = "排序顺序", readConverterExp = "选=择题选项排序")
    private Long sortOrder;

    /**
     * 得分
     */
    @Excel(name = "得分")
    private Double score;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("questionId", getQuestionId())
                .append("answerText", getAnswerText())
                .append("isCorrect", getIsCorrect())
                .append("sortOrder", getSortOrder())
                .append("score", getScore())
                .append("remark", getRemark())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
