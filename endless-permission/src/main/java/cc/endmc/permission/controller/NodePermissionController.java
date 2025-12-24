package cc.endmc.permission.controller;

import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.permission.domain.SysUserNodeServer;
import cc.endmc.permission.service.ISysUserNodeServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 节点服务器权限管理Controller
 *
 * @author Memory
 * @date 2025-12-20
 */
@RestController
@RequestMapping("/permission/node")
@RequiredArgsConstructor
public class NodePermissionController extends BaseController {

    private final ISysUserNodeServerService service;

    @PreAuthorize("@ss.hasPermi('permission:node:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUserNodeServer query) {
        startPage();
        List<SysUserNodeServer> list = service.selectList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('permission:node:export')")
    @Log(title = "节点权限", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysUserNodeServer query) {
        List<SysUserNodeServer> list = service.selectList(query);
        ExcelUtil<SysUserNodeServer> util = new ExcelUtil<>(SysUserNodeServer.class);
        util.exportExcel(response, list, "节点服务器权限数据");
    }

    @PreAuthorize("@ss.hasPermi('permission:node:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(service.selectById(id));
    }

    @PreAuthorize("@ss.hasPermi('permission:node:add')")
    @Log(title = "节点权限", businessType = BusinessType.GRANT)
    @PostMapping()
    public AjaxResult grant(@Validated @RequestBody SysUserNodeServer permission) {
        return toAjax(service.grantPermission(permission));
    }

    @PreAuthorize("@ss.hasPermi('permission:node:add')")
    @Log(title = "节点权限", businessType = BusinessType.GRANT)
    @PostMapping("/grantByTemplate")
    public AjaxResult grantByTemplate(@RequestParam Long userId, @RequestParam Long nodeId, @RequestParam String templateKey) {
        return toAjax(service.grantByTemplate(userId, nodeId, templateKey));
    }

    @PreAuthorize("@ss.hasPermi('permission:node:edit')")
    @Log(title = "节点权限", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysUserNodeServer permission) {
        return toAjax(service.update(permission));
    }

    @PreAuthorize("@ss.hasPermi('permission:node:remove')")
    @Log(title = "节点权限", businessType = BusinessType.DELETE)
    @DeleteMapping()
    public AjaxResult revoke(@RequestParam Long userId, @RequestParam Long nodeId) {
        return toAjax(service.revokePermission(userId, nodeId));
    }

    @PreAuthorize("@ss.hasPermi('permission:node:remove')")
    @Log(title = "节点权限", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(service.deleteByIds(ids));
    }
}
