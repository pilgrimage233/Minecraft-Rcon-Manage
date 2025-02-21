package com.ruoyi.server.config;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 全局任务监听器
 */
public class GlobalJobListener implements JobListener {
    private static final Logger log = LoggerFactory.getLogger(GlobalJobListener.class);

    @Override
    public String getName() {
        return "globalJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        String jobName = context.getJobDetail().getKey().getName();
        log.info("任务 [{}] 开始执行", jobName);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        // 可以不实现
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        String jobName = context.getJobDetail().getKey().getName();
        if (jobException != null) {
            log.error("任务 [{}] 执行失败: {}", jobName, jobException.getMessage());
        } else {
            log.info("任务 [{}] 执行完成", jobName);
        }
    }
} 