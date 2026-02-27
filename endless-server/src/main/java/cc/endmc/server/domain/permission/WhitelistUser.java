package cc.endmc.server.domain.permission;

import cc.endmc.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 白名单用户对象 whitelist_user
 *
 * @author Memory
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WhitelistUser extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 白名单ID
     */
    private Long whitelistId;

    /**
     * 登录账号
     */
    private String userName;

    /**
     * QQ号
     */
    private String qqNum;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态(0-正常 1-停用)
     */
    private String status;

    /**
     * 用户等级(1成员, 50代表, 80管理员, 100Owner)
     */
    private Integer roleLevel;

    /**
     * 用户头衔
     */
    private String roleTitle;

    /**
     * 是否可发起投票(0否 1是)
     */
    private Integer canInitiateVote;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;
}
