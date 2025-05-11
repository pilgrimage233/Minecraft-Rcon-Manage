package cc.endmc.server.mapper.bot;

import cc.endmc.server.domain.bot.QqBotLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 机器人日志Mapper接口
 *
 * @author Memory
 * @date 2025-04-18
 */
@Mapper
public interface QqBotLogMapper {
    /**
     * 查询机器人日志
     *
     * @param id 机器人日志主键
     * @return 机器人日志
     */
    public QqBotLog selectQqBotLogById(Long id);

    /**
     * 查询机器人日志列表
     *
     * @param qqBotLog 机器人日志
     * @return 机器人日志集合
     */
    public List<QqBotLog> selectQqBotLogList(QqBotLog qqBotLog);

    /**
     * 新增机器人日志
     *
     * @param qqBotLog 机器人日志
     * @return 结果
     */
    public int insertQqBotLog(QqBotLog qqBotLog);

    /**
     * 修改机器人日志
     *
     * @param qqBotLog 机器人日志
     * @return 结果
     */
    public int updateQqBotLog(QqBotLog qqBotLog);

    /**
     * 删除机器人日志
     *
     * @param id 机器人日志主键
     * @return 结果
     */
    public int deleteQqBotLogById(Long id);

    /**
     * 批量删除机器人日志
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteQqBotLogByIds(Long[] ids);
}
