package cc.endmc.server.request;

import lombok.Data;

/**
 * 白名单用户登录请求
 */
@Data
public class WhitelistUserLoginRequest {
    private String userName;
    private String password;
}
