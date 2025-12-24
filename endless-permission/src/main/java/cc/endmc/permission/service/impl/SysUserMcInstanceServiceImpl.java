package cc.endmc.permission.service.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.SecurityUtils;
import cc.endmc.permission.domain.SysPermissionTemplate;
import cc.endmc.permission.domain.SysUserMcInstance;
import cc.endmc.permission.mapper.SysUserMcInstanceMapper;
import cc.endmc.permission.service.IResourcePermissionService;
import cc.endmc.permission.service.ISysPermissionTemplateService;
import cc.endmc.permission.service.ISysResourcePermissionLogService;
import cc.endmc.permission.service.ISysUserMcInstanceService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户-MC实例权限Service实现
 *
 * @author Memory
 * @date 2025-12-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserMcInstanceServiceImpl implements ISysUserMcInstanceService {

    private final SysUserMcInstanceMapper mapper;
    private final ISysPermissionTemplateService templateService;
    private final ISysResourcePermissionLogService logService;
    private final IResourcePermissionService resourcePermissionService;

    @Override
    public SysUserMcInstance selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public SysUserMcInstance selectByUserAndInstance(Long userId, Long instanceId) {
        return mapper.selectByUserAndInstance(userId, instanceId);
    }

    @Override
    public List<SysUserMcInstance> selectList(SysUserMcInstance query) {
        return mapper.selectList(query);
    }

    @Override
    @Transactional
    public int insert(SysUserMcInstance record) {
        record.setCreateBy(SecurityUtils.getUsername());
        record.setCreateTime(DateUtils.getNowDate());
        int result = mapper.insert(record);
        if (result > 0) {
            resourcePermissionService.clearUserPermissionCache(record.getUserId());
            logService.logPermissionAction("mc_instance", record.getInstanceId(), record.getInstanceName(),
                    "grant", "user", record.getUserId(), record.getUserName(),
                    JSON.toJSONString(record), "0", null);
        }
        return result;
    }

    @Override
    @Transactional
    public int update(SysUserMcInstance record) {
        record.setUpdateBy(SecurityUtils.getUsername());
        record.setUpdateTime(DateUtils.getNowDate());
        int result = mapper.update(record);
        if (result > 0) {
            resourcePermissionService.clearUserPermissionCache(record.getUserId());
            logService.logPermissionAction("mc_instance", record.getInstanceId(), record.getInstanceName(),
                    "modify", "user", record.getUserId(), record.getUserName(),
                    JSON.toJSONString(record), "0", null);
        }
        return result;
    }

    @Override
    @Transactional
    public int deleteByIds(Long[] ids) {
        for (Long id : ids) {
            SysUserMcInstance record = mapper.selectById(id);
            if (record != null) {
                resourcePermissionService.clearUserPermissionCache(record.getUserId());
                logService.logPermissionAction("mc_instance", record.getInstanceId(), null,
                        "revoke", "user", record.getUserId(), null, null, "0", null);
            }
        }
        return mapper.deleteByIds(ids);
    }

    @Override
    @Transactional
    public int deleteById(Long id) {
        SysUserMcInstance record = mapper.selectById(id);
        if (record != null) {
            resourcePermissionService.clearUserPermissionCache(record.getUserId());
            logService.logPermissionAction("mc_instance", record.getInstanceId(), null,
                    "revoke", "user", record.getUserId(), null, null, "0", null);
        }
        return mapper.deleteById(id);
    }

    @Override
    @Transactional
    public int grantPermission(SysUserMcInstance permission) {
        SysUserMcInstance existing = mapper.selectByUserAndInstance(permission.getUserId(), permission.getInstanceId());
        if (existing != null) {
            permission.setId(existing.getId());
            return update(permission);
        }
        return insert(permission);
    }

    @Override
    @Transactional
    public int revokePermission(Long userId, Long instanceId) {
        SysUserMcInstance existing = mapper.selectByUserAndInstance(userId, instanceId);
        if (existing != null) {
            return deleteById(existing.getId());
        }
        return 0;
    }

    @Override
    @Transactional
    public int grantByTemplate(Long userId, Long instanceId, String templateKey) {
        SysPermissionTemplate template = templateService.selectByKey(templateKey);
        if (template == null || !"mc_instance".equals(template.getResourceType())) {
            throw new RuntimeException("权限模板不存在或类型不匹配");
        }

        JSONObject config = JSON.parseObject(template.getPermissionConfig());
        SysUserMcInstance permission = new SysUserMcInstance();
        permission.setUserId(userId);
        permission.setInstanceId(instanceId);
        permission.setPermissionType(config.getString("permission_type"));
        permission.setCanView(config.getBooleanValue("can_view") ? 1 : 0);
        permission.setCanStart(config.getBooleanValue("can_start") ? 1 : 0);
        permission.setCanStop(config.getBooleanValue("can_stop") ? 1 : 0);
        permission.setCanRestart(config.getBooleanValue("can_restart") ? 1 : 0);
        permission.setCanConsole(config.getBooleanValue("can_console") ? 1 : 0);
        permission.setCanFile(config.getBooleanValue("can_file") ? 1 : 0);
        permission.setCanConfig(config.getBooleanValue("can_config") ? 1 : 0);
        permission.setCanDelete(config.getBooleanValue("can_delete") ? 1 : 0);
        permission.setStatus("0");

        return grantPermission(permission);
    }
}
