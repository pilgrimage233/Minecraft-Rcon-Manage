package cc.endmc.server.utils;

import cc.endmc.server.common.constant.BotApi;
import cc.endmc.server.domain.bot.QqBotConfig;
import cc.endmc.server.ws.BotClient;
import cc.endmc.server.ws.BotManager;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ClassName: BotUtil <br>
 * Description:
 * date: 2025/8/13 22:53 <br>
 *
 * @author Memory <br>
 * @since JDK 1.8
 */
@Slf4j
public class BotUtil {

    /**
     * 发送消息到所有在线机器人已配置的群组
     */
    public static void sendMessage(String msg) {
        sendMessageByActiveBots(msg, null);
    }

    /**
     * 发送消息到指定单个群号
     */
    public static void sendMessage(String msg, String group) {
        if (group == null || group.trim().isEmpty()) {
            log.error("无法发送消息：指定群号为空");
            return;
        }
        sendMessageByActiveBots(msg, Set.of(group.trim()));
    }

    /**
     * 发送消息到指定多个群号
     */
    public static void sendMessage(String msg, String[] groups) {
        if (groups == null || groups.length == 0) {
            log.error("无法发送消息：指定群组为空");
            return;
        }
        Set<String> targetGroups = Arrays.stream(groups)
                .filter(group -> group != null && !group.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (targetGroups.isEmpty()) {
            log.error("无法发送消息：指定群组为空");
            return;
        }
        sendMessageByActiveBots(msg, targetGroups);
    }

    private static void sendMessageByActiveBots(String msg, Set<String> targetGroups) {
        if (msg == null || msg.isEmpty()) {
            log.error("无法发送消息：消息内容为空");
            return;
        }

        Map<Long, BotClient> activeClients = BotManager.botClients;
        if (activeClients == null || activeClients.isEmpty()) {
            log.warn("无法发送消息：当前没有在线机器人客户端");
            return;
        }

        QqBotConfig fallbackConfig = null;
        Set<String> remainingGroups = targetGroups == null ? null : new LinkedHashSet<>(targetGroups);

        for (BotClient botClient : activeClients.values()) {
            if (botClient == null || botClient.getConfig() == null) {
                continue;
            }
            QqBotConfig config = botClient.getConfig();
            if (fallbackConfig == null) {
                fallbackConfig = config;
            }

            if (targetGroups == null || targetGroups.isEmpty()) {
                sendMessage(msg, config.getGroupIds(), config);
                continue;
            }

            Set<String> configGroups = parseGroups(config.getGroupIds());
            if (configGroups.isEmpty()) {
                continue;
            }

            Set<String> matchedGroups = new LinkedHashSet<>(remainingGroups);
            matchedGroups.retainAll(configGroups);
            if (matchedGroups.isEmpty()) {
                continue;
            }

            sendMessage(msg, String.join(",", matchedGroups), config);
            remainingGroups.removeAll(matchedGroups);
            if (remainingGroups.isEmpty()) {
                return;
            }
        }

        // 对未匹配到机器人配置群组的目标群，使用任意在线机器人兜底尝试发送
        if (remainingGroups != null && !remainingGroups.isEmpty() && fallbackConfig != null) {
            sendMessage(msg, String.join(",", remainingGroups), fallbackConfig);
        }
    }

    private static Set<String> parseGroups(String groups) {
        if (groups == null || groups.trim().isEmpty()) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(groups.split(","))
                .filter(group -> group != null && !group.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 发送消息到QQ群
     *
     * @param groups 群消息对象
     * @param msg    要发送的消息内容
     */
    public static void sendMessage(String msg, String groups, QqBotConfig config) {
        // 发送消息
        try {
            if (config == null) {
                log.error("无法发送消息：机器人配置为空");
                return;
            }

            if (groups == null || groups.isEmpty()) {
                log.error("无法发送消息：群组列表为空");
                return;
            }

            for (String group : groups.split(",")) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("group_id", group);
                jsonObject.put("message", msg);

                final HttpResponse response = HttpUtil.createPost(config.getHttpUrl() + BotApi.SEND_GROUP_MSG)
                        // 设置Authorization头
                        .header("Authorization", "Bearer " + config.getToken())
                        .body(jsonObject.toJSONString())
                        .execute();
                log.info("发送消息结果: {}", response.body());
            }

        } catch (Exception e) {
            log.debug(e.toString());
            log.error("发送消息失败: {}", e.getMessage());
        }
    }
}
