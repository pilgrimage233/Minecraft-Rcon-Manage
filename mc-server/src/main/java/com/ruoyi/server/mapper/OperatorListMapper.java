package com.ruoyi.server.mapper;

import com.ruoyi.server.domain.OperatorList;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 管理员名单Mapper接口
 *
 * @author Memory
 * @date 2025-01-11
 */
@Mapper
public interface OperatorListMapper {
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
     * 删除管理员名单
     *
     * @param id 管理员名单主键
     * @return 结果
     */
    public int deleteOperatorListById(Long id);

    /**
     * 批量删除管理员名单
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOperatorListByIds(Long[] ids);

}
