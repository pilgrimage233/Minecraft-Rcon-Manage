package cc.endmc.server.service.other;

import cc.endmc.server.domain.other.IpLimitInfo;

import java.util.List;

/**
 * IP限流Service接口
 *
 * @author ruoyi
 * @date 2024-03-22
 */
public interface IIpLimitInfoService {
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
     * 批量删除IP限流
     *
     * @param ids 需要删除的IP限流主键集合
     * @return 结果
     */
    public int deleteIpLimitInfoByIds(Long[] ids);

    /**
     * 删除IP限流信息
     *
     * @param id IP限流主键
     * @return 结果
     */
    public int deleteIpLimitInfoById(Long id);
}
