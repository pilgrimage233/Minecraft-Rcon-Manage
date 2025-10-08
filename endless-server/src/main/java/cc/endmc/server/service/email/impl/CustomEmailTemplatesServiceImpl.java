package cc.endmc.server.service.email.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.cache.EmailTempCache;
import cc.endmc.server.domain.email.CustomEmailTemplates;
import cc.endmc.server.mapper.email.CustomEmailTemplatesMapper;
import cc.endmc.server.service.email.ICustomEmailTemplatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 自定义邮件通知模板Service业务层处理
 *
 * @author memory
 * @date 2025-10-03
 */
@Service
public class CustomEmailTemplatesServiceImpl implements ICustomEmailTemplatesService {
    @Autowired
    private CustomEmailTemplatesMapper customEmailTemplatesMapper;

    /**
     * 查询自定义邮件通知模板
     *
     * @param id 自定义邮件通知模板主键
     * @return 自定义邮件通知模板
     */
    @Override
    public CustomEmailTemplates selectCustomEmailTemplatesById(Long id) {
        return customEmailTemplatesMapper.selectCustomEmailTemplatesById(id);
    }

    /**
     * 查询自定义邮件通知模板列表
     *
     * @param customEmailTemplates 自定义邮件通知模板
     * @return 自定义邮件通知模板
     */
    @Override
    public List<CustomEmailTemplates> selectCustomEmailTemplatesList(CustomEmailTemplates customEmailTemplates) {
        return customEmailTemplatesMapper.selectCustomEmailTemplatesList(customEmailTemplates);
    }

    /**
     * 新增自定义邮件通知模板
     *
     * @param customEmailTemplates 自定义邮件通知模板
     * @return 结果
     */
    @Override
    public int insertCustomEmailTemplates(CustomEmailTemplates customEmailTemplates) {
        customEmailTemplates.setCreateTime(DateUtils.getNowDate());
        final int i = customEmailTemplatesMapper.insertCustomEmailTemplates(customEmailTemplates);
        return updateCache(customEmailTemplates, i);
    }

    /**
     * 修改自定义邮件通知模板
     *
     * @param customEmailTemplates 自定义邮件通知模板
     * @return 结果
     */
    @Override
    public int updateCustomEmailTemplates(CustomEmailTemplates customEmailTemplates) {
        customEmailTemplates.setUpdateTime(DateUtils.getNowDate());
        final int i = customEmailTemplatesMapper.updateCustomEmailTemplates(customEmailTemplates);
        return updateCache(customEmailTemplates, i);
    }

    /**
     * 更新缓存
     *
     * @param customEmailTemplates 自定义邮件通知模板
     * @param i                    数据库操作结果
     * @return 结果
     */
    private int updateCache(CustomEmailTemplates customEmailTemplates, int i) {
        if (i > 0) {
            if (customEmailTemplates.getStatus() != 1L) {
                // 如果不是启用状态，则从缓存中移除
                EmailTempCache.remove(customEmailTemplates.getServerId() != null
                        ? customEmailTemplates.getServerId().toString()
                        : "default");
                return 1;
            }
            EmailTempCache.put(customEmailTemplates.getServerId() != null
                    ? customEmailTemplates.getServerId().toString()
                    : "default", customEmailTemplates
            );
            return 1;
        }
        return 0;
    }

    /**
     * 批量删除自定义邮件通知模板
     *
     * @param ids 需要删除的自定义邮件通知模板主键
     * @return 结果
     */
    @Override
    public int deleteCustomEmailTemplatesByIds(Long[] ids) {
        final int i = customEmailTemplatesMapper.deleteCustomEmailTemplatesByIds(ids);
        if (i > 0) {
            for (Long id : ids) {
                EmailTempCache.remove(id.toString());
            }
            return i;
        }
        return 0;
    }

    /**
     * 删除自定义邮件通知模板信息
     *
     * @param id 自定义邮件通知模板主键
     * @return 结果
     */
    @Override
    public int deleteCustomEmailTemplatesById(Long id) {
        final int i = customEmailTemplatesMapper.deleteCustomEmailTemplatesById(id);
        if (i > 0) {
            EmailTempCache.remove(id.toString());
            return 1;
        }
        return 0;
    }
}
