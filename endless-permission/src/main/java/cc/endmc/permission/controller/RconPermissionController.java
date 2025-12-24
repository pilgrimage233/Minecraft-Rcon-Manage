package cc.endmc.permission.controller;

import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.permission.domain.SysUserRconServer;
import cc.endmc.permission.service.ISysUserRconServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * RCON服务器权限管理Controller
 *
 * @author Memory
 * @date 2025-12-20
 */
@RestController
@RequestMapping("/permission/rcon")
@RequiredArgsConstructor
public class RconPermissionController extends BaseController {

    private final ISysUserRconServerService service;

    /**
     * 查询RCON服务器权限列表
     */
    @PreAuthorize("@ss.hasPermi('permission:rcon:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUserRconServer query) {
        startPage();
        List<SysUserRconServer> list = service.selectList(query);
        return getDataTable(list);
    }

    /**
     * 导出RCON服务器权限列表
     */
    @PreAuthorize("@ss.hasPermi('permission:rcon:export')")
    @Log(title = "RCON权限", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysUserRconServer query) {
        List<SysUserRconServer> list = service.selectList(query);
        ExcelUtil<SysUserRconServer> util = new ExcelUtil<>(SysUserRconServer.class);
        util.exportExcel(response, list, "RCON服务器权限数据");
    }

    /**
     * 获取RCON服务器权限详细信息
     */
    @PreAuthorize("@ss.hasPermi('permission:rcon:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(service.selectById(id));
    }

    /**
     * 授予RCON服务器权限
     */
    @PreAuthorize("@ss.hasPermi('permission:rcon:add')")
    @Log(title = "RCON权限", businessType = BusinessType.GRANT)
    @PostMapping()
    public AjaxResult grant(@Validated @RequestBody SysUserRconServer permission) {
        return toAjax(service.grantPermission(permission));
    }

    /**
     * 使用模板授权
     */
    @PreAuthorize("@ss.hasPermi('permission:rcon:add')")
    @Log(title = "RCON权限", businessType = BusinessType.GRANT)
    @PostMapping("/grantByTemplate")
    public AjaxResult grantByTemplate(@RequestParam Long userId, @RequestParam Long serverId, @RequestParam String templateKey) {
        return toAjax(service.grantByTemplate(userId, serverId, templateKey));
    }

    /**
     * 修改RCON服务器权限
     */
    @PreAuthorize("@ss.hasPermi('permission:rcon:edit')")
    @Log(title = "RCON权限", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysUserRconServer permission) {
        return toAjax(service.update(permission));
    }

    /**
     * 撤销RCON服务器权限
     */
    @PreAuthorize("@ss.hasPermi('permission:rcon:remove')")
    @Log(title = "RCON权限", businessType = BusinessType.DELETE)
    @DeleteMapping()
    public AjaxResult revoke(@RequestParam Long userId, @RequestParam Long serverId) {
        return toAjax(service.revokePermission(userId, serverId));
    }

    /**
     * 批量删除RCON服务器权限
     */
    @PreAuthorize("@ss.hasPermi('permission:rcon:remove')")
    @Log(title = "RCON权限", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(service.deleteByIds(ids));
    }
}
