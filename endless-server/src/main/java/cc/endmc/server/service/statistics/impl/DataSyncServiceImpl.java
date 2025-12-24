package cc.endmc.server.service.statistics.impl;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.domain.statistics.DailyPlayerActivity;
import cc.endmc.server.domain.statistics.PlayerActivityStats;
import cc.endmc.server.mapper.statistics.DailyPlayerActivityMapper;
import cc.endmc.server.service.statistics.IDataSyncService;
import cc.endmc.server.service.statistics.IPlayerActivityStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 数据同步服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncServiceImpl implements IDataSyncService {


    private final RedisCache redisCache;
    private final DailyPlayerActivityMapper dailyPlayerActivityMapper;
    private final IPlayerActivityStatsService playerActivityStatsService;

    private static final String DAILY_ACTIVITY_KEY = "player:daily:activity:";
    private static final String STATS_KEY = "player:stats:";

    @Override
    @Transactional
    public boolean syncDailyActivityToDatabase(Date date) {
        try {
            String dateStr = DateUtils.parseDateToStr("yyyy-MM-dd", date);
            String cacheKey = DAILY_ACTIVITY_KEY + dateStr;
            
            // 从Redis获取数据
            Map<String, DailyPlayerActivity> dailyActivities = redisCache.getCacheObject(cacheKey);
            if (dailyActivities == null || dailyActivities.isEmpty()) {
                log.info("日期 {} 没有Redis缓存数据需要同步", dateStr);
                return true;
            }
            
            // 批量同步到数据库
            List<DailyPlayerActivity> activities = new ArrayList<>(dailyActivities.values());
            for (DailyPlayerActivity activity : activities) {
                try {
                    // 检查数据库中是否已存在
                    DailyPlayerActivity existing = dailyPlayerActivityMapper.selectByPlayerAndDate(
                            activity.getPlayerName(), activity.getActivityDate());
                    
                    if (existing == null) {
                        dailyPlayerActivityMapper.insertDailyPlayerActivity(activity);
                    } else {
                        // 合并数据
                        existing.setOnlineMinutes(Math.max(existing.getOnlineMinutes(), activity.getOnlineMinutes()));
                        existing.setLoginCount(Math.max(existing.getLoginCount(), activity.getLoginCount()));
                        existing.setLastLoginTime(activity.getLastLoginTime());
                        existing.setActivityScore(Math.max(existing.getActivityScore(), activity.getActivityScore()));
                        existing.setUpdateTime(new Date());
                        
                        dailyPlayerActivityMapper.updateDailyPlayerActivity(existing);
                    }
                } catch (Exception e) {
                    log.error("同步玩家 {} 的活跃度数据失败: {}", activity.getPlayerName(), e.getMessage());
                }
            }
            
            log.info("成功同步日期 {} 的 {} 条活跃度记录到数据库", dateStr, activities.size());
            return true;
            
        } catch (Exception e) {
            log.error("同步每日活跃度数据到数据库失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean loadDailyActivityFromDatabase(Date date) {
        try {
            String dateStr = DateUtils.parseDateToStr("yyyy-MM-dd", date);
            String cacheKey = DAILY_ACTIVITY_KEY + dateStr;
            
            // 从数据库加载数据
            List<DailyPlayerActivity> activities = dailyPlayerActivityMapper.selectByDate(date);
            if (activities.isEmpty()) {
                log.info("日期 {} 没有数据库数据需要加载", dateStr);
                return true;
            }
            
            // 转换为Map格式并缓存到Redis
            Map<String, DailyPlayerActivity> dailyActivities = new HashMap<>();
            for (DailyPlayerActivity activity : activities) {
                dailyActivities.put(activity.getPlayerName(), activity);
            }
            
            redisCache.setCacheObject(cacheKey, dailyActivities, 7, TimeUnit.DAYS);
            
            log.info("成功从数据库加载日期 {} 的 {} 条活跃度记录到Redis", dateStr, activities.size());
            return true;
            
        } catch (Exception e) {
            log.error("从数据库加载每日活跃度数据失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean cleanExpiredCache(int daysToKeep) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -daysToKeep);
            Date expireDate = cal.getTime();
            
            int cleanedCount = 0;
            
            // 清理每日活跃度缓存
            for (int i = daysToKeep; i <= 365; i++) {
                cal.setTime(new Date());
                cal.add(Calendar.DAY_OF_MONTH, -i);
                String dateStr = DateUtils.parseDateToStr("yyyy-MM-dd", cal.getTime());
                String cacheKey = DAILY_ACTIVITY_KEY + dateStr;
                
                if (redisCache.hasKey(cacheKey)) {
                    redisCache.deleteObject(cacheKey);
                    cleanedCount++;
                }
            }
            
            // 清理统计数据缓存
            Collection<String> statsKeys = redisCache.keys(STATS_KEY + "*");
            if (statsKeys != null) {
                for (String key : statsKeys) {
                    try {
                        PlayerActivityStats stats = redisCache.getCacheObject(key);
                        if (stats != null && stats.getStatsDate().before(expireDate)) {
                            redisCache.deleteObject(key);
                            cleanedCount++;
                        }
                    } catch (Exception e) {
                        log.warn("清理缓存键 {} 时出错: {}", key, e.getMessage());
                    }
                }
            }
            
            log.info("清理了 {} 个过期缓存项", cleanedCount);
            return true;
            
        } catch (Exception e) {
            log.error("清理过期缓存失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean backupStatisticsData(Date startDate, Date endDate) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            
            int backedUpCount = 0;
            
            while (!cal.getTime().after(endDate)) {
                Date currentDate = cal.getTime();
                
                // 同步每日活跃度数据
                if (syncDailyActivityToDatabase(currentDate)) {
                    backedUpCount++;
                }
                
                // 生成并保存统计数据
                try {
                    PlayerActivityStats dailyStats = playerActivityStatsService.generateDailyStats(currentDate);
                    if (dailyStats != null) {
                        playerActivityStatsService.saveActivityStats(dailyStats);
                    }
                } catch (Exception e) {
                    log.warn("备份日期 {} 的统计数据时出错: {}", 
                            DateUtils.parseDateToStr("yyyy-MM-dd", currentDate), e.getMessage());
                }
                
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            log.info("成功备份了 {} 天的统计数据", backedUpCount);
            return true;
            
        } catch (Exception e) {
            log.error("备份统计数据失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean repairMissingStats(Date startDate, Date endDate) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            
            int repairedCount = 0;
            
            while (!cal.getTime().after(endDate)) {
                Date currentDate = cal.getTime();
                String dateStr = DateUtils.parseDateToStr("yyyy-MM-dd", currentDate);
                
                try {
                    // 检查是否缺少每日统计数据
                    PlayerActivityStats existingStats = playerActivityStatsService.selectActivityStatsList(
                            new PlayerActivityStats() {{
                                setStatsDate(currentDate);
                                setStatsType("daily");
                            }}
                    ).stream().findFirst().orElse(null);
                    
                    if (existingStats == null) {
                        // 生成缺失的统计数据
                        PlayerActivityStats dailyStats = playerActivityStatsService.generateDailyStats(currentDate);
                        if (dailyStats != null && dailyStats.getActivePlayerCount() > 0) {
                            playerActivityStatsService.saveActivityStats(dailyStats);
                            repairedCount++;
                            log.info("修复了日期 {} 的统计数据", dateStr);
                        }
                    }
                    
                } catch (Exception e) {
                    log.warn("修复日期 {} 的统计数据时出错: {}", dateStr, e.getMessage());
                }
                
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            log.info("成功修复了 {} 天的缺失统计数据", repairedCount);
            return true;
            
        } catch (Exception e) {
            log.error("修复缺失统计数据失败: {}", e.getMessage(), e);
            return false;
        }
    }
}