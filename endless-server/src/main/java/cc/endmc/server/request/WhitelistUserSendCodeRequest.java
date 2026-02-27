package cc.endmc.server.request;

import lombok.Data;

/**
 * 白名单用户验证码请求
 */
@Data
public class WhitelistUserSendCodeRequest {
    private String qqNum;
}
