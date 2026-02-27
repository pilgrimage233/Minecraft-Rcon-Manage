package cc.endmc.server.domain.vote;

import cc.endmc.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 投票记录对象 vote_record
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VoteRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long voteId;
    private Long voterUserId;
    private String voterUserName;
    private Integer voteDecision;
    private String voteComment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
