package cc.endmc.server.service.permission.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.domain.permission.WhitelistUserPrivacy;
import cc.endmc.server.mapper.permission.WhitelistUserPrivacyMapper;
import cc.endmc.server.service.permission.IWhitelistUserPrivacyService;
import org.springframework.stereotype.Service;

/**
 * 白名单用户隐私设置Service业务层处理
 */
@Service
public class WhitelistUserPrivacyServiceImpl implements IWhitelistUserPrivacyService {
    private final WhitelistUserPrivacyMapper whitelistUserPrivacyMapper;

    public WhitelistUserPrivacyServiceImpl(WhitelistUserPrivacyMapper whitelistUserPrivacyMapper) {
        this.whitelistUserPrivacyMapper = whitelistUserPrivacyMapper;
    }

    @Override
    public WhitelistUserPrivacy selectWhitelistUserPrivacyByWhitelistId(Long whitelistId) {
        return whitelistUserPrivacyMapper.selectWhitelistUserPrivacyByWhitelistId(whitelistId);
    }

    @Override
    public int insertWhitelistUserPrivacy(WhitelistUserPrivacy privacy) {
        privacy.setCreateTime(DateUtils.getNowDate());
        return whitelistUserPrivacyMapper.insertWhitelistUserPrivacy(privacy);
    }

    @Override
    public int updateWhitelistUserPrivacy(WhitelistUserPrivacy privacy) {
        privacy.setUpdateTime(DateUtils.getNowDate());
        return whitelistUserPrivacyMapper.updateWhitelistUserPrivacy(privacy);
    }
}
