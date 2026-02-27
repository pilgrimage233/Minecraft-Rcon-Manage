package cc.endmc.server.service.player;

import cc.endmc.server.domain.player.PlayerDetails;
import cc.endmc.server.domain.player.PlayerOnlineRecord;
import cc.endmc.server.mapper.player.PlayerOnlineRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class PlayerAsyncService {

    private static final long MAX_SESSION_MINUTES = 24 * 60;
    private final IPlayerDetailsService playerDetailsService;
    private final PlayerOnlineRecordMapper playerOnlineRecordMapper;

    /**
     * 异步处理玩家上线
     *
     * @param playerNames 玩家名列表
     */
    @Async("virtualThreadExecutor")
    public void handlePlayersOnlineAsync(List<String> playerNames) {
        try {
            // 批量更新玩家最后上线时间
            playerDetailsService.updateLastOnlineTimeByUserNames(playerNames);

            Date now = new Date();
            for (String playerName : playerNames) {
                try {
                    createOnlineRecord(playerName, now);
                } catch (Exception e) {
                    log.error("记录玩家 {} 上线记录失败: {}", playerName, e.getMessage());
                }
            }

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
    @Async("virtualThreadExecutor")
    public void handlePlayersOfflineAsync(Set<String> newOfflinePlayers) {
        if (newOfflinePlayers.isEmpty()) {
            return;
        }

        // 对每个下线的玩家计算游戏时间
        for (String playerName : newOfflinePlayers) {
            try {
                finishOnlineRecord(playerName, new Date());
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

        PlayerDetails playerDetails = playerList.getFirst();
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

    private void createOnlineRecord(String playerName, Date loginTime) {
        if (playerName == null) {
            return;
        }
        String normalizedName = playerName.toLowerCase().trim();

        PlayerOnlineRecord existing = playerOnlineRecordMapper.selectLatestOpenRecord(normalizedName);
        if (existing != null) {
            return;
        }

        PlayerOnlineRecord record = new PlayerOnlineRecord();
        record.setUserName(normalizedName);
        record.setWhitelistId(resolveWhitelistId(normalizedName));
        record.setLoginTime(loginTime);
        record.setCreateTime(loginTime);
        playerOnlineRecordMapper.insertPlayerOnlineRecord(record);
    }

    private void finishOnlineRecord(String playerName, Date logoutTime) {
        if (playerName == null) {
            return;
        }
        String normalizedName = playerName.toLowerCase().trim();

        PlayerOnlineRecord record = playerOnlineRecordMapper.selectLatestOpenRecord(normalizedName);
        Date loginTime = record != null ? record.getLoginTime() : null;

        if (loginTime == null) {
            loginTime = resolveLastOnlineTime(normalizedName);
        }
        if (loginTime == null) {
            log.warn("玩家 {} 没有可用的上线时间记录", normalizedName);
            return;
        }

        long minutes = Math.max(0, (logoutTime.getTime() - loginTime.getTime()) / (1000 * 60));
        long cappedMinutes = Math.min(minutes, MAX_SESSION_MINUTES);

        if (record != null && record.getId() != null) {
            PlayerOnlineRecord update = new PlayerOnlineRecord();
            update.setId(record.getId());
            update.setLogoutTime(logoutTime);
            update.setPlayMinutes(cappedMinutes);
            update.setUpdateTime(logoutTime);
            playerOnlineRecordMapper.updatePlayerOnlineRecord(update);
            return;
        }

        PlayerOnlineRecord fallback = new PlayerOnlineRecord();
        fallback.setUserName(normalizedName);
        fallback.setWhitelistId(resolveWhitelistId(normalizedName));
        fallback.setLoginTime(loginTime);
        fallback.setLogoutTime(logoutTime);
        fallback.setPlayMinutes(cappedMinutes);
        fallback.setCreateTime(loginTime);
        fallback.setUpdateTime(logoutTime);
        playerOnlineRecordMapper.insertPlayerOnlineRecord(fallback);
    }

    private Long resolveWhitelistId(String playerName) {
        PlayerDetails details = new PlayerDetails();
        details.setUserName(playerName);
        List<PlayerDetails> list = playerDetailsService.selectPlayerDetailsList(details);
        if (list.isEmpty()) {
            return null;
        }
        return list.getFirst().getWhitelistId();
    }

    private Date resolveLastOnlineTime(String playerName) {
        PlayerDetails details = new PlayerDetails();
        details.setUserName(playerName);
        List<PlayerDetails> list = playerDetailsService.selectPlayerDetailsList(details);
        if (list.isEmpty()) {
            return null;
        }
        return list.getFirst().getLastOnlineTime();
    }
}