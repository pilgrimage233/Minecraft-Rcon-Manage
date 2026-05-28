package cc.endmc.node.utils;

import org.slf4j.MDC;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 日志工具类
 * 提供结构化日志、脱敏和追踪ID功能
 *
 * @author Memory
 */
public class LogUtil {

    /**
     * 追踪ID 的 MDC 键
     */
    public static final String TRACE_ID_KEY = "traceId";

    /**
     * 节点ID 的 MDC 键
     */
    public static final String NODE_ID_KEY = "nodeId";

    /**
     * 用户ID 的 MDC 键
     */
    public static final String USER_ID_KEY = "userId";

    /**
     * Token 正则模式（用于脱敏）
     */
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "(?i)(token|password|secret|key)[\"']?\\s*[:=]\\s*[\"']?([^\"'&\\s,}]+)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * IP 地址正则模式（用于脱敏）
     */
    private static final Pattern IP_PATTERN = Pattern.compile(
            "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"
    );

    /**
     * 生成并设置追踪ID
     *
     * @return 追踪ID
     */
    public static String generateTraceId() {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put(TRACE_ID_KEY, traceId);
        return traceId;
    }

    /**
     * 设置追踪ID
     *
     * @param traceId 追踪ID
     */
    public static void setTraceId(String traceId) {
        if (traceId != null) {
            MDC.put(TRACE_ID_KEY, traceId);
        }
    }

    /**
     * 获取当前追踪ID
     *
     * @return 追踪ID
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 设置节点ID
     *
     * @param nodeId 节点ID
     */
    public static void setNodeId(Long nodeId) {
        if (nodeId != null) {
            MDC.put(NODE_ID_KEY, String.valueOf(nodeId));
        }
    }

    /**
     * 获取当前节点ID
     *
     * @return 节点ID
     */
    public static String getNodeId() {
        return MDC.get(NODE_ID_KEY);
    }

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(String userId) {
        if (userId != null) {
            MDC.put(USER_ID_KEY, userId);
        }
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static String getUserId() {
        return MDC.get(USER_ID_KEY);
    }

    /**
     * 清除所有 MDC 上下文
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
        MDC.remove(NODE_ID_KEY);
        MDC.remove(USER_ID_KEY);
    }

    /**
     * 脱敏 Token
     *
     * @param message 原始消息
     * @return 脱敏后的消息
     */
    public static String maskToken(String message) {
        if (message == null) {
            return null;
        }

        return TOKEN_PATTERN.matcher(message).replaceAll(matchResult -> {
            String prefix = matchResult.group(1);
            String value = matchResult.group(2);
            if (value != null && value.length() > 8) {
                return prefix + "=" + value.substring(0, 4) + "****" + value.substring(value.length() - 4);
            }
            return prefix + "=****";
        });
    }

    /**
     * 脱敏 IP 地址
     *
     * @param message 原始消息
     * @return 脱敏后的消息
     */
    public static String maskIp(String message) {
        if (message == null) {
            return null;
        }

        return IP_PATTERN.matcher(message).replaceAll(matchResult -> {
            String ip = matchResult.group();
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + ".*.*";
            }
            return ip;
        });
    }

    /**
     * 完全脱敏（Token + IP）
     *
     * @param message 原始消息
     * @return 脱敏后的消息
     */
    public static String mask(String message) {
        if (message == null) {
            return null;
        }
        return maskIp(maskToken(message));
    }

    /**
     * 格式化日志消息（添加上下文信息）
     *
     * @param message 原始消息
     * @param args 参数
     * @return 格式化后的消息
     */
    public static String format(String message, Object... args) {
        if (message == null) {
            return null;
        }

        // 简单的 {} 占位符替换
        String result = message;
        if (args != null) {
            for (Object arg : args) {
                int index = result.indexOf("{}");
                if (index >= 0) {
                    result = result.substring(0, index) + arg + result.substring(index + 2);
                } else {
                    break;
                }
            }
        }
        return result;
    }
}
