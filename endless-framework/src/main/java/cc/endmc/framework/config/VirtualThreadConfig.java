package cc.endmc.framework.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

/**
 * 虚拟线程配置类
 * 需要Java 21+支持
 * 
 * @author Memory
 */
@Configuration
@EnableAsync
@ConditionalOnProperty(name = "spring.threads.virtual.enabled", havingValue = "true")
public class VirtualThreadConfig {

    /**
     * 配置虚拟线程执行器
     */
    @Bean("virtualThreadExecutor")
    public AsyncTaskExecutor virtualThreadExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * Web请求处理的虚拟线程执行器
     */
    @Bean("webVirtualThreadExecutor")
    public AsyncTaskExecutor webVirtualThreadExecutor() {
        return new TaskExecutorAdapter(
            Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual()
                    .name("web-virtual-", 0)
                    .factory()
            )
        );
    }

    /**
     * 配置Tomcat使用虚拟线程处理HTTP请求
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }
}