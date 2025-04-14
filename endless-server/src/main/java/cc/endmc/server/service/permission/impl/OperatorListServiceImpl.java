package cc.endmc.server.service.permission.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.domain.permission.OperatorList;
import cc.endmc.server.mapper.permission.OperatorListMapper;
import cc.endmc.server.service.permission.IOperatorListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理员名单Service业务层处理
 *
 * @author Memory
 * @date 2025-01-11
 */
@Service
public class OperatorListServiceImpl implements IOperatorListService {
    @Autowired
    private OperatorListMapper operatorListMapper;

    /**
     * 查询管理员名单
     *
     * @param id 管理员名单主键
     * @return 管理员名单
     */
    @Override
    public OperatorList selectOperatorListById(Long id) {
        return operatorListMapper.selectOperatorListById(id);
    }

    /**
     * 查询管理员名单列表
     *
     * @param operatorList 管理员名单
     * @return 管理员名单
     */
    @Override
    public List<OperatorList> selectOperatorListList(OperatorList operatorList) {
        return operatorListMapper.selectOperatorListList(operatorList);
    }

    /**
     * 新增管理员名单
     *
     * @param operatorList 管理员名单
     * @return 结果
     */
    @Override
    public int insertOperatorList(OperatorList operatorList) {
        operatorList.setCreateTime(DateUtils.getNowDate());
        return operatorListMapper.insertOperatorList(operatorList);
    }

    /**
     * 修改管理员名单
     *
     * @param operatorList 管理员名单
     * @return 结果
     */
    @Override
    public int updateOperatorList(OperatorList operatorList) {
        operatorList.setUpdateTime(DateUtils.getNowDate());
        return operatorListMapper.updateOperatorList(operatorList);
    }

    /**
     * 批量删除管理员名单
     *
     * @param ids 需要删除的管理员名单主键
     * @return 结果
     */
    @Override
    public int deleteOperatorListByIds(Long[] ids) {
        return operatorListMapper.deleteOperatorListByIds(ids);
    }

    /**
     * 删除管理员名单信息
     *
     * @param id 管理员名单主键
     * @return 结果
     */
    @Override
    public int deleteOperatorListById(Long id) {
        return operatorListMapper.deleteOperatorListById(id);
    }
}
