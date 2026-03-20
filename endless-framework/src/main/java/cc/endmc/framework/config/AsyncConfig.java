package cc.endmc.framework.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 异步任务配置类
 * 当虚拟线程不可用时的备用配置
 *
 * @author Memory
 */
@Configuration
public class AsyncConfig {

    /**
     * 备用线程池执行器
     * 当虚拟线程配置未启用时，直接复用项目统一线程池，避免额外创建线程池
     */
    @Bean("virtualThreadExecutor")
    @ConditionalOnProperty(name = "spring.threads.virtual.enabled", havingValue = "false", matchIfMissing = true)
    public AsyncTaskExecutor fallbackExecutor(
            @Qualifier("threadPoolTaskExecutor") ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        return threadPoolTaskExecutor;
    }
}