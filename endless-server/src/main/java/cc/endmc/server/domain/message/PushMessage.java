package cc.endmc.server.domain.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 推送消息实体
 *
 * @author Memory
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushMessage {

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;
    /**
     * 消息ID
     */
    private String messageId;
    /**
     * 玩家名称
     */
    private String playerName;
    /**
     * 消息内容
     */
    private String message;
    /**
     * 服务器ID
     */
    private String serverId;
    /**
     * 服务器名称
     */
    private String serverName;
    /**
     * 格式化后的消息
     */
    private String formattedMessage;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 重试次数
     */
    private int retryCount;

    /**
     * 构造方法
     */
    public PushMessage(String playerName, String message, String serverId, String serverName) {
        this.messageId = generateMessageId();
        this.playerName = playerName;
        this.message = message;
        this.serverId = serverId;
        this.serverName = serverName;
        this.formattedMessage = String.format("[%s] %s: %s", serverName, playerName, message);
        this.createTime = LocalDateTime.now();
        this.retryCount = 0;
    }

    /**
     * 生成消息ID
     */
    private String generateMessageId() {
        return System.currentTimeMillis() + "-" + System.identityHashCode(Thread.currentThread());
    }

    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return retryCount < MAX_RETRY_COUNT;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetry() {
        this.retryCount++;
    }
}