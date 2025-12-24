package cc.endmc.permission.service.impl;

import cc.endmc.common.core.domain.entity.SysUser;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.SecurityUtils;
import cc.endmc.permission.domain.SysUserMcInstance;
import cc.endmc.permission.domain.SysUserNodeServer;
import cc.endmc.permission.domain.SysUserRconServer;
import cc.endmc.permission.mapper.SysUserMcInstanceMapper;
import cc.endmc.permission.mapper.SysUserNodeServerMapper;
import cc.endmc.permission.mapper.SysUserRconServerMapper;
import cc.endmc.permission.service.IResourcePermissionService;
import cc.endmc.system.service.ISysUserService;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 资源权限服务实现类
 *
 * @author Memory
 * @date 2025-12-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourcePermissionServiceImpl implements IResourcePermissionService {

    private final SysUserRconServerMapper rconServerMapper;
    private final SysUserNodeServerMapper nodeServerMapper;
    private final SysUserMcInstanceMapper mcInstanceMapper;
    private final ISysUserService userService;
    private final RedisCache redisCache;

    private static final String CACHE_PREFIX = "resource_permission:";
    private static final int CACHE_EXPIRE_MINUTES = 30;

    @Override
    public boolean hasRconPermission(Long userId, Long serverId, String permission) {
        if (isAdmin(userId)) {
            return true;
        }
        return rconServerMapper.checkPermission(userId, serverId, permission) > 0;
    }

    @Override
    public List<Long> getUserRconServerIds(Long userId) {
        if (isAdmin(userId)) {
            return null; // null表示有所有权限
        }
        String cacheKey = CACHE_PREFIX + "rcon:" + userId;
        List<Long> ids = redisCache.getCacheObject(cacheKey);
        if (ids == null) {
            ids = rconServerMapper.selectServerIdsByUserId(userId);
            if (ids == null) {
                ids = Collections.emptyList();
            }
            redisCache.setCacheObject(cacheKey, ids, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        return ids;
    }

    @Override
    public SysUserRconServer getUserRconPermission(Long userId, Long serverId) {
        return rconServerMapper.selectByUserAndServer(userId, serverId);
    }

    @Override
    public boolean isCommandAllowed(Long userId, Long serverId, String command) {
        if (isAdmin(userId)) {
            return true;
        }
        SysUserRconServer permission = getUserRconPermission(userId, serverId);
        if (permission == null || permission.getCanExecuteCmd() != 1) {
            return false;
        }
        return checkCommandAgainstLists(command, permission.getCmdWhitelist(), permission.getCmdBlacklist());
    }

    @Override
    public boolean hasNodePermission(Long userId, Long nodeId, String permission) {
        if (isAdmin(userId)) {
            return true;
        }
        return nodeServerMapper.checkPermission(userId, nodeId, permission) > 0;
    }

    @Override
    public List<Long> getUserNodeIds(Long userId) {
        if (isAdmin(userId)) {
            return null;
        }
        String cacheKey = CACHE_PREFIX + "node:" + userId;
        List<Long> ids = redisCache.getCacheObject(cacheKey);
        if (ids == null) {
            ids = nodeServerMapper.selectNodeIdsByUserId(userId);
            if (ids == null) {
                ids = Collections.emptyList();
            }
            redisCache.setCacheObject(cacheKey, ids, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        return ids;
    }

    @Override
    public SysUserNodeServer getUserNodePermission(Long userId, Long nodeId) {
        return nodeServerMapper.selectByUserAndNode(userId, nodeId);
    }

    @Override
    public boolean hasInstancePermission(Long userId, Long instanceId, String permission) {
        if (isAdmin(userId)) {
            return true;
        }
        return mcInstanceMapper.checkPermission(userId, instanceId, permission) > 0;
    }

    @Override
    public List<Long> getUserInstanceIds(Long userId) {
        if (isAdmin(userId)) {
            return null;
        }
        String cacheKey = CACHE_PREFIX + "instance:" + userId;
        List<Long> ids = redisCache.getCacheObject(cacheKey);
        if (ids == null) {
            ids = mcInstanceMapper.selectInstanceIdsByUserId(userId);
            if (ids == null) {
                ids = Collections.emptyList();
            }
            redisCache.setCacheObject(cacheKey, ids, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        return ids;
    }

    @Override
    public List<Long> getUserInstanceIdsByNode(Long userId, Long nodeId) {
        if (isAdmin(userId)) {
            return null;
        }
        return mcInstanceMapper.selectInstanceIdsByUserAndNode(userId, nodeId);
    }

    @Override
    public SysUserMcInstance getUserInstancePermission(Long userId, Long instanceId) {
        return mcInstanceMapper.selectByUserAndInstance(userId, instanceId);
    }

    @Override
    public boolean isConsoleCommandAllowed(Long userId, Long instanceId, String command) {
        if (isAdmin(userId)) {
            return true;
        }
        SysUserMcInstance permission = getUserInstancePermission(userId, instanceId);
        if (permission == null || permission.getCanConsole() != 1) {
            return false;
        }
        return checkCommandAgainstLists(command, permission.getConsoleCmdWhitelist(), permission.getConsoleCmdBlacklist());
    }

    @Override
    public boolean isFilePathAllowed(Long userId, Long instanceId, String filePath) {
        if (isAdmin(userId)) {
            return true;
        }
        SysUserMcInstance permission = getUserInstancePermission(userId, instanceId);
        if (permission == null || permission.getCanFile() != 1) {
            return false;
        }
        return checkPathAgainstLists(filePath, permission.getFilePathWhitelist(), permission.getFilePathBlacklist());
    }

    @Override
    public boolean isAdmin(Long userId) {
        try {
            SysUser user = userService.selectUserById(userId);
            return user != null && user.isAdmin();
        } catch (Exception e) {
            log.error("检查用户是否为管理员失败", e);
            return false;
        }
    }

    @Override
    public void clearUserPermissionCache(Long userId) {
        redisCache.deleteObject(CACHE_PREFIX + "rcon:" + userId);
        redisCache.deleteObject(CACHE_PREFIX + "node:" + userId);
        redisCache.deleteObject(CACHE_PREFIX + "instance:" + userId);
    }

    @Override
    public void clearAllPermissionCache() {
        redisCache.deleteObject(CACHE_PREFIX + "*");
    }

    /**
     * 检查命令是否符合白名单/黑名单规则
     */
    private boolean checkCommandAgainstLists(String command, String whitelist, String blacklist) {
        // 先检查黑名单
        if (blacklist != null && !blacklist.isEmpty()) {
            List<String> blacklistCmds = JSON.parseArray(blacklist, String.class);
            if (blacklistCmds != null) {
                for (String pattern : blacklistCmds) {
                    if (matchCommand(command, pattern)) {
                        return false;
                    }
                }
            }
        }
        // 如果有白名单，必须在白名单中
        if (whitelist != null && !whitelist.isEmpty()) {
            List<String> whitelistCmds = JSON.parseArray(whitelist, String.class);
            if (whitelistCmds != null && !whitelistCmds.isEmpty()) {
                for (String pattern : whitelistCmds) {
                    if (matchCommand(command, pattern)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return true;
    }

    /**
     * 检查路径是否符合白名单/黑名单规则
     */
    private boolean checkPathAgainstLists(String path, String whitelist, String blacklist) {
        if (blacklist != null && !blacklist.isEmpty()) {
            List<String> blacklistPaths = JSON.parseArray(blacklist, String.class);
            if (blacklistPaths != null) {
                for (String pattern : blacklistPaths) {
                    if (matchPath(path, pattern)) {
                        return false;
                    }
                }
            }
        }
        if (whitelist != null && !whitelist.isEmpty()) {
            List<String> whitelistPaths = JSON.parseArray(whitelist, String.class);
            if (whitelistPaths != null && !whitelistPaths.isEmpty()) {
                for (String pattern : whitelistPaths) {
                    if (matchPath(path, pattern)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return true;
    }

    private boolean matchCommand(String command, String pattern) {
        if (pattern.endsWith("*")) {
            return command.startsWith(pattern.substring(0, pattern.length() - 1));
        }
        return command.equalsIgnoreCase(pattern);
    }

    private boolean matchPath(String path, String pattern) {
        String normalizedPath = path.replace("\\", "/");
        String normalizedPattern = pattern.replace("\\", "/");
        if (normalizedPattern.endsWith("**")) {
            return normalizedPath.startsWith(normalizedPattern.substring(0, normalizedPattern.length() - 2));
        }
        if (normalizedPattern.endsWith("*")) {
            String prefix = normalizedPattern.substring(0, normalizedPattern.length() - 1);
            return normalizedPath.startsWith(prefix) && !normalizedPath.substring(prefix.length()).contains("/");
        }
        return normalizedPath.equals(normalizedPattern);
    }
}
