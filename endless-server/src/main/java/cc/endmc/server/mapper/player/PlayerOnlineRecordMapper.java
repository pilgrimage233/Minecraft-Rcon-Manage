package cc.endmc.server.mapper.player;

import cc.endmc.server.domain.player.PlayerOnlineRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 玩家上下线记录Mapper接口
 *
 * @author Memory
 * @date 2026-02-09
 */
@Mapper
public interface PlayerOnlineRecordMapper {
    /**
     * 查询玩家最新未结束的在线记录
     *
     * @param userName 玩家名(小写)
     * @return 在线记录
     */
    PlayerOnlineRecord selectLatestOpenRecord(@Param("userName") String userName);

    /**
     * 新增玩家上下线记录
     *
     * @param record 记录
     * @return 结果
     */
    int insertPlayerOnlineRecord(PlayerOnlineRecord record);

    /**
     * 更新玩家上下线记录
     *
     * @param record 记录
     * @return 结果
     */
    int updatePlayerOnlineRecord(PlayerOnlineRecord record);

    /**
     * 查询玩家最近上下线记录
     *
     * @param whitelistId 白名单ID
     * @param userName    玩家名(小写)
     * @param limit       限制条数
     * @return 记录列表
     */
    List<PlayerOnlineRecord> selectRecentRecords(@Param("whitelistId") Long whitelistId,
                                                 @Param("userName") String userName,
                                                 @Param("limit") Integer limit);
}
