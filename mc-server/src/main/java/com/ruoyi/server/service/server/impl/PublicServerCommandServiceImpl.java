package com.ruoyi.server.service.server.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.server.domain.server.PublicServerCommand;
import com.ruoyi.server.mapper.server.PublicServerCommandMapper;
import com.ruoyi.server.service.server.IPublicServerCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公开命令Service业务层处理
 *
 * @author ruoyi
 * @date 2024-05-08
 */
@Service
public class PublicServerCommandServiceImpl implements IPublicServerCommandService {
    @Autowired
    private PublicServerCommandMapper publicServerCommandMapper;

    /**
     * 查询公开命令
     *
     * @param id 公开命令主键
     * @return 公开命令
     */
    @Override
    public PublicServerCommand selectPublicServerCommandById(Long id) {
        return publicServerCommandMapper.selectPublicServerCommandById(id);
    }

    /**
     * 查询公开命令列表
     *
     * @param publicServerCommand 公开命令
     * @return 公开命令
     */
    @Override
    public List<PublicServerCommand> selectPublicServerCommandList(PublicServerCommand publicServerCommand) {
        return publicServerCommandMapper.selectPublicServerCommandList(publicServerCommand);
    }

    /**
     * 新增公开命令
     *
     * @param publicServerCommand 公开命令
     * @return 结果
     */
    @Override
    public int insertPublicServerCommand(PublicServerCommand publicServerCommand) {
        publicServerCommand.setCreateTime(DateUtils.getNowDate());
        return publicServerCommandMapper.insertPublicServerCommand(publicServerCommand);
    }

    /**
     * 修改公开命令
     *
     * @param publicServerCommand 公开命令
     * @return 结果
     */
    @Override
    public int updatePublicServerCommand(PublicServerCommand publicServerCommand) {
        publicServerCommand.setUpdateTime(DateUtils.getNowDate());
        return publicServerCommandMapper.updatePublicServerCommand(publicServerCommand);
    }

    /**
     * 批量删除公开命令
     *
     * @param ids 需要删除的公开命令主键
     * @return 结果
     */
    @Override
    public int deletePublicServerCommandByIds(Long[] ids) {
        return publicServerCommandMapper.deletePublicServerCommandByIds(ids);
    }

    /**
     * 删除公开命令信息
     *
     * @param id 公开命令主键
     * @return 结果
     */
    @Override
    public int deletePublicServerCommandById(Long id) {
        return publicServerCommandMapper.deletePublicServerCommandById(id);
    }
}
