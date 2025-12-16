package cc.endmc.quartz.task;

import cc.endmc.quartz.mapper.SysJobLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 日志清理定时任务
 *
 * @author EndMC
 * @date 2025-12-13
 */
@Slf4j
@Component("logCleanupTask")
@RequiredArgsConstructor
public class LogCleanupTask {

    private final SysJobLogMapper sysJobLogMapper;

    /**
     * 任务日志保留天数，默认30天
     */
    @Value("${cleanup.job-log.retain-days:30}")
    private int jobLogRetainDays;

    /**
     * 操作日志保留天数，默认90天
     */
    @Value("${cleanup.oper-log.retain-days:90}")
    private int operLogRetainDays;

    /**
     * 登录日志保留天数，默认30天
     */
    @Value("${cleanup.login-log.retain-days:30}")
    private int loginLogRetainDays;

    /**
     * QQ机器人日志保留天数，默认7天
     */
    @Value("${cleanup.qq-bot-log.retain-days:7}")
    private int qqBotLogRetainDays;

    /**
     * 清理任务日志
     */
    public void cleanJobLog() {
        log.info("开始清理任务日志，保留天数：{}", jobLogRetainDays);
        try {
            int deletedCount = sysJobLogMapper.deleteJobLogByRetainDays(jobLogRetainDays);
            log.info("任务日志清理完成，删除记录数：{}", deletedCount);
        } catch (Exception e) {
            log.error("清理任务日志失败", e);
        }
    }

    /**
     * 清理操作日志
     */
    public void cleanOperLog() {
        log.info("开始清理操作日志，保留天数：{}", operLogRetainDays);
        try {
            int deletedCount = sysJobLogMapper.deleteOperLogByRetainDays(operLogRetainDays);
            log.info("操作日志清理完成，删除记录数：{}", deletedCount);
        } catch (Exception e) {
            log.error("清理操作日志失败", e);
        }
    }

    /**
     * 清理登录日志
     */
    public void cleanLoginLog() {
        log.info("开始清理登录日志，保留天数：{}", loginLogRetainDays);
        try {
            int deletedCount = sysJobLogMapper.deleteLoginLogByRetainDays(loginLogRetainDays);
            log.info("登录日志清理完成，删除记录数：{}", deletedCount);
        } catch (Exception e) {
            log.error("清理登录日志失败", e);
        }
    }

    /**
     * 清理QQ机器人日志
     */
    public void cleanQqBotLog() {
        log.info("开始清理QQ机器人日志，保留天数：{}", qqBotLogRetainDays);
        try {
            int deletedCount = sysJobLogMapper.deleteQqBotLogByRetainDays(qqBotLogRetainDays);
            log.info("QQ机器人日志清理完成，删除记录数：{}", deletedCount);
        } catch (Exception e) {
            log.error("清理QQ机器人日志失败", e);
        }
    }

    /**
     * 清理所有日志（综合清理任务）
     */
    public void cleanAllLogs() {
        log.info("开始执行综合日志清理任务");
        cleanJobLog();
        cleanOperLog();
        cleanLoginLog();
        cleanQqBotLog();
        log.info("综合日志清理任务完成");
    }

    /**
     * 自定义保留天数清理任务日志
     *
     * @param retainDays 保留天数
     */
    public void cleanJobLogWithDays(String retainDays) {
        try {
            int days = Integer.parseInt(retainDays);
            log.info("开始清理任务日志，自定义保留天数：{}", days);
            int deletedCount = sysJobLogMapper.deleteJobLogByRetainDays(days);
            log.info("任务日志清理完成，删除记录数：{}", deletedCount);
        } catch (NumberFormatException e) {
            log.error("保留天数参数格式错误：{}", retainDays, e);
        } catch (Exception e) {
            log.error("清理任务日志失败", e);
        }
    }

    /**
     * 自定义保留天数清理QQ机器人日志
     *
     * @param retainDays 保留天数
     */
    public void cleanQqBotLogWithDays(String retainDays) {
        try {
            int days = Integer.parseInt(retainDays);
            log.info("开始清理QQ机器人日志，自定义保留天数：{}", days);
            int deletedCount = sysJobLogMapper.deleteQqBotLogByRetainDays(days);
            log.info("QQ机器人日志清理完成，删除记录数：{}", deletedCount);
        } catch (NumberFormatException e) {
            log.error("保留天数参数格式错误：{}", retainDays, e);
        } catch (Exception e) {
            log.error("清理QQ机器人日志失败", e);
        }
    }
}