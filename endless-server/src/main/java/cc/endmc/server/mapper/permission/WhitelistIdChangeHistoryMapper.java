package cc.endmc.server.mapper.permission;

import cc.endmc.server.domain.permission.WhitelistIdChangeHistory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 白名单ID更改历史Mapper接口
 *
 * @author endmc
 * @date 2026-02-04
 */
@Mapper
public interface WhitelistIdChangeHistoryMapper {
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
     * 删除白名单ID更改历史
     *
     * @param id 白名单ID更改历史主键
     * @return 结果
     */
    int deleteWhitelistIdChangeHistoryById(Long id);

    /**
     * 批量删除白名单ID更改历史
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteWhitelistIdChangeHistoryByIds(Long[] ids);
}
