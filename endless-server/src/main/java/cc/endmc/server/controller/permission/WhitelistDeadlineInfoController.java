package cc.endmc.server.controller.permission;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.server.domain.permission.WhitelistDeadlineInfo;
import cc.endmc.server.service.permission.IWhitelistDeadlineInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 时限管理Controller
 *
 * @author Memory
 * @date 2025-08-15
 */
@RestController
@RequestMapping("/mc/deadline")
public class WhitelistDeadlineInfoController extends BaseController {
    @Autowired
    private IWhitelistDeadlineInfoService whitelistDeadlineInfoService;

    /**
     * 查询时限管理列表
     */
    @PreAuthorize("@ss.hasPermi('mc:deadline:list')")
    @GetMapping("/list")
    public TableDataInfo list(WhitelistDeadlineInfo whitelistDeadlineInfo) {
        startPage();
        List<WhitelistDeadlineInfo> list = whitelistDeadlineInfoService.selectWhitelistDeadlineInfoList(whitelistDeadlineInfo);
        return getDataTable(list);
    }

    /**
     * 导出时限管理列表
     */
    @PreAuthorize("@ss.hasPermi('mc:deadline:export')")
    @Log(title = "时限管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WhitelistDeadlineInfo whitelistDeadlineInfo) {
        List<WhitelistDeadlineInfo> list = whitelistDeadlineInfoService.selectWhitelistDeadlineInfoList(whitelistDeadlineInfo);
        ExcelUtil<WhitelistDeadlineInfo> util = new ExcelUtil<WhitelistDeadlineInfo>(WhitelistDeadlineInfo.class);
        util.exportExcel(response, list, "时限管理数据");
    }

    /**
     * 获取时限管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('mc:deadline:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(whitelistDeadlineInfoService.selectWhitelistDeadlineInfoById(id));
    }

    /**
     * 新增时限管理
     */
    @PreAuthorize("@ss.hasPermi('mc:deadline:add')")
    @Log(title = "时限管理", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody WhitelistDeadlineInfo whitelistDeadlineInfo) {
        return toAjax(whitelistDeadlineInfoService.insertWhitelistDeadlineInfo(whitelistDeadlineInfo));
    }

    /**
     * 修改时限管理
     */
    @PreAuthorize("@ss.hasPermi('mc:deadline:edit')")
    @Log(title = "时限管理", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody WhitelistDeadlineInfo whitelistDeadlineInfo) {
        return toAjax(whitelistDeadlineInfoService.updateWhitelistDeadlineInfo(whitelistDeadlineInfo));
    }

    /**
     * 删除时限管理
     */
    @PreAuthorize("@ss.hasPermi('mc:deadline:remove')")
    @Log(title = "时限管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(whitelistDeadlineInfoService.deleteWhitelistDeadlineInfoByIds(ids));
    }
}
