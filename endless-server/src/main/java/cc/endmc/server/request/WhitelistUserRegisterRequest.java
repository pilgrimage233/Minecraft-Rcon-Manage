package cc.endmc.server.request;

import lombok.Data;

/**
 * 白名单用户注册/重置请求
 */
@Data
public class WhitelistUserRegisterRequest {
    private String qqNum;
    private String code;
    private String userName;
    private String password;
}
