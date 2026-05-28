package cc.endmc.server.ws.helper;

import cc.endmc.server.ws.QQMessage;

import java.util.Map;

/**
 * 机器人消息辅助类
 * 提供消息构建和格式化的公共方法
 */
public final class BotMessageHelper {

    private BotMessageHelper() {
        // 工具类，禁止实例化
    }

    /**
     * 获取用户ID的@前缀
     */
    public static String getAtPrefix(Long userId) {
        return "[CQ:at,qq=" + userId + "]";
    }

    /**
     * 获取消息发送者的@前缀
     */
    public static String getAtPrefix(QQMessage message) {
        return getAtPrefix(message.getSender().getUserId());
    }

    /**
     * 构建错误消息
     */
    public static String buildErrorMessage(Long userId, String errorMsg) {
        return getAtPrefix(userId) + " " + errorMsg;
    }

    /**
     * 构建错误消息
     */
    public static String buildErrorMessage(QQMessage message, String errorMsg) {
        return buildErrorMessage(message.getSender().getUserId(), errorMsg);
    }

    /**
     * 辅助方法：如果存在指定键值，则添加到响应消息中
     */
    public static void appendIfExists(StringBuilder response, Map<String, Object> data, String key) {
        if (data.containsKey(key)) {
            response.append(key).append(": ").append(data.get(key)).append("\n");
        }
    }

    /**
     * 清除Minecraft颜色代码
     * Minecraft使用§加颜色代码来表示颜色，如§a表示绿色，§c表示红色等
     */
    public static String stripMinecraftColorCodes(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("§[0-9a-fk-or]", "");
    }

    /**
     * 格式化运行时间
     */
    public static String formatUptime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        if (days > 0) {
            return String.format("%d天%d小时%d分钟", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes);
        } else {
            return String.format("%d分钟", minutes);
        }
    }

    /**
     * 格式化字节大小
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
