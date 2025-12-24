package cc.endmc.node.mapper;

import cc.endmc.node.domain.NodeEnv;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 节点Java多版本环境管理Mapper接口
 *
 * @author Memory
 * @date 2025-11-25
 */
@Mapper
public interface NodeEnvMapper {
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
     * 删除节点Java多版本环境管理
     *
     * @param id 节点Java多版本环境管理主键
     * @return 结果
     */
    public int deleteNodeEnvById(Long id);

    /**
     * 批量删除节点Java多版本环境管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteNodeEnvByIds(Long[] ids);
}
