package cc.endmc.server.request;

import lombok.Data;

/**
 * 白名单用户隐私设置更新请求
 */
@Data
public class WhitelistUserPrivacyUpdateRequest {
    private Integer showQq;
    private Integer showCity;
    private Integer showLastOnline;
    private Integer showGameTime;
    private Integer showNameHistory;
    private Integer showQuizResult;
    private Integer showUuid;
}
