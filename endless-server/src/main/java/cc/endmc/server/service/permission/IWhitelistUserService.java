package cc.endmc.server.service.permission;

import cc.endmc.server.domain.permission.WhitelistUser;

import java.util.Date;
import java.util.List;

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

    List<WhitelistUser> selectWhitelistUserList(WhitelistUser whitelistUser);

    int updateWhitelistUserRole(Long id, Integer roleLevel, String roleTitle, Integer canInitiateVote, String updateBy);

    long countWhitelistUsers();
}
