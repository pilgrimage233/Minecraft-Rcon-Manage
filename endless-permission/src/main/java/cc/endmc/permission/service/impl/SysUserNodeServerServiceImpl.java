package cc.endmc.permission.service.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.SecurityUtils;
import cc.endmc.permission.domain.SysPermissionTemplate;
import cc.endmc.permission.domain.SysUserNodeServer;
import cc.endmc.permission.mapper.SysUserNodeServerMapper;
import cc.endmc.permission.service.IResourcePermissionService;
import cc.endmc.permission.service.ISysPermissionTemplateService;
import cc.endmc.permission.service.ISysResourcePermissionLogService;
import cc.endmc.permission.service.ISysUserNodeServerService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户-节点服务器权限Service实现
 *
 * @author Memory
 * @date 2025-12-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserNodeServerServiceImpl implements ISysUserNodeServerService {

    private final SysUserNodeServerMapper mapper;
    private final ISysPermissionTemplateService templateService;
    private final ISysResourcePermissionLogService logService;
    private final IResourcePermissionService resourcePermissionService;

    @Override
    public SysUserNodeServer selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public SysUserNodeServer selectByUserAndNode(Long userId, Long nodeId) {
        return mapper.selectByUserAndNode(userId, nodeId);
    }

    @Override
    public List<SysUserNodeServer> selectList(SysUserNodeServer query) {
        return mapper.selectList(query);
    }

    @Override
    @Transactional
    public int insert(SysUserNodeServer record) {
        record.setCreateBy(SecurityUtils.getUsername());
        record.setCreateTime(DateUtils.getNowDate());
        int result = mapper.insert(record);
        if (result > 0) {
            resourcePermissionService.clearUserPermissionCache(record.getUserId());
            logService.logPermissionAction("node_server", record.getNodeId(), record.getNodeName(),
                    "grant", "user", record.getUserId(), record.getUserName(),
                    JSON.toJSONString(record), "0", null);
        }
        return result;
    }

    @Override
    @Transactional
    public int update(SysUserNodeServer record) {
        record.setUpdateBy(SecurityUtils.getUsername());
        record.setUpdateTime(DateUtils.getNowDate());
        int result = mapper.update(record);
        if (result > 0) {
            resourcePermissionService.clearUserPermissionCache(record.getUserId());
            logService.logPermissionAction("node_server", record.getNodeId(), record.getNodeName(),
                    "modify", "user", record.getUserId(), record.getUserName(),
                    JSON.toJSONString(record), "0", null);
        }
        return result;
    }

    @Override
    @Transactional
    public int deleteByIds(Long[] ids) {
        for (Long id : ids) {
            SysUserNodeServer record = mapper.selectById(id);
            if (record != null) {
                resourcePermissionService.clearUserPermissionCache(record.getUserId());
                logService.logPermissionAction("node_server", record.getNodeId(), null,
                        "revoke", "user", record.getUserId(), null, null, "0", null);
            }
        }
        return mapper.deleteByIds(ids);
    }

    @Override
    @Transactional
    public int deleteById(Long id) {
        SysUserNodeServer record = mapper.selectById(id);
        if (record != null) {
            resourcePermissionService.clearUserPermissionCache(record.getUserId());
            logService.logPermissionAction("node_server", record.getNodeId(), null,
                    "revoke", "user", record.getUserId(), null, null, "0", null);
        }
        return mapper.deleteById(id);
    }

    @Override
    @Transactional
    public int grantPermission(SysUserNodeServer permission) {
        SysUserNodeServer existing = mapper.selectByUserAndNode(permission.getUserId(), permission.getNodeId());
        if (existing != null) {
            permission.setId(existing.getId());
            return update(permission);
        }
        return insert(permission);
    }

    @Override
    @Transactional
    public int revokePermission(Long userId, Long nodeId) {
        SysUserNodeServer existing = mapper.selectByUserAndNode(userId, nodeId);
        if (existing != null) {
            return deleteById(existing.getId());
        }
        return 0;
    }

    @Override
    @Transactional
    public int grantByTemplate(Long userId, Long nodeId, String templateKey) {
        SysPermissionTemplate template = templateService.selectByKey(templateKey);
        if (template == null || !"node_server".equals(template.getResourceType())) {
            throw new RuntimeException("权限模板不存在或类型不匹配");
        }

        JSONObject config = JSON.parseObject(template.getPermissionConfig());
        SysUserNodeServer permission = new SysUserNodeServer();
        permission.setUserId(userId);
        permission.setNodeId(nodeId);
        permission.setPermissionType(config.getString("permission_type"));
        permission.setCanView(config.getBooleanValue("can_view") ? 1 : 0);
        permission.setCanOperate(config.getBooleanValue("can_operate") ? 1 : 0);
        permission.setCanManage(config.getBooleanValue("can_manage") ? 1 : 0);
        permission.setCanCreateInstance(config.getBooleanValue("can_create_instance") ? 1 : 0);
        permission.setStatus("0");

        return grantPermission(permission);
    }
}
