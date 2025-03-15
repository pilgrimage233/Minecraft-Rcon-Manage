package com.ruoyi.server.service.bot;


import com.ruoyi.server.domain.bot.QqBotConfig;

import java.util.List;

/**
 * QQ机器人配置Service接口
 *
 * @author ruoyi
 * @date 2025-03-12
 */
public interface IQqBotConfigService {
    /**
     * 查询QQ机器人配置
     *
     * @param id QQ机器人配置主键
     * @return QQ机器人配置
     */
    public QqBotConfig selectQqBotConfigById(Long id);

    /**
     * 查询QQ机器人配置列表
     *
     * @param qqBotConfig QQ机器人配置
     * @return QQ机器人配置集合
     */
    public List<QqBotConfig> selectQqBotConfigList(QqBotConfig qqBotConfig);

    /**
     * 新增QQ机器人配置
     *
     * @param qqBotConfig QQ机器人配置
     * @return 结果
     */
    public int insertQqBotConfig(QqBotConfig qqBotConfig);

    /**
     * 修改QQ机器人配置
     *
     * @param qqBotConfig QQ机器人配置
     * @return 结果
     */
    public int updateQqBotConfig(QqBotConfig qqBotConfig);

    /**
     * 批量删除QQ机器人配置
     *
     * @param ids 需要删除的QQ机器人配置主键集合
     * @return 结果
     */
    public int deleteQqBotConfigByIds(Long[] ids);

    /**
     * 删除QQ机器人配置信息
     *
     * @param id QQ机器人配置主键
     * @return 结果
     */
    public int deleteQqBotConfigById(Long id);
}
