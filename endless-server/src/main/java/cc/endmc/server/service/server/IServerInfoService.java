package cc.endmc.server.service.server;

import cc.endmc.server.domain.server.ServerInfo;

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
     * 查询服务器信息
     *
     * @param ids 服务器信息主键
     * @return 服务器信息
     */
    public List<ServerInfo> selectServerInfoByIds(List<Long> ids);

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
    Map<String, Object> getOnlinePlayer(boolean cache);

    /**
     * 聚合查询
     *
     * @return 聚合结果
     */
    Map<String, Object> aggregateQuery();

    /**
     * 根据用户RCON权限查询服务器信息列表
     *
     * @param serverInfo 服务器信息查询条件
     * @param userId     用户ID
     * @param permission 需要的权限类型
     * @return 用户有权限的服务器信息集合
     */
    List<ServerInfo> selectServerInfoListByRconPermission(ServerInfo serverInfo, Long userId, String permission);
}
