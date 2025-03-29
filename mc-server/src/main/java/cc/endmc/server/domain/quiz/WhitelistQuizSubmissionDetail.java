package cc.endmc.server.domain.quiz;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 白名单申请答题详情对象 whitelist_quiz_submission_detail
 *
 * @author Memory
 * @date 2025-03-20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WhitelistQuizSubmissionDetail extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 关联的提交记录ID
     */
    @Excel(name = "关联的提交记录ID")
    private Long submissionId;

    /**
     * 问题ID
     */
    @Excel(name = "问题ID")
    private Long questionId;

    /**
     * 问题类型：1-单选题，2-多选题，3-填空题
     */
    @Excel(name = "问题类型：1-单选题，2-多选题，3-填空题")
    private Long questionType;

    /**
     * 玩家答案
     */
    @Excel(name = "玩家答案")
    private String playerAnswer;

    /**
     * 是否正确：0-错误，1-正确
     */
    @Excel(name = "是否正确：0-错误，1-正确")
    private Integer isCorrect;

    /**
     * 得分
     */
    @Excel(name = "得分")
    private Long score;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("submissionId", getSubmissionId())
                .append("questionId", getQuestionId())
                .append("questionType", getQuestionType())
                .append("playerAnswer", getPlayerAnswer())
                .append("isCorrect", getIsCorrect())
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
