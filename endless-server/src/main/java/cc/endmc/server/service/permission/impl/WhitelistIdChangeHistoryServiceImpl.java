package cc.endmc.server.service.permission.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.domain.permission.WhitelistIdChangeHistory;
import cc.endmc.server.mapper.permission.WhitelistIdChangeHistoryMapper;
import cc.endmc.server.service.permission.IWhitelistIdChangeHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 白名单ID更改历史Service业务层处理
 *
 * @author endmc
 * @date 2026-02-04
 */
@Service
@RequiredArgsConstructor
public class WhitelistIdChangeHistoryServiceImpl implements IWhitelistIdChangeHistoryService {

    private final WhitelistIdChangeHistoryMapper whitelistIdChangeHistoryMapper;

    /**
     * 查询白名单ID更改历史
     *
     * @param id 白名单ID更改历史主键
     * @return 白名单ID更改历史
     */
    @Override
    public WhitelistIdChangeHistory selectWhitelistIdChangeHistoryById(Long id) {
        return whitelistIdChangeHistoryMapper.selectWhitelistIdChangeHistoryById(id);
    }

    /**
     * 查询白名单ID更改历史列表
     *
     * @param whitelistIdChangeHistory 白名单ID更改历史
     * @return 白名单ID更改历史
     */
    @Override
    public List<WhitelistIdChangeHistory> selectWhitelistIdChangeHistoryList(WhitelistIdChangeHistory whitelistIdChangeHistory) {
        return whitelistIdChangeHistoryMapper.selectWhitelistIdChangeHistoryList(whitelistIdChangeHistory);
    }

    /**
     * 新增白名单ID更改历史
     *
     * @param whitelistIdChangeHistory 白名单ID更改历史
     * @return 结果
     */
    @Override
    public int insertWhitelistIdChangeHistory(WhitelistIdChangeHistory whitelistIdChangeHistory) {
        whitelistIdChangeHistory.setCreateTime(DateUtils.getNowDate());
        return whitelistIdChangeHistoryMapper.insertWhitelistIdChangeHistory(whitelistIdChangeHistory);
    }

    /**
     * 修改白名单ID更改历史
     *
     * @param whitelistIdChangeHistory 白名单ID更改历史
     * @return 结果
     */
    @Override
    public int updateWhitelistIdChangeHistory(WhitelistIdChangeHistory whitelistIdChangeHistory) {
        whitelistIdChangeHistory.setUpdateTime(DateUtils.getNowDate());
        return whitelistIdChangeHistoryMapper.updateWhitelistIdChangeHistory(whitelistIdChangeHistory);
    }

    /**
     * 批量删除白名单ID更改历史
     *
     * @param ids 需要删除的白名单ID更改历史主键
     * @return 结果
     */
    @Override
    public int deleteWhitelistIdChangeHistoryByIds(Long[] ids) {
        return whitelistIdChangeHistoryMapper.deleteWhitelistIdChangeHistoryByIds(ids);
    }

    /**
     * 删除白名单ID更改历史信息
     *
     * @param id 白名单ID更改历史主键
     * @return 结果
     */
    @Override
    public int deleteWhitelistIdChangeHistoryById(Long id) {
        return whitelistIdChangeHistoryMapper.deleteWhitelistIdChangeHistoryById(id);
    }
}
