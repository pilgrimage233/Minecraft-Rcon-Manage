package cc.endmc.server.controller.statistics;

import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.domain.statistics.PlayerActivityStats;
import cc.endmc.server.service.statistics.IPlayerActivityStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 玩家活跃度统计控制器
 *
 */
@RestController
@RequestMapping("/statistics/activity")
public class PlayerActivityController extends BaseController {

    @Autowired
    private IPlayerActivityStatsService activityStatsService;

    /**
     * 查询玩家活跃度统计列表
     */
    @PreAuthorize("@ss.hasPermi('statistics:activity:list')")
    @GetMapping("/list")
    public TableDataInfo list(PlayerActivityStats stats) {
        startPage();
        List<PlayerActivityStats> list = activityStatsService.selectActivityStatsList(stats);
        return getDataTable(list);
    }

    /**
     * 获取活跃度概览
     */
    @PreAuthorize("@ss.hasPermi('statistics:activity:query')")
    @GetMapping("/overview")
    public AjaxResult getOverview() {
        Map<String, Object> overview = activityStatsService.getActivityOverview();
        return success(overview);
    }

    /**
     * 获取活跃度趋势
     */
    @PreAuthorize("@ss.hasPermi('statistics:activity:query')")
    @GetMapping("/trend")
    public AjaxResult getTrend(@RequestParam String startDate, 
                              @RequestParam String endDate, 
                              @RequestParam(defaultValue = "daily") String statsType) {
        try {
            Date start = DateUtils.parseDate(startDate);
            Date end = DateUtils.parseDate(endDate);
            List<PlayerActivityStats> trend = activityStatsService.getActivityTrend(start, end, statsType);
            return success(trend);
        } catch (Exception e) {
            return error("日期格式错误: " + e.getMessage());
        }
    }

    /**
     * 获取玩家活跃度排行榜
     */
    @PreAuthorize("@ss.hasPermi('statistics:activity:query')")
    @GetMapping("/ranking")
    public AjaxResult getRanking(@RequestParam String startDate, 
                                @RequestParam String endDate, 
                                @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Date start = DateUtils.parseDate(startDate);
            Date end = DateUtils.parseDate(endDate);
            List<Map<String, Object>> ranking = activityStatsService.getPlayerActivityRanking(start, end, limit);
            return success(ranking);
        } catch (Exception e) {
            return error("日期格式错误: " + e.getMessage());
        }
    }

    /**
     * 获取趋势分析
     */
    @PreAuthorize("@ss.hasPermi('statistics:activity:query')")
    @GetMapping("/analysis")
    public AjaxResult getTrendAnalysis(@RequestParam(defaultValue = "daily") String statsType, 
                                      @RequestParam(defaultValue = "30") Integer periods) {
        Map<String, Object> analysis = activityStatsService.getTrendAnalysis(statsType, periods);
        return success(analysis);
    }

    /**
     * 生成每日报告
     */
    @PreAuthorize("@ss.hasPermi('statistics:activity:generate')")
    @Log(title = "玩家活跃度统计", businessType = BusinessType.OTHER)
    @PostMapping("/generate/daily")
    public AjaxResult generateDailyReport(@RequestParam(required = false) String date) {
        try {
            Date targetDate = date != null ? DateUtils.parseDate(date) : DateUtils.addDays(new Date(), -1);
            PlayerActivityStats stats = activityStatsService.generateDailyStats(targetDate);
            
            if (stats != null) {
                activityStatsService.saveActivityStats(stats);
                return success("每日报告生成成功").put("data", stats);
            } else {
                return error("生成每日报告失败");
            }
        } catch (Exception e) {
            return error("生成每日报告失败: " + e.getMessage());
        }
    }

    /**
     * 生成周报
     */
    @PreAuthorize("@ss.hasPermi('statistics:activity:generate')")
    @Log(title = "玩家活跃度统计", businessType = BusinessType.OTHER)
    @PostMapping("/generate/weekly")
    public AjaxResult generateWeeklyReport(@RequestParam(required = false) String weekStart) {
        try {
            Date targetDate = weekStart != null ? DateUtils.parseDate(weekStart) : DateUtils.addDays(new Date(), -7);
            PlayerActivityStats stats = activityStatsService.generateWeeklyReport(targetDate);
            
            if (stats != null) {
                activityStatsService.saveActivityStats(stats);
                return success("周报生成成功").put("data", stats);
            } else {
                return error("生成周报失败");
            }
        } catch (Exception e) {
            return error("生成周报失败: " + e.getMessage());
        }
    }

    /**
     * 生成月报
     */
    @PreAuthorize("@ss.hasPermi('statistics:activity:generate')")
    @Log(title = "玩家活跃度统计", businessType = BusinessType.OTHER)
    @PostMapping("/generate/monthly")
    public AjaxResult generateMonthlyReport(@RequestParam(required = false) String month) {
        try {
            String targetMonth = month != null ? month : DateUtils.parseDateToStr("yyyy-MM", DateUtils.addMonths(new Date(), -1));
            PlayerActivityStats stats = activityStatsService.generateMonthlyReport(targetMonth);
            
            if (stats != null) {
                activityStatsService.saveActivityStats(stats);
                return success("月报生成成功").put("data", stats);
            } else {
                return error("生成月报失败");
            }
        } catch (Exception e) {
            return error("生成月报失败: " + e.getMessage());
        }
    }

    /**
     * 获取统计数据详情
     */
    @PreAuthorize("@ss.hasPermi('statistics:activity:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        // 根据实际需求实现详情查询
        return AjaxResult.success("功能开发中").put("id", id);
    }

    /**
     * 导出统计数据
     */
    @PreAuthorize("@ss.hasPermi('statistics:activity:export')")
    @Log(title = "玩家活跃度统计", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(PlayerActivityStats stats) {
        List<PlayerActivityStats> list = activityStatsService.selectActivityStatsList(stats);
        // 这里可以实现Excel导出功能
        // ExcelUtil<PlayerActivityStats> util = new ExcelUtil<PlayerActivityStats>(PlayerActivityStats.class);
        // util.exportExcel(response, list, "玩家活跃度统计数据");
    }
}