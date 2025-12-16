package cc.endmc.server.utils;

import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.rconclient.RconClient;
import cc.endmc.server.domain.server.ServerInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线玩家工具类
 * 统一处理各种服务器核心的在线玩家获取逻辑
 */
@Slf4j
public class OnlinePlayerUtil {

    /**
     * 从服务器获取在线玩家列表
     *
     * @param serverId   服务器ID
     * @param serverCore 服务器核心类型
     * @return 在线玩家列表
     */
    public static List<String> getOnlinePlayersFromServer(String serverId, String serverCore) {
        if (!RconCache.containsKey(serverId)) {
            log.warn("服务器 {} 的RCON连接不存在", serverId);
            return new ArrayList<>();
        }

        RconClient rconClient = RconCache.get(serverId);
        if (rconClient == null || !rconClient.isSocketChannelOpen()) {
            log.warn("服务器 {} 的RCON连接不可用", serverId);
            return new ArrayList<>();
        }

        try {
            // 根据服务器核心类型选择不同的处理方式
            switch (serverCore.toLowerCase()) {
                case "velocity":
                    return getVelocityOnlinePlayers(rconClient);
                case "bungeecord":
                case "waterfall":
                    return getBungeeOnlinePlayers(rconClient);
                case "paper":
                case "spigot":
                case "bukkit":
                case "purpur":
                case "fabric":
                case "forge":
                case "vanilla":
                default:
                    return getStandardOnlinePlayers(rconClient);
            }
        } catch (Exception e) {
            log.error("获取服务器 {} 在线玩家失败: {}", serverId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 从服务器获取在线玩家列表（使用ServerInfo对象）
     *
     * @param serverInfo 服务器信息
     * @return 在线玩家列表
     */
    public static List<String> getOnlinePlayersFromServer(ServerInfo serverInfo) {
        return getOnlinePlayersFromServer(serverInfo.getId().toString(), serverInfo.getServerCore());
    }

    /**
     * 获取所有服务器的在线玩家
     *
     * @return 所有在线玩家的集合
     */
    public static Set<String> getAllOnlinePlayers() {
        Set<String> allOnlinePlayers = new HashSet<>();
        Map<String, RconClient> rconMap = RconCache.getMap();

        if (rconMap == null || rconMap.isEmpty()) {
            return allOnlinePlayers;
        }

        for (Map.Entry<String, RconClient> entry : rconMap.entrySet()) {
            String serverId = entry.getKey();
            RconClient rconClient = entry.getValue();

            try {
                if (rconClient != null && rconClient.isSocketChannelOpen()) {
                    List<String> players = getStandardOnlinePlayers(rconClient);
                    // 转换为小写并添加到集合中
                    players.stream()
                            .map(String::toLowerCase)
                            .map(String::trim)
                            .forEach(allOnlinePlayers::add);
                }
            } catch (Exception e) {
                log.error("获取服务器 {} 在线玩家失败: {}", serverId, e.getMessage());
            }
        }

        return allOnlinePlayers;
    }

    /**
     * 获取Velocity服务器的在线玩家
     */
    private static List<String> getVelocityOnlinePlayers(RconClient rconClient) throws Exception {
        String response = rconClient.sendCommand("glist all");
        if (StringUtils.isEmpty(response)) {
            return new ArrayList<>();
        }

        List<String> playerList = new ArrayList<>();
        String[] lines = response.split("\n");

        for (String line : lines) {
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                if (parts.length > 1) {
                    String playersStr = parts[1].trim();
                    if (StringUtils.isNotEmpty(playersStr) && !playersStr.equals("(none)")) {
                        String[] players = playersStr.split(",\\s*");
                        playerList.addAll(Arrays.stream(players)
                                .filter(StringUtils::isNotEmpty)
                                .map(String::trim)
                                .collect(Collectors.toList()));
                    }
                }
            }
        }
        return playerList;
    }

    /**
     * 获取BungeeCord/Waterfall服务器的在线玩家
     */
    private static List<String> getBungeeOnlinePlayers(RconClient rconClient) throws Exception {
        String response = rconClient.sendCommand("glist");
        if (StringUtils.isEmpty(response)) {
            return new ArrayList<>();
        }

        List<String> playerList = new ArrayList<>();
        String[] lines = response.split("\n");

        for (String line : lines) {
            if (line.contains(":") && !line.startsWith("Total")) {
                String[] parts = line.split(":", 2);
                if (parts.length > 1) {
                    String playersStr = parts[1].trim();
                    if (StringUtils.isNotEmpty(playersStr)) {
                        String[] players = playersStr.split(",\\s*");
                        playerList.addAll(Arrays.stream(players)
                                .filter(StringUtils::isNotEmpty)
                                .map(String::trim)
                                .collect(Collectors.toList()));
                    }
                }
            }
        }
        return playerList;
    }

    /**
     * 获取标准Minecraft服务器的在线玩家
     */
    private static List<String> getStandardOnlinePlayers(RconClient rconClient) throws Exception {
        String response = rconClient.sendCommand("list");

        // 如果list命令被插件覆盖，尝试使用minecraft:list
        if (StringUtils.isNotEmpty(response) && !response.startsWith("There are")) {
            String minecraftList = rconClient.sendCommand("minecraft:list");
            if (StringUtils.isNotEmpty(minecraftList)) {
                response = minecraftList;
            }
        }

        if (StringUtils.isEmpty(response)) {
            return new ArrayList<>();
        }

        return parsePlayerListFromResponse(response);
    }

    /**
     * 从响应中解析玩家列表
     */
    private static List<String> parsePlayerListFromResponse(String response) {
        List<String> playerList = new ArrayList<>();

        // 处理多种可能的响应格式
        String[] patterns = {
                ":",  // 标准格式: "There are 3 of a max of 20 players online: player1, player2, player3"
                "在线玩家:",  // 中文格式
                "players online:",  // 变体格式
                "玩家在线:"
        };

        for (String pattern : patterns) {
            if (response.contains(pattern)) {
                String[] parts = response.split(pattern, 2);
                if (parts.length > 1) {
                    String playersStr = parts[1].trim();
                    if (StringUtils.isNotEmpty(playersStr)) {
                        // 移除可能的额外信息
                        playersStr = playersStr.split("\n")[0].trim();

                        if (!playersStr.isEmpty() && !playersStr.equals("无") && !playersStr.equals("none")) {
                            String[] players = playersStr.split(",\\s*");
                            playerList.addAll(Arrays.stream(players)
                                    .filter(StringUtils::isNotEmpty)
                                    .map(String::trim)
                                    .filter(player -> !player.matches(".*\\d+.*")) // 过滤掉包含数字的非玩家名
                                    .collect(Collectors.toList()));
                        }
                        break;
                    }
                }
            }
        }

        // 处理特殊格式，如 "Online (3/20): player1, player2, player3"
        if (playerList.isEmpty() && response.contains("Online (")) {
            String[] parts = response.split(":", 2);
            if (parts.length > 1) {
                String playersStr = parts[1].trim();
                if (StringUtils.isNotEmpty(playersStr) && !playersStr.equals("无") && !playersStr.equals("none")) {
                    String[] players = playersStr.split(",\\s*");
                    playerList.addAll(Arrays.stream(players)
                            .filter(StringUtils::isNotEmpty)
                            .map(String::trim)
                            .filter(player -> !player.matches(".*\\d+.*"))
                            .collect(Collectors.toList()));
                }
            }
        }

        return playerList;
    }

    /**
     * 带重试机制的获取在线玩家
     *
     * @param serverId   服务器ID
     * @param serverCore 服务器核心类型
     * @param maxRetries 最大重试次数
     * @return 在线玩家列表
     */
    public static List<String> getOnlinePlayersWithRetry(String serverId, String serverCore, int maxRetries) {
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                List<String> players = getOnlinePlayersFromServer(serverId, serverCore);
                if (players != null) {
                    return players;
                }
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    log.error("获取服务器 {} 在线玩家失败，已重试 {} 次: {}", serverId, maxRetries, e.getMessage());
                } else {
                    log.warn("获取服务器 {} 在线玩家失败，第 {} 次重试: {}", serverId, retryCount, e.getMessage());
                    try {
                        Thread.sleep(1000 * retryCount); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    /**
     * 验证玩家名是否有效
     *
     * @param playerName 玩家名
     * @return 是否有效
     */
    public static boolean isValidPlayerName(String playerName) {
        if (StringUtils.isEmpty(playerName)) {
            return false;
        }

        String trimmed = playerName.trim();

        // 检查长度（Minecraft玩家名长度限制）
        if (trimmed.length() < 3 || trimmed.length() > 16) {
            return false;
        }

        // 检查字符（只允许字母、数字和下划线）
        if (!trimmed.matches("^[a-zA-Z0-9_]+$")) {
            return false;
        }

        // 排除一些明显不是玩家名的字符串
        String lower = trimmed.toLowerCase();
        String[] invalidNames = {"none", "无", "null", "undefined", "admin", "console", "server"};

        for (String invalid : invalidNames) {
            if (lower.equals(invalid)) {
                return false;
            }
        }

        return true;
    }
}