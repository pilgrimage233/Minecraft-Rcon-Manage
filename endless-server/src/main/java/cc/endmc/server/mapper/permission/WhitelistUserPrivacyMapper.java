package cc.endmc.server.mapper.permission;

import cc.endmc.server.domain.permission.WhitelistUserPrivacy;
import org.apache.ibatis.annotations.Mapper;

/**
 * 白名单用户隐私设置Mapper接口
 */
@Mapper
public interface WhitelistUserPrivacyMapper {
    WhitelistUserPrivacy selectWhitelistUserPrivacyByWhitelistId(Long whitelistId);

    int insertWhitelistUserPrivacy(WhitelistUserPrivacy privacy);

    int updateWhitelistUserPrivacy(WhitelistUserPrivacy privacy);
}
