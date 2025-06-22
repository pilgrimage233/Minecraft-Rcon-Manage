package cc.endmc.node.service.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.node.domain.NodeOperationLog;
import cc.endmc.node.mapper.NodeOperationLogMapper;
import cc.endmc.node.service.INodeOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志Service业务层处理
 *
 * @author Memory
 * @date 2025-04-24
 */
@Service
public class NodeOperationLogServiceImpl implements INodeOperationLogService {
    @Autowired
    private NodeOperationLogMapper nodeOperationLogMapper;

    /**
     * 查询操作日志
     *
     * @param id 操作日志主键
     * @return 操作日志
     */
    @Override
    public NodeOperationLog selectNodeOperationLogById(Long id) {
        return nodeOperationLogMapper.selectNodeOperationLogById(id);
    }

    /**
     * 查询操作日志列表
     *
     * @param nodeOperationLog 操作日志
     * @return 操作日志
     */
    @Override
    public List<NodeOperationLog> selectNodeOperationLogList(NodeOperationLog nodeOperationLog) {
        return nodeOperationLogMapper.selectNodeOperationLogList(nodeOperationLog);
    }

    /**
     * 新增操作日志
     *
     * @param nodeOperationLog 操作日志
     * @return 结果
     */
    @Override
    public int insertNodeOperationLog(NodeOperationLog nodeOperationLog) {
        nodeOperationLog.setCreateTime(DateUtils.getNowDate());
        return nodeOperationLogMapper.insertNodeOperationLog(nodeOperationLog);
    }

    /**
     * 修改操作日志
     *
     * @param nodeOperationLog 操作日志
     * @return 结果
     */
    @Override
    public int updateNodeOperationLog(NodeOperationLog nodeOperationLog) {
        nodeOperationLog.setUpdateTime(DateUtils.getNowDate());
        return nodeOperationLogMapper.updateNodeOperationLog(nodeOperationLog);
    }

    /**
     * 批量删除操作日志
     *
     * @param ids 需要删除的操作日志主键
     * @return 结果
     */
    @Override
    public int deleteNodeOperationLogByIds(Long[] ids) {
        return nodeOperationLogMapper.deleteNodeOperationLogByIds(ids);
    }

    /**
     * 删除操作日志信息
     *
     * @param id 操作日志主键
     * @return 结果
     */
    @Override
    public int deleteNodeOperationLogById(Long id) {
        return nodeOperationLogMapper.deleteNodeOperationLogById(id);
    }
}
