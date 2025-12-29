package cc.endmc.server.mapper.relation;

import cc.endmc.server.domain.relation.RconNodeInstanceRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RCON和节点实例关联Mapper接口
 *
 * @author Memory
 * @date 2025-12-27
 */
@Mapper
public interface RconNodeInstanceRelationMapper {

    /**
     * 查询RCON和节点实例关联
     */
    RconNodeInstanceRelation selectById(Long id);

    /**
     * 根据RCON服务器ID和实例ID查询关联
     */
    RconNodeInstanceRelation selectByRconAndInstance(@Param("rconServerId") Long rconServerId, @Param("instanceId") Long instanceId);

    /**
     * 查询RCON和节点实例关联列表
     */
    List<RconNodeInstanceRelation> selectList(RconNodeInstanceRelation query);

    /**
     * 根据RCON服务器ID查询关联的实例ID列表
     */
    List<Long> selectInstanceIdsByRconServerId(Long rconServerId);

    /**
     * 根据实例ID查询关联的RCON服务器ID列表
     */
    List<Long> selectRconServerIdsByInstanceId(Long instanceId);

    /**
     * 根据节点ID查询关联列表
     */
    List<RconNodeInstanceRelation> selectByNodeId(Long nodeId);

    /**
     * 新增RCON和节点实例关联
     */
    int insert(RconNodeInstanceRelation relation);

    /**
     * 修改RCON和节点实例关联
     */
    int update(RconNodeInstanceRelation relation);

    /**
     * 删除RCON和节点实例关联
     */
    int deleteById(Long id);

    /**
     * 批量删除RCON和节点实例关联
     */
    int deleteByIds(Long[] ids);

    /**
     * 根据RCON服务器ID删除关联
     */
    int deleteByRconServerId(Long rconServerId);

    /**
     * 根据实例ID删除关联
     */
    int deleteByInstanceId(Long instanceId);

    /**
     * 根据节点ID删除关联
     */
    int deleteByNodeId(Long nodeId);
}