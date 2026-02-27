package cc.endmc.server.service.permission;

import cc.endmc.server.domain.permission.WhitelistUserPrivacy;

/**
 * 白名单用户隐私设置Service接口
 */
public interface IWhitelistUserPrivacyService {
    WhitelistUserPrivacy selectWhitelistUserPrivacyByWhitelistId(Long whitelistId);

    int insertWhitelistUserPrivacy(WhitelistUserPrivacy privacy);

    int updateWhitelistUserPrivacy(WhitelistUserPrivacy privacy);
}
