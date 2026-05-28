package cc.endmc.node.mapper;

import cc.endmc.node.domain.NodeServerSettings;

import java.util.List;

/**
 * 实例运维策略Mapper接口
 *
 * @author Memory
 * @date 2026-05-29
 */
@org.apache.ibatis.annotations.Mapper
public interface NodeServerSettingsMapper {

    NodeServerSettings selectNodeServerSettingsById(Long id);

    NodeServerSettings selectNodeServerSettingsByServerId(Long nodeServerId);

    List<NodeServerSettings> selectNodeServerSettingsList(NodeServerSettings nodeServerSettings);

    int insertNodeServerSettings(NodeServerSettings nodeServerSettings);

    int updateNodeServerSettings(NodeServerSettings nodeServerSettings);

    int deleteNodeServerSettingsById(Long id);

    int deleteNodeServerSettingsByIds(Long[] ids);
}
