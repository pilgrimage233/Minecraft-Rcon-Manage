package com.ruoyi.server.service.bot;

import com.ruoyi.server.domain.bot.QqBotManager;

import java.util.List;
import java.util.Map;

/**
 * QQ机器人管理员Service接口
 *
 * @author Memory
 * @date 2025-03-13
 */
public interface IQqBotManagerService {
    /**
     * 查询QQ机器人管理员
     *
     * @param id QQ机器人管理员主键
     * @return QQ机器人管理员
     */
    public QqBotManager selectQqBotManagerById(Long id);

    /**
     * 查询QQ机器人管理员列表
     *
     * @param qqBotManager QQ机器人管理员
     * @return QQ机器人管理员集合
     */
    public List<QqBotManager> selectQqBotManagerList(QqBotManager qqBotManager);

    /**
     * 通过机器人ID和管理员QQ和群组ID 查询QQ机器人管理员
     *
     * @param query QQ机器人管理员
     * @return 结果
     */
    public List<QqBotManager> selectQqBotManagerByBotIdAndManagerQqAndGroupId(Map<String, Object> query);

    /**
     * 新增QQ机器人管理员
     *
     * @param qqBotManager QQ机器人管理员
     * @return 结果
     */
    public int insertQqBotManager(QqBotManager qqBotManager);

    /**
     * 修改QQ机器人管理员
     *
     * @param qqBotManager QQ机器人管理员
     * @return 结果
     */
    public int updateQqBotManager(QqBotManager qqBotManager);

    /**
     * 修改QQ机器人管理员最后活跃时间
     *
     * @param qqBotManager QQ机器人管理员
     * @return 结果
     */
    public int updateQqBotManagerLastActiveTime(QqBotManager qqBotManager);

    /**
     * 批量删除QQ机器人管理员
     *
     * @param ids 需要删除的QQ机器人管理员主键集合
     * @return 结果
     */
    public int deleteQqBotManagerByIds(Long[] ids);

    /**
     * 删除QQ机器人管理员信息
     *
     * @param id QQ机器人管理员主键
     * @return 结果
     */
    public int deleteQqBotManagerById(Long id);
}
