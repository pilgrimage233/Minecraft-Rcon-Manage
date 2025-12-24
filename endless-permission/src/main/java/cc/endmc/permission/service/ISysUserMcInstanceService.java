package cc.endmc.permission.service;

import cc.endmc.permission.domain.SysUserMcInstance;

import java.util.List;

/**
 * 用户-MC实例权限Service接口
 *
 * @author Memory
 * @date 2025-12-20
 */
public interface ISysUserMcInstanceService {

    SysUserMcInstance selectById(Long id);

    SysUserMcInstance selectByUserAndInstance(Long userId, Long instanceId);

    List<SysUserMcInstance> selectList(SysUserMcInstance query);

    int insert(SysUserMcInstance record);

    int update(SysUserMcInstance record);

    int deleteByIds(Long[] ids);

    int deleteById(Long id);

    int grantPermission(SysUserMcInstance permission);

    int revokePermission(Long userId, Long instanceId);

    int grantByTemplate(Long userId, Long instanceId, String templateKey);
}
