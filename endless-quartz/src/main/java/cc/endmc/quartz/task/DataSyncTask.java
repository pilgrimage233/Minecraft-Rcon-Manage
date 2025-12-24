package cc.endmc.quartz.task;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.service.statistics.IDataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 数据同步定时任务
 */
@Slf4j
@Component("dataSyncTask")
@RequiredArgsConstructor
public class DataSyncTask {

    private final IDataSyncService dataSyncService;

    /**
     * 每日数据同步任务
     * 将Redis中的数据同步到数据库
     */
    public void dailyDataSync() {
        log.info("开始执行每日数据同步任务");
        
        try {
            // 同步昨天的数据
            Date yesterday = DateUtils.addDays(new Date(), -1);
            boolean result = dataSyncService.syncDailyActivityToDatabase(yesterday);
            
            if (result) {
                log.info("每日数据同步任务执行成功");
            } else {
                log.error("每日数据同步任务执行失败");
            }
            
        } catch (Exception e) {
            log.error("每日数据同步任务执行异常", e);
        }
    }

    /**
     * 清理过期缓存任务
     */
    public void cleanExpiredCache() {
        log.info("开始执行清理过期缓存任务");
        
        try {
            // 保留最近7天的缓存数据
            boolean result = dataSyncService.cleanExpiredCache(7);
            
            if (result) {
                log.info("清理过期缓存任务执行成功");
            } else {
                log.error("清理过期缓存任务执行失败");
            }
            
        } catch (Exception e) {
            log.error("清理过期缓存任务执行异常", e);
        }
    }

    /**
     * 数据备份任务
     */
    public void backupData() {
        log.info("开始执行数据备份任务");
        
        try {
            // 备份最近30天的数据
            Date endDate = DateUtils.addDays(new Date(), -1);
            Date startDate = DateUtils.addDays(endDate, -30);
            
            boolean result = dataSyncService.backupStatisticsData(startDate, endDate);
            
            if (result) {
                log.info("数据备份任务执行成功");
            } else {
                log.error("数据备份任务执行失败");
            }
            
        } catch (Exception e) {
            log.error("数据备份任务执行异常", e);
        }
    }

    /**
     * 修复缺失数据任务
     */
    public void repairMissingData() {
        log.info("开始执行修复缺失数据任务");
        
        try {
            // 检查并修复最近7天的缺失数据
            Date endDate = DateUtils.addDays(new Date(), -1);
            Date startDate = DateUtils.addDays(endDate, -7);
            
            boolean result = dataSyncService.repairMissingStats(startDate, endDate);
            
            if (result) {
                log.info("修复缺失数据任务执行成功");
            } else {
                log.error("修复缺失数据任务执行失败");
            }
            
        } catch (Exception e) {
            log.error("修复缺失数据任务执行异常", e);
        }
    }

    /**
     * 手动触发完整数据同步（用于测试或紧急情况）
     */
    public void fullDataSync() {
        log.info("开始执行完整数据同步任务");
        
        try {
            // 同步最近30天的数据
            Date endDate = new Date();
            Date startDate = DateUtils.addDays(endDate, -30);
            
            boolean backupResult = dataSyncService.backupStatisticsData(startDate, endDate);
            boolean repairResult = dataSyncService.repairMissingStats(startDate, endDate);
            boolean cleanResult = dataSyncService.cleanExpiredCache(7);
            
            if (backupResult && repairResult && cleanResult) {
                log.info("完整数据同步任务执行成功");
            } else {
                log.error("完整数据同步任务部分失败 - 备份:{}, 修复:{}, 清理:{}", 
                        backupResult, repairResult, cleanResult);
            }
            
        } catch (Exception e) {
            log.error("完整数据同步任务执行异常", e);
        }
    }
}