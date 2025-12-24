package cc.endmc.quartz.mapper;

import java.util.List;

import cc.endmc.quartz.domain.SysJobLog;
import org.apache.ibatis.annotations.Param;

/**
 * 调度任务日志信息 数据层
 *
 * @author ruoyi
 */
public interface SysJobLogMapper {
    /**
     * 获取quartz调度器日志的计划任务
     *
     * @param jobLog 调度日志信息
     * @return 调度任务日志集合
     */
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog);

    /**
     * 查询所有调度任务日志
     *
     * @return 调度任务日志列表
     */
    public List<SysJobLog> selectJobLogAll();

    /**
     * 通过调度任务日志ID查询调度信息
     *
     * @param jobLogId 调度任务日志ID
     * @return 调度任务日志对象信息
     */
    public SysJobLog selectJobLogById(Long jobLogId);

    /**
     * 新增任务日志
     *
     * @param jobLog 调度日志信息
     * @return 结果
     */
    public int insertJobLog(SysJobLog jobLog);

    /**
     * 批量删除调度日志信息
     *
     * @param logIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteJobLogByIds(Long[] logIds);

    /**
     * 删除任务日志
     *
     * @param jobId 调度日志ID
     * @return 结果
     */
    public int deleteJobLogById(Long jobId);

    /**
     * 清空任务日志
     */
    public void cleanJobLog();

    /**
     * 根据保留天数删除任务日志
     *
     * @param retainDays 保留天数
     * @return 删除记录数
     */
    int deleteJobLogByRetainDays(@Param("retainDays") int retainDays);

    /**
     * 根据保留天数删除操作日志
     *
     * @param retainDays 保留天数
     * @return 删除记录数
     */
    int deleteOperLogByRetainDays(@Param("retainDays") int retainDays);

    /**
     * 根据保留天数删除登录日志
     *
     * @param retainDays 保留天数
     * @return 删除记录数
     */
    int deleteLoginLogByRetainDays(@Param("retainDays") int retainDays);

    /**
     * 获取任务日志总数
     *
     * @return 总记录数
     */
    long getJobLogCount();

    /**
     * 获取操作日志总数
     *
     * @return 总记录数
     */
    long getOperLogCount();

    /**
     * 获取登录日志总数
     *
     * @return 总记录数
     */
    long getLoginLogCount();

    /**
     * 根据保留天数删除QQ机器人日志
     *
     * @param retainDays 保留天数
     * @return 删除记录数
     */
    int deleteQqBotLogByRetainDays(@Param("retainDays") int retainDays);

    /**
     * 获取QQ机器人日志总数
     *
     * @return 总记录数
     */
    long getQqBotLogCount();
}
