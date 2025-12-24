package cc.endmc.quartz.task;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.domain.bot.QqBotConfig;
import cc.endmc.server.domain.statistics.PlayerActivityStats;
import cc.endmc.server.mapper.bot.QqBotConfigMapper;
import cc.endmc.server.service.statistics.IPlayerActivityStatsService;
import cc.endmc.server.utils.BotUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 玩家活跃度报告定时任务
 */
@Slf4j
@Component("playerReportTask")
@RequiredArgsConstructor
public class PlayerReportTask {

    private final IPlayerActivityStatsService activityStatsService;
    private final QqBotConfigMapper qqBotConfigMapper;

    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    /**
     * 生成并发送每日报告
     */
    public void generateDailyReport() {
        log.info("开始生成每日活跃度报告");
        
        try {
            Date yesterday = DateUtil.yesterday();
            PlayerActivityStats dailyStats = activityStatsService.generateDailyStats(yesterday);
            
            if (dailyStats != null) {
                // 保存统计数据
                activityStatsService.saveActivityStats(dailyStats);
                
                // 发送报告
                String report = formatDailyReport(dailyStats);
                sendReportToGroups(report, "每日活跃度报告");
                
                log.info("每日报告生成完成: 活跃玩家{}人, 新增玩家{}人", 
                        dailyStats.getActivePlayerCount(), dailyStats.getNewPlayerCount());
            }
            
        } catch (Exception e) {
            log.error("生成每日报告失败", e);
        }
    }

    /**
     * 生成并发送周报
     */
    public void generateWeeklyReport() {
        log.info("开始生成周活跃度报告");
        
        try {
            // 获取上周一的日期
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.WEEK_OF_YEAR, -1);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            Date lastWeekStart = cal.getTime();
            
            PlayerActivityStats weeklyStats = activityStatsService.generateWeeklyReport(lastWeekStart);
            
            if (weeklyStats != null) {
                // 保存统计数据
                activityStatsService.saveActivityStats(weeklyStats);
                
                // 获取趋势分析
                Map<String, Object> trendAnalysis = activityStatsService.getTrendAnalysis("weekly", 4);
                
                // 发送报告
                String report = formatWeeklyReport(weeklyStats, trendAnalysis);
                sendReportToGroups(report, "周活跃度报告");
                
                log.info("周报生成完成: 活跃玩家{}人, 新增玩家{}人", 
                        weeklyStats.getActivePlayerCount(), weeklyStats.getNewPlayerCount());
            }
            
        } catch (Exception e) {
            log.error("生成周报失败", e);
        }
    }

    /**
     * 生成并发送月报
     */
    public void generateMonthlyReport() {
        log.info("开始生成月活跃度报告");
        
        try {
            // 获取上个月
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            String lastMonth = DateUtils.parseDateToStr("yyyy-MM", cal.getTime());
            
            PlayerActivityStats monthlyStats = activityStatsService.generateMonthlyReport(lastMonth);
            
            if (monthlyStats != null) {
                // 保存统计数据
                activityStatsService.saveActivityStats(monthlyStats);
                
                // 获取趋势分析
                Map<String, Object> trendAnalysis = activityStatsService.getTrendAnalysis("monthly", 6);
                
                // 获取玩家排行榜
                Date monthStart = monthlyStats.getPeriodStart();
                Date monthEnd = monthlyStats.getPeriodEnd();
                List<Map<String, Object>> ranking = activityStatsService.getPlayerActivityRanking(monthStart, monthEnd, 10);
                
                // 发送报告
                String report = formatMonthlyReport(monthlyStats, trendAnalysis, ranking);
                sendReportToGroups(report, "月活跃度报告");
                
                log.info("月报生成完成: 活跃玩家{}人, 新增玩家{}人", 
                        monthlyStats.getActivePlayerCount(), monthlyStats.getNewPlayerCount());
            }
            
        } catch (Exception e) {
            log.error("生成月报失败", e);
        }
    }

    /**
     * 格式化每日报告
     */
    private String formatDailyReport(PlayerActivityStats stats) {
        StringBuilder report = new StringBuilder();
        report.append("📊 每日活跃度报告\n");
        report.append("📅 日期: ").append(DateUtils.parseDateToStr("yyyy-MM-dd", stats.getStatsDate())).append("\n\n");
        
        report.append("👥 活跃玩家: ").append(stats.getActivePlayerCount()).append("人\n");
        report.append("🆕 新增玩家: ").append(stats.getNewPlayerCount()).append("人\n");
        report.append("⏰ 总在线时长: ").append(formatMinutes(stats.getTotalOnlineMinutes())).append("\n");
        report.append("📈 平均在线时长: ").append(formatMinutes(stats.getAvgOnlineMinutes())).append("\n");
        
        if (stats.getPeakOnlineCount() != null && stats.getPeakOnlineCount() > 0) {
            report.append("🔥 峰值在线: ").append(stats.getPeakOnlineCount()).append("人\n");
        }
        
        // 显示活跃玩家列表（前10名）
        List<String> activePlayers = JSON.parseArray(stats.getActivePlayerList(), String.class);
        if (!activePlayers.isEmpty()) {
            report.append("\n🎮 活跃玩家:\n");
            int count = Math.min(10, activePlayers.size());
            for (int i = 0; i < count; i++) {
                report.append("  ").append(i + 1).append(". ").append(activePlayers.get(i)).append("\n");
            }
            if (activePlayers.size() > 10) {
                report.append("  ... 还有").append(activePlayers.size() - 10).append("人\n");
            }
        }
        
        return report.toString();
    }

    /**
     * 格式化周报
     */
    private String formatWeeklyReport(PlayerActivityStats stats, Map<String, Object> trendAnalysis) {
        StringBuilder report = new StringBuilder();
        report.append("📊 周活跃度报告\n");
        report.append("📅 周期: ").append(DateUtils.parseDateToStr("MM-dd", stats.getPeriodStart()))
                .append(" ~ ").append(DateUtils.parseDateToStr("MM-dd", stats.getPeriodEnd())).append("\n\n");
        
        report.append("👥 活跃玩家: ").append(stats.getActivePlayerCount()).append("人\n");
        report.append("🆕 新增玩家: ").append(stats.getNewPlayerCount()).append("人\n");
        report.append("⏰ 总在线时长: ").append(formatMinutes(stats.getTotalOnlineMinutes())).append("\n");
        report.append("📈 平均在线时长: ").append(formatMinutes(stats.getAvgOnlineMinutes())).append("\n");
        report.append("🔥 峰值在线: ").append(stats.getPeakOnlineCount()).append("人\n");
        
        // 趋势分析
        if (trendAnalysis != null && !trendAnalysis.isEmpty()) {
            report.append("\n📈 趋势分析:\n");
            
            Double playerGrowthRate = (Double) trendAnalysis.get("playerCountGrowthRate");
            if (playerGrowthRate != null) {
                report.append("  玩家数量: ").append(formatTrend(playerGrowthRate)).append("\n");
            }
            
            Double newPlayerGrowthRate = (Double) trendAnalysis.get("newPlayerGrowthRate");
            if (newPlayerGrowthRate != null) {
                report.append("  新增玩家: ").append(formatTrend(newPlayerGrowthRate)).append("\n");
            }
            
            Double onlineTimeGrowthRate = (Double) trendAnalysis.get("onlineTimeGrowthRate");
            if (onlineTimeGrowthRate != null) {
                report.append("  在线时长: ").append(formatTrend(onlineTimeGrowthRate)).append("\n");
            }
        }
        
        return report.toString();
    }

    /**
     * 格式化月报
     */
    private String formatMonthlyReport(PlayerActivityStats stats, Map<String, Object> trendAnalysis, 
                                     List<Map<String, Object>> ranking) {
        StringBuilder report = new StringBuilder();
        report.append("📊 月活跃度报告\n");
        report.append("📅 月份: ").append(DateUtils.parseDateToStr("yyyy年MM月", stats.getStatsDate())).append("\n\n");
        
        report.append("👥 活跃玩家: ").append(stats.getActivePlayerCount()).append("人\n");
        report.append("🆕 新增玩家: ").append(stats.getNewPlayerCount()).append("人\n");
        report.append("⏰ 总在线时长: ").append(formatMinutes(stats.getTotalOnlineMinutes())).append("\n");
        report.append("📈 平均在线时长: ").append(formatMinutes(stats.getAvgOnlineMinutes())).append("\n");
        report.append("🔥 峰值在线: ").append(stats.getPeakOnlineCount()).append("人\n");
        
        // 趋势分析
        if (trendAnalysis != null && !trendAnalysis.isEmpty()) {
            report.append("\n📈 6个月趋势:\n");
            
            Double playerGrowthRate = (Double) trendAnalysis.get("playerCountGrowthRate");
            if (playerGrowthRate != null) {
                report.append("  玩家数量: ").append(formatTrend(playerGrowthRate)).append("\n");
            }
            
            Double newPlayerGrowthRate = (Double) trendAnalysis.get("newPlayerGrowthRate");
            if (newPlayerGrowthRate != null) {
                report.append("  新增玩家: ").append(formatTrend(newPlayerGrowthRate)).append("\n");
            }
        }
        
        // 玩家排行榜
        if (ranking != null && !ranking.isEmpty()) {
            report.append("\n🏆 活跃度排行榜:\n");
            int count = Math.min(10, ranking.size());
            for (int i = 0; i < count; i++) {
                Map<String, Object> player = ranking.get(i);
                String playerName = (String) player.get("playerName");
                Long totalMinutes = (Long) player.get("totalOnlineMinutes");
                report.append("  ").append(i + 1).append(". ").append(playerName)
                        .append(" - ").append(formatMinutes(totalMinutes)).append("\n");
            }
        }
        
        return report.toString();
    }

    /**
     * 格式化时间（分钟转换为小时分钟）
     */
    private String formatMinutes(Long minutes) {
        if (minutes == null || minutes == 0) {
            return "0分钟";
        }
        
        long hours = minutes / 60;
        long mins = minutes % 60;
        
        if (hours > 0) {
            return hours + "小时" + (mins > 0 ? mins + "分钟" : "");
        } else {
            return mins + "分钟";
        }
    }

    /**
     * 格式化趋势
     */
    private String formatTrend(Double growthRate) {
        if (growthRate == null) {
            return "无变化";
        }
        
        String rate = decimalFormat.format(Math.abs(growthRate));
        
        if (growthRate > 5) {
            return "↗️ 上升 " + rate + "%";
        } else if (growthRate < -5) {
            return "↘️ 下降 " + rate + "%";
        } else {
            return "➡️ 稳定 " + rate + "%";
        }
    }

    /**
     * 发送报告到群组
     */
    private void sendReportToGroups(String report, String reportType) {
        try {
            QqBotConfig config = new QqBotConfig();
            config.setStatus(1L);
            List<QqBotConfig> botConfigs = qqBotConfigMapper.selectQqBotConfigList(config);
            
            for (QqBotConfig botConfig : botConfigs) {
                String[] groupIds = botConfig.getGroupIds().split(",");
                for (String groupId : groupIds) {
                    try {
                        BotUtil.sendMessage(report, groupId.trim(), botConfig);
                        log.debug("已发送{}到群组: {}", reportType, groupId.trim());
                    } catch (Exception e) {
                        log.error("发送报告到群组{}失败: {}", groupId.trim(), e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("发送报告失败", e);
        }
    }

    /**
     * 手动触发生成所有报告（用于测试）
     */
    public void generateAllReports() {
        log.info("手动触发生成所有报告");
        
        try {
            generateDailyReport();
            Thread.sleep(1000);
            
            generateWeeklyReport();
            Thread.sleep(1000);
            
            generateMonthlyReport();
            
        } catch (Exception e) {
            log.error("生成所有报告失败", e);
        }
    }
}