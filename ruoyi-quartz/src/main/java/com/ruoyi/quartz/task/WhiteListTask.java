package com.ruoyi.quartz.task;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.server.common.MapCache;
import com.ruoyi.server.common.PushEmail;
import com.ruoyi.server.domain.WhitelistInfo;
import com.ruoyi.server.service.IWhitelistInfoService;
import com.ruoyi.system.service.ISysUserService;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务调度
 * 白名单同步
 * 作者：Memory
 */
@Component("whiteListTask")
public class WhiteListTask {

    @Autowired
    private IWhitelistInfoService whitelistInfoService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private PushEmail pushEmail;
    private static final Log log = LogFactory.getLog(WhiteListTask.class);


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
                    e.printStackTrace();
                }
            }
        }
    }

    // 根据传入参数同步对应服务器白名单
    public void syncWhitelistByServerId(String serverId) {
        log.debug("开始同步白名单：" + serverId);

        if (serverId == null || serverId.isEmpty()) {
            log.error("服务器ID为空");
            return;
        }

        if (!MapCache.containsKey(serverId)) {
            log.error("服务器未连接：" + serverId);
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
        if (StringUtils.isEmpty(list)) {
            list = ": ";
        }
        String[] split = list.substring(list.indexOf(": ")).split(", ");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].toLowerCase().trim();
        }

        List<String> user = new ArrayList<>();
        // 现有白名单列表比对已通过审核的白名单列表
        for (WhitelistInfo info : newList) {
            if (!Arrays.asList(split).contains(info.getUserName().toLowerCase())) {
                // 离线添加方式
                if (info.getOnlineFlag() == 0L) {
                    MapCache.get(serverId).sendCommand("auth addToForcedOffline " + info.getUserName().toLowerCase());
                    MapCache.get(serverId).sendCommand("easywhitelist add " + info.getUserName());
                } else {
                    // 在线添加方式
                    MapCache.get(serverId).sendCommand("whitelist add " + info.getUserName());
                }
                user.add(info.getUserName());
            }
        }
        log.debug("同步白名单成功：" + serverId + "，新增白名单：" + user);
    }
}
