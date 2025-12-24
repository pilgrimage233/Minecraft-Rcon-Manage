package cc.endmc.server.mapper.statistics;

import cc.endmc.server.domain.statistics.PlayerActivityStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 玩家活跃度统计Mapper接口
 *
 */
@Mapper
public interface PlayerActivityStatsMapper {

    /**
     * 查询玩家活跃度统计
     *
     * @param id 玩家活跃度统计主键
     * @return 玩家活跃度统计
     */
    PlayerActivityStats selectPlayerActivityStatsById(Long id);

    /**
     * 查询玩家活跃度统计列表
     *
     * @param playerActivityStats 玩家活跃度统计
     * @return 玩家活跃度统计集合
     */
    List<PlayerActivityStats> selectPlayerActivityStatsList(PlayerActivityStats playerActivityStats);

    /**
     * 根据日期和类型查询统计数据
     *
     * @param statsDate 统计日期
     * @param statsType 统计类型
     * @return 统计数据
     */
    PlayerActivityStats selectByDateAndType(@Param("statsDate") Date statsDate, @Param("statsType") String statsType);

    /**
     * 查询时间范围内的统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param statsType 统计类型
     * @return 统计数据列表
     */
    List<PlayerActivityStats> selectByDateRange(@Param("startDate") Date startDate, 
                                               @Param("endDate") Date endDate, 
                                               @Param("statsType") String statsType);

    /**
     * 新增玩家活跃度统计
     *
     * @param playerActivityStats 玩家活跃度统计
     * @return 结果
     */
    int insertPlayerActivityStats(PlayerActivityStats playerActivityStats);

    /**
     * 修改玩家活跃度统计
     *
     * @param playerActivityStats 玩家活跃度统计
     * @return 结果
     */
    int updatePlayerActivityStats(PlayerActivityStats playerActivityStats);

    /**
     * 删除玩家活跃度统计
     *
     * @param id 玩家活跃度统计主键
     * @return 结果
     */
    int deletePlayerActivityStatsById(Long id);

    /**
     * 批量删除玩家活跃度统计
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deletePlayerActivityStatsByIds(Long[] ids);

    /**
     * 插入或更新统计数据
     *
     * @param playerActivityStats 统计数据
     * @return 结果
     */
    int insertOrUpdatePlayerActivityStats(PlayerActivityStats playerActivityStats);
}