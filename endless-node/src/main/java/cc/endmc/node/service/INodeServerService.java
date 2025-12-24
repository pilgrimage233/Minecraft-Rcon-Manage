package cc.endmc.node.service;

import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.node.domain.NodeServer;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 节点服务器Service接口
 *
 * @author Memory
 * @date 2025-04-14
 */
public interface INodeServerService {
    /**
     * 查询节点服务器
     *
     * @param id 节点服务器主键
     * @return 节点服务器
     */
    public NodeServer selectNodeServerById(Long id);

    /**
     * 查询节点服务器列表
     *
     * @param nodeServer 节点服务器
     * @return 节点服务器集合
     */
    public List<NodeServer> selectNodeServerList(NodeServer nodeServer);

    /**
     * 新增节点服务器
     *
     * @param nodeServer 节点服务器
     * @return 结果
     */
    public AjaxResult insertNodeServer(NodeServer nodeServer);

    /**
     * 修改节点服务器
     *
     * @param nodeServer 节点服务器
     * @return 结果
     */
    public AjaxResult updateNodeServer(NodeServer nodeServer);

    /**
     * 批量删除节点服务器
     *
     * @param ids 需要删除的节点服务器主键集合
     * @return 结果
     */
    public int deleteNodeServerByIds(Long[] ids);

    /**
     * 删除节点服务器信息
     *
     * @param id 节点服务器主键
     * @return 结果
     */
    public int deleteNodeServerById(Long id);

    /**
     * 获取节点服务器信息
     *
     * @param id 节点服务器主键
     * @return 结果
     */
    public AjaxResult getServerInfo(Long id);

    /**
     * 获取节点服务器负载信息
     *
     * @param id 节点服务器主键
     * @return 结果
     */
    public AjaxResult getServerLoad(Long id);

    /**
     * 获取节点服务器文件列表
     *
     * @param params 参数
     * @return 结果
     */
    public AjaxResult getFileList(Map<String, Object> params);

    /**
     * 获取节点服务器文件列表
     *
     * @param params 参数
     * @return 结果
     */
    public void download(HttpServletResponse response, Map<String, Object> params);

    /**
     * 上传文件到节点服务器
     *
     * @param params 参数
     * @return 结果
     */
    public AjaxResult upload(Map<String, Object> params);

    /**
     * 下载文件到节点服务器
     *
     * @param params 参数
     * @return 结果
     */
    public AjaxResult downloadFromUrl(Map<String, Object> params);

    /**
     * 获取节点服务器心跳信息
     *
     * @param id 节点服务器主键
     * @return 结果
     */
    public AjaxResult getHeartbeat(Long id);

    /**
     * 删除节点服务器文件
     *
     * @param params 参数
     * @return 结果
     */
    public AjaxResult deleteFile(Map<String, Object> params);

    /**
     * 测试节点服务器连接
     *
     * @param nodeServer 节点服务器信息
     * @return 结果
     */
    public AjaxResult testConnection(NodeServer nodeServer);
}
