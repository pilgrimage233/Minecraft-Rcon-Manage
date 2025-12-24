package cc.endmc.permission.mapper;

import cc.endmc.permission.domain.SysPermissionTemplate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 权限模板Mapper接口
 *
 * @author Memory
 * @date 2025-12-20
 */
@Mapper
public interface SysPermissionTemplateMapper {

    SysPermissionTemplate selectById(Long id);

    SysPermissionTemplate selectByKey(String templateKey);

    List<SysPermissionTemplate> selectList(SysPermissionTemplate query);

    List<SysPermissionTemplate> selectByResourceType(String resourceType);

    /**
     * 检查模板标识唯一性
     */
    int checkTemplateKeyUnique(SysPermissionTemplate template);

    int insert(SysPermissionTemplate record);

    int update(SysPermissionTemplate record);

    int deleteById(Long id);

    int deleteByIds(Long[] ids);
}
