package cc.endmc.node.controller;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.node.common.annotation.NodeLog;
import cc.endmc.node.common.constant.OperationTarget;
import cc.endmc.node.common.constant.OperationType;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.service.INodeServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 节点服务器Controller
 *
 * @author Memory
 * @date 2025-04-14
 */
@RestController
@RequestMapping("/node/server")
public class NodeServerController extends BaseController {
    @Autowired
    private INodeServerService nodeServerService;

    /**
     * 查询节点服务器列表
     */
    @PreAuthorize("@ss.hasPermi('node:server:list')")
    @GetMapping("/list")
    public TableDataInfo list(NodeServer nodeServer) {
        startPage();
        List<NodeServer> list = nodeServerService.selectNodeServerList(nodeServer);
        return getDataTable(list);
    }

    /**
     * 导出节点服务器列表
     */
    @PreAuthorize("@ss.hasPermi('node:server:export')")
    @Log(title = "节点服务器", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, NodeServer nodeServer) {
        List<NodeServer> list = nodeServerService.selectNodeServerList(nodeServer);
        ExcelUtil<NodeServer> util = new ExcelUtil<NodeServer>(NodeServer.class);
        util.exportExcel(response, list, "节点服务器数据");
    }

    /**
     * 获取节点服务器详细信息
     */
    @PreAuthorize("@ss.hasPermi('node:server:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(nodeServerService.selectNodeServerById(id));
    }

    /**
     * 新增节点服务器
     */
    @PreAuthorize("@ss.hasPermi('node:server:add')")
    @Log(title = "节点服务器", businessType = BusinessType.INSERT)
    @NodeLog(operationType = OperationType.ADD_NODE, operationTarget = OperationTarget.NODE_SERVER, operationName = "")
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody NodeServer nodeServer) {
        nodeServer.setCreateTime(DateUtils.getNowDate());
        nodeServer.setCreateBy(getUsername());
        return nodeServerService.insertNodeServer(nodeServer);
    }

    /**
     * 修改节点服务器
     */
    @PreAuthorize("@ss.hasPermi('node:server:edit')")
    @Log(title = "节点服务器", businessType = BusinessType.UPDATE)
    @NodeLog(operationType = OperationType.UPDATE_NODE, operationTarget = OperationTarget.NODE_SERVER, operationName = "")
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody NodeServer nodeServer) {
        nodeServer.setUpdateTime(new Date());
        nodeServer.setUpdateBy(getUsername());
        return nodeServerService.updateNodeServer(nodeServer);
    }

    /**
     * 删除节点服务器
     */
    @PreAuthorize("@ss.hasPermi('node:server:remove')")
    @Log(title = "节点服务器", businessType = BusinessType.DELETE)
    @NodeLog(operationType = OperationType.DELETE_NODE, operationTarget = OperationTarget.NODE_SERVER, operationName = "")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(nodeServerService.deleteNodeServerByIds(ids));
    }

    /**
     * 获取节点服务器信息
     */
    @GetMapping("/getServerInfo/{id}")
    public AjaxResult getServerInfo(@PathVariable Long id) {
        return nodeServerService.getServerInfo(id);
    }

    /**
     * 获取节点服务器负载信息
     */
    @GetMapping("/getServerLoad/{id}")
    public AjaxResult getServerLoad(@PathVariable Long id) {
        return nodeServerService.getServerLoad(id);
    }

    /**
     * 获取节点服务器文件列表
     */
    @PostMapping("/getFileList")
    public AjaxResult getFileList(@RequestBody Map<String, Object> params) {
        return nodeServerService.getFileList(params);
    }

    /**
     * 获取节点服务器文件
     */
    @PostMapping("/download")
    public void download(HttpServletResponse response, @RequestBody Map<String, Object> params) {
        nodeServerService.download(response, params);
    }

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("id") Integer id,
                             @RequestParam("path") String path,
                             @RequestParam("file") MultipartFile file) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("path", path);
        params.put("file", file);
        return nodeServerService.upload(params);
    }

    /**
     * 下载URL文件到节点服务器
     */
    @PostMapping("/downloadFromUrl")
    public AjaxResult downloadFromUrl(@RequestBody Map<String, Object> params) {
        return nodeServerService.downloadFromUrl(params);
    }
}
