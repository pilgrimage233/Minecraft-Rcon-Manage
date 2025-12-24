package cc.endmc.server.utils;

import cc.endmc.common.utils.StringUtils;
import cc.endmc.common.utils.http.HttpUtils;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * Minecraft UUID 工具类
 * 用于生成正版和离线玩家的UUID
 */
public class MinecraftUUIDUtil {

    private static final Logger logger = LoggerFactory.getLogger(MinecraftUUIDUtil.class);

    /**
     * 获取玩家UUID，根据是否为正版账号选择不同的生成方式
     *
     * @param username 玩家名称
     * @param isOnline 是否为正版账号
     * @return 玩家UUID，如果生成失败则返回随机UUID
     */
    public static String getPlayerUUID(String username, boolean isOnline) {
        if (isOnline) {
            return getOnlinePlayerUUID(username);
        } else {
            return getOfflinePlayerUUID(username);
        }
    }

    /**
     * 获取正版玩家的UUID
     *
     * @param username 玩家名称
     * @return 正版玩家UUID，如果获取失败则返回离线UUID
     */
    public static String getOnlinePlayerUUID(String username) {
        if (StringUtils.isEmpty(username)) {
            return UUID.randomUUID().toString();
        }

        try {
            String result = HttpUtils.sendGet("https://api.mojang.com/users/profiles/minecraft/" + username);
            if (StringUtils.isEmpty(result)) {
                logger.warn("无法获取正版玩家UUID，玩家名称: {}, 将使用离线UUID", username);
                return getOfflinePlayerUUID(username);
            }

            JSONObject json = JSONObject.parseObject(result);
            if (json.containsKey("demo") || !json.containsKey("id")) {
                logger.warn("该账号未购买游戏或返回数据异常，玩家名称: {}, 将使用离线UUID", username);
                return getOfflinePlayerUUID(username);
            }

            String uuid = json.getString("id");
            if (StringUtils.isEmpty(uuid)) {
                return getOfflinePlayerUUID(username);
            }

            // 格式化成带横杠的UUID
            return uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" +
                    uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" +
                    uuid.substring(20);
        } catch (Exception e) {
            logger.error("获取正版UUID失败，玩家名称: {}", username, e);
            return getOfflinePlayerUUID(username);
        }
    }

    /**
     * 获取离线玩家的UUID (基于Minecraft离线UUID生成算法)
     *
     * @param username 玩家名称
     * @return 离线玩家UUID，如果生成失败则返回随机UUID
     */
    public static String getOfflinePlayerUUID(String username) {
        if (StringUtils.isEmpty(username)) {
            return UUID.randomUUID().toString();
        }

        try {
            // 计算MD5哈希
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));

            // 将MD5哈希转换为UUID v3格式
            hash[6] &= 0x0f; // 清除版本位
            hash[6] |= 0x30; // 设置为版本3
            hash[8] &= 0x3f; // 清除变体位
            hash[8] |= 0x80; // 设置为变体1

            // 构建UUID字符串
            StringBuilder uuid = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                if (i == 4 || i == 6 || i == 8 || i == 10) {
                    uuid.append("-");
                }
                uuid.append(String.format("%02x", hash[i]));
            }
            return uuid.toString();
        } catch (Exception e) {
            logger.error("生成离线UUID失败，玩家名称: {}", username, e);
            // 如果生成失败，回退到随机UUID
            return UUID.randomUUID().toString();
        }
    }
} 