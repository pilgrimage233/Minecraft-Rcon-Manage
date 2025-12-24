package cc.endmc.permission.service;

import cc.endmc.permission.domain.SysUserMcInstance;
import cc.endmc.permission.domain.SysUserNodeServer;
import cc.endmc.permission.domain.SysUserRconServer;

import java.util.List;

/**
 * 资源权限服务接口 - 统一的权限检查入口
 *
 * @author Memory
 * @date 2025-12-20
 */
public interface IResourcePermissionService {

    // ==================== RCON服务器权限 ====================

    /**
     * 检查用户是否有RCON服务器的指定权限
     */
    boolean hasRconPermission(Long userId, Long serverId, String permission);

    /**
     * 获取用户有权限的RCON服务器ID列表
     */
    List<Long> getUserRconServerIds(Long userId);

    /**
     * 获取用户的RCON服务器权限详情
     */
    SysUserRconServer getUserRconPermission(Long userId, Long serverId);

    /**
     * 检查命令是否在白名单/黑名单中
     */
    boolean isCommandAllowed(Long userId, Long serverId, String command);

    // ==================== 节点服务器权限 ====================

    /**
     * 检查用户是否有节点的指定权限
     */
    boolean hasNodePermission(Long userId, Long nodeId, String permission);

    /**
     * 获取用户有权限的节点ID列表
     */
    List<Long> getUserNodeIds(Long userId);

    /**
     * 获取用户的节点权限详情
     */
    SysUserNodeServer getUserNodePermission(Long userId, Long nodeId);

    // ==================== MC实例权限 ====================

    /**
     * 检查用户是否有MC实例的指定权限
     */
    boolean hasInstancePermission(Long userId, Long instanceId, String permission);

    /**
     * 获取用户有权限的MC实例ID列表
     */
    List<Long> getUserInstanceIds(Long userId);

    /**
     * 获取用户在指定节点下有权限的MC实例ID列表
     */
    List<Long> getUserInstanceIdsByNode(Long userId, Long nodeId);

    /**
     * 获取用户的MC实例权限详情
     */
    SysUserMcInstance getUserInstancePermission(Long userId, Long instanceId);

    /**
     * 检查控制台命令是否允许
     */
    boolean isConsoleCommandAllowed(Long userId, Long instanceId, String command);

    /**
     * 检查文件路径是否允许访问
     */
    boolean isFilePathAllowed(Long userId, Long instanceId, String filePath);

    // ==================== 通用方法 ====================

    /**
     * 检查用户是否为管理员(拥有所有权限)
     */
    boolean isAdmin(Long userId);

    /**
     * 清除用户权限缓存
     */
    void clearUserPermissionCache(Long userId);

    /**
     * 清除所有权限缓存
     */
    void clearAllPermissionCache();
}
