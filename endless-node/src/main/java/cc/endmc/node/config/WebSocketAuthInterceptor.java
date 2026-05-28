package cc.endmc.node.config;

import cc.endmc.node.common.NodeCache;
import cc.endmc.node.domain.NodeServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 认证拦截器
 * 用于验证 WebSocket 连接的认证信息
 *
 * @author Memory
 */
@Slf4j
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    // 会话认证信息缓存
    private final Map<String, Boolean> authenticatedSessions = new ConcurrentHashMap<>();

    // 最大认证会话数
    private static final int MAX_AUTH_SESSIONS = 1000;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        // 处理 CONNECT 命令 - 验证认证
        if (StompCommand.CONNECT.equals(command)) {
            return handleConnect(accessor, message);
        }

        // 处理 SUBSCRIBE 命令 - 验证订阅权限
        if (StompCommand.SUBSCRIBE.equals(command)) {
            return handleSubscribe(accessor, message);
        }

        // 处理 DISCONNECT 命令 - 清理会话
        if (StompCommand.DISCONNECT.equals(command)) {
            handleDisconnect(accessor);
        }

        return message;
    }

    /**
     * 处理连接认证
     */
    private Message<?> handleConnect(StompHeaderAccessor accessor, Message<?> message) {
        String sessionId = accessor.getSessionId();

        // 检查认证会话数限制
        if (authenticatedSessions.size() >= MAX_AUTH_SESSIONS) {
            log.warn("认证会话数已达上限: {}", MAX_AUTH_SESSIONS);
            // 可以选择拒绝连接或清理旧会话
            cleanOldSessions();
        }

        // 从 header 中获取认证 token
        String token = accessor.getFirstNativeHeader("X-Endless-Token");
        Long nodeId = parseNodeId(accessor.getFirstNativeHeader("X-Node-Id"));

        if (token == null || token.isEmpty()) {
            log.warn("WebSocket 连接缺少认证 token，拒绝连接: sessionId={}", sessionId);
            return null; // 拒绝连接
        }

        // 验证 token
        boolean authenticated = validateToken(token, nodeId);
        authenticatedSessions.put(sessionId, authenticated);

        if (!authenticated) {
            log.warn("WebSocket 认证失败: sessionId={}, nodeId={}", sessionId, nodeId);
        } else {
            log.debug("WebSocket 认证成功: sessionId={}, nodeId={}", sessionId, nodeId);
        }

        return message;
    }

    /**
     * 处理订阅验证
     */
    private Message<?> handleSubscribe(StompHeaderAccessor accessor, Message<?> message) {
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();

        // 检查会话是否已认证
        if (sessionId != null && !Boolean.TRUE.equals(authenticatedSessions.get(sessionId))) {
            log.warn("未认证会话尝试订阅，拒绝: sessionId={}, destination={}", sessionId, destination);
            return null; // 拒绝订阅
        }

        // 验证订阅目标是否合法
        if (destination != null && !isValidDestination(destination)) {
            log.warn("非法订阅目标: sessionId={}, destination={}", sessionId, destination);
        }

        return message;
    }

    /**
     * 处理断开连接
     */
    private void handleDisconnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        if (sessionId != null) {
            authenticatedSessions.remove(sessionId);
            log.debug("清理会话认证信息: sessionId={}", sessionId);
        }
    }

    /**
     * 验证 token 是否有效
     */
    private boolean validateToken(String token, Long nodeId) {
        if (nodeId == null) {
            return false;
        }

        NodeServer node = NodeCache.get(nodeId);
        if (node == null) {
            return false;
        }

        return token.equals(node.getToken());
    }

    /**
     * 验证订阅目标是否合法
     */
    private boolean isValidDestination(String destination) {
        // 允许的订阅目标前缀
        return destination.startsWith("/topic/node-console/") ||
               destination.startsWith("/topic/console/") ||
               destination.startsWith("/app/");
    }

    /**
     * 解析节点 ID
     */
    private Long parseNodeId(String nodeIdStr) {
        if (nodeIdStr == null || nodeIdStr.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(nodeIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 清理旧会话
     */
    private void cleanOldSessions() {
        // 简单策略：清理未认证的会话
        authenticatedSessions.entrySet().removeIf(entry -> !entry.getValue());
        log.info("清理旧会话后，当前认证会话数: {}", authenticatedSessions.size());
    }

    /**
     * 检查会话是否已认证
     */
    public boolean isAuthenticated(String sessionId) {
        return Boolean.TRUE.equals(authenticatedSessions.get(sessionId));
    }

    /**
     * 获取已认证会话数
     */
    public int getAuthenticatedSessionCount() {
        return (int) authenticatedSessions.values().stream().filter(Boolean::booleanValue).count();
    }
}
