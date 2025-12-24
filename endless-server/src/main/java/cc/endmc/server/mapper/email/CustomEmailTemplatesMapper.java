package cc.endmc.server.mapper.email;

import cc.endmc.server.domain.email.CustomEmailTemplates;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 自定义邮件通知模板Mapper接口
 *
 * @author memory
 * @date 2025-10-03
 */
@Mapper
public interface CustomEmailTemplatesMapper {
    /**
     * 查询自定义邮件通知模板
     *
     * @param id 自定义邮件通知模板主键
     * @return 自定义邮件通知模板
     */
    public CustomEmailTemplates selectCustomEmailTemplatesById(Long id);

    /**
     * 查询自定义邮件通知模板列表
     *
     * @param customEmailTemplates 自定义邮件通知模板
     * @return 自定义邮件通知模板集合
     */
    public List<CustomEmailTemplates> selectCustomEmailTemplatesList(CustomEmailTemplates customEmailTemplates);

    /**
     * 新增自定义邮件通知模板
     *
     * @param customEmailTemplates 自定义邮件通知模板
     * @return 结果
     */
    public int insertCustomEmailTemplates(CustomEmailTemplates customEmailTemplates);

    /**
     * 修改自定义邮件通知模板
     *
     * @param customEmailTemplates 自定义邮件通知模板
     * @return 结果
     */
    public int updateCustomEmailTemplates(CustomEmailTemplates customEmailTemplates);

    /**
     * 删除自定义邮件通知模板
     *
     * @param id 自定义邮件通知模板主键
     * @return 结果
     */
    public int deleteCustomEmailTemplatesById(Long id);

    /**
     * 批量删除自定义邮件通知模板
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCustomEmailTemplatesByIds(Long[] ids);
}
