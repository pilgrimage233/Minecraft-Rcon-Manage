package cc.endmc.node.service.impl;

import cc.endmc.node.domain.NodeEmailTemplate;
import cc.endmc.node.mapper.NodeEmailTemplateMapper;
import cc.endmc.node.service.INodeEmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NodeEmailTemplateServiceImpl implements INodeEmailTemplateService {

    private final NodeEmailTemplateMapper nodeEmailTemplateMapper;

    @Override
    public NodeEmailTemplate selectNodeEmailTemplateById(Long id) {
        return nodeEmailTemplateMapper.selectNodeEmailTemplateById(id);
    }

    @Override
    public NodeEmailTemplate selectNodeEmailTemplateByKey(String templateKey) {
        return nodeEmailTemplateMapper.selectNodeEmailTemplateByKey(templateKey);
    }

    @Override
    public List<NodeEmailTemplate> selectNodeEmailTemplateList(NodeEmailTemplate nodeEmailTemplate) {
        return nodeEmailTemplateMapper.selectNodeEmailTemplateList(nodeEmailTemplate);
    }

    @Override
    public int insertNodeEmailTemplate(NodeEmailTemplate nodeEmailTemplate) {
        return nodeEmailTemplateMapper.insertNodeEmailTemplate(nodeEmailTemplate);
    }

    @Override
    public int updateNodeEmailTemplate(NodeEmailTemplate nodeEmailTemplate) {
        return nodeEmailTemplateMapper.updateNodeEmailTemplate(nodeEmailTemplate);
    }

    @Override
    public int deleteNodeEmailTemplateByIds(Long[] ids) {
        return nodeEmailTemplateMapper.deleteNodeEmailTemplateByIds(ids);
    }

    public String renderTemplate(String templateKey, Map<String, String> variables) {
        NodeEmailTemplate template = nodeEmailTemplateMapper.selectNodeEmailTemplateByKey(templateKey);
        if (template == null) return null;
        String content = template.getContent();
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                content = content.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return content;
    }

    public String renderSubject(String templateKey, Map<String, String> variables) {
        NodeEmailTemplate template = nodeEmailTemplateMapper.selectNodeEmailTemplateByKey(templateKey);
        if (template == null) return null;
        String subject = template.getSubject();
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                subject = subject.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return subject;
    }
}
