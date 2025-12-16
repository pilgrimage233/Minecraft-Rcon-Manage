package cc.endmc.server.config;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Properties;

/**
 * 定时任务配置(单机内存方式)
 */
@Slf4j
@Configuration
public class QuartzConfig {

    @Bean
    public Properties quartzProperties() {
        Properties properties = new Properties();
        properties.put("org.quartz.scheduler.instanceName", "Endless");
        properties.put("org.quartz.scheduler.instanceId", "AUTO");
        properties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.put("org.quartz.threadPool.threadCount", "10");
        properties.put("org.quartz.threadPool.threadPriority", "5");
        properties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        // 设置时区
        properties.put("org.quartz.scheduler.timezone", "Asia/Shanghai");
        // 设置 misfire 阈值为 60 秒
        properties.put("org.quartz.jobStore.misfireThreshold", "60000");
        return properties;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(Properties quartzProperties) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setQuartzProperties(quartzProperties);
        factory.setAutoStartup(true);
        // factory.setStartupDelay(5);  // 增加启动延迟到 5 秒
        factory.setApplicationContextSchedulerContextKey("applicationContext");
        factory.setOverwriteExistingJobs(true);
        factory.setWaitForJobsToCompleteOnShutdown(true);
        return factory;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws Exception {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        log.info("Quartz Scheduler [{}] 正在启动...", scheduler.getSchedulerName());
        return scheduler;
    }
}