package cc.endmc.server.domain.permission;

import lombok.Data;

/**
 * 白名单用户登录会话
 *
 * @author Memory
 */
@Data
public class WhitelistUserSession {
    private Long userId;
    private Long whitelistId;
    private String userName;
    private String qqNum;
    private Integer roleLevel;
    private String roleTitle;
    private Integer canInitiateVote;
    private String token;
    private Long loginTime;
    private Long expireTime;
}
