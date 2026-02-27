package cc.endmc.server.dto;

import lombok.Data;

/**
 * 发起投票请求
 */
@Data
public class VoteCreateRequest {
    private Long templateId;
    private String templateCode;
    private Long serverId;
    private String targetPlayerName;
    private Long targetWhitelistId;
    private String targetRef;
    private String reason;
    private String extraContext;
}
