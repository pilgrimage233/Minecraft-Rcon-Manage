package cc.endmc.server.domain.vote;

import cc.endmc.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 投票实例对象 vote_instance
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VoteInstance extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long templateId;
    private String templateCode;
    private String templateName;
    private Long serverId;
    private String targetType;
    private String targetPlayerName;
    private Long targetWhitelistId;
    private String targetRef;
    private Long initiatorUserId;
    private String initiatorUserName;
    private Integer requiredVotes;
    private Integer agreeVotes;
    private Integer rejectVotes;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishedTime;

    private String executeStatus;
    private String executeResult;
    private String reason;
    private String extraContext;

    private List<VoteRecord> voteRecords;
}
