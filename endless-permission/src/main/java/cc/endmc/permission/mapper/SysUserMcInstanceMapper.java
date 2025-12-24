package cc.endmc.permission.mapper;

import cc.endmc.permission.domain.SysUserMcInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户-MC实例权限Mapper接口
 *
 * @author Memory
 * @date 2025-12-20
 */
@Mapper
public interface SysUserMcInstanceMapper {

    SysUserMcInstance selectById(Long id);

    SysUserMcInstance selectByUserAndInstance(@Param("userId") Long userId, @Param("instanceId") Long instanceId);

    List<SysUserMcInstance> selectList(SysUserMcInstance query);

    List<Long> selectInstanceIdsByUserId(Long userId);

    List<Long> selectInstanceIdsByUserAndNode(@Param("userId") Long userId, @Param("nodeId") Long nodeId);

    int insert(SysUserMcInstance record);

    int update(SysUserMcInstance record);

    int deleteById(Long id);

    int deleteByIds(Long[] ids);

    int deleteByUserId(Long userId);

    int deleteByInstanceId(Long instanceId);

    int deleteByNodeId(Long nodeId);

    int checkPermission(@Param("userId") Long userId, @Param("instanceId") Long instanceId, @Param("permission") String permission);
}
