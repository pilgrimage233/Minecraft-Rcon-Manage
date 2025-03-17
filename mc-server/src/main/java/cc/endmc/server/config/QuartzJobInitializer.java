package cc.endmc.server.config;

import cc.endmc.server.domain.other.RegularCmd;
import cc.endmc.server.service.other.IRegularCmdService;
import cc.endmc.server.utils.ScheduleUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class QuartzJobInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private static boolean initialized = false;
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private IRegularCmdService regularCmdService;

    @PostConstruct
    public void init() {
        log.info("QuartzJobInitializer 初始化...");
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        // 防止重复初始化
        if (initialized) {
            return;
        }
        initialized = true;

        log.info("开始初始化MC服务器定时任务...");
        try {
            // 等待 scheduler 启动
            int maxRetries = 10;
            int retryCount = 0;
            while (!scheduler.isStarted() && retryCount < maxRetries) {
                log.info("等待 Scheduler 启动...");
                Thread.sleep(1000);
                retryCount++;
            }

            if (!scheduler.isStarted()) {
                log.error("Scheduler 未能成功启动");
                return;
            }

            // 初始化定时任务
            RegularCmd query = new RegularCmd();
            query.setStatus(0L);
            List<RegularCmd> regularCmds = regularCmdService.selectRegularCmdList(query);

            log.info("开始初始化定时任务，共有{}个任务", regularCmds.size());

            for (RegularCmd job : regularCmds) {
                try {
                    // 检查cron表达式是否有效
                    if (job.getCron() == null || job.getCron().trim().isEmpty()) {
                        log.warn("任务[{}]的cron表达式为空，跳过", job.getTaskId());
                        continue;
                    }

                    // 如果任务已存在，先删除
                    if (scheduler.checkExists(ScheduleUtils.getJobKey(job.getTaskId(), "DEFAULT"))) {
                        scheduler.deleteJob(ScheduleUtils.getJobKey(job.getTaskId(), "DEFAULT"));
                    }

                    log.info("正在初始化任务: taskId=[{}], cron=[{}], cmd=[{}]",
                            job.getTaskId(), job.getCron(), job.getCmd());

                    // 创建定时任务
                    ScheduleUtils.createScheduleJob(scheduler, job);

                    // 验证任务是否创建成功
                    if (scheduler.checkExists(ScheduleUtils.getJobKey(job.getTaskId(), "DEFAULT"))) {
                        Trigger trigger = scheduler.getTrigger(ScheduleUtils.getTriggerKey(job.getTaskId(), "DEFAULT"));
                        if (trigger instanceof CronTrigger) {
                            log.info("任务[{}]初始化成功, 下次执行时间: {}",
                                    job.getTaskId(), trigger.getNextFireTime());
                        }
                    }
                } catch (Exception e) {
                    log.error("初始化任务[{}]失败: {}", job.getTaskId(), e.getMessage(), e);
                }
            }

            // 打印所有已注册的任务
            log.info("---当前所有已注册的任务---");
            for (String group : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    for (Trigger trigger : triggers) {
                        if (trigger instanceof CronTrigger) {
                            CronTrigger cronTrigger = (CronTrigger) trigger;
                            log.info("任务[{}]: cron=[{}], 下次执行时间=[{}]",
                                    jobKey.getName(),
                                    cronTrigger.getCronExpression(),
                                    cronTrigger.getNextFireTime());
                        }
                    }
                }
            }

            log.info("MC服务器定时任务初始化完成");

        } catch (Exception e) {
            log.error("初始化定时任务失败", e);
        }
    }
} 