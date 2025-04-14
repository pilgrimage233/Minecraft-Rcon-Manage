package cc.endmc.server.service.server;

import cc.endmc.server.domain.server.PublicServerCommand;

import java.util.List;

/**
 * 公开命令Service接口
 *
 * @author ruoyi
 * @date 2024-05-08
 */
public interface IPublicServerCommandService {
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
     * 批量删除公开命令
     *
     * @param ids 需要删除的公开命令主键集合
     * @return 结果
     */
    public int deletePublicServerCommandByIds(Long[] ids);

    /**
     * 删除公开命令信息
     *
     * @param id 公开命令主键
     * @return 结果
     */
    public int deletePublicServerCommandById(Long id);
}
