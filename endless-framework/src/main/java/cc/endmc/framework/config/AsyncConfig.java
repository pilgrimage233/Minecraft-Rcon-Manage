package cc.endmc.framework.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置类
 * 当虚拟线程不可用时的备用配置
 *
 * @author Memory
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 备用线程池执行器
     * 当虚拟线程配置未启用时创建
     */
    @Bean("virtualThreadExecutor")
    @ConditionalOnProperty(name = "spring.threads.virtual.enabled", havingValue = "false", matchIfMissing = true)
    public AsyncTaskExecutor fallbackExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(10);
        // 最大线程数
        executor.setMaxPoolSize(50);
        // 队列容量
        executor.setQueueCapacity(200);
        // 线程名前缀
        executor.setThreadNamePrefix("async-executor-");
        // 线程空闲时间
        executor.setKeepAliveSeconds(60);
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}