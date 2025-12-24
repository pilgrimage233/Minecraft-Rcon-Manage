package cc.endmc.node.mapper;

import cc.endmc.node.domain.NodeServer;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 节点服务器Mapper接口
 *
 * @author Memory
 * @date 2025-04-14
 */
@Mapper
public interface NodeServerMapper {
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
    public int insertNodeServer(NodeServer nodeServer);

    /**
     * 修改节点服务器
     *
     * @param nodeServer 节点服务器
     * @return 结果
     */
    public int updateNodeServer(NodeServer nodeServer);

    /**
     * 删除节点服务器
     *
     * @param id 节点服务器主键
     * @return 结果
     */
    public int deleteNodeServerById(Long id);

    /**
     * 批量删除节点服务器
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteNodeServerByIds(Long[] ids);
}
