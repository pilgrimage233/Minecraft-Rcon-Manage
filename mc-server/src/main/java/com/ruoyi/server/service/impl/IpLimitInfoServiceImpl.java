package com.ruoyi.server.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.server.domain.IpLimitInfo;
import com.ruoyi.server.mapper.IpLimitInfoMapper;
import com.ruoyi.server.service.IIpLimitInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * IP限流Service业务层处理
 *
 * @author ruoyi
 * @date 2024-03-22
 */
@Service
public class IpLimitInfoServiceImpl implements IIpLimitInfoService {
    @Autowired
    private IpLimitInfoMapper ipLimitInfoMapper;

    /**
     * 查询IP限流
     *
     * @param id IP限流主键
     * @return IP限流
     */
    @Override
    public IpLimitInfo selectIpLimitInfoById(Long id) {
        return ipLimitInfoMapper.selectIpLimitInfoById(id);
    }

    /**
     * 查询IP限流列表
     *
     * @param ipLimitInfo IP限流
     * @return IP限流
     */
    @Override
    public List<IpLimitInfo> selectIpLimitInfoList(IpLimitInfo ipLimitInfo) {
        return ipLimitInfoMapper.selectIpLimitInfoList(ipLimitInfo);
    }

    /**
     * 新增IP限流
     *
     * @param ipLimitInfo IP限流
     * @return 结果
     */
    @Override
    public int insertIpLimitInfo(IpLimitInfo ipLimitInfo) {
        ipLimitInfo.setCreateTime(DateUtils.getNowDate());
        return ipLimitInfoMapper.insertIpLimitInfo(ipLimitInfo);
    }

    /**
     * 修改IP限流
     *
     * @param ipLimitInfo IP限流
     * @return 结果
     */
    @Override
    public int updateIpLimitInfo(IpLimitInfo ipLimitInfo) {
        ipLimitInfo.setUpdateTime(DateUtils.getNowDate());
        return ipLimitInfoMapper.updateIpLimitInfo(ipLimitInfo);
    }

    /**
     * 批量删除IP限流
     *
     * @param ids 需要删除的IP限流主键
     * @return 结果
     */
    @Override
    public int deleteIpLimitInfoByIds(Long[] ids) {
        return ipLimitInfoMapper.deleteIpLimitInfoByIds(ids);
    }

    /**
     * 删除IP限流信息
     *
     * @param id IP限流主键
     * @return 结果
     */
    @Override
    public int deleteIpLimitInfoById(Long id) {
        return ipLimitInfoMapper.deleteIpLimitInfoById(id);
    }
}
