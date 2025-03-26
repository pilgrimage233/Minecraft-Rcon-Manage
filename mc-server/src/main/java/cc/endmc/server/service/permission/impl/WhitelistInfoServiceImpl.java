package cc.endmc.server.service.permission.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.async.AsyncManager;
import cc.endmc.server.common.EmailTemplates;
import cc.endmc.server.common.constant.Command;
import cc.endmc.server.common.service.EmailService;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.permission.BanlistInfo;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.player.PlayerDetails;
import cc.endmc.server.domain.server.ServerInfo;
import cc.endmc.server.enums.Identity;
import cc.endmc.server.mapper.permission.WhitelistInfoMapper;
import cc.endmc.server.service.permission.IBanlistInfoService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.service.player.IPlayerDetailsService;
import cc.endmc.server.service.server.IServerInfoService;
import com.alibaba.fastjson2.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 白名单Service业务层处理
 *
 * @author ruoyi
 * @date 2023-12-26
 */
@Slf4j
@Service
public class WhitelistInfoServiceImpl implements IWhitelistInfoService {

    // 时间格式化
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 异步执行器
    private final AsyncManager asyncManager = AsyncManager.getInstance();

    @Autowired
    private WhitelistInfoMapper whitelistInfoMapper;

    @Autowired
    private IBanlistInfoService banlistInfoService;

    @Autowired
    private IServerInfoService serverInfoService;

    @Autowired
    private EmailService pushEmail;

    @Autowired
    private RconService rconService;

    @Autowired
    private IPlayerDetailsService playerDetailsService;

    @Value("${app-url}")
    private String appUrl;

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
     * 查询白名单
     *
     * @param ids 白名单主键集合
     * @return 白名单
     */
    @Override
    public List<WhitelistInfo> selectWhitelistInfoByIds(List<Long> ids) {
        return whitelistInfoMapper.selectWhitelistInfoByIds(ids);
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
     * 查询白名单列表（包含封禁信息）
     *
     * @param whitelistInfo 白名单
     * @return 白名单
     */
    @Override
    public List<WhitelistInfo> selectWhitelistInfoListWithBan(WhitelistInfo whitelistInfo) {
        return whitelistInfoMapper.selectWhitelistInfoListWithBan(whitelistInfo);
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
    @Transactional
    public int updateWhitelistInfo(WhitelistInfo whitelistInfo, String user) {
        String name;
        try {
            name = SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            name = user;
        }

        if (name == null && user == null) {
            log.error("获取用户信息失败,请联系管理员!");
        }

        if (whitelistInfo.getAddState().isEmpty() || whitelistInfo.getStatus().isEmpty()) {
            return 0;
        }

        // 已存在过审核的不重复发邮件
        boolean flag = true;
        WhitelistInfo old = new WhitelistInfo();
        old.setUserName(whitelistInfo.getUserName());
        final List<WhitelistInfo> whitelistInfos = selectWhitelistInfoListWithBan(old);
        if (!whitelistInfos.isEmpty()) {
            old = whitelistInfos.get(0);
            if (old.getStatus().equals("1") && whitelistInfo.getStatus().equals("1")) {
                flag = false;
            }
        }

        // 全局封禁
        if ("true".equalsIgnoreCase(whitelistInfo.getBanFlag())) {
            handleGlobalBan(whitelistInfo, name);
        } else if (old.getBanState() != null && old.getBanState() == 1) {
            // 如果存在封禁记录且状态为1，则解除封禁
            handleUnban(whitelistInfo, name, old);
        }

        // 全局移除白名单
        if ("true".equalsIgnoreCase(whitelistInfo.getAddState())) {
            handleWhitelistOperation(whitelistInfo, name);
        }

        switch (whitelistInfo.getStatus()) {
            // 添加白名单
            case "1":
                handleWhitelistAddition(whitelistInfo, flag, name);
                break;
            case "2":
                // 拒审并移除白名单（如果原先通过）
                handleWhitelistFailure(whitelistInfo, name, old);
                break;
        }
        return whitelistInfoMapper.updateWhitelistInfo(whitelistInfo);
    }

    /**
     * 拒审并移除白名单
     *
     * @param whitelistInfo 白名单信息
     * @param name          审核人
     * @param old           原白名单信息
     */
    private void handleWhitelistFailure(WhitelistInfo whitelistInfo, String name, WhitelistInfo old) {
        String emailTitle = EmailTemplates.FAIL_TITLE;
        String timeTittle = EmailTemplates.FAIL_TIME_TITTLE;
        whitelistInfo.setReviewUsers(name);
        // 如果原先通过，拒审则删除白名单
        if (old.getStatus().equals("1")) {
            emailTitle = EmailTemplates.REMOVE_TITLE;
            timeTittle = EmailTemplates.REMOVE_TIME_TITTLE;
            whitelistInfo.setAddState("2");
            whitelistInfo.setStatus("2");
            whitelistInfo.setRemoveTime(new Date());
            whitelistInfo.setReviewUsers(name);

            try {
                sendCommand(whitelistInfo, String.format(Command.WHITELIST_REMOVE, whitelistInfo.getUserName()), whitelistInfo.getOnlineFlag() == 1);
            } catch (Exception e) {
                throw new RuntimeException("移除白名单失败,请联系管理员!");
            }
        }

        String finalEmailTitle = emailTitle;
        String finalTimeTittle = timeTittle;
        asyncManager.execute(new TimerTask() {
            @SneakyThrows
            @Override
            public void run() {
                pushEmail.push(whitelistInfo.getQqNum().trim() + EmailTemplates.QQ_EMAIL,
                        finalEmailTitle,
                        EmailTemplates.getWhitelistNotificationBan(
                                whitelistInfo.getQqNum(),
                                whitelistInfo.getUserName(),
                                dateFormat.format(whitelistInfo.getAddTime() == null ? whitelistInfo.getCreateTime() : whitelistInfo.getAddTime()),
                                DateUtils.getTime(),
                                finalTimeTittle,
                                whitelistInfo.getRemoveReason(),
                                finalEmailTitle)
                );
            }
        });
    }

    /**
     * 解除封禁
     *
     * @param whitelistInfo 白名单信息
     * @param name          审核人
     * @param old           原白名单信息
     */
    private void handleUnban(WhitelistInfo whitelistInfo, String name, WhitelistInfo old) {
        try {
            sendCommand(whitelistInfo, String.format(Command.BAN_REMOVE, whitelistInfo.getUserName()), whitelistInfo.getOnlineFlag() == 1);

            asyncManager.execute(new TimerTask() {
                @SneakyThrows
                @Override
                public void run() {
                    pushEmail.push(whitelistInfo.getQqNum().trim() + EmailTemplates.QQ_EMAIL,
                            EmailTemplates.UN_BAN_TITLE,
                            EmailTemplates.getWhitelistNotificationUnBan(
                                    whitelistInfo.getQqNum(),
                                    whitelistInfo.getUserName(),
                                    dateFormat.format(old.getCreateTime()),
                                    DateUtils.getTime()
                            )
                    );
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("解除封禁失败,请联系管理员!");
        }

        // 更新封禁状态
        BanlistInfo banlistInfo = new BanlistInfo();
        banlistInfo.setId(old.getBanId());
        banlistInfo.setWhiteId(whitelistInfo.getId());
        banlistInfo.setState(0L);
        banlistInfo.setUpdateBy(name);
        banlistInfo.setUpdateTime(new Date());
        banlistInfoService.updateBanlistInfo(banlistInfo, false);

        if (!whitelistInfo.getStatus().equals("1")) {
            whitelistInfo.setAddState("0");
            whitelistInfo.setStatus("0");
        }
    }

    /**
     * 全局封禁
     *
     * @param whitelistInfo 白名单信息
     * @param name          审核人
     */
    private void handleGlobalBan(WhitelistInfo whitelistInfo, String name) {
        whitelistInfo.setAddState("9"); // 如果全局封禁则将状态改为9
        whitelistInfo.setStatus("0");
        whitelistInfo.setRemoveTime(new Date());
        whitelistInfo.setReviewUsers(name);
        whitelistInfo.setRemoveReason(whitelistInfo.getBannedReason()); // 全局封禁原因

        try {
            sendCommand(whitelistInfo, String.format(Command.BAN_ADD, whitelistInfo.getUserName()), whitelistInfo.getOnlineFlag() == 1);
            sendCommand(whitelistInfo, String.format(Command.WHITELIST_REMOVE, whitelistInfo.getUserName()), whitelistInfo.getOnlineFlag() == 1);
            // 全局广播，使用英文
            rconService.sendCommand("all", "broadcast &c" + whitelistInfo.getUserName() + " &7has been banned by &c" + name + "&7, reason: &c" + whitelistInfo.getBannedReason(), false);

            asyncManager.execute(new TimerTask() {
                @SneakyThrows
                @Override
                public void run() {
                    pushEmail.push(whitelistInfo.getQqNum().trim() + EmailTemplates.QQ_EMAIL,
                            EmailTemplates.BAN_TITLE,
                            EmailTemplates.getWhitelistNotificationBan(
                                    whitelistInfo.getQqNum(),
                                    whitelistInfo.getUserName(),
                                    dateFormat.format(whitelistInfo.getAddTime()),
                                    DateUtils.getTime(),
                                    EmailTemplates.BAN_TIME_TITTLE,
                                    whitelistInfo.getBannedReason(),
                                    EmailTemplates.BAN_TITLE)
                    );
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("全局封禁失败,请联系管理员!");
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
                banlistInfoService.updateBanlistInfo(banlistInfo, false);
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
    }

    /**
     * 移除白名单
     *
     * @param whitelistInfo 白名单信息
     * @param name          审核人
     */
    private void handleWhitelistOperation(WhitelistInfo whitelistInfo, String name) {
        whitelistInfo.setAddState("2");
        whitelistInfo.setStatus("0");
        whitelistInfo.setRemoveTime(new Date());
        whitelistInfo.setReviewUsers(name);
        try {
            sendCommand(whitelistInfo, String.format(Command.WHITELIST_REMOVE, whitelistInfo.getUserName()), whitelistInfo.getOnlineFlag() == 1);
            // 异步发送邮件
            asyncManager.execute(new TimerTask() {
                @SneakyThrows
                @Override
                public void run() {
                    pushEmail.push(whitelistInfo.getQqNum().trim() + EmailTemplates.QQ_EMAIL,
                            EmailTemplates.REMOVE_TITLE,
                            EmailTemplates.getWhitelistNotificationBan(
                                    whitelistInfo.getQqNum(),
                                    whitelistInfo.getUserName(),
                                    dateFormat.format(whitelistInfo.getAddTime()),
                                    DateUtils.getTime(),
                                    EmailTemplates.REMOVE_TIME_TITTLE,
                                    whitelistInfo.getRemoveReason(),
                                    EmailTemplates.REMOVE_TITLE)
                    );
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("移除白名单失败,请联系管理员!");
        }
    }

    /**
     * 添加白名单
     *
     * @param whitelistInfo 白名单信息
     * @param flag          是否发送邮件
     * @param name          审核人
     */
    private void handleWhitelistAddition(WhitelistInfo whitelistInfo, boolean flag, String name) {
        try {
            sendCommand(whitelistInfo, String.format(Command.WHITELIST_ADD, whitelistInfo.getUserName()), whitelistInfo.getOnlineFlag() == 1);
            if (whitelistInfo.getOnlineFlag() != 1) {
                sendCommand(whitelistInfo, "auth addToForcedOffline " + whitelistInfo.getUserName().toLowerCase(), false);
            }
        } catch (Exception e) {
            throw new RuntimeException("添加白名单失败,请联系管理员!");
        }
        // 详情关联ID
        PlayerDetails details = new PlayerDetails();
        details.setQq(whitelistInfo.getQqNum());
        final List<PlayerDetails> playerDetails = playerDetailsService.selectPlayerDetailsList(details);

        if (!playerDetails.isEmpty()) {
            details = playerDetails.get(0);
            details.setWhitelistId(whitelistInfo.getId());
            playerDetailsService.updatePlayerDetails(details, false);
        }

        if (flag) {
            List<Map<String, Object>> data = null;
            final String servers = whitelistInfo.getServers();

            if (!servers.contains("all")) {
                List<Long> ids = new ArrayList<>();
                for (String s : servers.split(",")) {
                    ids.add(Long.parseLong(s));
                }
                if (ids.size() <= 3) {
                    final List<ServerInfo> serverInfos = serverInfoService.selectServerInfoByIds(ids);

                    data = new ArrayList<>();
                    Map<String, Object> map = null;
                    if (!serverInfos.isEmpty()) {
                        for (ServerInfo serverInfo : serverInfos) {
                            map = new HashMap<>();
                            map.put("name", serverInfo.getNameTag());
                            map.put("serverAddress", serverInfo.getPlayAddress());
                            map.put("port", serverInfo.getPlayAddressPort());
                            map.put("core", serverInfo.getServerCore());
                            map.put("version", serverInfo.getServerVersion());
                            data.add(map);
                        }
                    }
                }
            }
            List<Map<String, Object>> finalData = data;
            asyncManager.execute(new TimerTask() {
                @SneakyThrows
                @Override
                public void run() {
                    pushEmail.push(whitelistInfo.getQqNum().trim() + EmailTemplates.QQ_EMAIL,
                            EmailTemplates.SUCCESS_TITLE,
                            EmailTemplates.getWhitelistNotification
                                    (whitelistInfo.getQqNum(),
                                            whitelistInfo.getUserName(),
                                            dateFormat.format(whitelistInfo.getTime()),
                                            DateUtils.getTime(), EmailTemplates.SUCCESS_TITLE,
                                            appUrl, finalData
                                    )
                    );
                }
            });
        }

        whitelistInfo.setReviewUsers(name); // 设置审核人
        whitelistInfo.setAddState("1");
        whitelistInfo.setAddTime(new Date());
    }

    /**
     * 批量删除白名单
     *
     * @param ids 需要删除的白名单主键
     * @return 结果
     */
    @Override
    public int deleteWhitelistInfoByIds(Long[] ids) {
        final PlayerDetails details = new PlayerDetails();
        // 删除玩家详情
        for (Long id : ids) {
            WhitelistInfo whitelistInfo = selectWhitelistInfoById(id);
            if (whitelistInfo == null) {
                return 0;
            }

            details.setQq(whitelistInfo.getQqNum());
            playerDetailsService.deletePlayerDetailsByInfo(details);

            // 尝试删除白名单
            rconService.sendCommand("all", String.format(Command.WHITELIST_REMOVE, whitelistInfo.getUserName()),
                    whitelistInfo.getOnlineFlag() == 1);
        }
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

    @Override
    public Map<String, Object> check(Map<String, String> params) {
        Map<String, Object> map = new LinkedHashMap<>();
        WhitelistInfo whitelistInfo = new WhitelistInfo();
        if (params.containsKey("id") && !params.get("id").isEmpty()) {
            whitelistInfo.setUserName(params.get("id").toLowerCase());
        }
        if (params.containsKey("qq") && !params.get("qq").isEmpty()) {
            whitelistInfo.setQqNum(params.get("qq"));
        }

        if (!checkRepeat(whitelistInfo).isEmpty()) {
            List<WhitelistInfo> whitelistInfos = checkRepeat(whitelistInfo);
            WhitelistInfo obj = whitelistInfos.get(0);

            map.put("游戏ID", obj.getUserName());
            map.put("QQ号", obj.getQqNum());
            // map.put("提交时间", dateFormat.format(obj.getAddTime()));  // 容余
            if (obj.getOnlineFlag() == 1) {
                map.put("账号类型", "正版");
            } else {
                map.put("账号类型", "离线");
            }

            PlayerDetails playerDetails = new PlayerDetails();
            playerDetails.setUserName(obj.getUserName().toLowerCase());
            final List<PlayerDetails> details = playerDetailsService.selectPlayerDetailsList(playerDetails);

            if (!details.isEmpty()) {
                playerDetails = details.get(0);
            }

            // if (playerDetails.getProvince() != null) {
            //     map.put("省份", playerDetails.getProvince());
            // }

            // 直辖市
            String[] directCity = {"北京市", "天津市", "上海市", "重庆市"};
            if (playerDetails.getCity() != null) {
                if (Arrays.asList(directCity).contains(playerDetails.getCity())) {
                    map.put("城市", playerDetails.getCity());
                } else {
                    map.put("城市", playerDetails.getProvince() + "-" + playerDetails.getCity());
                }
            }


            if (playerDetails.getIdentity() != null) {
                String identity;
                switch (playerDetails.getIdentity()) {
                    case "player":
                        identity = Identity.PLAYER.getDesc();
                        break;
                    case "operator":
                        identity = Identity.OPERATOR.getDesc();
                        break;
                    case "banned":
                        identity = Identity.BANNED.getDesc();
                        break;
                    default:
                        identity = Identity.OTHER.getDesc();
                        break;
                }
                map.put("身份", identity);
            }

            if (playerDetails.getLastOnlineTime() != null && playerDetails.getLastOfflineTime() != null) {
                // 在线时间和离线时间取最大的
                map.put("最后上线时间", playerDetails.getLastOnlineTime().getTime()
                        > playerDetails.getLastOfflineTime().getTime()
                        ? dateFormat.format(playerDetails.getLastOnlineTime())
                        : dateFormat.format(playerDetails.getLastOfflineTime()));
            } else if (playerDetails.getLastOnlineTime() != null) {
                map.put("最后上线时间", dateFormat.format(playerDetails.getLastOnlineTime()));
            }

            if (playerDetails.getGameTime() != null) {
                if (playerDetails.getGameTime() > 60) {
                    map.put("游戏时间", playerDetails.getGameTime() / 60 + "小时");
                } else {
                    map.put("游戏时间", playerDetails.getGameTime() + "分钟");
                }
            }

            if (StringUtils.isNotEmpty(playerDetails.getParameters())) {
                // 取历史名称
                final JSONObject jsonObject = JSONObject.parseObject(playerDetails.getParameters());
                if (jsonObject.containsKey("name_history")) {
                    map.put("历史名称", jsonObject.getJSONArray("name_history"));
                }
            }

            map.put("审核人", obj.getReviewUsers());
            // map.put("UUID", obj.getUserUuid());
            switch (obj.getAddState()) {
                case "1":
                    map.put("审核状态", "已通过");
                    map.put("审核时间", dateFormat.format(obj.getAddTime()));
                    break;
                case "2":
                    map.put("审核状态", "未通过/已移除");
                    map.put("移除时间", dateFormat.format(obj.getRemoveTime()));
                    map.put("移除原因", obj.getRemoveReason());
                    break;
                case "9":
                    map.put("审核状态", "已封禁");
                    map.put("封禁时间", dateFormat.format(obj.getRemoveTime()));
                    map.put("封禁原因", obj.getRemoveReason());
                    break;
                default:
                    map.put("审核状态", "待审核");
                    map.put("UUID", obj.getUserUuid());
                    break;
            }
        }
        return map;
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
            // 发送Rcon命令给所有服务器
            rconService.sendCommand("all", command, onlineFlag);
        } else { // 发送给指定服务器
            for (String key : info.getServers().split(",")) {
                rconService.sendCommand(key, command, onlineFlag);
            }
        }
    }
}
