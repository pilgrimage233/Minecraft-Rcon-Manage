package cc.endmc.node.service;

import cc.endmc.node.domain.NodeEmailTemplate;

import java.util.List;
import java.util.Map;

/**
 * 邮件模板Service接口
 *
 * @author Memory
 * @date 2026-05-29
 */
public interface INodeEmailTemplateService {

    NodeEmailTemplate selectNodeEmailTemplateById(Long id);

    NodeEmailTemplate selectNodeEmailTemplateByKey(String templateKey);

    List<NodeEmailTemplate> selectNodeEmailTemplateList(NodeEmailTemplate nodeEmailTemplate);

    int insertNodeEmailTemplate(NodeEmailTemplate nodeEmailTemplate);

    int updateNodeEmailTemplate(NodeEmailTemplate nodeEmailTemplate);

    int deleteNodeEmailTemplateByIds(Long[] ids);

    String renderTemplate(String templateKey, Map<String, String> variables);

    String renderSubject(String templateKey, Map<String, String> variables);
}
