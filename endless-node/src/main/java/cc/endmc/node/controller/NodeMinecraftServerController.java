package cc.endmc.node.controller;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.node.common.annotation.NodeLog;
import cc.endmc.node.common.constant.OperationTarget;
import cc.endmc.node.common.constant.OperationType;
import cc.endmc.node.domain.NodeMinecraftServer;
import cc.endmc.node.service.INodeMinecraftServerService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class NodeMinecraftServerController extends BaseController {

    private final INodeMinecraftServerService nodeMinecraftServerService;

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
    @NodeLog(operationType = OperationType.ADD_GAME_SERVER, operationTarget = OperationTarget.GAME_SERVER, operationName = "新增游戏服务器")
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
    @NodeLog(operationType = OperationType.UPDATE_GAME_SERVER, operationTarget = OperationTarget.GAME_SERVER, operationName = "修改游戏服务器")
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
    @NodeLog(operationType = OperationType.DELETE_GAME_SERVER, operationTarget = OperationTarget.GAME_SERVER, operationName = "删除游戏服务器")
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
    @NodeLog(operationType = OperationType.ADD_GAME_SERVER, operationTarget = OperationTarget.GAME_SERVER, operationName = "创建游戏服务器实例")
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
    @NodeLog(operationType = OperationType.START_GAME_SERVER, operationTarget = OperationTarget.GAME_SERVER, operationName = "启动游戏服务器")
    @PostMapping("/instance/start")
    public AjaxResult startInstance(@RequestBody Map<String, Object> params) {
        return nodeMinecraftServerService.startInstance(params);
    }

    /**
     * 停止实例
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:edit')")
    @Log(title = "实例管理", businessType = BusinessType.UPDATE)
    @NodeLog(operationType = OperationType.STOP_GAME_SERVER, operationTarget = OperationTarget.GAME_SERVER, operationName = "停止游戏服务器")
    @PostMapping("/instance/stop")
    public AjaxResult stopInstance(@RequestBody Map<String, Object> params) {
        return nodeMinecraftServerService.stopInstance(params);
    }

    /**
     * 重启实例
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:edit')")
    @Log(title = "实例管理", businessType = BusinessType.UPDATE)
    @NodeLog(operationType = OperationType.RESTART_GAME_SERVER, operationTarget = OperationTarget.GAME_SERVER, operationName = "重启游戏服务器")
    @PostMapping("/instance/restart")
    public AjaxResult restartInstance(@RequestBody Map<String, Object> params) {
        return nodeMinecraftServerService.restartInstance(params);
    }

    /**
     * 强制终止实例
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:edit')")
    @Log(title = "实例管理", businessType = BusinessType.UPDATE)
    @NodeLog(operationType = OperationType.FORCE_TERMINATE_GAME_SERVER, operationTarget = OperationTarget.GAME_SERVER, operationName = "强制终止游戏服务器")
    @PostMapping("/instance/kill")
    public AjaxResult killInstance(@RequestBody Map<String, Object> params) {
        return nodeMinecraftServerService.killInstance(params);
    }

    /**
     * 删除实例
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:remove')")
    @Log(title = "实例管理", businessType = BusinessType.DELETE)
    @NodeLog(operationType = OperationType.DELETE_GAME_SERVER, operationTarget = OperationTarget.GAME_SERVER, operationName = "删除游戏服务器实例")
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
    @NodeLog(operationType = OperationType.UPDATE_GAME_SERVER, operationTarget = OperationTarget.GAME_SERVER, operationName = "发送游戏服务器命令")
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

    /**
     * 获取服务器在线玩家
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:list')")
    @GetMapping("/{nodeId}/servers/{serverId}/players")
    public AjaxResult getServerPlayers(@PathVariable Long nodeId, @PathVariable Integer serverId) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", nodeId.intValue());
        map.put("serverId", serverId);
        return nodeMinecraftServerService.getServerPlayers(map);
    }

    /**
     * 对玩家执行操作
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:edit')")
    @Log(title = "实例管理", businessType = BusinessType.UPDATE)
    @NodeLog(operationType = OperationType.UPDATE_GAME_SERVER, operationTarget = OperationTarget.GAME_SERVER, operationName = "执行玩家操作")
    @PostMapping("/{nodeId}/servers/{serverId}/players/{playerName}/action")
    public AjaxResult playerAction(@PathVariable Long nodeId,
                                   @PathVariable Integer serverId,
                                   @PathVariable String playerName,
                                   @RequestBody Map<String, Object> params) {
        params.put("id", nodeId.intValue());
        params.put("serverId", serverId);
        params.put("playerName", playerName);
        return nodeMinecraftServerService.playerAction(params);
    }

    /**
     * Query连接诊断
     */
    @PreAuthorize("@ss.hasPermi('node:mcs:list')")
    @GetMapping("/{nodeId}/servers/{serverId}/query-diagnostic")
    public AjaxResult queryDiagnostic(@PathVariable Long nodeId, @PathVariable Integer serverId) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", nodeId.intValue());
        map.put("serverId", serverId);
        return nodeMinecraftServerService.queryDiagnostic(map);
    }
}
