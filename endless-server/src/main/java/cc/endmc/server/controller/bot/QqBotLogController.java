package cc.endmc.server.controller.bot;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.server.domain.bot.QqBotLog;
import cc.endmc.server.service.bot.IQqBotLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 机器人日志Controller
 *
 * @author Memory
 * @date 2025-04-18
 */
@RestController
@RequestMapping("/bot/log")
public class QqBotLogController extends BaseController {
    @Autowired
    private IQqBotLogService qqBotLogService;

    /**
     * 查询机器人日志列表
     */
    @PreAuthorize("@ss.hasPermi('bot:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(QqBotLog qqBotLog) {
        startPage();
        List<QqBotLog> list = qqBotLogService.selectQqBotLogList(qqBotLog);
        return getDataTable(list);
    }

    /**
     * 导出机器人日志列表
     */
    @PreAuthorize("@ss.hasPermi('bot:log:export')")
    @Log(title = "机器人日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QqBotLog qqBotLog) {
        List<QqBotLog> list = qqBotLogService.selectQqBotLogList(qqBotLog);
        ExcelUtil<QqBotLog> util = new ExcelUtil<QqBotLog>(QqBotLog.class);
        util.exportExcel(response, list, "机器人日志数据");
    }

    /**
     * 获取机器人日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('bot:log:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(qqBotLogService.selectQqBotLogById(id));
    }

    /**
     * 新增机器人日志
     */
    @PreAuthorize("@ss.hasPermi('bot:log:add')")
    @Log(title = "机器人日志", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody QqBotLog qqBotLog) {
        return toAjax(qqBotLogService.insertQqBotLog(qqBotLog));
    }

    /**
     * 修改机器人日志
     */
    @PreAuthorize("@ss.hasPermi('bot:log:edit')")
    @Log(title = "机器人日志", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody QqBotLog qqBotLog) {
        return toAjax(qqBotLogService.updateQqBotLog(qqBotLog));
    }

    /**
     * 删除机器人日志
     */
    @PreAuthorize("@ss.hasPermi('bot:log:remove')")
    @Log(title = "机器人日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(qqBotLogService.deleteQqBotLogByIds(ids));
    }
}
