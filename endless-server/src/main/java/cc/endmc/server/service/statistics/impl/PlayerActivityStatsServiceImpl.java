package cc.endmc.server.service.statistics.impl;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.domain.statistics.DailyPlayerActivity;
import cc.endmc.server.domain.statistics.PlayerActivityStats;
import cc.endmc.server.mapper.statistics.DailyPlayerActivityMapper;
import cc.endmc.server.mapper.statistics.PlayerActivityStatsMapper;
import cc.endmc.server.service.statistics.IPlayerActivityStatsService;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 玩家活跃度统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerActivityStatsServiceImpl implements IPlayerActivityStatsService {

    private final RedisCache redisCache;
    private final PlayerActivityStatsMapper playerActivityStatsMapper;
    private final DailyPlayerActivityMapper dailyPlayerActivityMapper;

    private static final String DAILY_ACTIVITY_KEY = "player:daily:activity:";
    private static final String STATS_KEY = "player:stats:";
    private static final String PEAK_ONLINE_KEY = "player:peak:online:";

    @Override
    @Transactional
    public void recordDailyActivity(String playerName, Long onlineMinutes, Boolean isNewPlayer) {
        try {
            Date today = DateUtils.parseDate(DateUtils.dateTimeNow("yyyy-MM-dd"));
            
            // 先从数据库查询现有记录
            DailyPlayerActivity activity = dailyPlayerActivityMapper.selectByPlayerAndDate(playerName, today);
            
            if (activity == null) {
                // 创建新记录
                activity = new DailyPlayerActivity();
                activity.setPlayerName(playerName);
                activity.setActivityDate(today);
                activity.setOnlineMinutes(onlineMinutes);
                activity.setLoginCount(1);
                activity.setFirstLoginTime(new Date());
                activity.setLastLoginTime(new Date());
                activity.setIsNewPlayer(isNewPlayer);
                activity.setCreateTime(new Date());
                activity.setUpdateTime(new Date());
                
                // 计算活跃度评分
                double score = (activity.getOnlineMinutes() * 0.7) + (activity.getLoginCount() * 10 * 0.3);
                activity.setActivityScore(score);
                
                dailyPlayerActivityMapper.insertDailyPlayerActivity(activity);
            } else {
                // 更新现有记录
                activity.setOnlineMinutes(activity.getOnlineMinutes() + onlineMinutes);
                activity.setLoginCount(activity.getLoginCount() + 1);
                activity.setLastLoginTime(new Date());
                activity.setUpdateTime(new Date());
                
                // 重新计算活跃度评分
                double score = (activity.getOnlineMinutes() * 0.7) + (activity.getLoginCount() * 10 * 0.3);
                activity.setActivityScore(score);
                
                dailyPlayerActivityMapper.updateDailyPlayerActivity(activity);
            }
            
            // 同时更新Redis缓存以提高查询性能
            String cacheKey = DAILY_ACTIVITY_KEY + DateUtils.parseDateToStr("yyyy-MM-dd", today);
            Map<String, DailyPlayerActivity> dailyActivities = redisCache.getCacheObject(cacheKey);
            if (dailyActivities == null) {
                dailyActivities = new HashMap<>();
            }
            dailyActivities.put(playerName, activity);
            redisCache.setCacheObject(cacheKey, dailyActivities, 1, TimeUnit.DAYS);
            
            log.debug("记录玩家 {} 每日活跃度: 在线{}分钟, 登录{}次", 
                    playerName, onlineMinutes, activity.getLoginCount());
                    
        } catch (Exception e) {
            log.error("记录玩家每日活跃度失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public PlayerActivityStats generateDailyStats(Date date) {
        try {
            // 先检查数据库中是否已有统计数据
            PlayerActivityStats existingStats = playerActivityStatsMapper.selectByDateAndType(date, "daily");
            if (existingStats != null) {
                return existingStats;
            }
            
            // 从数据库获取当日活跃度数据
            List<DailyPlayerActivity> activities = dailyPlayerActivityMapper.selectByDate(date);
            
            if (activities.isEmpty()) {
                return createEmptyStats(date, "daily");
            }
            
            PlayerActivityStats stats = new PlayerActivityStats();
            stats.setStatsDate(date);
            stats.setStatsType("daily");
            stats.setPeriodStart(DateUtils.parseDate(DateUtils.parseDateToStr("yyyy-MM-dd", date) + " 00:00:00"));
            stats.setPeriodEnd(DateUtils.parseDate(DateUtils.parseDateToStr("yyyy-MM-dd", date) + " 23:59:59"));
            
            // 统计数据
            stats.setActivePlayerCount(activities.size());
            stats.setNewPlayerCount((int) activities.stream().filter(DailyPlayerActivity::getIsNewPlayer).count());
            stats.setTotalOnlineMinutes(activities.stream().mapToLong(DailyPlayerActivity::getOnlineMinutes).sum());
            stats.setAvgOnlineMinutes(stats.getTotalOnlineMinutes() / Math.max(1, stats.getActivePlayerCount()));
            
            // 查找峰值在线人数（这里简化处理，使用活跃玩家数作为峰值）
            stats.setPeakOnlineCount(stats.getActivePlayerCount());
            stats.setPeakOnlineTime(date);
            
            // 活跃玩家列表（按活跃度评分排序）
            List<String> activePlayerNames = activities.stream()
                    .sorted((a, b) -> Double.compare(b.getActivityScore(), a.getActivityScore()))
                    .map(DailyPlayerActivity::getPlayerName)
                    .collect(Collectors.toList());
            stats.setActivePlayerList(JSON.toJSONString(activePlayerNames));
            
            // 新增玩家列表
            List<String> newPlayerNames = activities.stream()
                    .filter(DailyPlayerActivity::getIsNewPlayer)
                    .map(DailyPlayerActivity::getPlayerName)
                    .collect(Collectors.toList());
            stats.setNewPlayerList(JSON.toJSONString(newPlayerNames));
            
            stats.setCreateTime(new Date());
            
            return stats;
            
        } catch (Exception e) {
            log.error("生成每日统计失败: {}", e.getMessage(), e);
            return createEmptyStats(date, "daily");
        }
    }

    @Override
    public PlayerActivityStats generateWeeklyReport(Date weekStart) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(weekStart);
            
            // 确保是周一
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                cal.add(Calendar.DAY_OF_MONTH, -1);
            }
            Date actualWeekStart = cal.getTime();
            
            cal.add(Calendar.DAY_OF_MONTH, 6);
            Date weekEnd = cal.getTime();
            
            PlayerActivityStats weeklyStats = new PlayerActivityStats();
            weeklyStats.setStatsDate(actualWeekStart);
            weeklyStats.setStatsType("weekly");
            weeklyStats.setPeriodStart(actualWeekStart);
            weeklyStats.setPeriodEnd(weekEnd);
            
            // 收集一周的数据
            Set<String> allActivePlayers = new HashSet<>();
            Set<String> allNewPlayers = new HashSet<>();
            long totalOnlineMinutes = 0;
            int peakOnlineCount = 0;
            Date peakOnlineTime = null;
            
            cal.setTime(actualWeekStart);
            for (int i = 0; i < 7; i++) {
                String dateStr = DateUtils.parseDateToStr("yyyy-MM-dd", cal.getTime());
                String cacheKey = DAILY_ACTIVITY_KEY + dateStr;
                
                Map<String, DailyPlayerActivity> dailyActivities = redisCache.getCacheObject(cacheKey);
                if (dailyActivities != null) {
                    for (DailyPlayerActivity activity : dailyActivities.values()) {
                        allActivePlayers.add(activity.getPlayerName());
                        if (activity.getIsNewPlayer()) {
                            allNewPlayers.add(activity.getPlayerName());
                        }
                        totalOnlineMinutes += activity.getOnlineMinutes();
                    }
                    
                    // 检查峰值在线人数
                    if (dailyActivities.size() > peakOnlineCount) {
                        peakOnlineCount = dailyActivities.size();
                        peakOnlineTime = cal.getTime();
                    }
                }
                
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            weeklyStats.setActivePlayerCount(allActivePlayers.size());
            weeklyStats.setNewPlayerCount(allNewPlayers.size());
            weeklyStats.setTotalOnlineMinutes(totalOnlineMinutes);
            weeklyStats.setAvgOnlineMinutes(totalOnlineMinutes / Math.max(1, allActivePlayers.size()));
            weeklyStats.setPeakOnlineCount(peakOnlineCount);
            weeklyStats.setPeakOnlineTime(peakOnlineTime);
            weeklyStats.setActivePlayerList(JSON.toJSONString(new ArrayList<>(allActivePlayers)));
            weeklyStats.setNewPlayerList(JSON.toJSONString(new ArrayList<>(allNewPlayers)));
            weeklyStats.setCreateTime(new Date());
            
            return weeklyStats;
            
        } catch (Exception e) {
            log.error("生成周报失败: {}", e.getMessage(), e);
            return createEmptyStats(weekStart, "weekly");
        }
    }

    @Override
    public PlayerActivityStats generateMonthlyReport(String month) {
        try {
            Date monthStart = DateUtil.lastMonth();
            Calendar cal = Calendar.getInstance();
            cal.setTime(monthStart);
            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            Date monthEnd = cal.getTime();
            
            PlayerActivityStats monthlyStats = new PlayerActivityStats();
            monthlyStats.setStatsDate(monthStart);
            monthlyStats.setStatsType("monthly");
            monthlyStats.setPeriodStart(monthStart);
            monthlyStats.setPeriodEnd(monthEnd);
            
            // 收集整月数据
            Set<String> allActivePlayers = new HashSet<>();
            Set<String> allNewPlayers = new HashSet<>();
            long totalOnlineMinutes = 0;
            int peakOnlineCount = 0;
            Date peakOnlineTime = null;
            
            cal.setTime(monthStart);
            while (!cal.getTime().after(monthEnd)) {
                String dateStr = DateUtils.parseDateToStr("yyyy-MM-dd", cal.getTime());
                String cacheKey = DAILY_ACTIVITY_KEY + dateStr;
                
                Map<String, DailyPlayerActivity> dailyActivities = redisCache.getCacheObject(cacheKey);
                if (dailyActivities != null) {
                    for (DailyPlayerActivity activity : dailyActivities.values()) {
                        allActivePlayers.add(activity.getPlayerName());
                        if (activity.getIsNewPlayer()) {
                            allNewPlayers.add(activity.getPlayerName());
                        }
                        totalOnlineMinutes += activity.getOnlineMinutes();
                    }
                    
                    if (dailyActivities.size() > peakOnlineCount) {
                        peakOnlineCount = dailyActivities.size();
                        peakOnlineTime = cal.getTime();
                    }
                }
                
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            monthlyStats.setActivePlayerCount(allActivePlayers.size());
            monthlyStats.setNewPlayerCount(allNewPlayers.size());
            monthlyStats.setTotalOnlineMinutes(totalOnlineMinutes);
            monthlyStats.setAvgOnlineMinutes(totalOnlineMinutes / Math.max(1, allActivePlayers.size()));
            monthlyStats.setPeakOnlineCount(peakOnlineCount);
            monthlyStats.setPeakOnlineTime(peakOnlineTime);
            monthlyStats.setActivePlayerList(JSON.toJSONString(new ArrayList<>(allActivePlayers)));
            monthlyStats.setNewPlayerList(JSON.toJSONString(new ArrayList<>(allNewPlayers)));
            monthlyStats.setCreateTime(new Date());
            
            return monthlyStats;
            
        } catch (Exception e) {
            log.error("生成月报失败: {}", e.getMessage(), e);
            return createEmptyStats(new Date(), "monthly");
        }
    }

    @Override
    public List<PlayerActivityStats> getActivityTrend(Date startDate, Date endDate, String statsType) {
        try {
            // 先从数据库查询已有的统计数据
            List<PlayerActivityStats> existingStats = playerActivityStatsMapper.selectByDateRange(startDate, endDate, statsType);
            
            // 如果数据库中有完整数据，直接返回
            if (!existingStats.isEmpty()) {
                return existingStats;
            }
            
            // 否则生成新的统计数据
            List<PlayerActivityStats> trendData = new ArrayList<>();
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            
            while (!cal.getTime().after(endDate)) {
                PlayerActivityStats stats;
                
                switch (statsType) {
                    case "daily":
                        stats = generateDailyStats(cal.getTime());
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                        break;
                    case "weekly":
                        stats = generateWeeklyReport(cal.getTime());
                        cal.add(Calendar.WEEK_OF_YEAR, 1);
                        break;
                    case "monthly":
                        String monthStr = DateUtils.parseDateToStr("yyyy-MM", cal.getTime());
                        stats = generateMonthlyReport(monthStr);
                        cal.add(Calendar.MONTH, 1);
                        break;
                    default:
                        stats = generateDailyStats(cal.getTime());
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                        break;
                }
                
                if (stats != null) {
                    trendData.add(stats);
                }
            }
            
            return trendData;
            
        } catch (Exception e) {
            log.error("获取活跃度趋势失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getPlayerActivityRanking(Date startDate, Date endDate, Integer limit) {
        try {
            return dailyPlayerActivityMapper.selectPlayerRanking(startDate, endDate, limit);
        } catch (Exception e) {
            log.error("获取玩家活跃度排行榜失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getActivityOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        try {
            Date today = new Date();
            Date yesterday = DateUtils.addDays(today, -1);
            Date weekAgo = DateUtils.addDays(today, -7);
            Date monthAgo = DateUtils.addDays(today, -30);
            
            // 今日数据
            PlayerActivityStats todayStats = generateDailyStats(today);
            overview.put("today", todayStats);
            
            // 昨日数据
            PlayerActivityStats yesterdayStats = generateDailyStats(yesterday);
            overview.put("yesterday", yesterdayStats);
            
            // 7天趋势
            List<PlayerActivityStats> weekTrend = getActivityTrend(weekAgo, today, "daily");
            overview.put("weekTrend", weekTrend);
            
            // 30天活跃玩家排行
            List<Map<String, Object>> monthRanking = getPlayerActivityRanking(monthAgo, today, 10);
            overview.put("monthRanking", monthRanking);
            
            // 增长率计算
            if (todayStats != null && yesterdayStats != null) {
                double playerGrowthRate = calculateGrowthRate(
                        yesterdayStats.getActivePlayerCount(), 
                        todayStats.getActivePlayerCount()
                );
                overview.put("playerGrowthRate", playerGrowthRate);
                
                double onlineTimeGrowthRate = calculateGrowthRate(
                        yesterdayStats.getTotalOnlineMinutes(), 
                        todayStats.getTotalOnlineMinutes()
                );
                overview.put("onlineTimeGrowthRate", onlineTimeGrowthRate);
            }
            
        } catch (Exception e) {
            log.error("获取活跃度概览失败: {}", e.getMessage(), e);
        }
        
        return overview;
    }

    @Override
    public Map<String, Object> getTrendAnalysis(String statsType, Integer periods) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            Date endDate = new Date();
            Date startDate;
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);
            
            switch (statsType) {
                case "weekly":
                    cal.add(Calendar.WEEK_OF_YEAR, -periods);
                    break;
                case "monthly":
                    cal.add(Calendar.MONTH, -periods);
                    break;
                default: // daily
                    cal.add(Calendar.DAY_OF_MONTH, -periods);
                    break;
            }
            startDate = cal.getTime();
            
            List<PlayerActivityStats> trendData = getActivityTrend(startDate, endDate, statsType);
            
            if (!trendData.isEmpty()) {
                // 计算各项指标的趋势
                analysis.put("trendData", trendData);
                analysis.put("playerCountTrend", calculateTrendDirection(trendData, "activePlayerCount"));
                analysis.put("newPlayerTrend", calculateTrendDirection(trendData, "newPlayerCount"));
                analysis.put("onlineTimeTrend", calculateTrendDirection(trendData, "totalOnlineMinutes"));
                
                // 计算平均值和增长率
                PlayerActivityStats first = trendData.get(0);
                PlayerActivityStats last = trendData.get(trendData.size() - 1);
                
                analysis.put("avgActivePlayerCount", 
                        trendData.stream().mapToInt(PlayerActivityStats::getActivePlayerCount).average().orElse(0));
                analysis.put("avgNewPlayerCount", 
                        trendData.stream().mapToInt(PlayerActivityStats::getNewPlayerCount).average().orElse(0));
                analysis.put("avgOnlineMinutes", 
                        trendData.stream().mapToLong(PlayerActivityStats::getTotalOnlineMinutes).average().orElse(0));
                
                analysis.put("playerCountGrowthRate", 
                        calculateGrowthRate(first.getActivePlayerCount(), last.getActivePlayerCount()));
                analysis.put("newPlayerGrowthRate", 
                        calculateGrowthRate(first.getNewPlayerCount(), last.getNewPlayerCount()));
                analysis.put("onlineTimeGrowthRate", 
                        calculateGrowthRate(first.getTotalOnlineMinutes(), last.getTotalOnlineMinutes()));
            }
            
        } catch (Exception e) {
            log.error("获取趋势分析失败: {}", e.getMessage(), e);
        }
        
        return analysis;
    }

    @Override
    @Transactional
    public int saveActivityStats(PlayerActivityStats stats) {
        try {
            // 保存到数据库
            int result = playerActivityStatsMapper.insertOrUpdatePlayerActivityStats(stats);
            
            // 同时缓存到Redis
            String cacheKey = STATS_KEY + stats.getStatsType() + ":" + 
                    DateUtils.parseDateToStr("yyyy-MM-dd", stats.getStatsDate());
            redisCache.setCacheObject(cacheKey, stats, 30, TimeUnit.DAYS);
            
            return result;
        } catch (Exception e) {
            log.error("保存统计数据失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public List<PlayerActivityStats> selectActivityStatsList(PlayerActivityStats stats) {
        try {
            return playerActivityStatsMapper.selectPlayerActivityStatsList(stats);
        } catch (Exception e) {
            log.error("查询统计数据列表失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 创建空的统计数据
     */
    private PlayerActivityStats createEmptyStats(Date date, String statsType) {
        PlayerActivityStats stats = new PlayerActivityStats();
        stats.setStatsDate(date);
        stats.setStatsType(statsType);
        stats.setActivePlayerCount(0);
        stats.setNewPlayerCount(0);
        stats.setTotalOnlineMinutes(0L);
        stats.setAvgOnlineMinutes(0L);
        stats.setPeakOnlineCount(0);
        stats.setActivePlayerList("[]");
        stats.setNewPlayerList("[]");
        stats.setCreateTime(new Date());
        return stats;
    }

    /**
     * 计算增长率
     */
    private double calculateGrowthRate(Number oldValue, Number newValue) {
        if (oldValue == null || newValue == null || oldValue.doubleValue() == 0) {
            return 0.0;
        }
        return ((newValue.doubleValue() - oldValue.doubleValue()) / oldValue.doubleValue()) * 100;
    }

    /**
     * 计算趋势方向
     */
    private String calculateTrendDirection(List<PlayerActivityStats> trendData, String field) {
        if (trendData.size() < 2) {
            return "stable";
        }
        
        try {
            PlayerActivityStats first = trendData.get(0);
            PlayerActivityStats last = trendData.get(trendData.size() - 1);
            
            Number firstValue = getFieldValue(first, field);
            Number lastValue = getFieldValue(last, field);
            
            if (firstValue == null || lastValue == null) {
                return "stable";
            }
            
            double growthRate = calculateGrowthRate(firstValue, lastValue);
            
            if (growthRate > 5) {
                return "increasing";
            } else if (growthRate < -5) {
                return "decreasing";
            } else {
                return "stable";
            }
            
        } catch (Exception e) {
            log.error("计算趋势方向失败: {}", e.getMessage(), e);
            return "stable";
        }
    }

    /**
     * 通过反射获取字段值
     */
    private Number getFieldValue(PlayerActivityStats stats, String fieldName) {
        try {
            switch (fieldName) {
                case "activePlayerCount":
                    return stats.getActivePlayerCount();
                case "newPlayerCount":
                    return stats.getNewPlayerCount();
                case "totalOnlineMinutes":
                    return stats.getTotalOnlineMinutes();
                case "avgOnlineMinutes":
                    return stats.getAvgOnlineMinutes();
                case "peakOnlineCount":
                    return stats.getPeakOnlineCount();
                default:
                    return 0;
            }
        } catch (Exception e) {
            log.error("获取字段值失败: {}", e.getMessage(), e);
            return 0;
        }
    }
}