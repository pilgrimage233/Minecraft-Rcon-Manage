package com.ruoyi.server.controller.other;

import com.ruoyi.common.annotation.AddOrUpdateFilter;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.server.domain.other.RegularCmd;
import com.ruoyi.server.service.other.IRegularCmdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 定时命令Controller
 *
 * @author ruoyi
 * @date 2025-02-14
 */
@RestController
@RequestMapping("/regular/command")
public class RegularCmdController extends BaseController {
    @Autowired
    private IRegularCmdService regularCmdService;

    /**
     * 查询定时命令列表
     */
    @PreAuthorize("@ss.hasPermi('regular:command:list')")
    @GetMapping("/list")
    public TableDataInfo list(RegularCmd regularCmd) {
        startPage();
        List<RegularCmd> list = regularCmdService.selectRegularCmdList(regularCmd);
        return getDataTable(list);
    }

    /**
     * 导出定时命令列表
     */
    @PreAuthorize("@ss.hasPermi('regular:command:export')")
    @Log(title = "定时命令", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RegularCmd regularCmd) {
        List<RegularCmd> list = regularCmdService.selectRegularCmdList(regularCmd);
        ExcelUtil<RegularCmd> util = new ExcelUtil<RegularCmd>(RegularCmd.class);
        util.exportExcel(response, list, "定时命令数据");
    }

    /**
     * 获取定时命令详细信息
     */
    @PreAuthorize("@ss.hasPermi('regular:command:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(regularCmdService.selectRegularCmdById(id));
    }

    /**
     * 新增定时命令
     */
    @PreAuthorize("@ss.hasPermi('regular:command:add')")
    @Log(title = "定时命令", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody RegularCmd regularCmd) {
        return toAjax(regularCmdService.insertRegularCmd(regularCmd));
    }

    /**
     * 修改定时命令
     */
    @PreAuthorize("@ss.hasPermi('regular:command:edit')")
    @Log(title = "定时命令", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody RegularCmd regularCmd) {
        return toAjax(regularCmdService.updateRegularCmd(regularCmd));
    }

    /**
     * 删除定时命令
     */
    @PreAuthorize("@ss.hasPermi('regular:command:remove')")
    @Log(title = "定时命令", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(regularCmdService.deleteRegularCmdByIds(ids));
    }
}
