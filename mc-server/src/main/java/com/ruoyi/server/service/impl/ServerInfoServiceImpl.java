package com.ruoyi.server.service.impl;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.server.common.MapCache;
import com.ruoyi.server.domain.ServerInfo;
import com.ruoyi.server.mapper.ServerInfoMapper;
import com.ruoyi.server.service.IServerInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 服务器信息Service业务层处理
 *
 * @author ruoyi
 * @date 2024-03-10
 */
@Service
public class ServerInfoServiceImpl implements IServerInfoService {
    @Autowired
    private ServerInfoMapper serverInfoMapper;
    @Autowired
    private RedisCache redisCache;


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
    public Map<String, List<String>> getOnlinePlayer() {
        Map<String, List<String>> onlinePlayer = new HashMap<>();
        List<ServerInfo> serverInfo;
        // 从Redis缓存中获取serverInfo
        if (redisCache.hasKey("serverInfo")) {
            serverInfo = redisCache.getCacheObject("serverInfo");
        } else {
            serverInfo = serverInfoMapper.selectServerInfoList(new ServerInfo());
            redisCache.setCacheObject("serverInfo", serverInfo, 1, TimeUnit.DAYS);
        }
        if (serverInfo != null) {
            for (ServerInfo info : serverInfo) {
                if (info.getStatus() == 0) {
                    continue;
                }
                // 获取在线玩家
                List<String> playerList = new ArrayList<>();
                String list = MapCache.get(info.getId().toString()).sendCommand("list");
                if (list != null) {
                    String[] split = list.split(":");
                    if (split.length > 1) {
                        String[] players = split[1].trim().split(", ");
                        playerList = Arrays.asList(players);
                    }
                    onlinePlayer.put(info.getNameTag(), playerList);
                }
            }
        }
        return onlinePlayer;
    }
}
