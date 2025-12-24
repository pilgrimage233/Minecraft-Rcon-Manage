package cc.endmc.quartz.async;

import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.constant.Command;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 异步服务
 * 专门处理需要异步执行的操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncService {

    private final RconService rconService;
    private final IWhitelistInfoService whitelistInfoService;

    private static final String APPROVED_STATUS = "1";
    private static final String ALL_SERVERS = "all";
    private static final int BATCH_DELAY_MS = 500;
    private static final int BATCH_SIZE_THRESHOLD = 5;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * 同步单个服务器的白名单
     *
     * @param serverId 服务器ID
     */
    @Async("virtualThreadExecutor")
    public void syncSingleServer(String serverId) throws InterruptedException {
        log.debug("开始同步单个服务器白名单：{}", serverId);

        // 1. 获取数据库中已审核的白名单
        List<WhitelistInfo> approvedUsers = getApprovedWhitelistForServer(serverId);
        if (approvedUsers.isEmpty()) {
            log.debug("服务器 {} 没有已审核的白名单用户", serverId);
            return;
        }

        // 2. 获取服务器当前白名单
        WhitelistData currentWhitelist = fetchServerWhitelist(serverId, 0);
        if (currentWhitelist == null) {
            log.error("服务器 {} 白名单获取失败，已达最大重试次数", serverId);
            return;
        }

        // 3. 计算需要添加的用户
        List<WhitelistInfo> usersToAdd = calculateUsersToAdd(approvedUsers, currentWhitelist);

        // 4. 添加新用户
        List<String> addedUsers = addWhitelistUsers(serverId, usersToAdd);

        // 5. 计算并移除多余的用户
        List<String> removedUsers = removeExtraUsers(serverId, approvedUsers, currentWhitelist);

        log.info("同步白名单完成 - 服务器：{}，新增：{}，移除：{}", serverId, addedUsers, removedUsers);
    }

    /**
     * 获取指定服务器已审核的白名单用户
     */
    private List<WhitelistInfo> getApprovedWhitelistForServer(String serverId) {
        WhitelistInfo query = new WhitelistInfo();
        query.setStatus(APPROVED_STATUS);
        List<WhitelistInfo> allApproved = whitelistInfoService.selectWhitelistInfoList(query);

        return allApproved.stream()
                .filter(info -> info.getServers() != null && !info.getServers().isEmpty())
                .filter(info -> isServerIncluded(info.getServers(), serverId))
                .collect(Collectors.toList());
    }

    /**
     * 判断服务器是否在白名单配置中
     */
    private boolean isServerIncluded(String servers, String serverId) {
        Set<String> serverSet = new HashSet<>(Arrays.asList(servers.split(",")));
        return serverSet.contains(ALL_SERVERS) || serverSet.contains(serverId);
    }

    /**
     * 获取服务器当前白名单（带重试机制）
     */
    private WhitelistData fetchServerWhitelist(String serverId, int retryCount) throws InterruptedException {
        String list = RconCache.get(serverId).sendCommand(Command.WHITELIST_LIST);
        log.debug("服务器 {} 白名单列表：{}", serverId, list);

        if (StringUtils.isNotEmpty(list) && list.contains("There are")) {
            return parseWhitelistResponse(list);
        } else if (StringUtils.isNotEmpty(list) && !list.contains("There are")) {
            list = RconCache.get(serverId).sendCommand("minecraft:whitelist list");
            return parseWhitelistResponse(list);
        }

        // 重试逻辑
        if (retryCount < MAX_RETRY_ATTEMPTS) {
            log.warn("服务器 {} 白名单列表格式不正确，尝试初始化（第 {} 次）", serverId, retryCount + 1);
            rconService.sendCommand(serverId, "whitelist add test", false);
            Thread.sleep(1000); // 等待初始化完成
            return fetchServerWhitelist(serverId, retryCount + 1);
        }

        return null;
    }

    /**
     * 解析白名单响应
     */
    private WhitelistData parseWhitelistResponse(String response) {
        String playerList = response.split("whitelisted player\\(s\\):")[1].trim();
        Set<String> onlinePlayers = new HashSet<>(Arrays.asList(playerList.split(", ")));
        Set<String> offlinePlayers = new HashSet<>(Arrays.asList(playerList.toLowerCase().split(", ")));
        return new WhitelistData(onlinePlayers, offlinePlayers);
    }

    /**
     * 计算需要添加的用户
     */
    private List<WhitelistInfo> calculateUsersToAdd(List<WhitelistInfo> approvedUsers, WhitelistData currentWhitelist) {
        return approvedUsers.stream()
                .filter(info -> {
                    if (info.getOnlineFlag() == 1L) {
                        return !currentWhitelist.onlinePlayers.contains(info.getUserName());
                    } else {
                        return !currentWhitelist.offlinePlayers.contains(info.getUserName().toLowerCase());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 批量添加白名单用户
     */
    private List<String> addWhitelistUsers(String serverId, List<WhitelistInfo> usersToAdd) throws InterruptedException {
        List<String> addedUsers = new ArrayList<>();

        for (WhitelistInfo info : usersToAdd) {
            // 批量操作时添加延迟，避免服务器压力过大
            if (usersToAdd.size() >= BATCH_SIZE_THRESHOLD) {
                Thread.sleep(BATCH_DELAY_MS);
            }

            try {
                rconService.sendCommand(serverId, String.format(Command.WHITELIST_ADD, info.getUserName()),
                        info.getOnlineFlag() == 1L);
                addedUsers.add(info.getUserName());
            } catch (Exception e) {
                log.error("添加白名单失败 - 服务器：{}，用户：{}", serverId, info.getUserName(), e);
            }
        }

        return addedUsers;
    }

    /**
     * 移除不在数据库中的白名单用户
     */
    private List<String> removeExtraUsers(String serverId, List<WhitelistInfo> approvedUsers,
                                          WhitelistData currentWhitelist) {
        List<String> removedUsers = new ArrayList<>();

        // 构建已审核用户的快速查找集合
        Map<String, Boolean> approvedUserMap = approvedUsers.stream()
                .collect(Collectors.toMap(
                        info -> info.getUserName().toLowerCase(),
                        info -> info.getOnlineFlag() == 1L,
                        (v1, v2) -> v1
                ));

        for (String player : currentWhitelist.onlinePlayers) {
            String lowerPlayer = player.toLowerCase();
            Boolean isOnline = approvedUserMap.get(lowerPlayer);

            // 如果不在审核列表中，或者大小写不匹配（正版玩家需要精确匹配）
            if (isOnline == null || (isOnline && !approvedUsers.stream()
                    .anyMatch(u -> u.getOnlineFlag() == 1L && u.getUserName().equals(player)))) {
                try {
                    rconService.sendCommand(serverId, String.format(Command.WHITELIST_REMOVE, player),
                            isOnline != null && isOnline);
                    removedUsers.add(player);
                } catch (Exception e) {
                    log.error("移除白名单失败 - 服务器：{}，用户：{}", serverId, player, e);
                }
            }
        }

        return removedUsers;
    }

    @AllArgsConstructor
    private static class WhitelistData {
        final Set<String> onlinePlayers;
        final Set<String> offlinePlayers;
    }

}
