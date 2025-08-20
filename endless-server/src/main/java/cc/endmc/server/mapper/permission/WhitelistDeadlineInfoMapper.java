package cc.endmc.server.mapper.permission;

import cc.endmc.server.domain.permission.WhitelistDeadlineInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 时限管理Mapper接口
 *
 * @author Memory
 * @date 2025-08-15
 */
@Mapper
public interface WhitelistDeadlineInfoMapper {
    /**
     * 查询时限管理
     *
     * @param id 时限管理主键
     * @return 时限管理
     */
    public WhitelistDeadlineInfo selectWhitelistDeadlineInfoById(Long id);

    /**
     * 查询时限管理列表
     *
     * @param whitelistDeadlineInfo 时限管理
     * @return 时限管理集合
     */
    public List<WhitelistDeadlineInfo> selectWhitelistDeadlineInfoList(WhitelistDeadlineInfo whitelistDeadlineInfo);

    /**
     * 新增时限管理
     *
     * @param whitelistDeadlineInfo 时限管理
     * @return 结果
     */
    public int insertWhitelistDeadlineInfo(WhitelistDeadlineInfo whitelistDeadlineInfo);

    /**
     * 修改时限管理
     *
     * @param whitelistDeadlineInfo 时限管理
     * @return 结果
     */
    public int updateWhitelistDeadlineInfo(WhitelistDeadlineInfo whitelistDeadlineInfo);

    /**
     * 删除时限管理
     *
     * @param id 时限管理主键
     * @return 结果
     */
    public int deleteWhitelistDeadlineInfoById(Long id);

    /**
     * 批量删除时限管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWhitelistDeadlineInfoByIds(Long[] ids);

    /**
     * 查询已过期且未清除的时限管理列表（end_time < now 且 del_flag != 1 或为空）
     *
     * @return 时限管理集合
     */
    public List<WhitelistDeadlineInfo> selectExpiredWhitelistDeadlineInfoList();
}
