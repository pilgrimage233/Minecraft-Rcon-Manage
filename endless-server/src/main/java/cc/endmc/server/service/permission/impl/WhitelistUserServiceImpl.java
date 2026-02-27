package cc.endmc.server.service.permission.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.domain.permission.WhitelistUser;
import cc.endmc.server.mapper.permission.WhitelistUserMapper;
import cc.endmc.server.service.permission.IWhitelistUserService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 白名单用户Service业务层处理
 */
@Service
public class WhitelistUserServiceImpl implements IWhitelistUserService {
    private final WhitelistUserMapper whitelistUserMapper;

    public WhitelistUserServiceImpl(WhitelistUserMapper whitelistUserMapper) {
        this.whitelistUserMapper = whitelistUserMapper;
    }

    @Override
    public WhitelistUser selectWhitelistUserById(Long id) {
        return whitelistUserMapper.selectWhitelistUserById(id);
    }

    @Override
    public WhitelistUser selectWhitelistUserByQqNum(String qqNum) {
        return whitelistUserMapper.selectWhitelistUserByQqNum(qqNum);
    }

    @Override
    public WhitelistUser selectWhitelistUserByUserName(String userName) {
        return whitelistUserMapper.selectWhitelistUserByUserName(userName);
    }

    @Override
    public int insertWhitelistUser(WhitelistUser whitelistUser) {
        whitelistUser.setCreateTime(DateUtils.getNowDate());
        if (whitelistUser.getRoleLevel() == null) {
            whitelistUser.setRoleLevel(1);
        }
        if (whitelistUser.getRoleTitle() == null || whitelistUser.getRoleTitle().isEmpty()) {
            whitelistUser.setRoleTitle("成员");
        }
        if (whitelistUser.getCanInitiateVote() == null) {
            whitelistUser.setCanInitiateVote(0);
        }
        return whitelistUserMapper.insertWhitelistUser(whitelistUser);
    }

    @Override
    public int updateWhitelistUser(WhitelistUser whitelistUser) {
        whitelistUser.setUpdateTime(DateUtils.getNowDate());
        return whitelistUserMapper.updateWhitelistUser(whitelistUser);
    }

    @Override
    public int updateWhitelistUserLoginTime(Long id, Date lastLoginTime) {
        return whitelistUserMapper.updateWhitelistUserLoginTime(id, lastLoginTime);
    }

    @Override
    public List<WhitelistUser> selectWhitelistUserList(WhitelistUser whitelistUser) {
        return whitelistUserMapper.selectWhitelistUserList(whitelistUser);
    }

    @Override
    public int updateWhitelistUserRole(Long id, Integer roleLevel, String roleTitle, Integer canInitiateVote, String updateBy) {
        return whitelistUserMapper.updateWhitelistUserRole(id, roleLevel, roleTitle, canInitiateVote, updateBy);
    }

    @Override
    public long countWhitelistUsers() {
        return whitelistUserMapper.countWhitelistUsers();
    }
}
