package cc.endmc.server.request;

import lombok.Data;

/**
 * 白名单用户角色/头衔更新请求
 */
@Data
public class WhitelistUserRoleUpdateRequest {
    private Long userId;
    private Integer roleLevel;
    private String roleTitle;
    private Integer canInitiateVote;
}
