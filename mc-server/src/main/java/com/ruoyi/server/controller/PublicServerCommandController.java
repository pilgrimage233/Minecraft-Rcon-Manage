package com.ruoyi.server.controller;

import com.ruoyi.common.annotation.AddOrUpdateFilter;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.server.domain.PublicServerCommand;
import com.ruoyi.server.service.IPublicServerCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 公开命令Controller
 *
 * @author ruoyi
 * @date 2024-05-08
 */
@RestController
@RequestMapping("/server/public")
public class PublicServerCommandController extends BaseController {
    @Autowired
    private IPublicServerCommandService publicServerCommandService;

    /**
     * 查询公开命令列表
     */
    @PreAuthorize("@ss.hasPermi('server:public:list')")
    @GetMapping("/list")
    public TableDataInfo list(PublicServerCommand publicServerCommand) {
        startPage();
        List<PublicServerCommand> list = publicServerCommandService.selectPublicServerCommandList(publicServerCommand);
        return getDataTable(list);
    }

    /**
     * 导出公开命令列表
     */
    @PreAuthorize("@ss.hasPermi('server:public:export')")
    @Log(title = "公开命令", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PublicServerCommand publicServerCommand) {
        List<PublicServerCommand> list = publicServerCommandService.selectPublicServerCommandList(publicServerCommand);
        ExcelUtil<PublicServerCommand> util = new ExcelUtil<PublicServerCommand>(PublicServerCommand.class);
        util.exportExcel(response, list, "公开命令数据");
    }

    /**
     * 获取公开命令详细信息
     */
    @PreAuthorize("@ss.hasPermi('server:public:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(publicServerCommandService.selectPublicServerCommandById(id));
    }

    /**
     * 新增公开命令
     */
    @PreAuthorize("@ss.hasPermi('server:public:add')")
    @Log(title = "公开命令", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody PublicServerCommand publicServerCommand) {
        return toAjax(publicServerCommandService.insertPublicServerCommand(publicServerCommand));
    }

    /**
     * 修改公开命令
     */
    @PreAuthorize("@ss.hasPermi('server:public:edit')")
    @Log(title = "公开命令", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody PublicServerCommand publicServerCommand) {
        return toAjax(publicServerCommandService.updatePublicServerCommand(publicServerCommand));
    }

    /**
     * 删除公开命令
     */
    @PreAuthorize("@ss.hasPermi('server:public:remove')")
    @Log(title = "公开命令", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(publicServerCommandService.deletePublicServerCommandByIds(ids));
    }
}
