package cc.endmc.server.service.permission.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.common.constant.Command;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.permission.BanlistInfo;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.mapper.permission.BanlistInfoMapper;
import cc.endmc.server.mapper.permission.WhitelistInfoMapper;
import cc.endmc.server.service.permission.IBanlistInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 封禁管理Service业务层处理
 *
 * @author ruoyi
 * @date 2024-03-28
 */
@Service
@RequiredArgsConstructor
public class BanlistInfoServiceImpl implements IBanlistInfoService {

    private final BanlistInfoMapper banlistInfoMapper;
    private final WhitelistInfoMapper whitelistInfoMapper;
    private final RconService rconService;

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
    public int updateBanlistInfo(BanlistInfo banlistInfo, boolean flag) {
        banlistInfo.setUpdateTime(DateUtils.getNowDate());
        if (flag) {
            final WhitelistInfo whitelistInfo = whitelistInfoMapper.selectWhitelistInfoById(banlistInfo.getWhiteId());
            if (banlistInfo.getState() == 1) {
                if (whitelistInfo != null) {
                    // 封禁
                    rconService.sendCommand("all", String.format(Command.BAN_ADD, whitelistInfo.getUserName()),
                            whitelistInfo.getOnlineFlag() == 1);
                    // 移除
                    rconService.sendCommand("all", String.format(Command.WHITELIST_REMOVE, whitelistInfo.getUserName()),
                            whitelistInfo.getOnlineFlag() == 1);

                    // 更新白名单状态
                    whitelistInfo.setStatus("0");
                    whitelistInfo.setAddState("9");
                    whitelistInfo.setUpdateBy(banlistInfo.getUpdateBy());
                    whitelistInfo.setUpdateTime(DateUtils.getNowDate());
                    whitelistInfoMapper.updateWhitelistInfo(whitelistInfo);
                }
            } else {
                if (whitelistInfo != null) {
                    // 发送解封命令
                    rconService.sendCommand("all", String.format(Command.BAN_REMOVE, whitelistInfo.getUserName()),
                            whitelistInfo.getOnlineFlag() == 1);

                    // 更新白名单状态
                    whitelistInfo.setStatus("0");
                    whitelistInfo.setAddState("0");
                    whitelistInfo.setUpdateBy(banlistInfo.getUpdateBy());
                    whitelistInfo.setUpdateTime(DateUtils.getNowDate());
                    whitelistInfoMapper.updateWhitelistInfo(whitelistInfo);
                }
            }
        }
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
