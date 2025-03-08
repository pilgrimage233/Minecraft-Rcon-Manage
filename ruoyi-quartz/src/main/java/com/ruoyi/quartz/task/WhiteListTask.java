package com.ruoyi.quartz.task;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.server.common.MapCache;
import com.ruoyi.server.common.constant.Command;
import com.ruoyi.server.common.service.EmailService;
import com.ruoyi.server.common.service.RconService;
import com.ruoyi.server.domain.permission.WhitelistInfo;
import com.ruoyi.server.service.permission.IWhitelistInfoService;
import com.ruoyi.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务调度
 * 白名单同步
 * 作者：Memory
 */
@Slf4j
@Component("whiteListTask")
public class WhiteListTask {

    @Autowired
    private IWhitelistInfoService whitelistInfoService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private EmailService pushEmail;

    @Autowired
    private RconService rconService;


    /**
     * 定时任务调度
     * 白名单同步
     */
    public void polling() {
        // 查询未审核白名单
        WhitelistInfo whitelistInfo = new WhitelistInfo();
        whitelistInfo.setStatus("0");
        List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);
        if (whitelistInfos.isEmpty()) {
            return;
        }
        List<SysUser> list;
        // 缓存用户数据
        if (redisCache.getCacheObject("adminUserList") == null) {
            // 查询管理员用户邮箱
            list = userService.selectUserList(new SysUser());
            // 缓存1天
            redisCache.setCacheObject("adminUserList", list, 1, TimeUnit.DAYS);
        } else {
            list = redisCache.getCacheObject("adminUserList");
        }
        // 发邮件通知
        if (list != null && !list.isEmpty()) {
            for (SysUser sysUser : list) {
                try {
                    if (sysUser.getEmail() != null && !sysUser.getEmail().isEmpty()) {
                        pushEmail.push(sysUser.getEmail(), "白名单审核", "有新的白名单需要审核");
                    }
                } catch (Exception e) {
                    log.error("邮件发送失败：" + sysUser.getEmail() + " " + StringUtils.format("问题原因: {}", e.getMessage()));
                }
            }
        }
    }

    /**
     * 同步白名单
     *
     * @param serverId 服务器ID
     */
    // @SuppressWarnings("all")
    public void syncWhitelistByServerId(String serverId) throws InterruptedException {
        log.debug("开始同步白名单：{}", serverId);

        if (serverId == null || serverId.isEmpty()) {
            log.error("服务器ID为空");
            return;
        }

        if (!MapCache.containsKey(serverId)) {
            log.error("服务器未连接：{}", serverId);
            return;
        }

        // // 获取指令信息
        // Map<String, ServerCommandInfo> map = null;
        // if (ObjectCache.containsKey("serverCommandInfo")) {
        //     // 从缓存中获取指令信息
        //     map = ObjectCache.getCommandInfo();
        // } else {
        //     log.error("缓存中不存在指令信息");
        //     return;
        // }
        // ServerCommandInfo commandInfo = null;
        // if (map != null && map.containsKey(serverId)) {
        //     // 从缓存中获取指令信息
        //     commandInfo = map.get(serverId);
        //     if (commandInfo == null) {
        //         log.error("缓存中不存在服务器:[{}]的指令信息", serverId);
        //         return;
        //     }
        // }

        // 查询已通过审核的白名单
        WhitelistInfo whitelistInfo = new WhitelistInfo();
        whitelistInfo.setStatus("1");
        // whitelistInfo.setServers(serverId);
        List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);
        if (whitelistInfos.isEmpty()) {
            return;
        }

        List<WhitelistInfo> users = new ArrayList<>();
        for (WhitelistInfo info : whitelistInfos) {
            // log.debug("已通过审核的白名单：" + info.getUserName());
            if (info.getServers() == null || info.getServers().isEmpty()) {
                continue;
            }
            String[] split = info.getServers().split(",");
            if (Arrays.asList(split).contains("all") || Arrays.asList(split).contains(serverId)) {
                users.add(info);
            }
        }

        // 查询对应服务器现有白名单列表
        String list = MapCache.get(serverId).sendCommand(Command.WHITELIST_LIST);
        log.debug("现有白名单列表：{}", list);
        Set<String> online = new HashSet<>();
        Set<String> offline = new HashSet<>();
        if (StringUtils.isNotEmpty(list) && list.contains("There are")) {
            // 正版玩家
            online = new HashSet<>(Arrays.asList(list.split("whitelisted player\\(s\\):")[1].trim().split(", ")));
            // 离线转小写
            offline = new HashSet<>(Arrays.asList(list.split("whitelisted player\\(s\\):")[1].trim().toLowerCase().split(", ")));

        }

        // 待同步用户
        List<WhitelistInfo> newList = new ArrayList<>();
        for (WhitelistInfo info : users) {
            if (info.getOnlineFlag() == 1L) {
                if (!online.contains(info.getUserName())) {
                    newList.add(info);
                }
            } else {
                if (!offline.contains(info.getUserName().toLowerCase())) {
                    newList.add(info);
                }
            }
        }

        // 同步白名单
        List<String> user = new ArrayList<>();
        for (WhitelistInfo info : newList) {
            // 防止白名单过多，延迟0.5秒
            if (newList.size() >= 5) {
                Thread.sleep(500);
            }
            rconService.sendCommand(serverId, String.format(Command.WHITELIST_ADD, info.getUserName()), info.getOnlineFlag() == 1L);
            user.add(info.getUserName());
        }

        // 如果服务器白名单不在数据库中，则移除
        List<String> remove = new ArrayList<>();

        // 重新获取现有白名单列表
        list = MapCache.get(serverId).sendCommand(Command.WHITELIST_LIST);
        if (StringUtils.isNotEmpty(list) && list.contains("There are")) {
            // 正版玩家
            online = new HashSet<>(Arrays.asList(list.split("whitelisted player\\(s\\):")[1].trim().split(", ")));
        }
        for (String s : online) {
            boolean flag = false;
            boolean onlineFlag = false;
            for (WhitelistInfo info : users) {
                // 正版玩家不忽略大小写
                if (info.getOnlineFlag() == 1L && s.equals(info.getUserName())) {
                    flag = true;
                    onlineFlag = true;
                    break;
                } else if (s.equalsIgnoreCase(info.getUserName())) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                rconService.sendCommand(serverId, String.format(Command.WHITELIST_REMOVE, s), onlineFlag);
                remove.add(s);
            }
        }
        log.debug("同步白名单成功：{}，新增白名单：{}，移除白名单：{}", serverId, user, remove);
    }
}
