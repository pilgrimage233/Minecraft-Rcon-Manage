package com.ruoyi.server.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.server.domain.BanlistInfo;
import com.ruoyi.server.mapper.BanlistInfoMapper;
import com.ruoyi.server.service.IBanlistInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 封禁管理Service业务层处理
 *
 * @author ruoyi
 * @date 2024-03-28
 */
@Service
public class BanlistInfoServiceImpl implements IBanlistInfoService {
    @Autowired
    private BanlistInfoMapper banlistInfoMapper;

    /**
     * 查询封禁管理
     *
     * @param id 封禁管理主键
     * @return 封禁管理
     */
    @Override
    public BanlistInfo selectBanlistInfoById(Long id) {
        return banlistInfoMapper.selectBanlistInfoById(id);
    }

    /**
     * 查询封禁管理列表
     *
     * @param banlistInfo 封禁管理
     * @return 封禁管理
     */
    @Override
    public List<BanlistInfo> selectBanlistInfoList(BanlistInfo banlistInfo) {
        return banlistInfoMapper.selectBanlistInfoList(banlistInfo);
    }

    /**
     * 新增封禁管理
     *
     * @param banlistInfo 封禁管理
     * @return 结果
     */
    @Override
    public int insertBanlistInfo(BanlistInfo banlistInfo) {
        banlistInfo.setCreateTime(DateUtils.getNowDate());
        return banlistInfoMapper.insertBanlistInfo(banlistInfo);
    }

    /**
     * 修改封禁管理
     *
     * @param banlistInfo 封禁管理
     * @return 结果
     */
    @Override
    public int updateBanlistInfo(BanlistInfo banlistInfo) {
        banlistInfo.setUpdateTime(DateUtils.getNowDate());
        // 解封状态
       /* if (banlistInfo.getState() == 0) {
            RconUtil.sendCommand("all",RconUtil.replaceCommand("all", "pardon %s", banlistInfo.getUserName()));
        }*/
        return banlistInfoMapper.updateBanlistInfo(banlistInfo);
    }

    /**
     * 批量删除封禁管理
     *
     * @param ids 需要删除的封禁管理主键
     * @return 结果
     */
    @Override
    public int deleteBanlistInfoByIds(Long[] ids) {
        return banlistInfoMapper.deleteBanlistInfoByIds(ids);
    }

    /**
     * 删除封禁管理信息
     *
     * @param id 封禁管理主键
     * @return 结果
     */
    @Override
    public int deleteBanlistInfoById(Long id) {
        return banlistInfoMapper.deleteBanlistInfoById(id);
    }
}
