package cc.endmc.server.domain.quiz.vo;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 白名单申请题目答案对象 whitelist_quiz_answer
 *
 * @author Memory
 * @date 2025-03-19
 */
@Data
public class WhitelistQuizAnswerVo implements Serializable {
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
     * 排序顺序（选择题选项排序）
     */
    @Excel(name = "排序顺序", readConverterExp = "选择题选项排序")
    private Long sortOrder;


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("questionId", getQuestionId())
                .append("answerText", getAnswerText())
                .append("sortOrder", getSortOrder())
                .toString();
    }
}
