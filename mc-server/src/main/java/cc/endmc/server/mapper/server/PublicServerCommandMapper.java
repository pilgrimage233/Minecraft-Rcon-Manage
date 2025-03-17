package cc.endmc.server.mapper.server;

import cc.endmc.server.domain.server.PublicServerCommand;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 公开命令Mapper接口
 *
 * @author ruoyi
 * @date 2024-05-08
 */
@Mapper
public interface PublicServerCommandMapper {
    /**
     * 查询公开命令
     *
     * @param id 公开命令主键
     * @return 公开命令
     */
    public PublicServerCommand selectPublicServerCommandById(Long id);

    /**
     * 查询公开命令列表
     *
     * @param publicServerCommand 公开命令
     * @return 公开命令集合
     */
    public List<PublicServerCommand> selectPublicServerCommandList(PublicServerCommand publicServerCommand);

    /**
     * 新增公开命令
     *
     * @param publicServerCommand 公开命令
     * @return 结果
     */
    public int insertPublicServerCommand(PublicServerCommand publicServerCommand);

    /**
     * 修改公开命令
     *
     * @param publicServerCommand 公开命令
     * @return 结果
     */
    public int updatePublicServerCommand(PublicServerCommand publicServerCommand);

    /**
     * 删除公开命令
     *
     * @param id 公开命令主键
     * @return 结果
     */
    public int deletePublicServerCommandById(Long id);

    /**
     * 批量删除公开命令
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePublicServerCommandByIds(Long[] ids);
}
