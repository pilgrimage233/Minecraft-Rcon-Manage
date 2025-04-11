package cc.endmc.server.config;

import cc.endmc.server.common.rconclient.RconClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Rcon配置类
 * 用于配置Rcon连接的相关参数
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "rcon")
public class RconConfig {

    private int timeout = 5000; // 连接超时时间
    private int reconnectDelayMs = 1000; // 重连延迟时间
    private int maxResponseSize = 4096; // 最大响应大小
    private int maxBufferSize = 8196; // 最大缓冲区大小

    public void init() {
        RconClient.RECONNECT_DELAY_MS = reconnectDelayMs;
        RconClient.MAX_RESPONSE_SIZE = maxResponseSize;
        RconClient.DEFAULT_BUFFER_SIZE = maxBufferSize;
        RconClient.DEFAULT_TIMEOUT_MS = timeout;
        log.info("RCON配置初始化成功 : timeout={}, reconnectDelayMs={}, maxResponseSize={}, maxBufferSize={}",
                timeout, reconnectDelayMs, maxResponseSize, maxBufferSize);
    }

}