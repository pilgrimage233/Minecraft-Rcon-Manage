package com.ruoyi.server.service.permission;

import com.ruoyi.server.domain.permission.OperatorList;

import java.util.List;

/**
 * 管理员名单Service接口
 *
 * @author Memory
 * @date 2025-01-11
 */
public interface IOperatorListService {
    /**
     * 查询管理员名单
     *
     * @param id 管理员名单主键
     * @return 管理员名单
     */
    public OperatorList selectOperatorListById(Long id);

    /**
     * 查询管理员名单列表
     *
     * @param operatorList 管理员名单
     * @return 管理员名单集合
     */
    public List<OperatorList> selectOperatorListList(OperatorList operatorList);

    /**
     * 新增管理员名单
     *
     * @param operatorList 管理员名单
     * @return 结果
     */
    public int insertOperatorList(OperatorList operatorList);

    /**
     * 修改管理员名单
     *
     * @param operatorList 管理员名单
     * @return 结果
     */
    public int updateOperatorList(OperatorList operatorList);

    /**
     * 批量删除管理员名单
     *
     * @param ids 需要删除的管理员名单主键集合
     * @return 结果
     */
    public int deleteOperatorListByIds(Long[] ids);

    /**
     * 删除管理员名单信息
     *
     * @param id 管理员名单主键
     * @return 结果
     */
    public int deleteOperatorListById(Long id);
}
