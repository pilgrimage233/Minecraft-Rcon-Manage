package cc.endmc.quartz.task;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.common.utils.http.HttpUtils;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.bot.BotGroupCommandConfig;
import cc.endmc.server.domain.bot.QqBotConfig;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.player.PlayerDetails;
import cc.endmc.server.enums.Identity;
import cc.endmc.server.mapper.bot.QqBotConfigMapper;
import cc.endmc.server.mapper.permission.WhitelistInfoMapper;
import cc.endmc.server.mapper.player.PlayerDetailsMapper;
import cc.endmc.server.service.bot.IBotGroupCommandConfigService;
import cc.endmc.server.service.player.IPlayerDetailsService;
import cc.endmc.server.service.statistics.IPlayerActivityStatsService;
import cc.endmc.server.utils.BotUtil;
import cc.endmc.server.utils.OnlinePlayerUtil;
import cc.endmc.server.utils.PlayerMonitorUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * ClassName: OnlineTask <br>
 * Description:
 * date: 2024/4/7 下午7:51 <br>
 *
 * @author Administrator <br>
 * @since JDK 1.8
 */
@Slf4j
@Component("onlineTask")
@RequiredArgsConstructor
public class OnlineTask {

    private final IPlayerDetailsService playerDetailsService;
    private final IBotGroupCommandConfigService commandConfigService;
    private final IPlayerActivityStatsService activityStatsService;

    private final WhitelistInfoMapper whitelistInfoMapper;
    private final PlayerDetailsMapper playerDetailsMapper;
    private final QqBotConfigMapper qqBotConfigMapper;

    private final RedisCache cache;
    private final RconService rconService;
    private final PlayerMonitorUtil playerMonitorUtil;

    /**
     * 功能开关 - 玩家上线通知
     */
    private static final String CMD_ONLINE_NOTIFY = "玩家上线通知";
    /**
     * 功能开关 - 玩家下线通知
     */
    private static final String CMD_OFFLINE_NOTIFY = "玩家下线通知";

    /**
     * 根据用户uuid同步用户名称
     * Api：<a href="https://sessionserver.mojang.com/session/minecraft/profile/">...</a>{uuid}
     */
    public void syncUserNameForUuid() {
        log.debug("syncUserNameForUuid start");
        ArrayList<String> list = new ArrayList<>();
        // 查询所有正版用户
        WhitelistInfo whitelistInfo = new WhitelistInfo();
        whitelistInfo.setOnlineFlag(1L);
        whitelistInfoMapper.selectWhitelistInfoList(whitelistInfo).forEach(whitelist -> {
            // 查询用户名称
            try {
                String json = HttpUtils.sendGet("https://sessionserver.mojang.com/session/minecraft/profile/" + whitelist.getUserUuid().replace("-", ""));
                if (StringUtils.isNotEmpty(json)) {
                    final String oldName = whitelist.getUserName();
                    // json实例化
                    JSONObject jsonObject = JSONObject.parseObject(json);
                    String newName = jsonObject.getString("name");
                    if (newName.equals(whitelist.getUserName())) {
                        return;
                    }
                    // 更新用户名称
                    whitelist.setUserName(newName);
                    list.add(whitelist.getUserName());
                    whitelistInfoMapper.updateWhitelistInfo(whitelist);

                    // 更新玩家详情
                    final PlayerDetails details = new PlayerDetails();
                    details.setWhitelistId(whitelist.getId());
                    final List<PlayerDetails> playerDetails = playerDetailsService.selectPlayerDetailsList(details);

                    if (!playerDetails.isEmpty()) {
                        final PlayerDetails player = playerDetails.get(0);
                        player.setUserName(newName);
                        player.setUpdateTime(new Date());

                        JSONObject data = new JSONObject();
                        if (player.getParameters() != null) {
                            data = JSONObject.parseObject(player.getParameters());
                            data.getJSONArray("name_history").add(oldName);
                        } else {
                            data.put("name_history", new ArrayList<String>() {{
                                add(oldName);
                            }});
                            player.setParameters(data.toJSONString());
                        }
                        playerDetailsMapper.updatePlayerDetails(player);
                    } else {
                        PlayerDetails player = new PlayerDetails();
                        player.setWhitelistId(whitelist.getId());
                        player.setCreateTime(new Date());
                        player.setQq(whitelist.getQqNum());
                        player.setIdentity(Identity.PLAYER.getValue());
                        player.setUserName(newName);
                        player.setParameters("{}");
                        playerDetailsMapper.insertPlayerDetails(player);
                    }

                }
            } catch (Exception e) {
                log.error("syncUserNameForUuid error", e);
            }
        });
        log.debug("syncUserNameForUuid list: {}", list);
        log.debug("syncUserNameForUuid end");
    }

    /**
     * 根据高密度定时查询在线用户更新最后一次上线时间
     */
    public void monitor() {
        if (cache == null) {
            log.error("Cache is not initialized.");
            return;
        }

        try {
            // 获取所有在线玩家
            Set<String> currentOnlinePlayers = OnlinePlayerUtil.getAllOnlinePlayers();

            // 清理无效的玩家名
            currentOnlinePlayers = playerMonitorUtil.cleanPlayerNames(currentOnlinePlayers);

            // 检测玩家变化
            PlayerMonitorUtil.PlayerChangeInfo changeInfo = playerMonitorUtil.detectPlayerChanges(currentOnlinePlayers);

            if (!changeInfo.hasChanges()) {
                return; // 没有变化，直接返回
            }

            // 处理新上线的玩家
            if (!changeInfo.getNewOnlinePlayers().isEmpty()) {
                handleNewOnlinePlayers(changeInfo.getNewOnlinePlayers());
                playerMonitorUtil.handlePlayersOnline(changeInfo.getNewOnlinePlayers());
            }

            // 处理新下线的玩家
            if (!changeInfo.getNewOfflinePlayers().isEmpty()) {
                handleNewOfflinePlayers(changeInfo.getNewOfflinePlayers());
                playerMonitorUtil.handlePlayersOffline(changeInfo.getNewOfflinePlayers());
            }

            log.debug("玩家监控完成 - 在线: {}, 新上线: {}, 新下线: {}",
                    changeInfo.getTotalOnlineCount(),
                    changeInfo.getNewOnlinePlayers().size(),
                    changeInfo.getNewOfflinePlayers().size());

        } catch (Exception e) {
            log.error("玩家监控任务执行失败", e);
        }
    }

    /**
     * 处理新上线的玩家
     */
    private void handleNewOnlinePlayers(Set<String> newOnlinePlayers) {
        log.info("检测到玩家上线: {}", newOnlinePlayers);

        // 发送上线通知
        sendPlayerNotification(newOnlinePlayers, CMD_ONLINE_NOTIFY, "上线");

        // 记录玩家活跃度（上线时记录为新玩家检测）
        for (String playerName : newOnlinePlayers) {
            try {
                // 检查是否为新玩家（简单检查，可以根据实际需求优化）
                boolean isNewPlayer = isNewPlayerToday(playerName);
                activityStatsService.recordDailyActivity(playerName, 0L, isNewPlayer);
            } catch (Exception e) {
                log.error("记录玩家 {} 上线活跃度失败: {}", playerName, e.getMessage());
            }
        }
    }

    /**
     * 处理新下线的玩家
     */
    private void handleNewOfflinePlayers(Set<String> newOfflinePlayers) {
        log.info("检测到玩家下线: {}", newOfflinePlayers);

        // 发送下线通知
        sendPlayerNotification(newOfflinePlayers, CMD_OFFLINE_NOTIFY, "下线");

        // 记录玩家活跃度（下线时记录在线时长）
        for (String playerName : newOfflinePlayers) {
            try {
                // 计算本次在线时长
                Long onlineMinutes = calculatePlayerOnlineTime(playerName);
                if (onlineMinutes > 0) {
                    activityStatsService.recordDailyActivity(playerName, onlineMinutes, false);
                }
            } catch (Exception e) {
                log.error("记录玩家 {} 下线活跃度失败: {}", playerName, e.getMessage());
            }
        }
    }

    /**
     * 发送玩家上下线通知
     */
    private void sendPlayerNotification(Set<String> players, String commandKey, String actionType) {
        try {
            QqBotConfig config = new QqBotConfig();
            config.setStatus(1L);
            List<QqBotConfig> botConfigs = qqBotConfigMapper.selectQqBotConfigList(config);

            String message = playerMonitorUtil.formatPlayerNotification(players, actionType);

            for (QqBotConfig botConfig : botConfigs) {
                String[] groupIds = botConfig.getGroupIds().split(",");
                for (String groupId : groupIds) {
                    if (isCommandEnabled(groupId.trim(), commandKey)) {
                        BotUtil.sendMessage(message, groupId.trim(), botConfig);
                    }
                }
            }
        } catch (Exception e) {
            log.error("发送玩家{}通知失败", actionType, e);
        }
    }

    /**
     * 命令重试
     */
    public void commandRetry() {
        log.debug("commandRetry start");
        Map<String, Object> map = new HashMap<>();
        if (cache.hasKey(CacheKey.ERROR_COMMAND_CACHE_KEY)) {
            map = cache.getCacheObject(CacheKey.ERROR_COMMAND_CACHE_KEY);
            if (map.isEmpty()) {
                return;
            }
            map.remove("@type");

            // 发送缓存中的命令
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Set<String> commands = (Set<String>) entry.getValue();
                if (commands.isEmpty()) {
                    continue;
                }
                boolean flag = key.contains("all");

                // 已执行命令
                Set<String> executedCommands = new HashSet<>();

                for (String command : commands) {
                    try {
                        if (flag) {
                            RconCache.getMap().forEach((k, v) -> {
                                try {
                                    v.sendCommand(command);
                                } catch (Exception e) {
                                    log.error("Failed to send command: {}", command, e);
                                }
                            });
                        } else {
                            if (RconCache.containsKey(key)) {
                                RconCache.get(key).sendCommand(command);
                            } else {
                                // 移除
                                executedCommands.add(command);
                            }
                        }
                        log.info("Successfully sent command: {}", command);

                        executedCommands.add(command);
                    } catch (Exception e) {
                        log.error("Failed to send command: {}", command, e);
                    }
                }

                // 移除已执行命令
                commands.removeAll(executedCommands);

                if (commands.isEmpty()) {
                    // 删除缓存
                    map.remove(key);
                }
                if (map.isEmpty()) {
                    // 删除缓存
                    cache.deleteObject(CacheKey.ERROR_COMMAND_CACHE_KEY);
                } else {
                    // 更新缓存
                    cache.setCacheObject(CacheKey.ERROR_COMMAND_CACHE_KEY, map);
                }

            }
        }
        log.debug("commandRetry end");
    }

    /**
     * 检查指定群组的功能是否启用
     *
     * @param groupId    群组ID
     * @param commandKey 功能关键字
     * @return 是否启用
     */
    private boolean isCommandEnabled(String groupId, String commandKey) {
        try {
            BotGroupCommandConfig config = commandConfigService.checkCommandEnabled(groupId, commandKey);
            // 如果没有配置或启用状态为1，则认为启用
            return config == null || config.getIsEnabled() == null || config.getIsEnabled() == 1;
        } catch (Exception e) {
            log.error("检查功能开关状态失败: groupId={}, commandKey={}, error={}", groupId, commandKey, e.getMessage());
            // 出错时默认启用，避免影响正常功能
            return true;
        }
    }

    /**
     * 检查玩家是否为今日新玩家
     *
     * @param playerName 玩家名
     * @return 是否为新玩家
     */
    private boolean isNewPlayerToday(String playerName) {
        try {
            PlayerDetails details = new PlayerDetails();
            details.setUserName(playerName);
            List<PlayerDetails> playerList = playerDetailsService.selectPlayerDetailsList(details);

            if (playerList.isEmpty()) {
                return true; // 数据库中没有记录，认为是新玩家
            }

            PlayerDetails playerDetails = playerList.get(0);
            Date createTime = playerDetails.getCreateTime();

            if (createTime == null) {
                return true;
            }

            // 检查创建时间是否为今天
            String today = cc.endmc.common.utils.DateUtils.dateTimeNow("yyyy-MM-dd");
            String createDate = cc.endmc.common.utils.DateUtils.parseDateToStr("yyyy-MM-dd", createTime);

            return today.equals(createDate);

        } catch (Exception e) {
            log.error("检查玩家 {} 是否为新玩家失败: {}", playerName, e.getMessage());
            return false;
        }
    }

    /**
     * 计算玩家本次在线时长
     *
     * @param playerName 玩家名
     * @return 在线时长（分钟）
     */
    private Long calculatePlayerOnlineTime(String playerName) {
        try {
            PlayerDetails details = new PlayerDetails();
            details.setUserName(playerName);
            List<PlayerDetails> playerList = playerDetailsService.selectPlayerDetailsList(details);

            if (playerList.isEmpty()) {
                return 0L;
            }

            PlayerDetails playerDetails = playerList.get(0);
            Date lastOnlineTime = playerDetails.getLastOnlineTime();

            if (lastOnlineTime == null) {
                return 0L;
            }

            // 计算从上次上线到现在的时长
            long diffMs = System.currentTimeMillis() - lastOnlineTime.getTime();
            long minutes = diffMs / (1000 * 60);

            // 限制最大在线时长为24小时，避免异常数据
            return Math.min(minutes, 24 * 60);

        } catch (Exception e) {
            log.error("计算玩家 {} 在线时长失败: {}", playerName, e.getMessage());
            return 0L;
        }
    }
}
