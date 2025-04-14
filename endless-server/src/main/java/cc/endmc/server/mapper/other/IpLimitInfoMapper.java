package cc.endmc.server.mapper.other;

import cc.endmc.server.domain.other.IpLimitInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IP限流Mapper接口
 *
 * @author ruoyi
 * @date 2024-03-22
 */
@Mapper
public interface IpLimitInfoMapper {
    /**
     * 查询IP限流
     *
     * @param id IP限流主键
     * @return IP限流
     */
    public IpLimitInfo selectIpLimitInfoById(Long id);

    /**
     * 查询IP限流列表
     *
     * @param ipLimitInfo IP限流
     * @return IP限流集合
     */
    public List<IpLimitInfo> selectIpLimitInfoList(IpLimitInfo ipLimitInfo);

    /**
     * 新增IP限流
     *
     * @param ipLimitInfo IP限流
     * @return 结果
     */
    public int insertIpLimitInfo(IpLimitInfo ipLimitInfo);

    /**
     * 修改IP限流
     *
     * @param ipLimitInfo IP限流
     * @return 结果
     */
    public int updateIpLimitInfo(IpLimitInfo ipLimitInfo);

    /**
     * 删除IP限流
     *
     * @param id IP限流主键
     * @return 结果
     */
    public int deleteIpLimitInfoById(Long id);

    /**
     * 批量删除IP限流
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteIpLimitInfoByIds(Long[] ids);
}
