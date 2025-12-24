package cc.endmc.permission.mapper;

import cc.endmc.permission.domain.SysUserRconServer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户-RCON服务器权限Mapper接口
 *
 * @author Memory
 * @date 2025-12-20
 */
@Mapper
public interface SysUserRconServerMapper {

    /**
     * 查询用户-RCON服务器权限
     */
    SysUserRconServer selectById(Long id);

    /**
     * 查询用户对特定服务器的权限
     */
    SysUserRconServer selectByUserAndServer(@Param("userId") Long userId, @Param("serverId") Long serverId);

    /**
     * 查询用户-RCON服务器权限列表
     */
    List<SysUserRconServer> selectList(SysUserRconServer query);

    /**
     * 查询用户拥有权限的所有服务器ID
     */
    List<Long> selectServerIdsByUserId(Long userId);

    /**
     * 新增用户-RCON服务器权限
     */
    int insert(SysUserRconServer record);

    /**
     * 修改用户-RCON服务器权限
     */
    int update(SysUserRconServer record);

    /**
     * 删除用户-RCON服务器权限
     */
    int deleteById(Long id);

    /**
     * 批量删除
     */
    int deleteByIds(Long[] ids);

    /**
     * 删除用户的所有RCON服务器权限
     */
    int deleteByUserId(Long userId);

    /**
     * 删除服务器的所有用户权限
     */
    int deleteByServerId(Long serverId);

    /**
     * 检查用户是否有指定权限
     */
    int checkPermission(@Param("userId") Long userId, @Param("serverId") Long serverId, @Param("permission") String permission);
}
