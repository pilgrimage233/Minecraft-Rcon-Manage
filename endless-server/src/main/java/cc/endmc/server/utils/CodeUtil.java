package cc.endmc.server.utils;

import cc.endmc.common.core.redis.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Component
public class CodeUtil {

    private static RedisCache redisCache;

    @Autowired
    public CodeUtil(RedisCache redisCache) {
        CodeUtil.redisCache = redisCache;
    }

    /**
     * 生成验证码
     *
     * @param qq       QQ号
     * @param cacheKey 缓存键
     * @return 验证码
     */
    public static String generateCode(String qq, String cacheKey) {
        // 验证码
        String code;
        try {
            // 基于QQ号生成固定验证码
            // 改为1800秒(30分钟)来匹配缓存过期时间
            String rawKey = qq + "_" + System.currentTimeMillis() / 1000 / 1800;

            // 使用MD5加密并取前8位作为验证码
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            code = sb.substring(0, 8);

            // 检查是否已存在该验证码
            if (redisCache.hasKey(cacheKey + code)) {
                log.info("验证码已存在,QQ:{},CODE:{}", qq, code);
                return "isExist";
            }
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            return null;
        }
        return code;
    }

}
