package com.ruoyi.server.service.impl;

import com.ruoyi.server.domain.HistoryCommand;
import com.ruoyi.server.mapper.HistoryCommandMapper;
import com.ruoyi.server.service.IHistoryCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 历史命令Service业务层处理
 *
 * @author ruoyi
 * @date 2025-02-11
 */
@Service
public class HistoryCommandServiceImpl implements IHistoryCommandService {
    @Autowired
    private HistoryCommandMapper historyCommandMapper;

    /**
     * 查询历史命令
     *
     * @param id 历史命令主键
     * @return 历史命令
     */
    @Override
    public HistoryCommand selectHistoryCommandById(Long id) {
        return historyCommandMapper.selectHistoryCommandById(id);
    }

    /**
     * 查询历史命令列表
     *
     * @param historyCommand 历史命令
     * @return 历史命令
     */
    @Override
    public List<HistoryCommand> selectHistoryCommandList(HistoryCommand historyCommand) {
        return historyCommandMapper.selectHistoryCommandList(historyCommand);
    }

    /**
     * 新增历史命令
     *
     * @param historyCommand 历史命令
     * @return 结果
     */
    @Override
    public int insertHistoryCommand(HistoryCommand historyCommand) {
        return historyCommandMapper.insertHistoryCommand(historyCommand);
    }

    /**
     * 修改历史命令
     *
     * @param historyCommand 历史命令
     * @return 结果
     */
    @Override
    public int updateHistoryCommand(HistoryCommand historyCommand) {
        return historyCommandMapper.updateHistoryCommand(historyCommand);
    }

    /**
     * 批量删除历史命令
     *
     * @param ids 需要删除的历史命令主键
     * @return 结果
     */
    @Override
    public int deleteHistoryCommandByIds(Long[] ids) {
        return historyCommandMapper.deleteHistoryCommandByIds(ids);
    }

    /**
     * 删除历史命令信息
     *
     * @param id 历史命令主键
     * @return 结果
     */
    @Override
    public int deleteHistoryCommandById(Long id) {
        return historyCommandMapper.deleteHistoryCommandById(id);
    }
}
