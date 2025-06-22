package cc.endmc.node.controller;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.node.domain.NodeOperationLog;
import cc.endmc.node.service.INodeOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 操作日志Controller
 *
 * @author Memory
 * @date 2025-04-24
 */
@RestController
@RequestMapping("/node/log")
public class NodeOperationLogController extends BaseController {
    @Autowired
    private INodeOperationLogService nodeOperationLogService;

    /**
     * 查询操作日志列表
     */
    @PreAuthorize("@ss.hasPermi('node:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(NodeOperationLog nodeOperationLog) {
        startPage();
        List<NodeOperationLog> list = nodeOperationLogService.selectNodeOperationLogList(nodeOperationLog);
        return getDataTable(list);
    }

    /**
     * 导出操作日志列表
     */
    @PreAuthorize("@ss.hasPermi('node:log:export')")
    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, NodeOperationLog nodeOperationLog) {
        List<NodeOperationLog> list = nodeOperationLogService.selectNodeOperationLogList(nodeOperationLog);
        ExcelUtil<NodeOperationLog> util = new ExcelUtil<NodeOperationLog>(NodeOperationLog.class);
        util.exportExcel(response, list, "操作日志数据");
    }

    /**
     * 获取操作日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('node:log:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(nodeOperationLogService.selectNodeOperationLogById(id));
    }

    /**
     * 新增操作日志
     */
    @PreAuthorize("@ss.hasPermi('node:log:add')")
    @Log(title = "操作日志", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody NodeOperationLog nodeOperationLog) {
        return toAjax(nodeOperationLogService.insertNodeOperationLog(nodeOperationLog));
    }

    /**
     * 修改操作日志
     */
    @PreAuthorize("@ss.hasPermi('node:log:edit')")
    @Log(title = "操作日志", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody NodeOperationLog nodeOperationLog) {
        return toAjax(nodeOperationLogService.updateNodeOperationLog(nodeOperationLog));
    }

    /**
     * 删除操作日志
     */
    @PreAuthorize("@ss.hasPermi('node:log:remove')")
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(nodeOperationLogService.deleteNodeOperationLogByIds(ids));
    }
}
