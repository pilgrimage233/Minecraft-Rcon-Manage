package com.ruoyi.quartz.task;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.server.common.constant.BotApi;
import com.ruoyi.server.domain.bot.QqBotConfig;
import com.ruoyi.server.domain.permission.WhitelistInfo;
import com.ruoyi.server.service.permission.IWhitelistInfoService;
import com.ruoyi.server.ws.BotClient;
import com.ruoyi.server.ws.BotManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("botTask")
public class BotTask {
    @Autowired
    private IWhitelistInfoService whitelistInfoService;

    @Autowired
    private BotManager botManager;

    /**
     * 监控白名单用户是否退群
     */
    public void monitorWhiteList() {
        WhitelistInfo whitelistInfo = new WhitelistInfo();
        whitelistInfo.setStatus("1");
        final List<WhitelistInfo> whitelistInfos = whitelistInfoService.selectWhitelistInfoList(whitelistInfo);

        if (whitelistInfos.isEmpty()) {
            return;
        }

        // 获取所有活跃的机器人客户端
        Map<Long, BotClient> activeBots = botManager.getAllBots();
        if (activeBots.isEmpty()) {
            log.warn("没有活跃的机器人客户端");
            return;
        }

        // 用于存储每个群的退群用户列表
        Map<Long, StringBuilder> groupMessages = new HashMap<>();
        // 用于记录用户在任意群中的存在状态
        Map<Long, Boolean> userExistsInAnyGroup = new HashMap<>();
        // 用于记录用户退出的群
        Map<Long, List<Long>> userLeftGroups = new HashMap<>();
        // 所有需要监控的群ID列表
        Set<Long> allGroupIds = new HashSet<>();

        // 获取所有机器人配置的群ID
        for (BotClient bot : activeBots.values()) {
            QqBotConfig config = bot.getConfig();
            if (config != null && config.getGroupIds() != null) {
                allGroupIds.addAll(Arrays.stream(config.getGroupIds().split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toSet()));
            }
        }

        // 获取启用机器人的群员列表
        Map<String, Object> request = new HashMap<>();
        request.put("no_cache", false);

        // 遍历所有群，检查每个用户的存在状态
        for (Long groupId : allGroupIds) {
            request.put("group_id", String.valueOf(groupId));
            groupMessages.put(groupId, new StringBuilder());

            // 找到负责该群的机器人
            BotClient responsibleBot = findResponsibleBot(activeBots, groupId);
            if (responsibleBot == null) {
                log.warn("群 {} 没有对应的机器人客户端", groupId);
                continue;
            }

            // 使用机器人的配置发送请求
            String botUrl = responsibleBot.getConfig().getHttpUrl();
            final String post = HttpUtil.post(botUrl + BotApi.GET_GROUP_MEMBER_LIST, request);
            if (post == null) {
                log.warn("群 {} 获取成员列表失败", groupId);
                continue;
            }

            final JSONObject jsonObject = JSONObject.parseObject(post);
            if (jsonObject.getInteger("retcode") != 0) {
                log.warn("群 {} 获取成员列表失败: {}", groupId, jsonObject.getString("msg"));
                continue;
            }

            final List<JSONObject> members = jsonObject.getJSONArray("data").toJavaList(JSONObject.class);

            // 检查每个白名单用户在当前群中的状态
            whitelistInfos.forEach(whitelist -> {
                Long userId = Long.parseLong(whitelist.getQqNum());

                // 检查用户是否在当前群中
                boolean existsInCurrentGroup = members.stream()
                        .anyMatch(member -> userId.equals(member.getLong("user_id")));

                // 更新用户在任意群中的存在状态
                if (existsInCurrentGroup) {
                    userExistsInAnyGroup.put(userId, true);
                }

                // 如果用户不在当前群中，记录到退群列表
                if (!existsInCurrentGroup) {
                    userLeftGroups.computeIfAbsent(userId, k -> new ArrayList<>()).add(groupId);
                }
            });
        }

        // 处理所有不在任何群中的用户
        whitelistInfos.forEach(whitelist -> {
            Long userId = Long.parseLong(whitelist.getQqNum());

            // 如果用户不在任何群中
            if (!userExistsInAnyGroup.containsKey(userId)) {
                // 移除白名单
                whitelist.setAddState("true");
                whitelist.setRemoveReason("用户退群-同步");
                whitelistInfoService.updateWhitelistInfo(whitelist, "system");

                // 在所有相关群中添加通知消息
                List<Long> leftGroups = userLeftGroups.getOrDefault(userId, new ArrayList<>(allGroupIds));
                leftGroups.forEach(groupId -> {
                    groupMessages.get(groupId)
                            .append("\n- 用户：")
                            .append(whitelist.getUserName())
                            .append("(")
                            .append(userId)
                            .append(")");
                });

                log.info("用户 {} ({}) 已不在任何群中，已移除白名单", whitelist.getUserName(), userId);
            }
        });

        // 发送群通知
        groupMessages.forEach((groupId, messageBuilder) -> {
            String groupMessage = messageBuilder.toString();
            if (!groupMessage.isEmpty()) {
                // 找到负责该群的机器人
                BotClient responsibleBot = findResponsibleBot(activeBots, groupId);
                if (responsibleBot == null) {
                    log.error("群 {} 没有对应的机器人客户端，无法发送通知", groupId);
                    return;
                }

                // 构建消息对象
                JSONObject msgRequest = new JSONObject();
                msgRequest.put("group_id", groupId.toString());
                msgRequest.put("message", "⚠️退群白名单移除通知：\n以下用户已退群并移除白名单：" + groupMessage);

                // 发送群消息
                String response = HttpUtil.post(responsibleBot.getConfig().getHttpUrl() + BotApi.SEND_GROUP_MSG, msgRequest.toJSONString());
                if (response != null) {
                    JSONObject result = JSONObject.parseObject(response);
                    if (result.getInteger("retcode") != 0) {
                        log.error("群 {} 发送退群通知失败: {}", groupId, result.getString("msg"));
                    } else {
                        log.info("群 {} 退群移除白名单通知已发送: {}", groupId, groupMessage);
                    }
                }
            }
        });
    }

    /**
     * 查找负责特定群的机器人客户端
     *
     * @param activeBots 活跃的机器人客户端
     * @param groupId    群号
     * @return 负责该群的机器人客户端，如果没有找到则返回null
     */
    private BotClient findResponsibleBot(Map<Long, BotClient> activeBots, Long groupId) {
        for (BotClient bot : activeBots.values()) {
            QqBotConfig config = bot.getConfig();
            if (config != null && config.getGroupIds() != null) {
                boolean isResponsible = Arrays.stream(config.getGroupIds().split(","))
                        .map(Long::parseLong)
                        .anyMatch(id -> id.equals(groupId));
                if (isResponsible) {
                    return bot;
                }
            }
        }
        return null;
    }
}
