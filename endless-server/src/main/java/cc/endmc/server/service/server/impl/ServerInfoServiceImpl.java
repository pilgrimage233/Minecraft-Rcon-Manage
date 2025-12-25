package cc.endmc.server.service.server.impl;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.framework.manager.AsyncManager;
import cc.endmc.node.domain.NodeServer;
import cc.endmc.node.service.INodeServerService;
import cc.endmc.permission.service.IResourcePermissionService;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.PasswordManager;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.common.constant.Command;
import cc.endmc.server.common.constant.RconMsg;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.permission.BanlistInfo;
import cc.endmc.server.domain.permission.OperatorList;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.player.PlayerDetails;
import cc.endmc.server.domain.player.vo.PlayerDetailsVo;
import cc.endmc.server.domain.server.ServerCommandInfo;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.mapper.permission.WhitelistInfoMapper;
import cc.endmc.server.mapper.player.PlayerDetailsMapper;
import cc.endmc.server.mapper.server.ServerInfoMapper;
import cc.endmc.server.service.permission.IBanlistInfoService;
import cc.endmc.server.service.permission.IOperatorListService;
import cc.endmc.server.service.server.IServerCommandInfoService;
import cc.endmc.server.service.server.IServerInfoService;
import cc.endmc.server.utils.OnlinePlayerUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 服务器信息Service业务层处理
 *
 * @author ruoyi
 * @date 2024-03-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServerInfoServiceImpl implements IServerInfoService {

    private final ServerInfoMapper serverInfoMapper;
    private final RedisCache redisCache;
    private final RconService rconService;
    private final WhitelistInfoMapper whitelistInfoMapper;
    private final PlayerDetailsMapper playerDetailsMapper;
    private final IOperatorListService operatorListService;
    private final IBanlistInfoService banlistInfoService;
    private final PasswordManager PasswordManager;
    private final INodeServerService nodeServerService;
    private final IServerCommandInfoService serverCommandInfo;
    private final IResourcePermissionService resourcePermissionService;

    /**
     * 查询服务器信息
     *
     * @param id 服务器信息主键
     * @return 服务器信息
     */
    @Override
    public ServerInfo selectServerInfoById(Long id) {
        return serverInfoMapper.selectServerInfoById(id);
    }

    /**
     * 查询服务器信息
     *
     * @param ids 服务器信息主键
     * @return 服务器信息
     */
    @Override
    public List<ServerInfo> selectServerInfoByIds(List<Long> ids) {
        return serverInfoMapper.selectServerInfoByIds(ids);
    }

    /**
     * 查询服务器信息列表
     *
     * @param serverInfo 服务器信息
     * @return 服务器信息
     */
    @Override
    public List<ServerInfo> selectServerInfoList(ServerInfo serverInfo) {
        return serverInfoMapper.selectServerInfoList(serverInfo);
    }

    /**
     * 新增服务器信息
     *
     * @param serverInfo 服务器信息
     * @return 结果
     */
    @SneakyThrows
    @Override
    public int insertServerInfo(ServerInfo serverInfo) {
        // 设置随机UUID
        serverInfo.setUuid(UUID.randomUUID().toString());
        try {
            // 设置Rcon密码
            serverInfo.setRconPassword(PasswordManager.encrypt(serverInfo.getRconPassword()));
        } catch (Exception e) {
            log.error("密码加密失败：" + e.getMessage());
        }
        final int result = serverInfoMapper.insertServerInfo(serverInfo);
        // 更新缓存
        if (result > 0) {
            // 初始化服务器命令
            ServerCommandInfo commandInfo = new ServerCommandInfo();
            commandInfo.setServerId(serverInfo.getId().toString());
            commandInfo.setOnlineAddWhitelistCommand("whitelist add {player}");
            commandInfo.setOfflineAddWhitelistCommand("whitelist add {player}");
            commandInfo.setOnlineRmWhitelistCommand("whitelist remove {player}");
            commandInfo.setOfflineRmWhitelistCommand("whitelist remove {player}");
            commandInfo.setOnlineAddBanCommand("ban {player} {reason}");
            commandInfo.setOfflineAddBanCommand("ban {player} {reason}");
            commandInfo.setOnlineRmBanCommand("pardon {player}");
            commandInfo.setOfflineRmBanCommand("pardon {player}");
            serverCommandInfo.insertServerCommandInfo(commandInfo);

            this.rebuildCache();
            // 初始化Rcon连接
            if (RconCache.containsKey(serverInfo.getId().toString())) {
                rconService.init(serverInfo);
            }
        }
        // 异步同步黑名单
        AsyncManager.me().execute(new TimerTask() {
            @Override
            public void run() {
                syncBanList(serverInfo);
            }
        });

        return result;
    }

    /**
     * 修改服务器信息
     *
     * @param serverInfo 服务器信息
     * @return 结果
     */
    @Override
    public int updateServerInfo(ServerInfo serverInfo) {
        boolean sync = false;
        // 判断IP和端口是否变化，如果变化就同步黑名单
        final ServerInfo old = serverInfoMapper.selectServerInfoById(serverInfo.getId());
        if (old != null) {
            if (!old.getIp().equals(serverInfo.getIp()) || !Objects.equals(old.getRconPort(), serverInfo.getRconPort())) {
                sync = true;
            }
        }
        // 判断加密后的密码是否相同，如果相同就不要修改
        if (old != null) {
            if (old.getRconPassword().equals(serverInfo.getRconPassword())) {
                serverInfo.setRconPassword(null);
            } else {
                try {
                    // 设置Rcon密码
                    if (StringUtils.isNotEmpty(serverInfo.getRconPassword())) {
                        serverInfo.setRconPassword(PasswordManager.encrypt(serverInfo.getRconPassword()));
                    }
                } catch (Exception e) {
                    log.error("密码加密失败：" + e.getMessage());
                }
            }
        }
        // 更新服务器信息
        final int result = serverInfoMapper.updateServerInfo(serverInfo);

        // 异步同步黑名单
        if (sync) {
            AsyncManager.me().execute(new TimerTask() {
                @Override
                public void run() {
                    syncBanList(serverInfo);
                }
            });
        }

        // 更新缓存
        if (result > 0) {
            this.rebuildCache();
            // 初始化Rcon连接
            if (RconCache.containsKey(serverInfo.getId().toString())) {
                rconService.init(serverInfo);
            }
        }
        return result;
    }

    /**
     * 批量删除服务器信息
     *
     * @param ids 需要删除的服务器信息主键
     * @return 结果
     */
    @Override
    public int deleteServerInfoByIds(Long[] ids) {
        return serverInfoMapper.deleteServerInfoByIds(ids);
    }

    /**
     * 删除服务器信息信息
     *
     * @param id 服务器信息主键
     * @return 结果
     */
    @Override
    public int deleteServerInfoById(Long id) {
        int result = serverInfoMapper.deleteServerInfoById(id);

        // 关闭Rcon连接
        if (RconCache.containsKey(id.toString())) {
            RconService.close(id.toString());
            RconCache.remove(id.toString());
        }
        if (result > 0) {
            this.rebuildCache();
        }

        return result;
    }

    /**
     * 获取在线玩家
     *
     * @return 在线玩家
     */
    @Override
    public Map<String, Object> getOnlinePlayer(boolean cache) {
        Map<String, Object> result = new HashMap<>();
        List<ServerInfo> serverInfoList = getServerInfoList();

        if (serverInfoList != null) {
            // 使用并行流提高性能
            serverInfoList.parallelStream()
                    .filter(info -> info.getStatus() == 1) // 只处理在线服务器
                    .forEach(info -> {
                        String serverName = info.getNameTag();
                        try {
                            Map<String, Object> playerInfo = getServerPlayerInfo(info, cache);
                            synchronized (result) {
                                result.put(serverName, playerInfo);
                            }
                        } catch (Exception e) {
                            synchronized (result) {
                                result.put(serverName, "服务器连接失败，请检查服务器状态");
                            }
                            handleServerConnectionError(info, e);
                        }
                    });

            result.put("查询时间", DateUtils.getTime());
        }
        return result;
    }

    /**
     * 获取服务器信息列表
     */
    private List<ServerInfo> getServerInfoList() {
        if (redisCache.hasKey(CacheKey.SERVER_INFO_KEY)) {
            return redisCache.getCacheObject(CacheKey.SERVER_INFO_KEY);
        } else {
            rconService.reBuildCache();
            return redisCache.getCacheObject(CacheKey.SERVER_INFO_KEY);
        }
    }

    /**
     * 获取单个服务器的玩家信息
     */
    private Map<String, Object> getServerPlayerInfo(ServerInfo info, boolean cache) {
        String serverId = info.getId().toString();
        String cacheKey = CacheKey.SERVER_PLAYER_KEY + serverId;

        List<String> playerList;

        // 尝试从缓存获取
        if (cache && redisCache.hasKey(cacheKey)) {
            playerList = redisCache.getCacheList(cacheKey);
        } else {
            // 从服务器获取玩家列表
            playerList = fetchPlayerListFromServer(info);

            // 缓存结果
            if (!playerList.isEmpty() && cache) {
                redisCache.setCacheList(cacheKey, playerList);
                redisCache.expire(cacheKey, 30, TimeUnit.SECONDS);
            }
        }

        Map<String, Object> playerInfo = new HashMap<>();
        playerInfo.put("在线人数", playerList.size());
        playerInfo.put("在线玩家", playerList.toString());
        return playerInfo;
    }

    /**
     * 从服务器获取玩家列表
     */
    private List<String> fetchPlayerListFromServer(ServerInfo info) {
        return OnlinePlayerUtil.getOnlinePlayersFromServer(info);
    }

    /**
     * 处理服务器连接错误
     */
    private void handleServerConnectionError(ServerInfo info, Exception e) {
        String serverId = info.getId().toString();
        log.error("获取服务器 {} 在线玩家失败: {}", info.getNameTag(), e.getMessage());
        // 根据异常类型决定是否尝试重连
        if (shouldAttemptReconnect(e)) {
            AsyncManager.me().execute(new TimerTask() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000); // 等待1秒后重连
                        rconService.reconnect(serverId);
                        log.info("尝试重连服务器: {}", info.getNameTag());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("重连任务被中断: {}", info.getNameTag());
                    }
                }
            });
        }
    }

    /**
     * 判断是否应该尝试重连
     */
    private boolean shouldAttemptReconnect(Exception e) {
        String message = e.getMessage();
        if (message == null) return false;

        // 网络相关错误才尝试重连
        return message.contains("Connection") ||
                message.contains("timeout") ||
                message.contains("refused") ||
                message.contains("reset");
    }

    @Override
    public Map<String, Object> aggregateQuery() {
        Map<String, Object> result = new HashMap<>();

        // 在线玩家
        Map<String, Object> onlinePlayer = getOnlinePlayer(false);
        result.put("onlinePlayer", onlinePlayer);

        // 申请数量
        List<WhitelistInfo> whitelistInfos = whitelistInfoMapper.selectWhitelistInfoList(new WhitelistInfo());
        result.put("applyCount", whitelistInfos.size());

        // 白名单数量
        int index = (int) whitelistInfos.stream().filter(whitelistInfo -> whitelistInfo.getStatus().equals("1")).count();
        result.put("whiteListCount", index);

        // 未通过数量
        index = (int) whitelistInfos.stream().filter(whitelistInfo -> whitelistInfo.getStatus().equals("0")).count();
        result.put("notPassCount", index);

        // OP数量
        final OperatorList op = new OperatorList();
        op.setStatus(1L);
        final List<OperatorList> operatorLists = operatorListService.selectOperatorListList(op);
        result.put("opCount", operatorLists.size());

        // 封禁数量
        final BanlistInfo banlistInfo = new BanlistInfo();
        banlistInfo.setState(1L);
        int banCount = banlistInfoService.selectBanlistInfoList(banlistInfo).size();
        result.put("banCount", banCount);

        // 在线前十
        final List<PlayerDetails> playerDetails = playerDetailsMapper.selectTopTenByGameTime();
        final List<PlayerDetailsVo> playerDetailsVos = new ArrayList<>();
        playerDetails.forEach(o -> {
            final PlayerDetailsVo vo = new PlayerDetailsVo();
            BeanUtils.copyProperties(o, vo);
            playerDetailsVos.add(vo);
        });
        result.put("topTen", playerDetailsVos);

        // 服务器数量
        List<ServerInfo> serverInfo = serverInfoMapper.selectServerInfoList(new ServerInfo());
        result.put("serverCount", serverInfo.size());

        // 节点统计
        try {
            List<NodeServer> nodeServers = nodeServerService.selectNodeServerList(new NodeServer());
            result.put("nodeCount", nodeServers.size());
            // 在线节点数量
            long onlineNodeCount = nodeServers.stream()
                    .filter(node -> "0".equals(node.getStatus()))
                    .count();
            result.put("onlineNodeCount", onlineNodeCount);
            // 离线节点数量
            long offlineNodeCount = nodeServers.stream()
                    .filter(node -> "1".equals(node.getStatus()))
                    .count();
            result.put("offlineNodeCount", offlineNodeCount);
            // 节点列表简要信息
            List<Map<String, Object>> nodeList = new ArrayList<>();
            for (NodeServer node : nodeServers) {
                Map<String, Object> nodeInfo = new HashMap<>();
                nodeInfo.put("id", node.getId());
                nodeInfo.put("name", node.getName());
                nodeInfo.put("status", node.getStatus());
                nodeInfo.put("version", node.getVersion());
                nodeInfo.put("osType", node.getOsType());
                nodeInfo.put("lastHeartbeat", node.getLastHeartbeat());
                nodeList.add(nodeInfo);
            }
            result.put("nodeList", nodeList);
        } catch (Exception e) {
            log.error("获取节点统计信息失败", e);
            result.put("nodeCount", 0);
            result.put("onlineNodeCount", 0);
            result.put("offlineNodeCount", 0);
            result.put("nodeList", new ArrayList<>());
        }

        return result;
    }

    public void rebuildCache() {
        redisCache.deleteObject(CacheKey.SERVER_INFO_KEY);
        final List<ServerInfo> serverInfos = selectServerInfoList(new ServerInfo());
        if (serverInfos == null || serverInfos.isEmpty()) {
            log.error(RconMsg.SERVER_EMPTY);
        }
        Map<String, ServerInfo> map = new HashMap<>();
        if (serverInfos != null) {
            for (ServerInfo serverInfo : serverInfos) {
                map.put(serverInfo.getId().toString(), serverInfo);
            }
        }
        redisCache.setCacheObject(CacheKey.SERVER_INFO_MAP_KEY, map);
        redisCache.setCacheObject(CacheKey.SERVER_INFO_KEY, serverInfos, 3, TimeUnit.DAYS);
        redisCache.setCacheObject(CacheKey.SERVER_INFO_UPDATE_TIME_KEY, DateUtils.getNowDate());
    }

    @SneakyThrows
    public void syncBanList(ServerInfo serverInfo) {
        log.info("同步黑名单 服务器ID ---> " + serverInfo.getNameTag());
        // 简单的黑名单同步
        if (serverInfo.getStatus() == 1) {
            final BanlistInfo banlistInfo = new BanlistInfo();
            banlistInfo.setState(1L);
            final List<BanlistInfo> banlistInfos = banlistInfoService.selectBanlistInfoList(banlistInfo);

            List<Long> ids = new ArrayList<>();

            if (banlistInfos == null || banlistInfos.isEmpty()) {
                return;
            }

            banlistInfos.forEach(o -> ids.add(o.getWhiteId()));
            final List<WhitelistInfo> whitelistInfos = whitelistInfoMapper.selectWhitelistInfoByIds(ids);
            Map<Long, WhitelistInfo> map = whitelistInfos.stream()
                    .collect(Collectors.toMap(WhitelistInfo::getId, info -> info, (a, b) -> b));

            List<String> names = new ArrayList<>();
            // 同步黑名单
            if (map.isEmpty()) {
                return;
            }

            for (BanlistInfo info : banlistInfos) {
                if (map.containsKey(info.getWhiteId())) {
                    final WhitelistInfo whitelistInfo = map.get(info.getWhiteId());
                    if (whitelistInfo != null) {
                        try {
                            if (RconCache.containsKey(serverInfo.getId().toString())) {
                                try {
                                    RconCache.get(serverInfo.getId().toString()).
                                            sendCommand(String.format(Command.BAN_ADD, whitelistInfo.getUserName()));
                                } catch (Exception e) {
                                    // 尝试重连
                                    if (rconService.init(serverInfo)) {
                                        // 回调
                                        syncBanList(serverInfo);
                                    }
                                }
                            }
                            names.add(info.getUserName());
                        } catch (Exception e) {
                            log.error("同步黑名单失败：" + e.getMessage());
                        }
                    }
                    // 防止Rcon频繁操作
                    if (banlistInfos.size() > 5) Thread.sleep(500);
                }
            }
            log.info("同步黑名单成功：" + names);
        }
    }

    @Override
    public List<ServerInfo> selectServerInfoListByRconPermission(ServerInfo serverInfo, Long userId, String permission) {
        // 如果是管理员，返回所有服务器
        if (resourcePermissionService.isAdmin(userId)) {
            return selectServerInfoList(serverInfo);
        }

        // 获取用户有权限的服务器ID列表
        List<Long> userServerIds = resourcePermissionService.getUserRconServerIds(userId);
        if (userServerIds == null || userServerIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询所有服务器
        List<ServerInfo> allServers = selectServerInfoList(serverInfo);

        // 过滤出用户有权限的服务器
        return allServers.stream()
                .filter(server -> userServerIds.contains(server.getId()))
                .collect(Collectors.toList());
    }

}
