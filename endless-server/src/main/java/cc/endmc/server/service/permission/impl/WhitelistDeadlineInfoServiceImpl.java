package cc.endmc.server.service.permission.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.domain.permission.WhitelistDeadlineInfo;
import cc.endmc.server.mapper.permission.WhitelistDeadlineInfoMapper;
import cc.endmc.server.service.permission.IWhitelistDeadlineInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 时限管理Service业务层处理
 *
 * @author Memory
 * @date 2025-08-15
 */
@Service
public class WhitelistDeadlineInfoServiceImpl implements IWhitelistDeadlineInfoService {
    @Autowired
    private WhitelistDeadlineInfoMapper whitelistDeadlineInfoMapper;

    /**
     * 查询时限管理
     *
     * @param id 时限管理主键
     * @return 时限管理
     */
    @Override
    public WhitelistDeadlineInfo selectWhitelistDeadlineInfoById(Long id) {
        return whitelistDeadlineInfoMapper.selectWhitelistDeadlineInfoById(id);
    }

    /**
     * 查询时限管理列表
     *
     * @param whitelistDeadlineInfo 时限管理
     * @return 时限管理
     */
    @Override
    public List<WhitelistDeadlineInfo> selectWhitelistDeadlineInfoList(WhitelistDeadlineInfo whitelistDeadlineInfo) {
        return whitelistDeadlineInfoMapper.selectWhitelistDeadlineInfoList(whitelistDeadlineInfo);
    }

    /**
     * 新增时限管理
     *
     * @param whitelistDeadlineInfo 时限管理
     * @return 结果
     */
    @Override
    public int insertWhitelistDeadlineInfo(WhitelistDeadlineInfo whitelistDeadlineInfo) {
        whitelistDeadlineInfo.setCreateTime(DateUtils.getNowDate());
        return whitelistDeadlineInfoMapper.insertWhitelistDeadlineInfo(whitelistDeadlineInfo);
    }

    /**
     * 修改时限管理
     *
     * @param whitelistDeadlineInfo 时限管理
     * @return 结果
     */
    @Override
    public int updateWhitelistDeadlineInfo(WhitelistDeadlineInfo whitelistDeadlineInfo) {
        whitelistDeadlineInfo.setUpdateTime(DateUtils.getNowDate());
        return whitelistDeadlineInfoMapper.updateWhitelistDeadlineInfo(whitelistDeadlineInfo);
    }

    /**
     * 批量删除时限管理
     *
     * @param ids 需要删除的时限管理主键
     * @return 结果
     */
    @Override
    public int deleteWhitelistDeadlineInfoByIds(Long[] ids) {
        return whitelistDeadlineInfoMapper.deleteWhitelistDeadlineInfoByIds(ids);
    }

    /**
     * 删除时限管理信息
     *
     * @param id 时限管理主键
     * @return 结果
     */
    @Override
    public int deleteWhitelistDeadlineInfoById(Long id) {
        return whitelistDeadlineInfoMapper.deleteWhitelistDeadlineInfoById(id);
    }

    /**
     * 查询已过期且未清除的时限管理列表
     *
     * @return 时限管理集合
     */
    @Override
    public List<WhitelistDeadlineInfo> selectExpiredWhitelistDeadlineInfoList() {
        return whitelistDeadlineInfoMapper.selectExpiredWhitelistDeadlineInfoList();
    }
}
