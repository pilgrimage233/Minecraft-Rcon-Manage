package cc.endmc.node.controller;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.node.domain.NodeEnv;
import cc.endmc.node.service.INodeEnvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 节点Java多版本环境管理Controller
 *
 * @author Memory
 * @date 2025-11-25
 */
@RestController
@RequestMapping("/node/env")
public class NodeEnvController extends BaseController {
    @Autowired
    private INodeEnvService nodeEnvService;

    /**
     * 查询节点Java多版本环境管理列表
     */
    @PreAuthorize("@ss.hasPermi('node:env:list')")
    @GetMapping("/list")
    public TableDataInfo list(NodeEnv nodeEnv) {
        startPage();
        List<NodeEnv> list = nodeEnvService.selectNodeEnvList(nodeEnv);
        return getDataTable(list);
    }

    /**
     * 导出节点Java多版本环境管理列表
     */
    @PreAuthorize("@ss.hasPermi('node:env:export')")
    @Log(title = "节点Java多版本环境管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, NodeEnv nodeEnv) {
        List<NodeEnv> list = nodeEnvService.selectNodeEnvList(nodeEnv);
        ExcelUtil<NodeEnv> util = new ExcelUtil<NodeEnv>(NodeEnv.class);
        util.exportExcel(response, list, "节点Java多版本环境管理数据");
    }

    /**
     * 获取节点Java多版本环境管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('node:env:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(nodeEnvService.selectNodeEnvById(id));
    }

    /**
     * 新增节点Java多版本环境管理
     */
    @PreAuthorize("@ss.hasPermi('node:env:add')")
    @Log(title = "节点Java多版本环境管理", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody NodeEnv nodeEnv) {
        return toAjax(nodeEnvService.insertNodeEnv(nodeEnv));
    }

    /**
     * 修改节点Java多版本环境管理
     */
    @PreAuthorize("@ss.hasPermi('node:env:edit')")
    @Log(title = "节点Java多版本环境管理", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody NodeEnv nodeEnv) {
        return toAjax(nodeEnvService.updateNodeEnv(nodeEnv));
    }

    /**
     * 删除节点Java多版本环境管理
     */
    @PreAuthorize("@ss.hasPermi('node:env:remove')")
    @Log(title = "节点Java多版本环境管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(nodeEnvService.deleteNodeEnvByIds(ids));
    }

    /**
     * 验证Java环境
     */
    @PreAuthorize("@ss.hasPermi('node:env:query')")
    @PostMapping("/verify")
    public AjaxResult verifyEnvironment(@RequestBody NodeEnv nodeEnv) {
        return nodeEnvService.verifyEnvironment(nodeEnv);
    }

    /**
     * 扫描节点上的Java环境
     */
    @PreAuthorize("@ss.hasPermi('node:env:query')")
    @GetMapping("/scan/{nodeId}")
    public AjaxResult scanEnvironments(@PathVariable Long nodeId) {
        return nodeEnvService.scanEnvironments(nodeId);
    }
}
