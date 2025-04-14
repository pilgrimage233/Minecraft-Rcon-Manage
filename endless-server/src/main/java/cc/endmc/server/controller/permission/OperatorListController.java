package cc.endmc.server.controller.permission;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.server.domain.permission.OperatorList;
import cc.endmc.server.service.permission.IOperatorListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 管理员名单Controller
 *
 * @author Memory
 * @date 2025-01-11
 */
@RestController
@RequestMapping("/player/operator")
public class OperatorListController extends BaseController {
    @Autowired
    private IOperatorListService operatorListService;

    /**
     * 查询管理员名单列表
     */
    @PreAuthorize("@ss.hasPermi('player:operator:list')")
    @GetMapping("/list")
    public TableDataInfo list(OperatorList operatorList) {
        startPage();
        List<OperatorList> list = operatorListService.selectOperatorListList(operatorList);
        return getDataTable(list);
    }

    /**
     * 导出管理员名单列表
     */
    @PreAuthorize("@ss.hasPermi('player:operator:export')")
    @Log(title = "管理员名单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OperatorList operatorList) {
        List<OperatorList> list = operatorListService.selectOperatorListList(operatorList);
        ExcelUtil<OperatorList> util = new ExcelUtil<OperatorList>(OperatorList.class);
        util.exportExcel(response, list, "管理员名单数据");
    }

    /**
     * 获取管理员名单详细信息
     */
    @PreAuthorize("@ss.hasPermi('player:operator:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(operatorListService.selectOperatorListById(id));
    }

    /**
     * 新增管理员名单
     */
    @PreAuthorize("@ss.hasPermi('player:operator:add')")
    @Log(title = "管理员名单", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody OperatorList operatorList) {
        return toAjax(operatorListService.insertOperatorList(operatorList));
    }

    /**
     * 修改管理员名单
     */
    @PreAuthorize("@ss.hasPermi('player:operator:edit')")
    @Log(title = "管理员名单", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody OperatorList operatorList) {
        return toAjax(operatorListService.updateOperatorList(operatorList));
    }

    /**
     * 删除管理员名单
     */
    @PreAuthorize("@ss.hasPermi('player:operator:remove')")
    @Log(title = "管理员名单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(operatorListService.deleteOperatorListByIds(ids));
    }
}
