package cc.endmc.server.dto;

import lombok.Data;

/**
 * 跟投请求
 */
@Data
public class VoteCastRequest {
    private Long voteId;
    private Integer voteDecision;
    private String voteComment;
}
