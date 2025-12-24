package cc.endmc.permission.mapper;

import cc.endmc.permission.domain.SysResourcePermissionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资源权限操作日志Mapper接口
 *
 * @author Memory
 * @date 2025-12-20
 */
@Mapper
public interface SysResourcePermissionLogMapper {

    SysResourcePermissionLog selectById(Long id);

    List<SysResourcePermissionLog> selectList(SysResourcePermissionLog query);

    /**
     * 根据资源查询日志
     */
    List<SysResourcePermissionLog> selectByResource(@Param("resourceType") String resourceType, @Param("resourceId") Long resourceId);

    /**
     * 根据目标查询日志
     */
    List<SysResourcePermissionLog> selectByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    int insert(SysResourcePermissionLog record);

    int deleteById(Long id);

    int deleteByIds(Long[] ids);

    /**
     * 清理指定天数之前的日志
     */
    int cleanLogBeforeDays(int days);
}
