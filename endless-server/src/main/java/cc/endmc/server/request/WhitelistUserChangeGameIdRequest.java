package cc.endmc.server.request;

import lombok.Data;

/**
 * 已登录白名单用户更改游戏ID请求
 */
@Data
public class WhitelistUserChangeGameIdRequest {
    /**
     * 新游戏ID
     */
    private String newUserName;

    /**
     * 更改原因
     */
    private String changeReason;
}
