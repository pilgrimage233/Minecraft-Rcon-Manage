package cc.endmc.permission.utils;

import cc.endmc.common.exception.ServiceException;
import cc.endmc.common.utils.SecurityUtils;
import cc.endmc.permission.service.IResourcePermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * RCON权限检查工具类
 *
 * @author Memory
 * @date 2025-12-25
 */
@Slf4j
@Component
public class RconPermissionUtils {

    private static IResourcePermissionService resourcePermissionService;

    /**
     * 检查当前用户是否有RCON服务器权限
     *
     * @param serverId   服务器ID
     * @param permission 权限类型
     * @return 是否有权限
     */
    public static boolean hasRconPermission(Long serverId, String permission) {
        try {
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                return false;
            }
            return resourcePermissionService.hasRconPermission(userId, serverId, permission);
        } catch (Exception e) {
            log.error("检查RCON权限失败", e);
            return false;
        }
    }

    /**
     * 检查当前用户是否有RCON服务器权限，无权限时抛出异常
     *
     * @param serverId   服务器ID
     * @param permission 权限类型
     * @param message    错误消息
     */
    public static void checkRconPermission(Long serverId, String permission, String message) {
        if (!hasRconPermission(serverId, permission)) {
            throw new ServiceException(message != null ? message : "您没有访问该RCON服务器的权限");
        }
    }

    /**
     * 检查当前用户是否有RCON服务器权限，无权限时抛出异常
     *
     * @param serverId   服务器ID
     * @param permission 权限类型
     */
    public static void checkRconPermission(Long serverId, String permission) {
        checkRconPermission(serverId, permission, null);
    }

    /**
     * 检查当前用户是否可以执行指定命令
     *
     * @param serverId 服务器ID
     * @param command  命令
     * @return 是否允许执行
     */
    public static boolean canExecuteCommand(Long serverId, String command) {
        try {
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                return false;
            }
            return resourcePermissionService.isCommandAllowed(userId, serverId, command);
        } catch (Exception e) {
            log.error("检查命令权限失败", e);
            return false;
        }
    }

    /**
     * 检查当前用户是否可以执行指定命令，无权限时抛出异常
     *
     * @param serverId 服务器ID
     * @param command  命令
     * @param message  错误消息
     */
    public static void checkCommandPermission(Long serverId, String command, String message) {
        if (!canExecuteCommand(serverId, command)) {
            throw new ServiceException(message != null ? message : "您没有权限执行此命令");
        }
    }

    /**
     * 检查当前用户是否可以执行指定命令，无权限时抛出异常
     *
     * @param serverId 服务器ID
     * @param command  命令
     */
    public static void checkCommandPermission(Long serverId, String command) {
        checkCommandPermission(serverId, command, null);
    }

    /**
     * 检查当前用户是否为管理员
     *
     * @return 是否为管理员
     */
    public static boolean isAdmin() {
        try {
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                return false;
            }
            return resourcePermissionService.isAdmin(userId);
        } catch (Exception e) {
            log.error("检查管理员权限失败", e);
            return false;
        }
    }

    /**
     * 检查当前用户是否有任意RCON服务器的指定权限
     *
     * @param permission 权限类型
     * @return 是否有权限
     */
    public static boolean hasAnyRconPermission(String permission) {
        try {
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                return false;
            }

            if (resourcePermissionService.isAdmin(userId)) {
                return true;
            }

            List<Long> serverIds = resourcePermissionService.getUserRconServerIds(userId);
            return serverIds != null && !serverIds.isEmpty();
        } catch (Exception e) {
            log.error("检查RCON权限失败", e);
            return false;
        }
    }

    /**
     * 获取当前用户可访问的RCON服务器ID列表
     *
     * @return 服务器ID列表，null表示管理员（可访问所有）
     */
    public static List<Long> getUserRconServerIds() {
        try {
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                return new ArrayList<>();
            }
            return resourcePermissionService.getUserRconServerIds(userId);
        } catch (Exception e) {
            log.error("获取用户RCON服务器列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 验证服务器ID参数
     *
     * @param serverId 服务器ID
     */
    public static void validateServerId(Long serverId) {
        if (serverId == null || serverId <= 0) {
            throw new ServiceException("服务器ID参数无效");
        }
    }

    /**
     * 验证命令参数
     *
     * @param command 命令
     */
    public static void validateCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            throw new ServiceException("命令不能为空");
        }
    }

    @Autowired
    public void setResourcePermissionService(IResourcePermissionService resourcePermissionService) {
        RconPermissionUtils.resourcePermissionService = resourcePermissionService;
    }
}