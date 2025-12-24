package cc.endmc.permission.service.impl;

import cc.endmc.common.utils.SecurityUtils;
import cc.endmc.common.utils.ServletUtils;
import cc.endmc.common.utils.ip.IpUtils;
import cc.endmc.permission.domain.SysResourcePermissionLog;
import cc.endmc.permission.mapper.SysResourcePermissionLogMapper;
import cc.endmc.permission.service.ISysResourcePermissionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 资源权限操作日志Service实现
 *
 * @author Memory
 * @date 2025-12-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysResourcePermissionLogServiceImpl implements ISysResourcePermissionLogService {

    private final SysResourcePermissionLogMapper mapper;

    @Override
    public SysResourcePermissionLog selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<SysResourcePermissionLog> selectList(SysResourcePermissionLog query) {
        return mapper.selectList(query);
    }

    @Override
    public int insert(SysResourcePermissionLog record) {
        return mapper.insert(record);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        return mapper.deleteByIds(ids);
    }

    @Override
    @Async
    public void logPermissionAction(String resourceType, Long resourceId, String resourceName,
                                    String actionType, String targetType, Long targetId, String targetName,
                                    String permissionDetail, String status, String errorMsg) {
        try {
            SysResourcePermissionLog logRecord = new SysResourcePermissionLog();
            logRecord.setUserId(SecurityUtils.getUserId());
            logRecord.setUserName(SecurityUtils.getUsername());
            logRecord.setResourceType(resourceType);
            logRecord.setResourceId(resourceId);
            logRecord.setResourceName(resourceName);
            logRecord.setActionType(actionType);
            logRecord.setTargetType(targetType);
            logRecord.setTargetId(targetId);
            logRecord.setTargetName(targetName);
            logRecord.setPermissionDetail(permissionDetail);
            logRecord.setIpAddress(IpUtils.getIpAddr(ServletUtils.getRequest()));
            logRecord.setStatus(status);
            logRecord.setErrorMsg(errorMsg);
            logRecord.setCreateTime(new Date());
            
            mapper.insert(logRecord);
        } catch (Exception e) {
            log.error("记录权限操作日志失败", e);
        }
    }

    @Override
    public int cleanLogBeforeDays(int days) {
        return mapper.cleanLogBeforeDays(days);
    }
}
