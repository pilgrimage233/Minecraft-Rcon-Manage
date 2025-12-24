package cc.endmc.permission.service;

import cc.endmc.permission.domain.SysResourcePermissionLog;

import java.util.List;

/**
 * 资源权限操作日志Service接口
 *
 * @author Memory
 * @date 2025-12-20
 */
public interface ISysResourcePermissionLogService {

    SysResourcePermissionLog selectById(Long id);

    List<SysResourcePermissionLog> selectList(SysResourcePermissionLog query);

    int insert(SysResourcePermissionLog record);

    int deleteByIds(Long[] ids);

    /**
     * 记录权限操作日志
     */
    void logPermissionAction(String resourceType, Long resourceId, String resourceName,
                             String actionType, String targetType, Long targetId, String targetName,
                             String permissionDetail, String status, String errorMsg);

    /**
     * 清理指定天数之前的日志
     */
    int cleanLogBeforeDays(int days);
}
