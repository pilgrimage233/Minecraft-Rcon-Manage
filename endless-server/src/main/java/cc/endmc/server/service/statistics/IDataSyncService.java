package cc.endmc.server.service.statistics;

import java.util.Date;

/**
 * 数据同步服务接口
 * 用于Redis和数据库之间的数据同步
 */
public interface IDataSyncService {

    /**
     * 同步Redis中的每日活跃度数据到数据库
     *
     * @param date 同步日期
     * @return 同步结果
     */
    boolean syncDailyActivityToDatabase(Date date);

    /**
     * 从数据库加载数据到Redis缓存
     *
     * @param date 加载日期
     * @return 加载结果
     */
    boolean loadDailyActivityFromDatabase(Date date);

    /**
     * 清理过期的Redis缓存数据
     *
     * @param daysToKeep 保留天数
     * @return 清理结果
     */
    boolean cleanExpiredCache(int daysToKeep);

    /**
     * 备份统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 备份结果
     */
    boolean backupStatisticsData(Date startDate, Date endDate);

    /**
     * 修复缺失的统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 修复结果
     */
    boolean repairMissingStats(Date startDate, Date endDate);
}