package cc.endmc.server.service.server.impl;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.async.AsyncManager;
import cc.endmc.server.common.MapCache;
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
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.mapper.player.PlayerDetailsMapper;
import cc.endmc.server.mapper.server.ServerInfoMapper;
import cc.endmc.server.service.permission.IBanlistInfoService;
import cc.endmc.server.service.permission.IOperatorListService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.server.IServerInfoService;
import lombok.SneakyThrows;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
@Service
public class ServerInfoServiceImpl implements IServerInfoService {

    private static final Log log = LogFactory.getLog(ServerInfoServiceImpl.class);

    @Autowired
    private ServerInfoMapper serverInfoMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RconService rconService;

    @Autowired
    private IWhitelistInfoService whitelistInfo;

    @Autowired
    private PlayerDetailsMapper playerDetailsMapper;

    @Autowired
    private IOperatorListService operatorListService;

    @Autowired
    private IBanlistInfoService banlistInfoService;

    @Autowired
    private PasswordManager PasswordManager;


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
            this.rebuildCache();
            // 初始化Rcon连接
            if (MapCache.containsKey(serverInfo.getId().toString())) {
                rconService.init(serverInfo);
            }
        }
        // 异步同步黑名单
        AsyncManager.getInstance().execute(new TimerTask() {
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
            AsyncManager.getInstance().execute(new TimerTask() {
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
            if (MapCache.containsKey(serverInfo.getId().toString())) {
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
        if (MapCache.containsKey(id.toString())) {
            RconService.close(id.toString());
            MapCache.remove(id.toString());
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
    public Map<String, Object> getOnlinePlayer() {
        Map<String, Object> result = new HashMap<>();
        List<ServerInfo> serverInfo;
        // 从Redis缓存中获取serverInfo
        if (redisCache.hasKey(CacheKey.SERVER_INFO_KEY)) {
            serverInfo = redisCache.getCacheObject(CacheKey.SERVER_INFO_KEY);
        } else {
            serverInfo = serverInfoMapper.selectServerInfoList(new ServerInfo());
            redisCache.setCacheObject(CacheKey.SERVER_INFO_KEY, serverInfo, 3, TimeUnit.DAYS);
        }
        if (serverInfo != null) {
            for (ServerInfo info : serverInfo) {
                if (info.getStatus() == 0) {
                    continue;
                }
                // 获取在线玩家
                Map<String, Object> onlinePlayer = new HashMap<>();
                List<String> playerList = new ArrayList<>();

                try {
                    String list = MapCache.get(info.getId().toString()).sendCommand("list");
                    if (list != null) {
                        String[] split = new String[0];
                        // 判断是否插件服装有ESS
                        if (!list.startsWith("There are")) {
                            list = MapCache.get(info.getId().toString()).sendCommand("minecraft:list");
                        }
                        split = list.split(":");
                        if (split.length > 1) {
                            String[] players = split[1].trim().split(", ");
                            playerList = Arrays.stream(players)
                                    .filter(StringUtils::isNotEmpty)
                                    .collect(Collectors.toList());
                        }
                        onlinePlayer.put("在线人数", playerList.size());
                        onlinePlayer.put("在线玩家", playerList.toString());
                        result.put(info.getNameTag(), onlinePlayer);

                    }
                } catch (Exception e) {
                    result.put(info.getNameTag(), "服务器连接失败，请检查服务器状态");
                    // 随机重连
                    Random random = new Random();
                    int i = random.nextInt(10);
                    if (i % 2 == 0) {
                        rconService.reconnect(info.getId().toString());
                    }
                    log.error("获取在线玩家失败：" + e.getMessage());
                }
            }
            result.put("查询时间", DateUtils.getTime());
        }
        return result;
    }

    @Override
    public Map<String, Object> aggregateQuery() {
        Map<String, Object> result = new HashMap<>();

        // 在线玩家
        Map<String, Object> onlinePlayer = getOnlinePlayer();
        result.put("onlinePlayer", onlinePlayer);

        // 申请数量
        List<WhitelistInfo> whitelistInfos = whitelistInfo.selectWhitelistInfoList(new WhitelistInfo());
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
            final List<WhitelistInfo> whitelistInfos = whitelistInfo.selectWhitelistInfoByIds(ids);
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
                            if (MapCache.containsKey(serverInfo.getId().toString())) {
                                try {
                                    MapCache.get(serverInfo.getId().toString()).
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

}
