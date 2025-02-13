package com.ruoyi.server.mapper.server;

import com.ruoyi.server.domain.server.ServerInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 服务器信息Mapper接口
 *
 * @author ruoyi
 * @date 2024-03-10
 */
@Mapper
public interface ServerInfoMapper {
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
     * 删除服务器信息
     *
     * @param id 服务器信息主键
     * @return 结果
     */
    public int deleteServerInfoById(Long id);

    /**
     * 批量删除服务器信息
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteServerInfoByIds(Long[] ids);

    /**
     * 根据多个ID查询服务器名称
     */
    public List<String> selectServerNameByIds(Long[] ids);
}
