package cc.endmc.permission.controller;

import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.permission.domain.SysPermissionTemplate;
import cc.endmc.permission.service.ISysPermissionTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限模板管理Controller
 *
 * @author Memory
 * @date 2025-12-20
 */
@RestController
@RequestMapping("/permission/template")
@RequiredArgsConstructor
public class PermissionTemplateController extends BaseController {

    private final ISysPermissionTemplateService service;

    @PreAuthorize("@ss.hasPermi('permission:template:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysPermissionTemplate query) {
        startPage();
        List<SysPermissionTemplate> list = service.selectList(query);
        return getDataTable(list);
    }

    /**
     * 根据资源类型获取模板列表(不分页)
     */
    @PreAuthorize("@ss.hasPermi('permission:template:list')")
    @GetMapping("/listByType/{resourceType}")
    public AjaxResult listByType(@PathVariable String resourceType) {
        return success(service.selectByResourceType(resourceType));
    }

    @PreAuthorize("@ss.hasPermi('permission:template:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(service.selectById(id));
    }

    @PreAuthorize("@ss.hasPermi('permission:template:add')")
    @Log(title = "权限模板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysPermissionTemplate template) {
        return toAjax(service.insert(template));
    }

    @PreAuthorize("@ss.hasPermi('permission:template:edit')")
    @Log(title = "权限模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysPermissionTemplate template) {
        return toAjax(service.update(template));
    }

    @PreAuthorize("@ss.hasPermi('permission:template:remove')")
    @Log(title = "权限模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(service.deleteByIds(ids));
    }
}
