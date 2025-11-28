package cc.endmc.node.service;

import cc.endmc.node.domain.NodeEnv;

import java.util.List;

/**
 * 节点Java多版本环境管理Service接口
 *
 * @author Memory
 * @date 2025-11-25
 */
public interface INodeEnvService {
    /**
     * 查询节点Java多版本环境管理
     *
     * @param id 节点Java多版本环境管理主键
     * @return 节点Java多版本环境管理
     */
    public NodeEnv selectNodeEnvById(Long id);

    /**
     * 查询节点Java多版本环境管理列表
     *
     * @param nodeEnv 节点Java多版本环境管理
     * @return 节点Java多版本环境管理集合
     */
    public List<NodeEnv> selectNodeEnvList(NodeEnv nodeEnv);

    /**
     * 新增节点Java多版本环境管理
     *
     * @param nodeEnv 节点Java多版本环境管理
     * @return 结果
     */
    public int insertNodeEnv(NodeEnv nodeEnv);

    /**
     * 修改节点Java多版本环境管理
     *
     * @param nodeEnv 节点Java多版本环境管理
     * @return 结果
     */
    public int updateNodeEnv(NodeEnv nodeEnv);

    /**
     * 批量删除节点Java多版本环境管理
     *
     * @param ids 需要删除的节点Java多版本环境管理主键集合
     * @return 结果
     */
    public int deleteNodeEnvByIds(Long[] ids);

    /**
     * 删除节点Java多版本环境管理信息
     *
     * @param id 节点Java多版本环境管理主键
     * @return 结果
     */
    public int deleteNodeEnvById(Long id);

    /**
     * 验证Java环境
     *
     * @param nodeEnv 节点Java环境信息
     * @return 验证结果
     */
    public cc.endmc.common.core.domain.AjaxResult verifyEnvironment(NodeEnv nodeEnv);

    /**
     * 扫描节点上的Java环境
     *
     * @param nodeId 节点ID
     * @return 扫描结果
     */
    public cc.endmc.common.core.domain.AjaxResult scanEnvironments(Long nodeId);
}
