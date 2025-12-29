package cc.endmc.server.service.relation.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.domain.relation.RconNodeInstanceRelation;
import cc.endmc.server.mapper.relation.RconNodeInstanceRelationMapper;
import cc.endmc.server.service.relation.IRconNodeInstanceRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RCON和节点实例关联Service业务层处理
 *
 * @author Memory
 * @date 2025-12-27
 */
@Service
public class RconNodeInstanceRelationServiceImpl implements IRconNodeInstanceRelationService {

    @Autowired
    private RconNodeInstanceRelationMapper relationMapper;

    /**
     * 查询RCON和节点实例关联
     */
    @Override
    public RconNodeInstanceRelation selectById(Long id) {
        return relationMapper.selectById(id);
    }

    /**
     * 查询RCON和节点实例关联列表
     */
    @Override
    public List<RconNodeInstanceRelation> selectList(RconNodeInstanceRelation query) {
        return relationMapper.selectList(query);
    }

    /**
     * 根据RCON服务器ID查询关联的实例
     */
    @Override
    public RconNodeInstanceRelation selectByRconServerId(Long rconServerId) {
        RconNodeInstanceRelation query = new RconNodeInstanceRelation();
        query.setRconServerId(rconServerId);
        List<RconNodeInstanceRelation> list = relationMapper.selectList(query);
        return list.isEmpty() ? null : list.getFirst();
    }

    /**
     * 根据实例ID查询关联的RCON服务器
     */
    @Override
    public RconNodeInstanceRelation selectByInstanceId(Long instanceId) {
        RconNodeInstanceRelation query = new RconNodeInstanceRelation();
        query.setInstanceId(instanceId);
        List<RconNodeInstanceRelation> list = relationMapper.selectList(query);
        return list.isEmpty() ? null : list.getFirst();
    }

    /**
     * 新增RCON和节点实例关联
     */
    @Override
    public int insert(RconNodeInstanceRelation relation) {
        // 检查RCON服务器是否已有关联
        RconNodeInstanceRelation existingByRcon = this.selectByRconServerId(relation.getRconServerId());
        if (existingByRcon != null) {
            throw new RuntimeException("该RCON服务器已关联实例: " + existingByRcon.getInstanceName());
        }

        // 检查实例是否已被关联
        RconNodeInstanceRelation existingByInstance = this.selectByInstanceId(relation.getInstanceId());
        if (existingByInstance != null) {
            throw new RuntimeException("该实例已被其他RCON服务器关联");
        }

        relation.setCreateTime(DateUtils.getNowDate());
        relation.setStatus("0"); // 默认正常状态
        return relationMapper.insert(relation);
    }

    /**
     * 修改RCON和节点实例关联
     */
    @Override
    public int update(RconNodeInstanceRelation relation) {
        relation.setUpdateTime(DateUtils.getNowDate());
        return relationMapper.update(relation);
    }

    /**
     * 批量删除RCON和节点实例关联
     */
    @Override
    public int deleteByIds(Long[] ids) {
        return relationMapper.deleteByIds(ids);
    }

    /**
     * 删除RCON和节点实例关联
     */
    @Override
    public int deleteById(Long id) {
        return relationMapper.deleteById(id);
    }

    /**
     * 根据RCON服务器ID删除关联
     */
    @Override
    public int deleteByRconServerId(Long rconServerId) {
        return relationMapper.deleteByRconServerId(rconServerId);
    }

    /**
     * 根据实例ID删除关联
     */
    @Override
    public int deleteByInstanceId(Long instanceId) {
        return relationMapper.deleteByInstanceId(instanceId);
    }
}