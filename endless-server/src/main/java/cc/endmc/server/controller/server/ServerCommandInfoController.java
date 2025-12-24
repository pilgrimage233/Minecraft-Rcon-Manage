package cc.endmc.server.controller.server;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.server.domain.server.ServerCommandInfo;
import cc.endmc.server.service.server.IServerCommandInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 指令管理Controller
 *
 * @author ruoyi
 * @date 2024-04-16
 */
@RestController
@RequestMapping("/mc/command")
public class ServerCommandInfoController extends BaseController {
    @Autowired
    private IServerCommandInfoService serverCommandInfoService;

    /**
     * 查询指令管理列表
     */
    @PreAuthorize("@ss.hasPermi('mc:command:list')")
    @GetMapping("/list")
    public TableDataInfo list(ServerCommandInfo serverCommandInfo) {
        startPage();
        List<ServerCommandInfo> list = serverCommandInfoService.selectServerCommandInfoList(serverCommandInfo);
        return getDataTable(list);
    }

    /**
     * 导出指令管理列表
     */
    @PreAuthorize("@ss.hasPermi('mc:command:export')")
    @Log(title = "指令管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ServerCommandInfo serverCommandInfo) {
        List<ServerCommandInfo> list = serverCommandInfoService.selectServerCommandInfoList(serverCommandInfo);
        ExcelUtil<ServerCommandInfo> util = new ExcelUtil<ServerCommandInfo>(ServerCommandInfo.class);
        util.exportExcel(response, list, "指令管理数据");
    }

    /**
     * 获取指令管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('mc:command:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(serverCommandInfoService.selectServerCommandInfoById(id));
    }

    /**
     * 新增指令管理
     */
    @PreAuthorize("@ss.hasPermi('mc:command:add')")
    @Log(title = "指令管理", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody ServerCommandInfo serverCommandInfo) {
        return toAjax(serverCommandInfoService.insertServerCommandInfo(serverCommandInfo));
    }

    /**
     * 修改指令管理
     */
    @PreAuthorize("@ss.hasPermi('mc:command:edit')")
    @Log(title = "指令管理", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody ServerCommandInfo serverCommandInfo) {
        return toAjax(serverCommandInfoService.updateServerCommandInfo(serverCommandInfo));
    }

    /**
     * 删除指令管理
     */
    @PreAuthorize("@ss.hasPermi('mc:command:remove')")
    @Log(title = "指令管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(serverCommandInfoService.deleteServerCommandInfoByIds(ids));
    }
}
