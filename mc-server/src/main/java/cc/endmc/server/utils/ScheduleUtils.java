package cc.endmc.server.utils;

import cc.endmc.common.constant.ScheduleConstants;
import cc.endmc.common.exception.job.TaskException;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.domain.other.RegularCmd;
import cc.endmc.server.job.QuartzJobBean;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 定时任务工具类
 */
public class ScheduleUtils {
    private static final Logger log = LoggerFactory.getLogger(ScheduleUtils.class);

    /**
     * 得到quartz任务类
     */
    private static Class<? extends Job> getQuartzJobClass() {
        return QuartzJobBean.class;
    }

    /**
     * 构建任务触发对象
     */
    public static TriggerKey getTriggerKey(String jobId, String jobGroup) {
        return TriggerKey.triggerKey(ScheduleConstants.TASK_CLASS_NAME + jobId, jobGroup);
    }

    /**
     * 构建任务键对象
     */
    public static JobKey getJobKey(String jobId, String jobGroup) {
        return JobKey.jobKey(ScheduleConstants.TASK_CLASS_NAME + jobId, jobGroup);
    }

    /**
     * 创建定时任务
     */
    public static void createScheduleJob(Scheduler scheduler, RegularCmd regularCmd) throws SchedulerException, TaskException {
        Class<? extends Job> jobClass = getQuartzJobClass();
        // 构建job信息
        String jobId = regularCmd.getTaskId();
        String jobGroup = "DEFAULT";
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(getJobKey(jobId, jobGroup))
                .requestRecovery(true)
                .storeDurably(true)
                .build();

        // 表达式调度构建器
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(regularCmd.getCron())
                .withMisfireHandlingInstructionDoNothing();  // 使用 DoNothing 策略

        // 按新的cronExpression表达式构建一个新的trigger
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(getTriggerKey(jobId, jobGroup))
                .withSchedule(cronScheduleBuilder)
                .forJob(jobDetail)
                .build();

        // 放入参数，运行时的方法可以获取
        jobDetail.getJobDataMap().put(ScheduleConstants.TASK_PROPERTIES, regularCmd);

        // 判断是否存在
        if (scheduler.checkExists(getJobKey(jobId, jobGroup))) {
            scheduler.deleteJob(getJobKey(jobId, jobGroup));
        }

        // 判断任务是否过期
        if (StringUtils.isNotNull(CronUtils.getNextExecution(regularCmd.getCron()))) {
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("任务[{}]创建成功，下次执行时间: {}", jobId, trigger.getNextFireTime());
        } else {
            log.warn("任务[{}]的下次执行时间为空，可能是无效的cron表达式: {}", jobId, regularCmd.getCron());
        }

        // 暂停任务
        if (String.valueOf(regularCmd.getStatus()).equals(ScheduleConstants.Status.PAUSE.getValue())) {
            scheduler.pauseJob(getJobKey(jobId, jobGroup));
            log.info("任务[{}]已暂停", jobId);
        } else {
            log.info("任务[{}]已启动", jobId);
        }
    }

    /**
     * 执行定时任务
     */
    public static void executeCronJob(Scheduler scheduler, RegularCmd regularCmd) throws SchedulerException {
        String jobId = regularCmd.getTaskId();
        String jobGroup = "DEFAULT";
        scheduler.triggerJob(getJobKey(jobId, jobGroup));
    }

    /**
     * 暂停任务
     */
    public static void pauseJob(Scheduler scheduler, String jobId, String jobGroup) throws SchedulerException {
        scheduler.pauseJob(getJobKey(jobId, jobGroup));
    }

    /**
     * 恢复任务
     */
    public static void resumeJob(Scheduler scheduler, String jobId, String jobGroup) throws SchedulerException {
        scheduler.resumeJob(getJobKey(jobId, jobGroup));
    }

    /**
     * 删除定时任务
     */
    public static void deleteScheduleJob(Scheduler scheduler, String jobId, String jobGroup) throws SchedulerException {
        scheduler.deleteJob(getJobKey(jobId, jobGroup));
    }
}
