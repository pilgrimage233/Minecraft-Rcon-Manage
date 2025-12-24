package cc.endmc.server.mapper.statistics;

import cc.endmc.server.domain.statistics.DailyPlayerActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 每日玩家活跃度Mapper接口
 *
 */
@Mapper
public interface DailyPlayerActivityMapper {

    /**
     * 查询每日玩家活跃度
     *
     * @param id 每日玩家活跃度主键
     * @return 每日玩家活跃度
     */
    DailyPlayerActivity selectDailyPlayerActivityById(Long id);

    /**
     * 查询每日玩家活跃度列表
     *
     * @param dailyPlayerActivity 每日玩家活跃度
     * @return 每日玩家活跃度集合
     */
    List<DailyPlayerActivity> selectDailyPlayerActivityList(DailyPlayerActivity dailyPlayerActivity);

    /**
     * 根据玩家名和日期查询活跃度
     *
     * @param playerName   玩家名
     * @param activityDate 活跃日期
     * @return 活跃度记录
     */
    DailyPlayerActivity selectByPlayerAndDate(@Param("playerName") String playerName, 
                                             @Param("activityDate") Date activityDate);

    /**
     * 查询指定日期的所有玩家活跃度
     *
     * @param activityDate 活跃日期
     * @return 活跃度记录列表
     */
    List<DailyPlayerActivity> selectByDate(@Param("activityDate") Date activityDate);

    /**
     * 查询时间范围内的玩家活跃度排行
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param limit     限制数量
     * @return 排行数据
     */
    List<Map<String, Object>> selectPlayerRanking(@Param("startDate") Date startDate, 
                                                 @Param("endDate") Date endDate, 
                                                 @Param("limit") Integer limit);

    /**
     * 统计时间范围内的活跃玩家数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据
     */
    Map<String, Object> selectActivitySummary(@Param("startDate") Date startDate, 
                                             @Param("endDate") Date endDate);

    /**
     * 新增每日玩家活跃度
     *
     * @param dailyPlayerActivity 每日玩家活跃度
     * @return 结果
     */
    int insertDailyPlayerActivity(DailyPlayerActivity dailyPlayerActivity);

    /**
     * 修改每日玩家活跃度
     *
     * @param dailyPlayerActivity 每日玩家活跃度
     * @return 结果
     */
    int updateDailyPlayerActivity(DailyPlayerActivity dailyPlayerActivity);

    /**
     * 删除每日玩家活跃度
     *
     * @param id 每日玩家活跃度主键
     * @return 结果
     */
    int deleteDailyPlayerActivityById(Long id);

    /**
     * 批量删除每日玩家活跃度
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteDailyPlayerActivityByIds(Long[] ids);

    /**
     * 插入或更新每日活跃度记录
     *
     * @param dailyPlayerActivity 活跃度记录
     * @return 结果
     */
    int insertOrUpdateDailyPlayerActivity(DailyPlayerActivity dailyPlayerActivity);

    /**
     * 批量插入每日活跃度记录
     *
     * @param activities 活跃度记录列表
     * @return 结果
     */
    int batchInsertDailyPlayerActivity(@Param("activities") List<DailyPlayerActivity> activities);
}