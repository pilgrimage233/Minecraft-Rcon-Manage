package cc.endmc.node.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 配置类
 * 配置消息代理、端点和安全拦截器
 *
 * @author Memory
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
class MainWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor authInterceptor;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("websocket-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @Value("${endless.node.websocket.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${endless.node.websocket.max-message-size:65536}")
    private int maxMessageSize;

    @Value("${endless.node.websocket.max-session-idle-timeout:1800000}")
    private long sessionIdleTimeout;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单消息代理，配置心跳和TaskScheduler
        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{10000, 10000}) // 心跳间隔
                .setTaskScheduler(taskScheduler()); // 设置任务调度器

        // 设置应用目标前缀
        registry.setApplicationDestinationPrefixes("/app");

        // 设置用户目标前缀（用于点对点消息）
        registry.setUserDestinationPrefix("/user");

        log.info("WebSocket 消息代理配置完成");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 配置允许的来源
        String[] origins = allowedOrigins.split(",");
        String[] allowedOriginPatterns = origins.length > 0 ? origins : new String[]{"*"};

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOriginPatterns)
                .withSockJS()
                .setSessionCookieNeeded(false)
                .setDisconnectDelay(30000) // 断开延迟
                .setHeartbeatTime(25000); // 心跳时间

        log.info("WebSocket 端点注册完成，允许的来源: {}", allowedOrigins);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 添加认证拦截器
        registration.interceptors(authInterceptor);

        // 配置线程池
        registration.taskExecutor()
                .corePoolSize(4)
                .maxPoolSize(8)
                .queueCapacity(100);

        log.info("WebSocket 入站通道配置完成");
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // 配置出站通道线程池
        registration.taskExecutor()
                .corePoolSize(4)
                .maxPoolSize(8)
                .queueCapacity(100);
    }
}

