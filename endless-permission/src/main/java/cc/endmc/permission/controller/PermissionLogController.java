package cc.endmc.permission.controller;

import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.permission.domain.SysResourcePermissionLog;
import cc.endmc.permission.service.ISysResourcePermissionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 权限操作日志Controller
 *
 * @author Memory
 * @date 2025-12-20
 */
@RestController
@RequestMapping("/permission/log")
@RequiredArgsConstructor
public class PermissionLogController extends BaseController {

    private final ISysResourcePermissionLogService service;

    @PreAuthorize("@ss.hasPermi('permission:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysResourcePermissionLog query) {
        startPage();
        List<SysResourcePermissionLog> list = service.selectList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('permission:log:export')")
    @Log(title = "权限日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysResourcePermissionLog query) {
        List<SysResourcePermissionLog> list = service.selectList(query);
        ExcelUtil<SysResourcePermissionLog> util = new ExcelUtil<>(SysResourcePermissionLog.class);
        util.exportExcel(response, list, "权限操作日志");
    }

    @PreAuthorize("@ss.hasPermi('permission:log:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(service.selectById(id));
    }

    @PreAuthorize("@ss.hasPermi('permission:log:remove')")
    @Log(title = "权限日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(service.deleteByIds(ids));
    }

    /**
     * 清理指定天数之前的日志
     */
    @PreAuthorize("@ss.hasPermi('permission:log:remove')")
    @Log(title = "权限日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean/{days}")
    public AjaxResult clean(@PathVariable int days) {
        return toAjax(service.cleanLogBeforeDays(days));
    }
}
