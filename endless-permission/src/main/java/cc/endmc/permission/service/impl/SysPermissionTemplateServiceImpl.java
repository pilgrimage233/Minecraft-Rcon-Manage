package cc.endmc.permission.service.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.SecurityUtils;
import cc.endmc.permission.domain.SysPermissionTemplate;
import cc.endmc.permission.mapper.SysPermissionTemplateMapper;
import cc.endmc.permission.service.ISysPermissionTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限模板Service实现
 *
 * @author Memory
 * @date 2025-12-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionTemplateServiceImpl implements ISysPermissionTemplateService {

    private final SysPermissionTemplateMapper mapper;

    @Override
    public SysPermissionTemplate selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public SysPermissionTemplate selectByKey(String templateKey) {
        return mapper.selectByKey(templateKey);
    }

    @Override
    public List<SysPermissionTemplate> selectList(SysPermissionTemplate query) {
        return mapper.selectList(query);
    }

    @Override
    public List<SysPermissionTemplate> selectByResourceType(String resourceType) {
        return mapper.selectByResourceType(resourceType);
    }

    @Override
    public int insert(SysPermissionTemplate record) {
        record.setCreateBy(SecurityUtils.getUsername());
        record.setCreateTime(DateUtils.getNowDate());
        return mapper.insert(record);
    }

    @Override
    public int update(SysPermissionTemplate record) {
        record.setUpdateBy(SecurityUtils.getUsername());
        record.setUpdateTime(DateUtils.getNowDate());
        return mapper.update(record);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        return mapper.deleteByIds(ids);
    }

    @Override
    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }
}
