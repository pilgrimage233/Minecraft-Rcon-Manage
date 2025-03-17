package cc.endmc.server.mapper.server;

import cc.endmc.server.domain.server.ServerCommandInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 指令管理Mapper接口
 *
 * @author ruoyi
 * @date 2024-04-16
 */
@Mapper
public interface ServerCommandInfoMapper {
    /**
     * 查询指令管理
     *
     * @param id 指令管理主键
     * @return 指令管理
     */
    public ServerCommandInfo selectServerCommandInfoById(Long id);

    /**
     * 查询指令管理列表
     *
     * @param serverCommandInfo 指令管理
     * @return 指令管理集合
     */
    public List<ServerCommandInfo> selectServerCommandInfoList(ServerCommandInfo serverCommandInfo);

    /**
     * 新增指令管理
     *
     * @param serverCommandInfo 指令管理
     * @return 结果
     */
    public int insertServerCommandInfo(ServerCommandInfo serverCommandInfo);

    /**
     * 修改指令管理
     *
     * @param serverCommandInfo 指令管理
     * @return 结果
     */
    public int updateServerCommandInfo(ServerCommandInfo serverCommandInfo);

    /**
     * 删除指令管理
     *
     * @param id 指令管理主键
     * @return 结果
     */
    public int deleteServerCommandInfoById(Long id);

    /**
     * 批量删除指令管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteServerCommandInfoByIds(Long[] ids);
}
