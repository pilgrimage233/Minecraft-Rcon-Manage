package cc.endmc.node.service;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.node.domain.NodeMinecraftServer;

import java.util.List;
import java.util.Map;

/**
 * 实例管理Service接口
 *
 * @author ruoyi
 * @date 2025-10-28
 */
public interface INodeMinecraftServerService {
    /**
     * 查询实例管理
     *
     * @param id 实例管理主键
     * @return 实例管理
     */
    public NodeMinecraftServer selectNodeMinecraftServerById(Long id);

    /**
     * 查询实例管理列表
     *
     * @param nodeMinecraftServer 实例管理
     * @return 实例管理集合
     */
    public List<NodeMinecraftServer> selectNodeMinecraftServerList(NodeMinecraftServer nodeMinecraftServer);

    /**
     * 新增实例管理
     *
     * @param nodeMinecraftServer 实例管理
     * @return 结果
     */
    public int insertNodeMinecraftServer(NodeMinecraftServer nodeMinecraftServer);

    /**
     * 修改实例管理
     *
     * @param nodeMinecraftServer 实例管理
     * @return 结果
     */
    public int updateNodeMinecraftServer(NodeMinecraftServer nodeMinecraftServer);

    /**
     * 批量删除实例管理
     *
     * @param ids 需要删除的实例管理主键集合
     * @return 结果
     */
    public int deleteNodeMinecraftServerByIds(Long[] ids);

    /**
     * 删除实例管理信息
     *
     * @param id 实例管理主键
     * @return 结果
     */
    public int deleteNodeMinecraftServerById(Long id);

    // —— 节点端实例操控 ——
    public AjaxResult listInstances(Long nodeId);

    public AjaxResult createInstance(Map<String, Object> params);

    public AjaxResult startInstance(Map<String, Object> params);

    public AjaxResult stopInstance(Map<String, Object> params);

    public AjaxResult restartInstance(Map<String, Object> params);

    public AjaxResult killInstance(Map<String, Object> params);

    public AjaxResult deleteInstance(Map<String, Object> params);

    public AjaxResult getConsole(Map<String, Object> params);

    public AjaxResult getConsoleHistory(Map<String, Object> params);

    public AjaxResult getStatus(Map<String, Object> params);

    public AjaxResult sendCommand(Map<String, Object> params);

    /**
     * 仅更新服务器状态（不触发节点API同步）
     * 用于定时任务同步状态
     *
     * @param id     服务器ID
     * @param status 新状态
     * @return 结果
     */
    public int updateServerStatusOnly(Long id, String status);
}
