package cc.endmc.server.domain.quiz.vo;

import cc.endmc.common.annotation.Excel;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

/**
 * 白名单申请题库问题对象 whitelist_quiz_question
 *
 * @author Memory
 * @date 2025-03-19
 */

@Data
public class WhitelistQuizQuestionVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 问题内容
     */
    @Excel(name = "问题内容")
    private String questionText;

    /**
     * 问题类型：1-单选题，2-多选题，3-填空题
     */
    @Excel(name = "问题类型：1-单选题，2-多选题，3-填空题")
    private Long questionType;

    /**
     * 是否必答：0-否，1-是
     */
    @Excel(name = "是否必答：0-否，1-是")
    private Integer isRequired;

    /**
     * 排序顺序
     */
    @Excel(name = "排序顺序")
    private Long sortOrder;

    /**
     * 白名单申请题目答案信息
     */
    private List<WhitelistQuizAnswerVo> whitelistQuizAnswerVoList;


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("questionText", getQuestionText())
                .append("questionType", getQuestionType())
                .append("isRequired", getIsRequired())
                .append("sortOrder", getSortOrder())
                .append("whitelistQuizAnswerList", getWhitelistQuizAnswerVoList())
                .toString();
    }
}
