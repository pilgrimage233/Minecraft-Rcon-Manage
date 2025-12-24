package cc.endmc.permission.service.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.SecurityUtils;
import cc.endmc.permission.domain.SysPermissionTemplate;
import cc.endmc.permission.domain.SysUserRconServer;
import cc.endmc.permission.mapper.SysUserRconServerMapper;
import cc.endmc.permission.service.IResourcePermissionService;
import cc.endmc.permission.service.ISysPermissionTemplateService;
import cc.endmc.permission.service.ISysResourcePermissionLogService;
import cc.endmc.permission.service.ISysUserRconServerService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户-RCON服务器权限Service实现
 *
 * @author Memory
 * @date 2025-12-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserRconServerServiceImpl implements ISysUserRconServerService {

    private final SysUserRconServerMapper mapper;
    private final ISysPermissionTemplateService templateService;
    private final ISysResourcePermissionLogService logService;
    private final IResourcePermissionService resourcePermissionService;

    @Override
    public SysUserRconServer selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public SysUserRconServer selectByUserAndServer(Long userId, Long serverId) {
        return mapper.selectByUserAndServer(userId, serverId);
    }

    @Override
    public List<SysUserRconServer> selectList(SysUserRconServer query) {
        return mapper.selectList(query);
    }

    @Override
    @Transactional
    public int insert(SysUserRconServer record) {
        record.setCreateBy(SecurityUtils.getUsername());
        record.setCreateTime(DateUtils.getNowDate());
        int result = mapper.insert(record);
        if (result > 0) {
            resourcePermissionService.clearUserPermissionCache(record.getUserId());
            logService.logPermissionAction("rcon_server", record.getServerId(), record.getServerName(),
                    "grant", "user", record.getUserId(), record.getUserName(),
                    JSON.toJSONString(record), "0", null);
        }
        return result;
    }

    @Override
    @Transactional
    public int update(SysUserRconServer record) {
        record.setUpdateBy(SecurityUtils.getUsername());
        record.setUpdateTime(DateUtils.getNowDate());
        int result = mapper.update(record);
        if (result > 0) {
            resourcePermissionService.clearUserPermissionCache(record.getUserId());
            logService.logPermissionAction("rcon_server", record.getServerId(), record.getServerName(),
                    "modify", "user", record.getUserId(), record.getUserName(),
                    JSON.toJSONString(record), "0", null);
        }
        return result;
    }

    @Override
    @Transactional
    public int deleteByIds(Long[] ids) {
        for (Long id : ids) {
            SysUserRconServer record = mapper.selectById(id);
            if (record != null) {
                resourcePermissionService.clearUserPermissionCache(record.getUserId());
                logService.logPermissionAction("rcon_server", record.getServerId(), null,
                        "revoke", "user", record.getUserId(), null, null, "0", null);
            }
        }
        return mapper.deleteByIds(ids);
    }

    @Override
    @Transactional
    public int deleteById(Long id) {
        SysUserRconServer record = mapper.selectById(id);
        if (record != null) {
            resourcePermissionService.clearUserPermissionCache(record.getUserId());
            logService.logPermissionAction("rcon_server", record.getServerId(), null,
                    "revoke", "user", record.getUserId(), null, null, "0", null);
        }
        return mapper.deleteById(id);
    }

    @Override
    @Transactional
    public int grantPermission(SysUserRconServer permission) {
        SysUserRconServer existing = mapper.selectByUserAndServer(permission.getUserId(), permission.getServerId());
        if (existing != null) {
            permission.setId(existing.getId());
            return update(permission);
        }
        return insert(permission);
    }

    @Override
    @Transactional
    public int revokePermission(Long userId, Long serverId) {
        SysUserRconServer existing = mapper.selectByUserAndServer(userId, serverId);
        if (existing != null) {
            return deleteById(existing.getId());
        }
        return 0;
    }

    @Override
    @Transactional
    public int grantByTemplate(Long userId, Long serverId, String templateKey) {
        SysPermissionTemplate template = templateService.selectByKey(templateKey);
        if (template == null || !"rcon_server".equals(template.getResourceType())) {
            throw new RuntimeException("权限模板不存在或类型不匹配");
        }

        JSONObject config = JSON.parseObject(template.getPermissionConfig());
        SysUserRconServer permission = new SysUserRconServer();
        permission.setUserId(userId);
        permission.setServerId(serverId);
        permission.setPermissionType(config.getString("permission_type"));
        permission.setCanExecuteCmd(config.getBooleanValue("can_execute_cmd") ? 1 : 0);
        permission.setCanViewLog(config.getBooleanValue("can_view_log") ? 1 : 0);
        permission.setCanManage(config.getBooleanValue("can_manage") ? 1 : 0);
        permission.setStatus("0");

        return grantPermission(permission);
    }
}
