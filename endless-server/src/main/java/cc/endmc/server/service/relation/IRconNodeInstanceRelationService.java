package cc.endmc.server.service.relation;

import cc.endmc.server.domain.relation.RconNodeInstanceRelation;

import java.util.List;

/**
 * RCON和节点实例关联Service接口
 *
 * @author Memory
 * @date 2025-12-27
 */
public interface IRconNodeInstanceRelationService {

    /**
     * 查询RCON和节点实例关联
     */
    RconNodeInstanceRelation selectById(Long id);

    /**
     * 查询RCON和节点实例关联列表
     */
    List<RconNodeInstanceRelation> selectList(RconNodeInstanceRelation query);

    /**
     * 根据RCON服务器ID查询关联的实例
     */
    RconNodeInstanceRelation selectByRconServerId(Long rconServerId);

    /**
     * 根据实例ID查询关联的RCON服务器
     */
    RconNodeInstanceRelation selectByInstanceId(Long instanceId);

    /**
     * 新增RCON和节点实例关联
     */
    int insert(RconNodeInstanceRelation relation);

    /**
     * 修改RCON和节点实例关联
     */
    int update(RconNodeInstanceRelation relation);

    /**
     * 批量删除RCON和节点实例关联
     */
    int deleteByIds(Long[] ids);

    /**
     * 删除RCON和节点实例关联
     */
    int deleteById(Long id);

    /**
     * 根据RCON服务器ID删除关联
     */
    int deleteByRconServerId(Long rconServerId);

    /**
     * 根据实例ID删除关联
     */
    int deleteByInstanceId(Long instanceId);
}