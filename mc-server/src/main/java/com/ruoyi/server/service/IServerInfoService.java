package com.ruoyi.server.service;

import com.ruoyi.server.domain.ServerInfo;

import java.util.List;
import java.util.Map;

/**
 * 服务器信息Service接口
 *
 * @author ruoyi
 * @date 2024-03-10
 */
public interface IServerInfoService {
    /**
     * 查询服务器信息
     *
     * @param id 服务器信息主键
     * @return 服务器信息
     */
    public ServerInfo selectServerInfoById(Long id);

    /**
     * 查询服务器信息列表
     *
     * @param serverInfo 服务器信息
     * @return 服务器信息集合
     */
    public List<ServerInfo> selectServerInfoList(ServerInfo serverInfo);

    /**
     * 新增服务器信息
     *
     * @param serverInfo 服务器信息
     * @return 结果
     */
    public int insertServerInfo(ServerInfo serverInfo);

    /**
     * 修改服务器信息
     *
     * @param serverInfo 服务器信息
     * @return 结果
     */
    public int updateServerInfo(ServerInfo serverInfo);

    /**
     * 批量删除服务器信息
     *
     * @param ids 需要删除的服务器信息主键集合
     * @return 结果
     */
    public int deleteServerInfoByIds(Long[] ids);

    /**
     * 删除服务器信息信息
     *
     * @param id 服务器信息主键
     * @return 结果
     */
    public int deleteServerInfoById(Long id);

    /**
     * 获取在线玩家
     *
     * @return 在线玩家
     */
    Map<String, Object> getOnlinePlayer();

    /**
     * 聚合查询
     *
     * @return 聚合结果
     */
    Map<String, Object> aggregateQuery();
}
