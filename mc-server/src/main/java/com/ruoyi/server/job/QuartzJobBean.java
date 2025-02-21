package com.ruoyi.server.job;

import com.ruoyi.common.constant.ScheduleConstants;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.server.domain.other.RegularCmd;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 定时任务处理（禁止并发执行）
 */
@DisallowConcurrentExecution  // 确保不会并发执行
@PersistJobDataAfterExecution // 执行完成后持久化数据
public class QuartzJobBean implements Job {
    private static final Logger log = LoggerFactory.getLogger(QuartzJobBean.class);

    private static final Object LOCK = new Object();  // 添加锁对象

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 使用同步块确保同一时间只有一个任务在执行
        synchronized (LOCK) {
            try {
                log.info("开始执行定时任务");
                RegularCmd regularCmd = (RegularCmd) context.getMergedJobDataMap().get(ScheduleConstants.TASK_PROPERTIES);

                if (regularCmd == null) {
                    log.error("任务参数为空");
                    return;
                }

                log.info("执行任务详情: taskId=[{}], cmd=[{}], server=[{}]",
                        regularCmd.getTaskId(),
                        regularCmd.getCmd(),
                        regularCmd.getExecuteServer());

                // 获取spring bean
                TaskExecution taskExecution = SpringUtils.getBean(TaskExecution.class);
                if (taskExecution == null) {
                    log.error("获取TaskExecution Bean失败");
                    return;
                }

                // 执行任务
                taskExecution.execute(regularCmd);
                log.info("任务执行完成: taskId=[{}]", regularCmd.getTaskId());

            } catch (Exception e) {
                log.error("任务执行异常", e);
                throw new JobExecutionException(e);
            }
        }
    }
} 