package cc.endmc.server.mapper.permission;

import cc.endmc.server.domain.permission.WhitelistUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 白名单用户Mapper接口
 */
@Mapper
public interface WhitelistUserMapper {
    WhitelistUser selectWhitelistUserById(Long id);

    WhitelistUser selectWhitelistUserByQqNum(String qqNum);

    WhitelistUser selectWhitelistUserByUserName(String userName);

    int insertWhitelistUser(WhitelistUser whitelistUser);

    int updateWhitelistUser(WhitelistUser whitelistUser);

    int updateWhitelistUserLoginTime(@Param("id") Long id, @Param("lastLoginTime") Date lastLoginTime);

    List<WhitelistUser> selectWhitelistUserList(WhitelistUser whitelistUser);

    int updateWhitelistUserRole(@Param("id") Long id,
                                @Param("roleLevel") Integer roleLevel,
                                @Param("roleTitle") String roleTitle,
                                @Param("canInitiateVote") Integer canInitiateVote,
                                @Param("updateBy") String updateBy);

    long countWhitelistUsers();
}
