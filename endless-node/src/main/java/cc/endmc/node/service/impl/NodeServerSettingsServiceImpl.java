package cc.endmc.node.service.impl;

import cc.endmc.node.domain.NodeServerSettings;
import cc.endmc.node.mapper.NodeServerSettingsMapper;
import cc.endmc.node.service.INodeServerSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NodeServerSettingsServiceImpl implements INodeServerSettingsService {

    private final NodeServerSettingsMapper nodeServerSettingsMapper;

    @Override
    public NodeServerSettings selectNodeServerSettingsById(Long id) {
        return nodeServerSettingsMapper.selectNodeServerSettingsById(id);
    }

    @Override
    public NodeServerSettings selectNodeServerSettingsByServerId(Long nodeServerId) {
        return nodeServerSettingsMapper.selectNodeServerSettingsByServerId(nodeServerId);
    }

    @Override
    public List<NodeServerSettings> selectNodeServerSettingsList(NodeServerSettings nodeServerSettings) {
        return nodeServerSettingsMapper.selectNodeServerSettingsList(nodeServerSettings);
    }

    @Override
    public int insertNodeServerSettings(NodeServerSettings nodeServerSettings) {
        return nodeServerSettingsMapper.insertNodeServerSettings(nodeServerSettings);
    }

    @Override
    public int updateNodeServerSettings(NodeServerSettings nodeServerSettings) {
        return nodeServerSettingsMapper.updateNodeServerSettings(nodeServerSettings);
    }

    @Override
    public int deleteNodeServerSettingsByIds(Long[] ids) {
        return nodeServerSettingsMapper.deleteNodeServerSettingsByIds(ids);
    }
}
