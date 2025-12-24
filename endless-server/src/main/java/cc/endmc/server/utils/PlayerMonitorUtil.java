package cc.endmc.server.utils;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.service.player.IPlayerDetailsService;
import cc.endmc.server.service.player.PlayerAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 玩家监控工具类
 * 处理玩家上下线监控、通知等功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerMonitorUtil {

    private final RedisCache redisCache;
    private final IPlayerDetailsService playerDetailsService;
    private final PlayerAsyncService playerAsyncService;

    /**
     * 检测玩家上下线变化
     *
     * @param currentOnlinePlayers 当前在线玩家
     * @return 玩家变化信息
     */
    public PlayerChangeInfo detectPlayerChanges(Set<String> currentOnlinePlayers) {
        // 获取缓存中的在线玩家
        Set<String> cachedOnlinePlayers = getCachedOnlinePlayers();

        // 找出新上线的玩家
        Set<String> newOnlinePlayers = new HashSet<>(currentOnlinePlayers);
        newOnlinePlayers.removeAll(cachedOnlinePlayers);

        // 找出新下线的玩家
        Set<String> newOfflinePlayers = new HashSet<>(cachedOnlinePlayers);
        newOfflinePlayers.removeAll(currentOnlinePlayers);

        // 更新缓存
        updateCachedOnlinePlayers(currentOnlinePlayers);

        return new PlayerChangeInfo(newOnlinePlayers, newOfflinePlayers, currentOnlinePlayers);
    }

    /**
     * 获取缓存中的在线玩家
     */
    private Set<String> getCachedOnlinePlayers() {
        Set<String> cachedPlayers = redisCache.getCacheObject(CacheKey.ONLINE_PLAYER_KEY);
        return cachedPlayers != null ? cachedPlayers : new HashSet<>();
    }

    /**
     * 更新缓存中的在线玩家
     */
    private void updateCachedOnlinePlayers(Set<String> onlinePlayers) {
        redisCache.setCacheObject(CacheKey.ONLINE_PLAYER_KEY, onlinePlayers);
    }

    /**
     * 处理玩家上线
     *
     * @param newOnlinePlayers 新上线的玩家
     */
    public void handlePlayersOnline(Set<String> newOnlinePlayers) {
        if (newOnlinePlayers.isEmpty()) {
            return;
        }

        log.info("检测到玩家上线: {}", newOnlinePlayers);

        List<String> playerNames = new ArrayList<>(newOnlinePlayers);
        playerAsyncService.handlePlayersOnlineAsync(playerNames);
    }

    /**
     * 处理玩家下线
     *
     * @param newOfflinePlayers 新下线的玩家
     */
    public void handlePlayersOffline(Set<String> newOfflinePlayers) {
        if (newOfflinePlayers.isEmpty()) {
            return;
        }

        log.info("检测到玩家下线: {}", newOfflinePlayers);

        playerAsyncService.handlePlayersOfflineAsync(newOfflinePlayers);
    }


    /**
     * 格式化玩家列表为通知消息
     *
     * @param players    玩家集合
     * @param actionType 动作类型 ("上线" 或 "下线")
     * @return 格式化的消息
     */
    public String formatPlayerNotification(Set<String> players, String actionType) {
        if (players.isEmpty()) {
            return "";
        }

        StringBuilder message = new StringBuilder();
        message.append("玩家").append(actionType).append("通知：\n");

        if (players.size() == 1) {
            message.append(players.iterator().next());
        } else {
            message.append(String.join(", ", players));
        }

        if ("上线".equals(actionType)) {
            message.append(" 加入了游戏！");
        } else if ("下线".equals(actionType)) {
            message.append(" 离开了游戏！");
        }

        return message.toString();
    }

    /**
     * 获取在线玩家统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getOnlinePlayerStats() {
        Set<String> onlinePlayers = getCachedOnlinePlayers();
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalOnline", onlinePlayers.size());
        stats.put("playerList", new ArrayList<>(onlinePlayers));
        stats.put("lastUpdateTime", new Date());

        return stats;
    }

    /**
     * 清理无效的玩家名
     *
     * @param players 原始玩家集合
     * @return 清理后的玩家集合
     */
    public Set<String> cleanPlayerNames(Set<String> players) {
        return players.stream()
                .filter(StringUtils::isNotEmpty)
                .map(String::trim)
                .filter(OnlinePlayerUtil::isValidPlayerName)
                .collect(Collectors.toSet());
    }

    /**
     * 玩家变化信息类
     */
    public static class PlayerChangeInfo {
        private final Set<String> newOnlinePlayers;
        private final Set<String> newOfflinePlayers;
        private final Set<String> currentOnlinePlayers;

        public PlayerChangeInfo(Set<String> newOnlinePlayers, Set<String> newOfflinePlayers, Set<String> currentOnlinePlayers) {
            this.newOnlinePlayers = newOnlinePlayers;
            this.newOfflinePlayers = newOfflinePlayers;
            this.currentOnlinePlayers = currentOnlinePlayers;
        }

        public Set<String> getNewOnlinePlayers() {
            return newOnlinePlayers;
        }

        public Set<String> getNewOfflinePlayers() {
            return newOfflinePlayers;
        }

        public Set<String> getCurrentOnlinePlayers() {
            return currentOnlinePlayers;
        }

        public boolean hasChanges() {
            return !newOnlinePlayers.isEmpty() || !newOfflinePlayers.isEmpty();
        }

        public int getTotalOnlineCount() {
            return currentOnlinePlayers.size();
        }
    }
}