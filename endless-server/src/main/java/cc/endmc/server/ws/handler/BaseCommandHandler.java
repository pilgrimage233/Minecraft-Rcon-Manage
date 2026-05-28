package cc.endmc.server.ws.handler;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.server.domain.bot.QqBotConfig;
import cc.endmc.server.domain.bot.QqBotManager;
import cc.endmc.server.ws.BotClient;
import cc.endmc.server.ws.QQMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 命令处理器基类
 * 包含所有命令处理器共享的依赖和工具方法
 */
@Slf4j
public abstract class BaseCommandHandler {

    /**
     * 获取BotClient实例（用于发送消息和获取配置）
     */
    protected final BotClient botClient;

    /**
     * Redis缓存
     */
    protected final RedisCache redisCache;

    /**
     * 构造函数
     *
     * @param botClient BotClient实例
     * @param redisCache Redis缓存
     */
    protected BaseCommandHandler(BotClient botClient, RedisCache redisCache) {
        this.botClient = botClient;
        this.redisCache = redisCache;
    }

    /**
     * 获取机器人配置
     */
    protected QqBotConfig getConfig() {
        return botClient.getConfig();
    }

    /**
     * 发送消息
     */
    protected void sendMessage(QQMessage message, String msg) {
        botClient.sendMessage(message, msg);
    }

    /**
     * 获取用户ID的@前缀
     */
    protected String getAtPrefix(QQMessage message) {
        return "[CQ:at,qq=" + message.getSender().getUserId() + "]";
    }

    /**
     * 检查是否是管理员
     */
    protected boolean isAdmin(QQMessage message) {
        QqBotConfig config = getConfig();
        return config != null && !config.selectManagerForThisGroup(message.getGroupId(), message.getUserId()).isEmpty();
    }

    /**
     * 检查是否是超级管理员（权限类型为0）
     */
    protected boolean isSuperAdmin(QQMessage message) {
        QqBotConfig config = getConfig();
        if (config == null) {
            return false;
        }
        List<QqBotManager> managers = config.selectManagerForThisGroup(message.getGroupId(), message.getUserId());
        return !managers.isEmpty() && managers.get(0).getPermissionType() == 0;
    }

    /**
     * 获取管理员信息
     */
    protected List<QqBotManager> getManagers(QQMessage message) {
        QqBotConfig config = getConfig();
        if (config == null) {
            return List.of();
        }
        return config.selectManagerForThisGroup(message.getGroupId(), message.getUserId());
    }

    /**
     * 检查管理员权限，如果无权限则发送消息并返回false
     */
    protected boolean checkAdminPermission(QQMessage message) {
        if (!isAdmin(message)) {
            sendMessage(message, getAtPrefix(message) + " 您没有权限执行此操作。");
            return false;
        }
        return true;
    }

    /**
     * 检查超级管理员权限，如果无权限则发送消息并返回false
     */
    protected boolean checkSuperAdminPermission(QQMessage message) {
        if (!isSuperAdmin(message)) {
            sendMessage(message, getAtPrefix(message) + " 您没有权限执行此操作，此操作仅限超级管理员使用。");
            return false;
        }
        return true;
    }

    /**
     * 更新管理员最后活跃时间
     */
    protected void updateManagerLastActiveTime(Long userId, Long botId) {
        botClient.updateQqBotManagerLastActiveTime(userId, botId);
    }

    /**
     * 处理权限检查和管理员活跃时间更新的通用模板方法
     */
    protected void executeWithPermissionCheck(QQMessage message, boolean requireSuperAdmin, Runnable action) {
        if (requireSuperAdmin) {
            if (!checkSuperAdminPermission(message)) {
                return;
            }
        } else {
            if (!checkAdminPermission(message)) {
                return;
            }
        }

        try {
            action.run();
            updateManagerLastActiveTime(message.getSender().getUserId(), getConfig().getId());
        } catch (Exception e) {
            log.error("处理命令失败: {}", e.getMessage(), e);
            sendMessage(message, getAtPrefix(message) + " 操作失败，请稍后重试。");
        }
    }
}
