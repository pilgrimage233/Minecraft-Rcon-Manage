package cc.endmc.server.utils;

import cc.endmc.server.common.constant.BotApi;
import cc.endmc.server.domain.bot.QqBotConfig;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

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
