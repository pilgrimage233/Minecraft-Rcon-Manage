package cc.endmc.server.controller.relation;

import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.server.domain.relation.RconNodeInstanceRelation;
import cc.endmc.server.service.relation.IRconNodeInstanceRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * RCON和节点实例关联Controller
 *
 * @author Memory
 * @date 2025-12-27
 */
@RestController
@RequestMapping("/server/relation")
public class RconNodeInstanceRelationController extends BaseController {

    @Autowired
    private IRconNodeInstanceRelationService relationService;

    /**
     * 查询RCON和节点实例关联列表
     */
    @PreAuthorize("@ss.hasPermi('server:relation:list')")
    @GetMapping("/list")
    public TableDataInfo list(RconNodeInstanceRelation query) {
        startPage();
        List<RconNodeInstanceRelation> list = relationService.selectList(query);
        return getDataTable(list);
    }

    /**
     * 根据RCON服务器ID查询关联的实例
     */
    @PreAuthorize("@ss.hasPermi('server:relation:list')")
    @GetMapping("/getByRconServer/{rconServerId}")
    public AjaxResult getByRconServer(@PathVariable Long rconServerId) {
        RconNodeInstanceRelation relation = relationService.selectByRconServerId(rconServerId);
        return success(relation);
    }

    /**
     * 根据实例ID查询关联的RCON服务器
     */
    @PreAuthorize("@ss.hasPermi('server:relation:list')")
    @GetMapping("/getByInstance/{instanceId}")
    public AjaxResult getByInstance(@PathVariable Long instanceId) {
        RconNodeInstanceRelation relation = relationService.selectByInstanceId(instanceId);
        return success(relation);
    }

    /**
     * 导出RCON和节点实例关联列表
     */
    @PreAuthorize("@ss.hasPermi('server:relation:export')")
    @Log(title = "RCON和节点实例关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RconNodeInstanceRelation query) {
        List<RconNodeInstanceRelation> list = relationService.selectList(query);
        ExcelUtil<RconNodeInstanceRelation> util = new ExcelUtil<>(RconNodeInstanceRelation.class);
        util.exportExcel(list, "RCON和节点实例关联数据");
    }

    /**
     * 获取RCON和节点实例关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('server:relation:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(relationService.selectById(id));
    }

    /**
     * 新增RCON和节点实例关联
     */
    @PreAuthorize("@ss.hasPermi('server:relation:add')")
    @Log(title = "RCON和节点实例关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RconNodeInstanceRelation relation) {
        return toAjax(relationService.insert(relation));
    }

    /**
     * 修改RCON和节点实例关联
     */
    @PreAuthorize("@ss.hasPermi('server:relation:edit')")
    @Log(title = "RCON和节点实例关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RconNodeInstanceRelation relation) {
        return toAjax(relationService.update(relation));
    }

    /**
     * 删除RCON和节点实例关联
     */
    @PreAuthorize("@ss.hasPermi('server:relation:remove')")
    @Log(title = "RCON和节点实例关联", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(relationService.deleteByIds(ids));
    }
}