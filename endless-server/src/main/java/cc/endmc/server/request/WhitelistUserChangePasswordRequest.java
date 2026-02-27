package cc.endmc.server.request;

import lombok.Data;

/**
 * 白名单用户修改密码请求
 */
@Data
public class WhitelistUserChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
}
