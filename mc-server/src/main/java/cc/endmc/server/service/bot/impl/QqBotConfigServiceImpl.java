package cc.endmc.server.service.bot.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.domain.bot.QqBotConfig;
import cc.endmc.server.mapper.bot.QqBotConfigMapper;
import cc.endmc.server.mapper.bot.QqBotManagerMapper;
import cc.endmc.server.service.bot.IQqBotConfigService;
import cc.endmc.server.ws.BotManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * QQ机器人配置Service业务层处理
 *
 * @author ruoyi
 * @date 2025-03-12
 */
@Slf4j
@Service
public class QqBotConfigServiceImpl implements IQqBotConfigService {
    @Autowired
    private QqBotConfigMapper qqBotConfigMapper;

    @Autowired
    private QqBotManagerMapper qqBotManagerMapper;

    @Autowired
    private BotManager botManager;

    /**
     * 查询QQ机器人配置
     *
     * @param id QQ机器人配置主键
     * @return QQ机器人配置
     */
    @Override
    public QqBotConfig selectQqBotConfigById(Long id) {
        return qqBotConfigMapper.selectQqBotConfigById(id);
    }

    /**
     * 查询QQ机器人配置列表
     *
     * @param qqBotConfig QQ机器人配置
     * @return QQ机器人配置
     */
    @Override
    public List<QqBotConfig> selectQqBotConfigList(QqBotConfig qqBotConfig) {
        return qqBotConfigMapper.selectQqBotConfigList(qqBotConfig);
    }

    /**
     * 新增QQ机器人配置
     *
     * @param qqBotConfig QQ机器人配置
     * @return 结果
     */
    @Override
    public int insertQqBotConfig(QqBotConfig qqBotConfig) {
        qqBotConfig.setCreateTime(DateUtils.getNowDate());
        final int i = qqBotConfigMapper.insertQqBotConfig(qqBotConfig);
        if (i > 0) {
            log.info("insert QqBotConfig : {}", qqBotConfig);
            botManager.loadBotConfigs();
        }
        return i;
    }

    /**
     * 修改QQ机器人配置
     *
     * @param qqBotConfig QQ机器人配置
     * @return 结果
     */
    @Override
    public int updateQqBotConfig(QqBotConfig qqBotConfig) {
        qqBotConfig.setUpdateTime(DateUtils.getNowDate());
        final int i = qqBotConfigMapper.updateQqBotConfig(qqBotConfig);
        if (i > 0) {
            log.info("update QqBotConfig : {}", qqBotConfig);
            botManager.loadBotConfigs();
        }
        return i;
    }

    /**
     * 批量删除QQ机器人配置
     *
     * @param ids 需要删除的QQ机器人配置主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteQqBotConfigByIds(Long[] ids) {
        // 先删除关联的管理员记录
        for (Long id : ids) {
            // 删除管理员群组关联
            qqBotManagerMapper.deleteQqBotManagerGroupByBotId(id);
            // 删除管理员记录
            qqBotManagerMapper.deleteQqBotManagerByBotId(id);
        }
        // 再删除配置
        return qqBotConfigMapper.deleteQqBotConfigByIds(ids);
    }

    /**
     * 删除QQ机器人配置信息
     *
     * @param id QQ机器人配置主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteQqBotConfigById(Long id) {
        // 先删除关联的管理员记录
        qqBotManagerMapper.deleteQqBotManagerGroupByBotId(id);
        qqBotManagerMapper.deleteQqBotManagerByBotId(id);
        // 再删除配置
        final int i = qqBotConfigMapper.deleteQqBotConfigById(id);
        if (i > 0) {
            log.info("delete QqBotConfig : {}", id);
            // 更新配置
            botManager.loadBotConfigs();
        }
        return i;
    }
}
