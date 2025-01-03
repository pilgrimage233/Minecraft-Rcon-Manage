package com.ruoyi.server.service.impl;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.HashMap;

import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.server.mapper.PlayerDetailsMapper;
import com.ruoyi.server.domain.PlayerDetails;
import com.ruoyi.server.service.IPlayerDetailsService;

/**
 * 玩家详情Service业务层处理
 *
 * @author Memory
 * @date 2024-12-31
 */
@Service
public class PlayerDetailsServiceImpl implements IPlayerDetailsService {
    @Autowired
    private PlayerDetailsMapper playerDetailsMapper;

    /**
     * 查询玩家详情
     *
     * @param id 玩家详情主键
     * @return 玩家详情
     */
    @Override
    public PlayerDetails selectPlayerDetailsById(Long id) {
        return playerDetailsMapper.selectPlayerDetailsById(id);
    }

    /**
     * 查询玩家详情列表
     *
     * @param playerDetails 玩家详情
     * @return 玩家详情
     */
    @Override
    public List<PlayerDetails> selectPlayerDetailsList(PlayerDetails playerDetails) {
        return playerDetailsMapper.selectPlayerDetailsList(playerDetails);
    }

    /**
     * 新增玩家详情
     *
     * @param playerDetails 玩家详情
     * @return 结果
     */
    @Override
    public int insertPlayerDetails(PlayerDetails playerDetails) {
        playerDetails.setCreateTime(DateUtils.getNowDate());
        return playerDetailsMapper.insertPlayerDetails(playerDetails);
    }

    /**
     * 修改玩家详情
     *
     * @param playerDetails 玩家详情
     * @return 结果
     */
    @Override
    public int updatePlayerDetails(PlayerDetails playerDetails) {
        playerDetails.setUpdateTime(DateUtils.getNowDate());
        return playerDetailsMapper.updatePlayerDetails(playerDetails);
    }

    /**
     * 批量删除玩家详情
     *
     * @param ids 需要删除的玩家详情主键
     * @return 结果
     */
    @Override
    public int deletePlayerDetailsByIds(Long[] ids) {
        return playerDetailsMapper.deletePlayerDetailsByIds(ids);
    }

    /**
     * 删除玩家详情信息
     *
     * @param id 玩家详情主键
     * @return 结果
     */
    @Override
    public int deletePlayerDetailsById(Long id) {
        return playerDetailsMapper.deletePlayerDetailsById(id);
    }

    /**
     * 批量更新玩家最后上线时间
     *
     * @param userNames 玩家用户名集合
     * @return 结果
     */
    @Override
    public int updateLastOnlineTimeByUserNames(List<String> userNames) {
        if (userNames == null || userNames.isEmpty()) {
            return 0;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("userNames", userNames);
        // 使用系统默认时区创建时间
        TimeZone china = TimeZone.getTimeZone("Asia/Shanghai");
        Calendar calendar = Calendar.getInstance(china);
        params.put("currentTime", calendar.getTime());
        return playerDetailsMapper.updateLastOnlineTimeByUserNames(params);
    }

    /**
     * 批量更新玩家最后下线时间
     *
     * @param userNames 玩家用户名集合
     * @return 结果
     */
    @Override
    public int updateLastOfflineTimeByUserNames(List<String> userNames) {
        if (userNames == null || userNames.isEmpty()) {
            return 0;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("userNames", userNames);
        // 使用系统默认时区创建时间
        TimeZone china = TimeZone.getTimeZone("Asia/Shanghai");
        Calendar calendar = Calendar.getInstance(china);
        params.put("currentTime", calendar.getTime());
        return playerDetailsMapper.updateLastOfflineTimeByUserNames(params);
    }
}
