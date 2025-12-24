package cc.endmc.server.service.statistics;

import cc.endmc.server.domain.statistics.DailyPlayerActivity;
import cc.endmc.server.domain.statistics.PlayerActivityStats;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 玩家活跃度统计服务接口
 */
public interface IPlayerActivityStatsService {

    /**
     * 记录玩家每日活跃度
     *
     * @param playerName    玩家名
     * @param onlineMinutes 在线时长（分钟）
     * @param isNewPlayer   是否为新玩家
     */
    void recordDailyActivity(String playerName, Long onlineMinutes, Boolean isNewPlayer);

    /**
     * 生成每日统计报告
     *
     * @param date 统计日期
     * @return 统计结果
     */
    PlayerActivityStats generateDailyStats(Date date);

    /**
     * 生成周报
     *
     * @param weekStart 周开始日期
     * @return 周报数据
     */
    PlayerActivityStats generateWeeklyReport(Date weekStart);

    /**
     * 生成月报
     *
     * @param month 月份（yyyy-MM格式）
     * @return 月报数据
     */
    PlayerActivityStats generateMonthlyReport(String month);

    /**
     * 获取活跃度趋势数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param statsType 统计类型
     * @return 趋势数据
     */
    List<PlayerActivityStats> getActivityTrend(Date startDate, Date endDate, String statsType);

    /**
     * 获取玩家活跃度排行榜
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param limit     限制数量
     * @return 排行榜数据
     */
    List<Map<String, Object>> getPlayerActivityRanking(Date startDate, Date endDate, Integer limit);

    /**
     * 获取服务器活跃度概览
     *
     * @return 概览数据
     */
    Map<String, Object> getActivityOverview();

    /**
     * 保存统计数据
     *
     * @param stats 统计数据
     * @return 结果
     */
    int saveActivityStats(PlayerActivityStats stats);

    /**
     * 查询统计数据列表
     *
     * @param stats 查询条件
     * @return 统计数据列表
     */
    List<PlayerActivityStats> selectActivityStatsList(PlayerActivityStats stats);

    /**
     * 获取增减趋势分析
     *
     * @param statsType 统计类型
     * @param periods   周期数
     * @return 趋势分析数据
     */
    Map<String, Object> getTrendAnalysis(String statsType, Integer periods);
}