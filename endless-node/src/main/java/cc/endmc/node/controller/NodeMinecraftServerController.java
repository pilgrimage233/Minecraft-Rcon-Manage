package cc.endmc.node.controller;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.node.domain.NodeMinecraftServer;
import cc.endmc.node.service.INodeMinecraftServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实例管理Controller
 *
 * @author ruoyi
 * @date 2025-10-28
 */
@RestController
@RequestMapping("/node/mcs")
public class NodeMinecraftServerController extends BaseController {
    @Autowired
    private INodeMinecraftServerService nodeMinecraftServerService;

    /**
     * 查询实例管理列表
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:list')")
    @GetMapping("/list")
    public TableDataInfo list(NodeMinecraftServer nodeMinecraftServer) {
        startPage();
        List<NodeMinecraftServer> list = nodeMinecraftServerService.selectNodeMinecraftServerList(nodeMinecraftServer);
        return getDataTable(list);
    }

    /**
     * 导出实例管理列表
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:export')")
    @Log(title = "实例管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, NodeMinecraftServer nodeMinecraftServer) {
        List<NodeMinecraftServer> list = nodeMinecraftServerService.selectNodeMinecraftServerList(nodeMinecraftServer);
        ExcelUtil<NodeMinecraftServer> util = new ExcelUtil<>(NodeMinecraftServer.class);
        util.exportExcel(response, list, "实例管理数据");
    }

    /**
     * 获取实例管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(nodeMinecraftServerService.selectNodeMinecraftServerById(id));
    }

    /**
     * 新增实例管理
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:add')")
    @Log(title = "实例管理", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody NodeMinecraftServer nodeMinecraftServer) {
        return toAjax(nodeMinecraftServerService.insertNodeMinecraftServer(nodeMinecraftServer));
    }

    /**
     * 修改实例管理
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:edit')")
    @Log(title = "实例管理", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody NodeMinecraftServer nodeMinecraftServer) {
        return toAjax(nodeMinecraftServerService.updateNodeMinecraftServer(nodeMinecraftServer));
    }

    /**
     * 删除实例管理
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:remove')")
    @Log(title = "实例管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(nodeMinecraftServerService.deleteNodeMinecraftServerByIds(ids));
    }

    /**
     * 获取节点端服务器实例列表
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:list')")
    @GetMapping("/{nodeId}/instances")
    public AjaxResult listInstances(@PathVariable Long nodeId) {
        return nodeMinecraftServerService.listInstances(nodeId);
    }

    /**
     * 在节点端创建服务器实例
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:add')")
    @Log(title = "实例管理", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping("/instance/create")
    public AjaxResult createInstance(@RequestBody Map<String, Object> params) {
        return nodeMinecraftServerService.createInstance(params);
    }

    /**
     * 启动实例
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:edit')")
    @Log(title = "实例管理", businessType = BusinessType.UPDATE)
    @PostMapping("/instance/start")
    public AjaxResult startInstance(@RequestBody Map<String, Object> params) {
        return nodeMinecraftServerService.startInstance(params);
    }

    /**
     * 停止实例
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:edit')")
    @Log(title = "实例管理", businessType = BusinessType.UPDATE)
    @PostMapping("/instance/stop")
    public AjaxResult stopInstance(@RequestBody Map<String, Object> params) {
        return nodeMinecraftServerService.stopInstance(params);
    }

    /**
     * 重启实例
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:edit')")
    @Log(title = "实例管理", businessType = BusinessType.UPDATE)
    @PostMapping("/instance/restart")
    public AjaxResult restartInstance(@RequestBody Map<String, Object> params) {
        return nodeMinecraftServerService.restartInstance(params);
    }

    /**
     * 强制终止实例
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:edit')")
    @Log(title = "实例管理", businessType = BusinessType.UPDATE)
    @PostMapping("/instance/kill")
    public AjaxResult killInstance(@RequestBody Map<String, Object> params) {
        return nodeMinecraftServerService.killInstance(params);
    }

    /**
     * 删除实例
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:remove')")
    @Log(title = "实例管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/instance")
    public AjaxResult deleteInstance(@RequestBody Map<String, Object> params) {
        return nodeMinecraftServerService.deleteInstance(params);
    }

    /**
     * 获取实例控制台
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:list')")
    @GetMapping("/instance/console")
    public AjaxResult getConsole(@RequestParam("id") Long nodeId, @RequestParam("serverId") Integer serverId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", nodeId.intValue());
        map.put("serverId", serverId);
        return nodeMinecraftServerService.getConsole(map);
    }

    /**
     * 获取实例控制台历史日志
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:list')")
    @GetMapping("/instance/console/history")
    public AjaxResult getConsoleHistory(@RequestParam("id") Long nodeId, @RequestParam("serverId") Integer serverId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", nodeId.intValue());
        map.put("serverId", serverId);
        return nodeMinecraftServerService.getConsoleHistory(map);
    }

    /**
     * 发送实例命令
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:edit')")
    @Log(title = "实例管理", businessType = BusinessType.UPDATE)
    @PostMapping("/instance/command")
    public AjaxResult sendCommand(@RequestBody Map<String, Object> params) {
        return nodeMinecraftServerService.sendCommand(params);
    }

    /**
     * 获取实例状态
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:list')")
    @GetMapping("/instance/status")
    public AjaxResult getStatus(@RequestParam("id") Long nodeId, @RequestParam("serverId") Integer serverId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", nodeId.intValue());
        map.put("serverId", serverId);
        return nodeMinecraftServerService.getStatus(map);
    }
}
