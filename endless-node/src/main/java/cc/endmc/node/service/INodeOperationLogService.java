package cc.endmc.node.service;

import cc.endmc.node.domain.NodeOperationLog;

import java.util.List;

/**
 * 操作日志Service接口
 *
 * @author Memory
 * @date 2025-04-24
 */
public interface INodeOperationLogService {
    /**
     * 查询操作日志
     *
     * @param id 操作日志主键
     * @return 操作日志
     */
    public NodeOperationLog selectNodeOperationLogById(Long id);

    /**
     * 查询操作日志列表
     *
     * @param nodeOperationLog 操作日志
     * @return 操作日志集合
     */
    public List<NodeOperationLog> selectNodeOperationLogList(NodeOperationLog nodeOperationLog);

    /**
     * 新增操作日志
     *
     * @param nodeOperationLog 操作日志
     * @return 结果
     */
    public int insertNodeOperationLog(NodeOperationLog nodeOperationLog);

    /**
     * 修改操作日志
     *
     * @param nodeOperationLog 操作日志
     * @return 结果
     */
    public int updateNodeOperationLog(NodeOperationLog nodeOperationLog);

    /**
     * 批量删除操作日志
     *
     * @param ids 需要删除的操作日志主键集合
     * @return 结果
     */
    public int deleteNodeOperationLogByIds(Long[] ids);

    /**
     * 删除操作日志信息
     *
     * @param id 操作日志主键
     * @return 结果
     */
    public int deleteNodeOperationLogById(Long id);
}
