package com.ruoyi.server.controller.bot;

import com.ruoyi.common.annotation.AddOrUpdateFilter;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.server.domain.bot.QqBotManager;
import com.ruoyi.server.service.bot.IQqBotManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * QQ机器人管理员Controller
 *
 * @author Memory
 * @date 2025-03-13
 */
@RestController
@RequestMapping("/bot/manager")
public class QqBotManagerController extends BaseController {
    @Autowired
    private IQqBotManagerService qqBotManagerService;

    /**
     * 查询QQ机器人管理员列表
     */
    @PreAuthorize("@ss.hasPermi('bot:manager:list')")
    @GetMapping("/list")
    public TableDataInfo list(QqBotManager qqBotManager) {
        startPage();
        List<QqBotManager> list = qqBotManagerService.selectQqBotManagerList(qqBotManager);
        return getDataTable(list);
    }

    /**
     * 导出QQ机器人管理员列表
     */
    @PreAuthorize("@ss.hasPermi('bot:manager:export')")
    @Log(title = "QQ机器人管理员", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QqBotManager qqBotManager) {
        List<QqBotManager> list = qqBotManagerService.selectQqBotManagerList(qqBotManager);
        ExcelUtil<QqBotManager> util = new ExcelUtil<QqBotManager>(QqBotManager.class);
        util.exportExcel(response, list, "QQ机器人管理员数据");
    }

    /**
     * 获取QQ机器人管理员详细信息
     */
    @PreAuthorize("@ss.hasPermi('bot:manager:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(qqBotManagerService.selectQqBotManagerById(id));
    }

    /**
     * 新增QQ机器人管理员
     */
    @PreAuthorize("@ss.hasPermi('bot:manager:add')")
    @Log(title = "QQ机器人管理员", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody QqBotManager qqBotManager) {
        return toAjax(qqBotManagerService.insertQqBotManager(qqBotManager));
    }

    /**
     * 修改QQ机器人管理员
     */
    @PreAuthorize("@ss.hasPermi('bot:manager:edit')")
    @Log(title = "QQ机器人管理员", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody QqBotManager qqBotManager) {
        return toAjax(qqBotManagerService.updateQqBotManager(qqBotManager));
    }

    /**
     * 删除QQ机器人管理员
     */
    @PreAuthorize("@ss.hasPermi('bot:manager:remove')")
    @Log(title = "QQ机器人管理员", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(qqBotManagerService.deleteQqBotManagerByIds(ids));
    }
}
