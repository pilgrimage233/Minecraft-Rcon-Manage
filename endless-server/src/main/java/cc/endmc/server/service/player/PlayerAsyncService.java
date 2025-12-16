package cc.endmc.server.service.player;

import cc.endmc.server.domain.player.PlayerDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 玩家异步处理服务
 * 专门处理需要异步执行的玩家相关操作
 */
@Slf4j
@Service
public class PlayerAsyncService {

    @Autowired
    private IPlayerDetailsService playerDetailsService;

    /**
     * 异步处理玩家上线
     *
     * @param playerNames 玩家名列表
     */
    @Async
    public void handlePlayersOnlineAsync(List<String> playerNames) {
        try {
            // 批量更新玩家最后上线时间
            playerDetailsService.updateLastOnlineTimeByUserNames(playerNames);

            log.debug("已更新 {} 个玩家的上线时间", playerNames.size());
        } catch (Exception e) {
            log.error("异步处理玩家上线失败: {}", e.getMessage());
        }
    }

    /**
     * 异步处理玩家下线
     *
     * @param newOfflinePlayers 新下线的玩家集合
     */
    @Async
    public void handlePlayersOfflineAsync(Set<String> newOfflinePlayers) {
        if (newOfflinePlayers.isEmpty()) {
            return;
        }

        // 对每个下线的玩家计算游戏时间
        for (String playerName : newOfflinePlayers) {
            try {
                updatePlayerGameTime(playerName);
            } catch (Exception e) {
                log.error("更新玩家 {} 游戏时间失败: {}", playerName, e.getMessage());
            }
        }
    }

    /**
     * 更新玩家游戏时间
     *
     * @param playerName 玩家名
     */
    private void updatePlayerGameTime(String playerName) {
        PlayerDetails queryDetails = new PlayerDetails();
        queryDetails.setUserName(playerName);
        List<PlayerDetails> playerList = playerDetailsService.selectPlayerDetailsList(queryDetails);

        if (playerList.isEmpty()) {
            log.warn("未找到玩家 {} 的详细信息", playerName);
            return;
        }

        PlayerDetails playerDetails = playerList.get(0);
        Date lastOnlineTime = playerDetails.getLastOnlineTime();
        Date now = new Date();

        if (lastOnlineTime != null) {
            // 计算本次游戏时间(分钟)
            long gameTimeMinutes = (now.getTime() - lastOnlineTime.getTime()) / (1000 * 60);

            // 更新总游戏时间，处理null值情况
            Long currentGameTime = playerDetails.getGameTime();
            currentGameTime = (currentGameTime == null) ? gameTimeMinutes : currentGameTime + gameTimeMinutes;
            playerDetails.setGameTime(currentGameTime);

            // 更新最后离线时间
            playerDetails.setLastOfflineTime(now);

            // 更新到数据库
            playerDetailsService.updatePlayerDetails(playerDetails, false);

            log.debug("更新玩家 {} 游戏时间: 本次 {} 分钟, 总计 {} 分钟",
                    playerName, gameTimeMinutes, currentGameTime);
        } else {
            log.warn("玩家 {} 没有上线时间记录", playerName);
        }
    }
}