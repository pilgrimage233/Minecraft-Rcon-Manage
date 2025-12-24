package cc.endmc.server.service.bot.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.domain.bot.QqBotLog;
import cc.endmc.server.mapper.bot.QqBotLogMapper;
import cc.endmc.server.service.bot.IQqBotLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 机器人日志Service业务层处理
 *
 * @author Memory
 * @date 2025-04-18
 */
@Service
public class QqBotLogServiceImpl implements IQqBotLogService {
    @Autowired
    private QqBotLogMapper qqBotLogMapper;

    /**
     * 查询机器人日志
     *
     * @param id 机器人日志主键
     * @return 机器人日志
     */
    @Override
    public QqBotLog selectQqBotLogById(Long id) {
        return qqBotLogMapper.selectQqBotLogById(id);
    }

    /**
     * 查询机器人日志列表
     *
     * @param qqBotLog 机器人日志
     * @return 机器人日志
     */
    @Override
    public List<QqBotLog> selectQqBotLogList(QqBotLog qqBotLog) {
        return qqBotLogMapper.selectQqBotLogList(qqBotLog);
    }

    /**
     * 新增机器人日志
     *
     * @param qqBotLog 机器人日志
     * @return 结果
     */
    @Override
    public int insertQqBotLog(QqBotLog qqBotLog) {
        qqBotLog.setCreateTime(DateUtils.getNowDate());
        return qqBotLogMapper.insertQqBotLog(qqBotLog);
    }

    /**
     * 修改机器人日志
     *
     * @param qqBotLog 机器人日志
     * @return 结果
     */
    @Override
    public int updateQqBotLog(QqBotLog qqBotLog) {
        return qqBotLogMapper.updateQqBotLog(qqBotLog);
    }

    /**
     * 批量删除机器人日志
     *
     * @param ids 需要删除的机器人日志主键
     * @return 结果
     */
    @Override
    public int deleteQqBotLogByIds(Long[] ids) {
        return qqBotLogMapper.deleteQqBotLogByIds(ids);
    }

    /**
     * 删除机器人日志信息
     *
     * @param id 机器人日志主键
     * @return 结果
     */
    @Override
    public int deleteQqBotLogById(Long id) {
        return qqBotLogMapper.deleteQqBotLogById(id);
    }
}
