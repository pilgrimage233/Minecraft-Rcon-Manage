package com.ruoyi.server.service.other;

import com.ruoyi.server.domain.other.HistoryCommand;

import java.util.List;

/**
 * 历史命令Service接口
 *
 * @author ruoyi
 * @date 2025-02-11
 */
public interface IHistoryCommandService {
    /**
     * 查询历史命令
     *
     * @param id 历史命令主键
     * @return 历史命令
     */
    public HistoryCommand selectHistoryCommandById(Long id);

    /**
     * 查询历史命令列表
     *
     * @param historyCommand 历史命令
     * @return 历史命令集合
     */
    public List<HistoryCommand> selectHistoryCommandList(HistoryCommand historyCommand);

    /**
     * 新增历史命令
     *
     * @param historyCommand 历史命令
     * @return 结果
     */
    public int insertHistoryCommand(HistoryCommand historyCommand);

    /**
     * 修改历史命令
     *
     * @param historyCommand 历史命令
     * @return 结果
     */
    public int updateHistoryCommand(HistoryCommand historyCommand);

    /**
     * 批量删除历史命令
     *
     * @param ids 需要删除的历史命令主键集合
     * @return 结果
     */
    public int deleteHistoryCommandByIds(Long[] ids);

    /**
     * 删除历史命令信息
     *
     * @param id 历史命令主键
     * @return 结果
     */
    public int deleteHistoryCommandById(Long id);
}
