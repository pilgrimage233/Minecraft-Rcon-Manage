package com.ruoyi.server.mapper.bot;

import com.ruoyi.server.domain.bot.QqBotManager;
import com.ruoyi.server.domain.bot.QqBotManagerGroup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;


/**
 * QQ机器人管理员Mapper接口
 *
 * @author Memory
 * @date 2025-03-13
 */
@Mapper
public interface QqBotManagerMapper {
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
     * 删除QQ机器人管理员
     *
     * @param id QQ机器人管理员主键
     * @return 结果
     */
    public int deleteQqBotManagerById(Long id);

    /**
     * 批量删除QQ机器人管理员
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteQqBotManagerByIds(Long[] ids);

    /**
     * 批量删除QQ机器人管理员群组关联
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteQqBotManagerGroupByManagerIds(Long[] ids);

    /**
     * 批量新增QQ机器人管理员群组关联
     *
     * @param qqBotManagerGroupList QQ机器人管理员群组关联列表
     * @return 结果
     */
    public int batchQqBotManagerGroup(List<QqBotManagerGroup> qqBotManagerGroupList);


    /**
     * 通过QQ机器人管理员主键删除QQ机器人管理员群组关联信息
     *
     * @param id QQ机器人管理员ID
     * @return 结果
     */
    public int deleteQqBotManagerGroupByManagerId(Long id);

    /**
     * 根据机器人ID删除管理员群组关联
     *
     * @param botId 机器人ID
     * @return 结果
     */
    public int deleteQqBotManagerGroupByBotId(Long botId);

    /**
     * 根据机器人ID删除管理员
     *
     * @param botId 机器人ID
     * @return 结果
     */
    public int deleteQqBotManagerByBotId(Long botId);
}
