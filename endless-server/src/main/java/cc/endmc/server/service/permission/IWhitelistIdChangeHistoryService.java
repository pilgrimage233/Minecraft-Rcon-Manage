package cc.endmc.server.service.permission;

import cc.endmc.server.domain.permission.WhitelistIdChangeHistory;

import java.util.List;

/**
 * 白名单ID更改历史Service接口
 *
 * @author endmc
 * @date 2026-02-04
 */
public interface IWhitelistIdChangeHistoryService {
    /**
     * 查询白名单ID更改历史
     *
     * @param id 白名单ID更改历史主键
     * @return 白名单ID更改历史
     */
    WhitelistIdChangeHistory selectWhitelistIdChangeHistoryById(Long id);

    /**
     * 查询白名单ID更改历史列表
     *
     * @param whitelistIdChangeHistory 白名单ID更改历史
     * @return 白名单ID更改历史集合
     */
    List<WhitelistIdChangeHistory> selectWhitelistIdChangeHistoryList(WhitelistIdChangeHistory whitelistIdChangeHistory);

    /**
     * 新增白名单ID更改历史
     *
     * @param whitelistIdChangeHistory 白名单ID更改历史
     * @return 结果
     */
    int insertWhitelistIdChangeHistory(WhitelistIdChangeHistory whitelistIdChangeHistory);

    /**
     * 修改白名单ID更改历史
     *
     * @param whitelistIdChangeHistory 白名单ID更改历史
     * @return 结果
     */
    int updateWhitelistIdChangeHistory(WhitelistIdChangeHistory whitelistIdChangeHistory);

    /**
     * 批量删除白名单ID更改历史
     *
     * @param ids 需要删除的白名单ID更改历史主键集合
     * @return 结果
     */
    int deleteWhitelistIdChangeHistoryByIds(Long[] ids);

    /**
     * 删除白名单ID更改历史信息
     *
     * @param id 白名单ID更改历史主键
     * @return 结果
     */
    int deleteWhitelistIdChangeHistoryById(Long id);

    /**
     * 根据白名单ID查询更改历史
     *
     * @param whitelistId 白名单ID
     * @return 更改历史列表
     */
    List<WhitelistIdChangeHistory> selectChangeHistoryByWhitelistId(Long whitelistId);
}
