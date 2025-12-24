package cc.endmc.server.service.bot.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.domain.bot.QqBotManager;
import cc.endmc.server.domain.bot.QqBotManagerGroup;
import cc.endmc.server.mapper.bot.QqBotManagerMapper;
import cc.endmc.server.service.bot.IQqBotManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * QQ机器人管理员Service业务层处理
 *
 * @author Memory
 * @date 2025-03-13
 */
@Service
public class QqBotManagerServiceImpl implements IQqBotManagerService {
    @Autowired
    private QqBotManagerMapper qqBotManagerMapper;


    /**
     * 查询QQ机器人管理员
     *
     * @param id QQ机器人管理员主键
     * @return QQ机器人管理员
     */
    @Override
    public QqBotManager selectQqBotManagerById(Long id) {
        return qqBotManagerMapper.selectQqBotManagerById(id);
    }

    /**
     * 查询QQ机器人管理员列表
     *
     * @param qqBotManager QQ机器人管理员
     * @return QQ机器人管理员
     */
    @Override
    public List<QqBotManager> selectQqBotManagerList(QqBotManager qqBotManager) {
        return qqBotManagerMapper.selectQqBotManagerList(qqBotManager);
    }

    /**
     * 通过机器人ID和管理员QQ和群组ID 查询QQ机器人管理员
     *
     * @param query QQ机器人管理员
     * @return 结果
     */
    @Override
    public List<QqBotManager> selectQqBotManagerByBotIdAndManagerQqAndGroupId(Map<String, Object> query) {
        return qqBotManagerMapper.selectQqBotManagerByBotIdAndManagerQqAndGroupId(query);
    }

    /**
     * 新增QQ机器人管理员
     *
     * @param qqBotManager QQ机器人管理员
     * @return 结果
     */
    @Transactional
    @Override
    public int insertQqBotManager(QqBotManager qqBotManager) {
        qqBotManager.setCreateTime(DateUtils.getNowDate());
        int rows = qqBotManagerMapper.insertQqBotManager(qqBotManager);
        insertQqBotManagerGroup(qqBotManager);
        return rows;
    }

    /**
     * 修改QQ机器人管理员
     *
     * @param qqBotManager QQ机器人管理员
     * @return 结果
     */
    @Transactional
    @Override
    public int updateQqBotManager(QqBotManager qqBotManager) {
        qqBotManager.setUpdateTime(DateUtils.getNowDate());
        qqBotManagerMapper.deleteQqBotManagerGroupByManagerId(qqBotManager.getId());
        insertQqBotManagerGroup(qqBotManager);
        return qqBotManagerMapper.updateQqBotManager(qqBotManager);
    }

    /**
     * 修改QQ机器人管理员最后活跃时间
     *
     * @param qqBotManager QQ机器人管理员
     * @return 结果
     */
    @Override
    public int updateQqBotManagerLastActiveTime(QqBotManager qqBotManager) {
        return qqBotManagerMapper.updateQqBotManagerLastActiveTime(qqBotManager);
    }

    /**
     * 批量删除QQ机器人管理员
     *
     * @param ids 需要删除的QQ机器人管理员主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteQqBotManagerByIds(Long[] ids) {
        qqBotManagerMapper.deleteQqBotManagerGroupByManagerIds(ids);
        return qqBotManagerMapper.deleteQqBotManagerByIds(ids);
    }

    /**
     * 删除QQ机器人管理员信息
     *
     * @param id QQ机器人管理员主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteQqBotManagerById(Long id) {
        qqBotManagerMapper.deleteQqBotManagerGroupByManagerId(id);
        return qqBotManagerMapper.deleteQqBotManagerById(id);
    }

    /**
     * 新增QQ机器人管理员群组关联信息
     *
     * @param qqBotManager QQ机器人管理员对象
     */
    public void insertQqBotManagerGroup(QqBotManager qqBotManager) {
        List<QqBotManagerGroup> qqBotManagerGroupList = qqBotManager.getQqBotManagerGroupList();
        Long id = qqBotManager.getId();
        if (StringUtils.isNotNull(qqBotManagerGroupList)) {
            List<QqBotManagerGroup> list = new ArrayList<QqBotManagerGroup>();
            for (QqBotManagerGroup qqBotManagerGroup : qqBotManagerGroupList) {
                qqBotManagerGroup.setManagerId(id);
                list.add(qqBotManagerGroup);
            }
            if (!list.isEmpty()) {
                qqBotManagerMapper.batchQqBotManagerGroup(list);
            }
        }
    }
}
