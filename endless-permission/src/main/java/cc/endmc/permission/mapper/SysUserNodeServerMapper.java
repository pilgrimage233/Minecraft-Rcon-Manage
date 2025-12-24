package cc.endmc.permission.mapper;

import cc.endmc.permission.domain.SysUserNodeServer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户-节点服务器权限Mapper接口
 *
 * @author Memory
 * @date 2025-12-20
 */
@Mapper
public interface SysUserNodeServerMapper {

    SysUserNodeServer selectById(Long id);

    SysUserNodeServer selectByUserAndNode(@Param("userId") Long userId, @Param("nodeId") Long nodeId);

    List<SysUserNodeServer> selectList(SysUserNodeServer query);

    List<Long> selectNodeIdsByUserId(Long userId);

    int insert(SysUserNodeServer record);

    int update(SysUserNodeServer record);

    int deleteById(Long id);

    int deleteByIds(Long[] ids);

    int deleteByUserId(Long userId);

    int deleteByNodeId(Long nodeId);

    int checkPermission(@Param("userId") Long userId, @Param("nodeId") Long nodeId, @Param("permission") String permission);
}
