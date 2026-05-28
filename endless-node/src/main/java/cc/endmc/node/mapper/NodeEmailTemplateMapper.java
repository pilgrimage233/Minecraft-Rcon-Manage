package cc.endmc.node.mapper;

import cc.endmc.node.domain.NodeEmailTemplate;

import java.util.List;

/**
 * 邮件模板Mapper接口
 *
 * @author Memory
 * @date 2026-05-29
 */
@org.apache.ibatis.annotations.Mapper
public interface NodeEmailTemplateMapper {

    NodeEmailTemplate selectNodeEmailTemplateById(Long id);

    NodeEmailTemplate selectNodeEmailTemplateByKey(String templateKey);

    List<NodeEmailTemplate> selectNodeEmailTemplateList(NodeEmailTemplate nodeEmailTemplate);

    int insertNodeEmailTemplate(NodeEmailTemplate nodeEmailTemplate);

    int updateNodeEmailTemplate(NodeEmailTemplate nodeEmailTemplate);

    int deleteNodeEmailTemplateById(Long id);

    int deleteNodeEmailTemplateByIds(Long[] ids);
}
