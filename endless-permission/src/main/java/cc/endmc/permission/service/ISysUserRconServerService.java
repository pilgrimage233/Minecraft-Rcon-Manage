package cc.endmc.permission.service;

import cc.endmc.permission.domain.SysUserRconServer;

import java.util.List;

/**
 * 用户-RCON服务器权限Service接口
 *
 * @author Memory
 * @date 2025-12-20
 */
public interface ISysUserRconServerService {

    /**
     * 查询用户-RCON服务器权限
     */
    SysUserRconServer selectById(Long id);

    /**
     * 查询用户对特定服务器的权限
     */
    SysUserRconServer selectByUserAndServer(Long userId, Long serverId);

    /**
     * 查询用户-RCON服务器权限列表
     */
    List<SysUserRconServer> selectList(SysUserRconServer query);

    /**
     * 新增用户-RCON服务器权限
     */
    int insert(SysUserRconServer record);

    /**
     * 修改用户-RCON服务器权限
     */
    int update(SysUserRconServer record);

    /**
     * 批量删除用户-RCON服务器权限
     */
    int deleteByIds(Long[] ids);

    /**
     * 删除用户-RCON服务器权限
     */
    int deleteById(Long id);

    /**
     * 授予用户RCON服务器权限
     */
    int grantPermission(SysUserRconServer permission);

    /**
     * 撤销用户RCON服务器权限
     */
    int revokePermission(Long userId, Long serverId);

    /**
     * 使用模板授权
     */
    int grantByTemplate(Long userId, Long serverId, String templateKey);
}
