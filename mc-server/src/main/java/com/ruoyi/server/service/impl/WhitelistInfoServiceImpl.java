package com.ruoyi.server.service.impl;

import com.github.t9t.minecraftrconclient.RconClient;
import com.ruoyi.server.async.AsyncManager;
import com.ruoyi.server.common.EmailTemplate;
import com.ruoyi.server.common.MapCache;
import com.ruoyi.server.common.PushEmail;
import com.ruoyi.server.common.RconUtil;
import com.ruoyi.server.domain.BanlistInfo;
import com.ruoyi.server.domain.WhitelistInfo;
import com.ruoyi.server.mapper.WhitelistInfoMapper;
import com.ruoyi.server.service.IBanlistInfoService;
import com.ruoyi.server.service.IWhitelistInfoService;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

/**
 * 白名单Service业务层处理
 *
 * @author ruoyi
 * @date 2023-12-26
 */
@Service
public class WhitelistInfoServiceImpl implements IWhitelistInfoService {
    private static final Log log = LogFactory.getLog(WhitelistInfoServiceImpl.class);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // 异步执行器
    private final AsyncManager asyncManager = AsyncManager.getInstance();
    @Autowired
    private WhitelistInfoMapper whitelistInfoMapper;
    @Autowired
    private IBanlistInfoService banlistInfoService;
    @Autowired
    private PushEmail pushEmail;

    /**
     * 查询白名单
     *
     * @param id 白名单主键
     * @return 白名单
     */
    @Override
    public WhitelistInfo selectWhitelistInfoById(Long id) {
        // if (whitelistInfo.getServers() != null) {
        //     // whitelistInfo.getServers().split(",") 转Long数组
        //     List<String> collect = Arrays.stream(whitelistInfo.getServers().split(",")).collect(Collectors.toList());
        //     collect.remove("all");
        //     Long[] ids = new Long[collect.size()];
        //     for (String s : collect) {
        //         ids[collect.indexOf(s)] = Long.parseLong(s);
        //     }
        //     List<String> name = serverInfoMapper.selectServerNameByIds(ids);
        //     // name用，分割用于前端展示
        //     whitelistInfo.setServers(String.join(",", name));
        // }

        // 查询有无封禁记录
        BanlistInfo banlistInfo = new BanlistInfo();
        banlistInfo.setWhiteId(id);
        List<BanlistInfo> banlistInfos = banlistInfoService.selectBanlistInfoList(banlistInfo);
        if (!banlistInfos.isEmpty()) {
            banlistInfo = banlistInfos.get(0);
            if (banlistInfo.getState() == 1) {
                WhitelistInfo whitelistInfo = whitelistInfoMapper.selectWhitelistInfoById(id);
                whitelistInfo.setBanFlag("true");
                whitelistInfo.setBannedReason(banlistInfo.getReason());
                return whitelistInfo;
            }
        }

        return whitelistInfoMapper.selectWhitelistInfoById(id);
    }

    /**
     * 查询白名单列表
     *
     * @param whitelistInfo 白名单
     * @return 白名单
     */
    @Override
    public List<WhitelistInfo> selectWhitelistInfoList(WhitelistInfo whitelistInfo) {
        return whitelistInfoMapper.selectWhitelistInfoList(whitelistInfo);
    }

    /**
     * 新增白名单
     *
     * @param whitelistInfo 白名单
     * @return 结果
     */
    @Override
    public int insertWhitelistInfo(WhitelistInfo whitelistInfo) {
        return whitelistInfoMapper.insertWhitelistInfo(whitelistInfo);
    }

    /**
     * 修改白名单
     *
     * @param whitelistInfo 白名单
     * @return 结果
     */
    @Override
    public int updateWhitelistInfo(WhitelistInfo whitelistInfo) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        if (whitelistInfo.getAddState().isEmpty()) {
            return 0;
        }

        if (whitelistInfo.getStatus().isEmpty()) {
            return 0;
        }

        // 全局封禁
        if (Boolean.parseBoolean(whitelistInfo.getBanFlag())) {
            whitelistInfo.setAddState("9"); // 如果全局封禁则将状态改为9
            whitelistInfo.setStatus("0");
            whitelistInfo.setRemoveTime(new Date());
            whitelistInfo.setReviewUsers(name);
            whitelistInfo.setRemoveReason(whitelistInfo.getBannedReason()); // 全局封禁原因

            try {
                sendCommand(whitelistInfo, "ban_add " + whitelistInfo.getUserName(), whitelistInfo.getOnlineFlag() == 1);
                // 全局广播
                sendCommand(whitelistInfo, "say §4[全局封禁] §c" + whitelistInfo.getUserName() + " §4已被全局封禁,原因: §c[" + whitelistInfo.getBannedReason() + "] §4审核人: §c" + name, true);

                try {
                    pushEmail.push(whitelistInfo.getQqNum().trim() + "@qq.com", EmailTemplate.TITLE,
                            "用户: " + whitelistInfo.getUserName() + " 已被全局封禁,原因: [" + whitelistInfo.getBannedReason() + "] 审核人: " + name);
                } catch (Exception e) {
                    log.error("发送邮件失败,请联系管理员!");
                    return 0;
                }
            } catch (Exception e) {
                log.error("全局封禁失败,请联系管理员!");
                return 0;
            }

            // 查询是否有封禁记录
            BanlistInfo banlistInfo = new BanlistInfo();
            banlistInfo.setWhiteId(whitelistInfo.getId());
            List<BanlistInfo> banlistInfos = banlistInfoService.selectBanlistInfoList(banlistInfo);
            if (!banlistInfos.isEmpty()) {
                banlistInfo = banlistInfos.get(0);
                if (banlistInfo.getState() == 0) {
                    banlistInfo.setState(1L);
                    banlistInfo.setUpdateBy(name);
                    banlistInfo.setUpdateTime(new Date());
                    banlistInfoService.updateBanlistInfo(banlistInfo);
                }
            } else {
                // 如果没有封禁记录则新增封禁记录
                banlistInfo.setReason(whitelistInfo.getBannedReason());
                banlistInfo.setUserName(whitelistInfo.getUserName());
                banlistInfo.setState(1L);
                banlistInfo.setCreateBy(name);
                banlistInfo.setCreateTime(new Date());
                banlistInfoService.insertBanlistInfo(banlistInfo);
            }
        } else {
            // 是否为解除封禁
            BanlistInfo banlistInfo = new BanlistInfo();
            banlistInfo.setWhiteId(whitelistInfo.getId());
            List<BanlistInfo> banlistInfos = banlistInfoService.selectBanlistInfoList(banlistInfo);
            if (!banlistInfos.isEmpty()) {
                banlistInfo = banlistInfos.get(0);
                // 如果isBanned为false并且封禁列表状态为1，则解除封禁
                if (banlistInfo.getState() == 1) {
                    try {
                        sendCommand(whitelistInfo, "ban_remove " + whitelistInfo.getUserName(), whitelistInfo.getOnlineFlag() == 1);
                        try {
                            pushEmail.push(whitelistInfo.getQqNum().trim() + "@qq.com", EmailTemplate.TITLE,
                                    "用户: " + whitelistInfo.getUserName() + " 的全局封禁已于 " + dateFormat.format(new Date()) + " 日被解除,审核人: " + name);
                        } catch (Exception e) {
                            log.error("发送邮件失败,请联系管理员!");
                            return 0;
                        }
                    } catch (Exception e) {
                        log.error("解除全局封禁失败,请联系管理员!");
                        return 0;
                    }
                    banlistInfo.setState(0L);
                    banlistInfo.setUpdateBy(name);
                    banlistInfo.setUpdateTime(new Date());
                    banlistInfoService.updateBanlistInfo(banlistInfo);
                }
                if (!whitelistInfo.getStatus().equals("1")) {
                    whitelistInfo.setAddState("0");
                }
            }
        }

        // 全局移除白名单
        if (Boolean.parseBoolean(whitelistInfo.getAddState())) {
            whitelistInfo.setAddState("2");
            whitelistInfo.setStatus("0");
            whitelistInfo.setRemoveTime(new Date());
            whitelistInfo.setReviewUsers(name);
            try {
                // 根据在线添加标识判断是发送在线移除命令还是离线移除命令
                sendCommand(whitelistInfo, "white_remove " + whitelistInfo.getUserName(), whitelistInfo.getOnlineFlag() == 1);
                try {
                    pushEmail.push(whitelistInfo.getQqNum().trim() + "@qq.com", EmailTemplate.TITLE,
                            "用户: " + whitelistInfo.getUserName() + " 的白名单申请已于 " + dateFormat.format(new Date()) + " 日被移除,原因: [" + whitelistInfo.getRemoveReason() + "] 审核人: " + name);
                } catch (Exception e) {
                    log.error("发送邮件失败,请联系管理员!");
                    return 0;
                }
            } catch (Exception e) {
                whitelistInfo.setAddState("0"); // 如果移除失败则将状态改为1
                log.error("移除白名单失败,请联系管理员!");
                return 0;
            }
        }

        // 添加白名单
        if (whitelistInfo.getStatus().equals("1")) {
            // 如果在线添加标识不为1，则发送离线添加命令
            if (whitelistInfo.getOnlineFlag() != 1) {
                try {
                    sendCommand(whitelistInfo, "auth addToForcedOffline " + whitelistInfo.getUserName().toLowerCase(), false);
                    sendCommand(whitelistInfo, "white_add " + whitelistInfo.getUserName(), false);
                } catch (Exception e) {
                    whitelistInfo.setAddState("0");
                    log.error("添加离线失败,请联系管理员!");
                    return 0;
                }
            } else {
                // 如果在线添加标识为1，则发送在线添加命令
                try {
                    sendCommand(whitelistInfo, "white_add " + whitelistInfo.getUserName(), true);
                } catch (Exception e) {
                    whitelistInfo.setAddState("0");
                    log.error("添加白名单失败,请联系管理员!");
                    return 0;
                }
            }
            whitelistInfo.setReviewUsers(name); // 设置审核人
            try {
                pushEmail.push(whitelistInfo.getQqNum().trim() + "@qq.com", EmailTemplate.TITLE,
                        "用户: " + whitelistInfo.getUserName() + " 的白名单申请已于 " + dateFormat.format(new Date()) + " 日通过审核,审核人: " + name);
            } catch (Exception e) {
                log.error("发送邮件失败,请联系管理员!");
                return 0;
            }

            whitelistInfo.setAddState("1");
            whitelistInfo.setAddTime(new Date());
        } else if (whitelistInfo.getStatus().equals("2")) {

            whitelistInfo.setReviewUsers(name);
            try {
                pushEmail.push(whitelistInfo.getQqNum().trim() + "@qq.com", EmailTemplate.TITLE,
                        "用户: " + whitelistInfo.getUserName() + " 的白名单申请已于 " + dateFormat.format(new Date()) + " 日被拒绝,审核人: " + name);
            } catch (Exception e) {
                log.error("发送邮件失败,请联系管理员!");
                return 0;
            }

        }
        return whitelistInfoMapper.updateWhitelistInfo(whitelistInfo);
    }

    /**
     * 批量删除白名单
     *
     * @param ids 需要删除的白名单主键
     * @return 结果
     */
    @Override
    public int deleteWhitelistInfoByIds(Long[] ids) {
        return whitelistInfoMapper.deleteWhitelistInfoByIds(ids);
    }

    /**
     * 删除白名单信息
     *
     * @param id 白名单主键
     * @return 结果
     */
    @Override
    public int deleteWhitelistInfoById(Long id) {
        return whitelistInfoMapper.deleteWhitelistInfoById(id);
    }

    /**
     * 查重
     *
     * @param whitelistInfo
     * @return
     */
    @Override
    public List<WhitelistInfo> checkRepeat(WhitelistInfo whitelistInfo) {
        return whitelistInfoMapper.checkRepeat(whitelistInfo);
    }

    /**
     * 发送Rcon命令
     *
     * @param command
     */
    private void sendCommand(WhitelistInfo info, String command, boolean onlineFlag) {

        if (info.getServers() == null) {
            return;
        }

        // 包含all则发送给所有服务器
        if (info.getServers().contains("all")) {
            final Map<String, RconClient> map = MapCache.getMap();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // 发送Rcon命令给所有服务器
                    for (String key : map.keySet()) {
                        RconUtil.sendCommand(key, RconUtil.replaceCommand(key, command, onlineFlag));
                    }
                }
            };
            asyncManager.execute(task);
        } else { // 发送给指定服务器
            for (String key : info.getServers().split(",")) {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        RconUtil.sendCommand(key, RconUtil.replaceCommand(key, command, onlineFlag));
                    }
                };
                asyncManager.execute(task);
            }
        }
    }
}
