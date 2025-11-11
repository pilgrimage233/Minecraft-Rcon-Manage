package cc.endmc.node.mapper;

import cc.endmc.node.domain.NodeMinecraftServer;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 实例管理Mapper接口
 *
 * @author ruoyi
 * @date 2025-10-28
 */
@Mapper
public interface NodeMinecraftServerMapper {
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
     * 删除实例管理
     *
     * @param id 实例管理主键
     * @return 结果
     */
    public int deleteNodeMinecraftServerById(Long id);

    /**
     * 批量删除实例管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteNodeMinecraftServerByIds(Long[] ids);
}
