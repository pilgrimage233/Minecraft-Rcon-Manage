package com.ruoyi.server.mapper.player;

import com.ruoyi.server.domain.player.PlayerDetails;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 玩家详情Mapper接口
 *
 * @author Memory
 * @date 2024-12-31
 */
@Mapper
public interface PlayerDetailsMapper {
    /**
     * 查询玩家详情
     *
     * @param id 玩家详情主键
     * @return 玩家详情
     */
    public PlayerDetails selectPlayerDetailsById(Long id);

    /**
     * 查询玩家详情列表
     *
     * @param playerDetails 玩家详情
     * @return 玩家详情集合
     */
    public List<PlayerDetails> selectPlayerDetailsList(PlayerDetails playerDetails);

    /**
     * 新增玩家详情
     *
     * @param playerDetails 玩家详情
     * @return 结果
     */
    public int insertPlayerDetails(PlayerDetails playerDetails);

    /**
     * 修改玩家详情
     *
     * @param playerDetails 玩家详情
     * @return 结果
     */
    public int updatePlayerDetails(PlayerDetails playerDetails);

    /**
     * 删除玩家详情
     *
     * @param id 玩家详情主键
     * @return 结果
     */
    public int deletePlayerDetailsById(Long id);

    /**
     * 批量删除玩家详情
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePlayerDetailsByIds(Long[] ids);

    /**
     * 根据用户名称更新最后在线时间
     *
     * @param params 包含userNames和currentTime的参数Map
     * @return 结果
     */
    public int updateLastOnlineTimeByUserNames(Map<String, Object> params);

    /**
     * 根据用户名称更新最后离线时间
     *
     * @param params 包含userNames和currentTime的参数Map
     * @return 结果
     */
    public int updateLastOfflineTimeByUserNames(Map<String, Object> params);

    /**
     * 查询游戏时间最长的前十名玩家
     *
     * @return 玩家详情集合
     */
    public List<PlayerDetails> selectTopTenByGameTime();
}
