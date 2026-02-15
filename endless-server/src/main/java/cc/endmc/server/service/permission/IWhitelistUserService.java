package cc.endmc.server.service.permission;

import cc.endmc.server.domain.permission.WhitelistUser;

import java.util.Date;

/**
 * 白名单用户Service接口
 */
public interface IWhitelistUserService {
    WhitelistUser selectWhitelistUserById(Long id);

    WhitelistUser selectWhitelistUserByQqNum(String qqNum);

    WhitelistUser selectWhitelistUserByUserName(String userName);

    int insertWhitelistUser(WhitelistUser whitelistUser);

    int updateWhitelistUser(WhitelistUser whitelistUser);

    int updateWhitelistUserLoginTime(Long id, Date lastLoginTime);

    long countWhitelistUsers();
}
