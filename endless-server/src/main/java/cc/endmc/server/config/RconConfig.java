package cc.endmc.server.config;

import cc.endmc.server.common.rconclient.RconClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Rcon配置类
 * 用于配置Rcon连接的相关参数
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "rcon")
public class RconConfig {

    /**
     * 连接与读写超时时间（毫秒）。
     */
    private int timeout = 5000;

    /**
     * 连接失败后的重连间隔（毫秒）。
     */
    private int reconnectDelayMs = 1000;

    /**
     * 单次响应最大允许字节数，用于防止异常大包。
     */
    private int maxResponseSize = 4096;

    /**
     * Socket 收发缓冲区默认大小（字节）。
     */
    private int maxBufferSize = 8196;

    /**
     * RCON 负载编码字符集，默认 UTF-8。
     */
    private String payloadCharset = StandardCharsets.UTF_8.name();

    /**
     * 单次命令发送失败后的最大重试次数。
     */
    private int maxReconnectAttempts = 3;

    /**
     * ByteBuffer 对象池初始容量。
     */
    private int bufferPoolSize = 10;

    /**
     * RCON 命令包类型（协议默认 2）。
     */
    private int typeCommand = 2;

    /**
     * RCON 认证包类型（协议默认 3）。
     */
    private int typeAuth = 3;

    public void init() {
        RconClient.RECONNECT_DELAY_MS = reconnectDelayMs;
        RconClient.MAX_RESPONSE_SIZE = maxResponseSize;
        RconClient.DEFAULT_BUFFER_SIZE = maxBufferSize;
        RconClient.DEFAULT_TIMEOUT_MS = timeout;
        RconClient.MAX_RECONNECT_ATTEMPTS = maxReconnectAttempts;
        RconClient.BUFFER_POOL_SIZE = bufferPoolSize;
        RconClient.TYPE_COMMAND = typeCommand;
        RconClient.TYPE_AUTH = typeAuth;

        try {
            RconClient.PAYLOAD_CHARSET = Charset.forName(payloadCharset);
        } catch (Exception e) {
            RconClient.PAYLOAD_CHARSET = StandardCharsets.UTF_8;
            log.warn("RCON配置中的 payloadCharset={} 非法，已回退为 UTF-8", payloadCharset);
        }

        log.info("RCON配置初始化成功 : timeout={}, reconnectDelayMs={}, maxResponseSize={}, maxBufferSize={}, payloadCharset={}, maxReconnectAttempts={}, bufferPoolSize={}, typeCommand={}, typeAuth={}",
                timeout, reconnectDelayMs, maxResponseSize, maxBufferSize, RconClient.PAYLOAD_CHARSET,
                maxReconnectAttempts, bufferPoolSize, typeCommand, typeAuth);
    }

}