package cc.endmc.permission.service;

import cc.endmc.permission.domain.SysUserNodeServer;

import java.util.List;

/**
 * 用户-节点服务器权限Service接口
 *
 * @author Memory
 * @date 2025-12-20
 */
public interface ISysUserNodeServerService {

    SysUserNodeServer selectById(Long id);

    SysUserNodeServer selectByUserAndNode(Long userId, Long nodeId);

    List<SysUserNodeServer> selectList(SysUserNodeServer query);

    int insert(SysUserNodeServer record);

    int update(SysUserNodeServer record);

    int deleteByIds(Long[] ids);

    int deleteById(Long id);

    int grantPermission(SysUserNodeServer permission);

    int revokePermission(Long userId, Long nodeId);

    int grantByTemplate(Long userId, Long nodeId, String templateKey);
}
