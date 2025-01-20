package com.ruoyi.server.service.impl;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.server.common.MapCache;
import com.ruoyi.server.common.RconService;
import com.ruoyi.server.domain.*;
import com.ruoyi.server.domain.v2.PlayerDetailsVo;
import com.ruoyi.server.mapper.PlayerDetailsMapper;
import com.ruoyi.server.mapper.ServerInfoMapper;
import com.ruoyi.server.service.IBanlistInfoService;
import com.ruoyi.server.service.IOperatorListService;
import com.ruoyi.server.service.IServerInfoService;
import com.ruoyi.server.service.IWhitelistInfoService;
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
    @Override
    public int insertServerInfo(ServerInfo serverInfo) {
        // 设置随机UUID
        serverInfo.setUuid(UUID.randomUUID().toString());
        return serverInfoMapper.insertServerInfo(serverInfo);
    }

    /**
     * 修改服务器信息
     *
     * @param serverInfo 服务器信息
     * @return 结果
     */
    @Override
    public int updateServerInfo(ServerInfo serverInfo) {
        return serverInfoMapper.updateServerInfo(serverInfo);
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
        return serverInfoMapper.deleteServerInfoById(id);
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
        if (redisCache.hasKey("serverInfo")) {
            serverInfo = redisCache.getCacheObject("serverInfo");
        } else {
            serverInfo = serverInfoMapper.selectServerInfoList(new ServerInfo());
            redisCache.setCacheObject("serverInfo", serverInfo, 3, TimeUnit.DAYS);
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
                        if (!list.contains("There are")) {
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
}
