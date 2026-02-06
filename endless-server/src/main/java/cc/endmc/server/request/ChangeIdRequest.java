package cc.endmc.server.request;

import lombok.Data;

/**
 * 更改游戏ID请求对象
 *
 * @author Memory
 */
@Data
public class ChangeIdRequest {
    /**
     * 旧游戏ID
     */
    private String oldUserName;

    /**
     * 新游戏ID
     */
    private String newUserName;

    /**
     * QQ号
     */
    private String qqNum;

    /**
     * 更改原因
     */
    private String changeReason;
}
