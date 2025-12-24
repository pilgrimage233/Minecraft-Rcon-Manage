package cc.endmc.permission.controller;

import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.permission.domain.SysUserMcInstance;
import cc.endmc.permission.service.ISysUserMcInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * MC实例权限管理Controller
 *
 * @author Memory
 * @date 2025-12-20
 */
@RestController
@RequestMapping("/permission/instance")
@RequiredArgsConstructor
public class InstancePermissionController extends BaseController {

    private final ISysUserMcInstanceService service;

    @PreAuthorize("@ss.hasPermi('permission:instance:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUserMcInstance query) {
        startPage();
        List<SysUserMcInstance> list = service.selectList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('permission:instance:export')")
    @Log(title = "实例权限", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysUserMcInstance query) {
        List<SysUserMcInstance> list = service.selectList(query);
        ExcelUtil<SysUserMcInstance> util = new ExcelUtil<>(SysUserMcInstance.class);
        util.exportExcel(response, list, "MC实例权限数据");
    }

    @PreAuthorize("@ss.hasPermi('permission:instance:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(service.selectById(id));
    }

    @PreAuthorize("@ss.hasPermi('permission:instance:add')")
    @Log(title = "实例权限", businessType = BusinessType.GRANT)
    @PostMapping()
    public AjaxResult grant(@Validated @RequestBody SysUserMcInstance permission) {
        return toAjax(service.grantPermission(permission));
    }

    @PreAuthorize("@ss.hasPermi('permission:instance:add')")
    @Log(title = "实例权限", businessType = BusinessType.GRANT)
    @PostMapping("/grantByTemplate")
    public AjaxResult grantByTemplate(@RequestParam Long userId, @RequestParam Long instanceId, @RequestParam String templateKey) {
        return toAjax(service.grantByTemplate(userId, instanceId, templateKey));
    }

    @PreAuthorize("@ss.hasPermi('permission:instance:edit')")
    @Log(title = "实例权限", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysUserMcInstance permission) {
        return toAjax(service.update(permission));
    }

    @PreAuthorize("@ss.hasPermi('permission:instance:remove')")
    @Log(title = "实例权限", businessType = BusinessType.DELETE)
    @DeleteMapping()
    public AjaxResult revoke(@RequestParam Long userId, @RequestParam Long instanceId) {
        return toAjax(service.revokePermission(userId, instanceId));
    }

    @PreAuthorize("@ss.hasPermi('permission:instance:remove')")
    @Log(title = "实例权限", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(service.deleteByIds(ids));
    }
}
