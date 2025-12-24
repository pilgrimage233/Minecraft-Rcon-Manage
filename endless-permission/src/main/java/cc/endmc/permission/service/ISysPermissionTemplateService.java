package cc.endmc.permission.service;

import cc.endmc.permission.domain.SysPermissionTemplate;

import java.util.List;

/**
 * 权限模板Service接口
 *
 * @author Memory
 * @date 2025-12-20
 */
public interface ISysPermissionTemplateService {

    SysPermissionTemplate selectById(Long id);

    SysPermissionTemplate selectByKey(String templateKey);

    List<SysPermissionTemplate> selectList(SysPermissionTemplate query);

    List<SysPermissionTemplate> selectByResourceType(String resourceType);

    int insert(SysPermissionTemplate record);

    int update(SysPermissionTemplate record);

    int deleteByIds(Long[] ids);

    int deleteById(Long id);
}
