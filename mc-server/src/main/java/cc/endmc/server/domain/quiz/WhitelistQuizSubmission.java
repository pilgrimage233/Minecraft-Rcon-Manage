package cc.endmc.server.domain.quiz;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

/**
 * 答题记录对象 whitelist_quiz_submission
 *
 * @author Memory
 * @date 2025-03-20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WhitelistQuizSubmission extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 玩家UUID
     */
    @Excel(name = "玩家UUID")
    private String playerUuid;

    /**
     * 玩家名称
     */
    @Excel(name = "玩家名称")
    private String playerName;

    /**
     * 总得分
     */
    @Excel(name = "总得分")
    private Long totalScore;

    /**
     * 通过状态：0-未通过，1-已通过
     */
    @Excel(name = "通过状态：0-未通过，1-已通过")
    private Integer passStatus;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "提交时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date submitTime;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date reviewTime;

    /**
     * 审核人
     */
    @Excel(name = "审核人")
    private String reviewer;

    /**
     * 审核备注
     */
    private String reviewComment;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 白名单申请答题详情信息
     */
    private List<WhitelistQuizSubmissionDetail> whitelistQuizSubmissionDetailList;


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("playerUuid", getPlayerUuid())
                .append("playerName", getPlayerName())
                .append("totalScore", getTotalScore())
                .append("passStatus", getPassStatus())
                .append("submitTime", getSubmitTime())
                .append("reviewTime", getReviewTime())
                .append("reviewer", getReviewer())
                .append("reviewComment", getReviewComment())
                .append("remark", getRemark())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("delFlag", getDelFlag())
                .append("whitelistQuizSubmissionDetailList", getWhitelistQuizSubmissionDetailList())
                .toString();
    }
}
