package cc.endmc.server.service.player;

import cc.endmc.server.domain.player.PlayerDetails;

import java.util.List;

/**
 * 玩家详情Service接口
 *
 * @author Memory
 * @date 2024-12-31
 */
public interface IPlayerDetailsService {
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
     * @param checkOperator 是否检查管理员权限
     * @return 结果
     */
    public int updatePlayerDetails(PlayerDetails playerDetails, boolean checkOperator);

    /**
     * 批量删除玩家详情
     *
     * @param ids 需要删除的玩家详情主键集合
     * @return 结果
     */
    public int deletePlayerDetailsByIds(Long[] ids);

    /**
     * 根据条件删除玩家详情信息
     *
     * @param info
     * @return
     */
    public int deletePlayerDetailsByInfo(PlayerDetails info);

    /**
     * 删除玩家详情信息
     *
     * @param id 玩家详情主键
     * @return 结果
     */
    public int deletePlayerDetailsById(Long id);

    /**
     * 批量更新玩家最后上线时间
     *
     * @param userNames 玩家用户名集合
     * @return 结果
     */
    public int updateLastOnlineTimeByUserNames(List<String> userNames);

    /**
     * 批量更新玩家最后下线时间
     *
     * @param userNames 玩家用户名集合
     * @return 结果
     */
    public int updateLastOfflineTimeByUserNames(List<String> userNames);


}
