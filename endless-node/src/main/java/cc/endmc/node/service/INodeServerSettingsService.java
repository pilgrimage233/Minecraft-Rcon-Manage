package cc.endmc.node.service;

import cc.endmc.node.domain.NodeServerSettings;

import java.util.List;

/**
 * 实例服务器运维策略Service接口
 *
 * @author Memory
 * @date 2026-05-29
 */
public interface INodeServerSettingsService {

    NodeServerSettings selectNodeServerSettingsById(Long id);

    NodeServerSettings selectNodeServerSettingsByServerId(Long nodeServerId);

    List<NodeServerSettings> selectNodeServerSettingsList(NodeServerSettings nodeServerSettings);

    int insertNodeServerSettings(NodeServerSettings nodeServerSettings);

    int updateNodeServerSettings(NodeServerSettings nodeServerSettings);

    int deleteNodeServerSettingsByIds(Long[] ids);
}
