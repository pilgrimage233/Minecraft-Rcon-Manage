package cc.endmc.quartz.async;

import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.constant.Command;
import cc.endmc.server.common.service.RconService;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 异步服务
 * 专门处理需要异步执行的操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncService {

    private final RconService rconService;

    private final IWhitelistInfoService whitelistInfoService;

    /**
     * 同步单个服务器的白名单
     *
     * @param serverId 服务器ID
     */
    @Async()
    public void syncSingleServer(String serverId) throws InterruptedException {

        log.debug("开始同步单个服务器白名单：{}", serverId);

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
        String list = RconCache.get(serverId).sendCommand(Command.WHITELIST_LIST);
        log.debug("现有白名单列表：{}", list);
        Set<String> online;
        Set<String> offline;
        if (StringUtils.isNotEmpty(list) && list.contains("There are")) {
            // 正版玩家
            online = new HashSet<>(Arrays.asList(list.split("whitelisted player\\(s\\):")[1].trim().split(", ")));
            // 离线转小写
            offline = new HashSet<>(Arrays.asList(list.split("whitelisted player\\(s\\):")[1].trim().toLowerCase().split(", ")));

        } else {
            log.warn("服务器 {} 的白名单列表为空或格式不正确，无法进行同步，尝试初始化白名单。", serverId);
            rconService.sendCommand(serverId, "whitelist add test", false);
            // 尝试任务回调
            syncSingleServer(serverId);
            return;
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
        list = RconCache.get(serverId).sendCommand(Command.WHITELIST_LIST);
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
