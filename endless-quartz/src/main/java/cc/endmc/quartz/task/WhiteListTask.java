package cc.endmc.quartz.task;

import cc.endmc.common.core.domain.entity.SysUser;
import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.quartz.async.AsyncService;
import cc.endmc.server.cache.RconCache;
import cc.endmc.server.common.service.EmailService;
import cc.endmc.server.domain.bot.QqBotConfig;
import cc.endmc.server.domain.permission.WhitelistDeadlineInfo;
import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.service.bot.IQqBotConfigService;
import cc.endmc.server.service.permission.IWhitelistDeadlineInfoService;
import cc.endmc.server.service.permission.IWhitelistInfoService;
import cc.endmc.server.utils.BotUtil;
import cc.endmc.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务调度
 * 白名单同步
 * 作者：Memory
 */
@Slf4j
@Component("whiteListTask")
@RequiredArgsConstructor
public class WhiteListTask {

    private final IWhitelistInfoService whitelistInfoService;
    private final IWhitelistDeadlineInfoService whitelistDeadlineInfoService;
    private final IQqBotConfigService qqBotConfigService;
    private final ISysUserService userService;
    private final RedisCache redisCache;
    private final EmailService pushEmail;
    private final AsyncService asyncService;

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
                    log.error("邮件发送失败：{} {}", sysUser.getEmail(), StringUtils.format("问题原因: {}", e.getMessage()));
                }
            }
        }
    }

    /**
     * 同步白名单
     *
     * @param serverId 服务器ID，支持"all"同步所有服务器
     */
    // @SuppressWarnings("all")
    public void syncWhitelistByServerId(String serverId) throws InterruptedException {
        log.debug("开始同步白名单：{}", serverId);

        if (serverId == null || serverId.isEmpty()) {
            log.error("服务器ID为空");
            return;
        }

        // 同步所有服务器
        if ("all".equals(serverId)) {
            Set<String> allServerIds = RconCache.getMap().keySet();
            if (allServerIds.isEmpty()) {
                log.warn("没有找到任何已连接的Rcon服务器");
                return;
            }

            log.info("开始同步所有服务器白名单，共{}个服务器", allServerIds.size());
            for (String singleServerId : allServerIds) {
                try {
                    log.debug("正在同步服务器：{}", singleServerId);
                    asyncService.syncSingleServer(singleServerId);
                } catch (Exception e) {
                    log.error("同步服务器 {} 白名单时发生异常：{}", singleServerId, e.getMessage(), e);
                }
            }
            log.info("所有服务器白名单同步完成");
            return;
        }

        // 单个服务器同步
        if (!RconCache.containsKey(serverId)) {
            log.error("服务器未连接：{}", serverId);
            return;
        }

        asyncService.syncSingleServer(serverId);
    }

    /**
     * 白名单时限检查
     * 如果白名单过期，则移除白名单
     */
    public void checkWhitelistExpiry() {
        log.debug("开始检查过期白名单...");

        // 查询过期未清除的白名单信息
        List<WhitelistDeadlineInfo> expireds = whitelistDeadlineInfoService.selectExpiredWhitelistDeadlineInfoList();

        if (expireds.isEmpty()) {
            // log.debug("没有找到任何白名单信息");
            return;
        }

        for (WhitelistDeadlineInfo info : expireds) {
            final WhitelistInfo whitelistInfo = whitelistInfoService.selectWhitelistInfoById(info.getWhitelistId());

            if (whitelistInfo == null) {
                log.warn("白名单信息 {} 已被删除或不存在，跳过处理", info.getId() + "---" + info.getUserName());
                continue;
            }

            // 获取白名单用户
            String userName = info.getUserName();
            if (userName == null || userName.isEmpty()) {
                log.warn("白名单信息 {} 的用户名为空，跳过处理", info.getId());
                continue;
            }

            // 移除白名单
            // rconService.sendCommand("all", String.format(Command.WHITELIST_REMOVE, userName), whitelistInfo.getOnlineFlag() == 1L);

            // 标记为已删除
            info.setDelFlag(1L);
            whitelistDeadlineInfoService.updateWhitelistDeadlineInfo(info);

            whitelistInfo.setAddState("true"); // 移除白名单
            whitelistInfo.setStatus("0"); // 设置白名单状态为未审核
            whitelistInfo.setRemoveReason("白名单于 [" + DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, info.getEndTime()) + "] 过期，已自动移除");
            whitelistInfo.setUpdateBy("System(Auto_Expiry_Task)");
            final int i = whitelistInfoService.updateWhitelistInfo(whitelistInfo, "System(Auto_Expiry_Task)");
            if (i == 1) {
                log.info("已移除白名单用户 {}", userName);
                log.info("白名单信息 {} 已成功更新为未添加状态", info.getId());

                // 群消息通知
                final QqBotConfig qqBotConfig = new QqBotConfig();
                qqBotConfig.setStatus(1L);
                List<QqBotConfig> qqBotConfigs = qqBotConfigService.selectQqBotConfigList(qqBotConfig);
                if (qqBotConfigs != null && !qqBotConfigs.isEmpty()) {
                    for (QqBotConfig config : qqBotConfigs) {
                        String message = "⚠️ 白名单用户 👤【" + userName + "】 已于 ⏰ "
                                + DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, info.getEndTime()) + " 过期，已从白名单中移除 🗑。";
                        if (config.getGroupIds() != null && !config.getGroupIds().isEmpty()) {
                            // 发送群消息
                            BotUtil.sendMessage(message, config.getGroupIds(), config);
                            log.info("已向群 {} 发送消息：{}", config.getGroupIds(), message);
                        } else {
                            log.warn("QQ机器人配置 {} 没有设置群ID，无法发送群消息", config.getId());
                        }
                    }
                } else {
                    log.warn("没有找到可用的QQ机器人配置，无法发送群消息");
                }
            } else {
                log.error("白名单信息 {} 更新失败，可能是数据库操作异常", info.getId());
            }
        }

        log.debug("过期白名单检查完成，共处理 {} 条记录", expireds.size());

    }

}
