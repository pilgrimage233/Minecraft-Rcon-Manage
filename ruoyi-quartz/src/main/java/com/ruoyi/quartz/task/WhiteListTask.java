package com.ruoyi.quartz.task;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.server.common.MapCache;
import com.ruoyi.server.common.ObjectCache;
import com.ruoyi.server.common.EmailService;
import com.ruoyi.server.domain.ServerCommandInfo;
import com.ruoyi.server.domain.WhitelistInfo;
import com.ruoyi.server.service.IWhitelistInfoService;
import com.ruoyi.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

        // 查询已通过审核的白名单
        WhitelistInfo whitelistInfo = new WhitelistInfo();
        whitelistInfo.setStatus("1");
        // whitelistInfo.setServers(serverId);
        List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);
        if (whitelistInfos.isEmpty()) {
            return;
        }

        List<WhitelistInfo> newList = new ArrayList<>();
        for (WhitelistInfo info : whitelistInfos) {
            // log.debug("已通过审核的白名单：" + info.getUserName());
            if (info.getServers() == null || info.getServers().isEmpty()) {
                continue;
            }
            String[] split = info.getServers().split(",");
            if (Arrays.asList(split).contains("all") || Arrays.asList(split).contains(serverId)) {
                newList.add(info);
            }
        }

        // 查询对应服务器现有白名单列表
        String list = MapCache.get(serverId).sendCommand("whitelist list");
        log.debug("现有白名单列表：{}", list);
        String[] split = new String[0];
        String[] split1 = new String[0];
        if (StringUtils.isNotEmpty(list) && list.contains("There are")) {
            String[] temp = list.split("whitelisted player\\(s\\):")[1].trim().split(", ");
            split = new String[temp.length];
            split1 = new String[temp.length];
            System.arraycopy(temp, 0, split1, 0, temp.length);
            for (int i = 0; i < temp.length; i++) {
                split[i] = temp[i].toLowerCase().trim();
            }
        }

        List<String> user = new ArrayList<>();
        List<String> remove = new ArrayList<>();

        // 获取指令信息
        Map<String, ServerCommandInfo> map = null;
        if (ObjectCache.containsKey("serverCommandInfo")) {
            // 从缓存中获取指令信息
            map = ObjectCache.getCommandInfo();
        } else {
            log.error("缓存中不存在指令信息");
            return;
        }
        ServerCommandInfo commandInfo = null;
        if (map != null && map.containsKey(serverId)) {
            // 从缓存中获取指令信息
            commandInfo = map.get(serverId);
            if (commandInfo == null) {
                log.error("缓存中不存在服务器:[{}]的指令信息", serverId);
                return;
            }
        }

        // 现有白名单列表比对已通过审核的白名单列表
        for (WhitelistInfo info : newList) {
            // 防止白名单过多，延迟0.8秒
            if (newList.size() >= 10) {
                Thread.sleep(800);
            }
            // 正版玩家不忽略大小写
            if (info.getOnlineFlag() == 1L) {
                if (!Arrays.asList(split1).contains(info.getUserName())) {
                    MapCache.get(serverId).sendCommand(commandInfo.getOnlineAddWhitelistCommand().replace("{player}", info.getUserName()));
                    user.add(info.getUserName());
                }
            } else {
                if (!Arrays.asList(split).contains(info.getUserName().toLowerCase())) {
                    MapCache.get(serverId).sendCommand(commandInfo.getOfflineAddWhitelistCommand().replace("{player}", info.getUserName()));
                    user.add(info.getUserName().toLowerCase());
                }
            }
        }

        // 如果服务器白名单不在数据库中，则移除
        for (String s : split1) {
            boolean flag = false;
            boolean onlineFlag = false;
            for (WhitelistInfo info : newList) {
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
                if (onlineFlag) {
                    MapCache.get(serverId).sendCommand(commandInfo.getOnlineRmWhitelistCommand().replace("{player}", s));
                } else {
                    MapCache.get(serverId).sendCommand(commandInfo.getOfflineRmWhitelistCommand().replace("{player}", s));
                }
                remove.add(s);
            }
        }
        log.debug("同步白名单成功：{}，新增白名单：{}，移除白名单：{}", serverId, user, remove);
    }
}
