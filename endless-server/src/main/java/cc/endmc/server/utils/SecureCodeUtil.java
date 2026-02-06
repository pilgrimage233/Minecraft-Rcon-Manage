package cc.endmc.server.utils;

import cc.endmc.common.core.redis.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 安全验证码生成工具类
 * 使用加密安全的随机数生成器，防止验证码被逆向
 *
 * @author endmc
 */
@Slf4j
@Component
public class SecureCodeUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    // 验证码字符集（排除易混淆字符：0O、1Il等）
    private static final String CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String NUMERIC_CHARS = "0123456789";
    private static RedisCache redisCache;

    @Autowired
    public SecureCodeUtil(RedisCache redisCache) {
        SecureCodeUtil.redisCache = redisCache;
    }

    /**
     * 生成安全的随机验证码（字母数字混合）
     *
     * @param identifier    标识符（如QQ号）
     * @param cacheKey      缓存键前缀
     * @param length        验证码长度
     * @param expireMinutes 过期时间（分钟）
     * @return 验证码，如果已存在返回"isExist"，失败返回null
     */
    public static String generateSecureCode(String identifier, String cacheKey, int length, int expireMinutes) {
        try {
            String code;
            int maxRetries = 10; // 最多重试10次
            int retryCount = 0;

            do {
                // 生成随机验证码
                code = generateRandomCode(length, false);

                // 检查是否已存在
                if (!redisCache.hasKey(cacheKey + code)) {
                    // 缓存验证码关联信息
                    redisCache.setCacheObject(
                            cacheKey + code + ":identifier",
                            identifier,
                            expireMinutes,
                            TimeUnit.MINUTES
                    );
                    log.info("生成安全验证码成功, 标识符:{}, 验证码长度:{}", identifier, length);
                    return code;
                }

                retryCount++;
                log.warn("验证码冲突，重新生成, 重试次数:{}", retryCount);

            } while (retryCount < maxRetries);

            log.error("生成验证码失败，达到最大重试次数, 标识符:{}", identifier);
            return null;

        } catch (Exception e) {
            log.error("生成验证码异常, 标识符:{}", identifier, e);
            return null;
        }
    }

    /**
     * 生成纯数字验证码
     *
     * @param identifier    标识符（如QQ号）
     * @param cacheKey      缓存键前缀
     * @param length        验证码长度
     * @param expireMinutes 过期时间（分钟）
     * @return 验证码，如果已存在返回"isExist"，失败返回null
     */
    public static String generateNumericCode(String identifier, String cacheKey, int length, int expireMinutes) {
        try {
            String code;
            int maxRetries = 10;
            int retryCount = 0;

            do {
                code = generateRandomCode(length, true);

                if (!redisCache.hasKey(cacheKey + code)) {
                    redisCache.setCacheObject(
                            cacheKey + code + ":identifier",
                            identifier,
                            expireMinutes,
                            TimeUnit.MINUTES
                    );
                    log.info("生成数字验证码成功, 标识符:{}, 验证码长度:{}", identifier, length);
                    return code;
                }

                retryCount++;
                log.warn("验证码冲突，重新生成, 重试次数:{}", retryCount);

            } while (retryCount < maxRetries);

            log.error("生成验证码失败，达到最大重试次数, 标识符:{}", identifier);
            return null;

        } catch (Exception e) {
            log.error("生成验证码异常, 标识符:{}", identifier, e);
            return null;
        }
    }

    /**
     * 验证验证码是否有效，并检查标识符是否匹配
     *
     * @param code       验证码
     * @param cacheKey   缓存键前缀
     * @param identifier 标识符（如QQ号）
     * @return true-验证通过，false-验证失败
     */
    public static boolean verifyCode(String code, String cacheKey, String identifier) {
        try {
            String cachedIdentifier = redisCache.getCacheObject(cacheKey + code + ":identifier");

            if (cachedIdentifier == null) {
                log.warn("验证码不存在或已过期, 验证码:{}", code);
                return false;
            }

            if (!cachedIdentifier.equals(identifier)) {
                log.warn("验证码标识符不匹配, 验证码:{}, 期望:{}, 实际:{}", code, identifier, cachedIdentifier);
                return false;
            }

            log.info("验证码验证成功, 标识符:{}, 验证码:{}", identifier, code);
            return true;

        } catch (Exception e) {
            log.error("验证验证码异常, 验证码:{}", code, e);
            return false;
        }
    }

    /**
     * 删除验证码
     *
     * @param code     验证码
     * @param cacheKey 缓存键前缀
     */
    public static void deleteCode(String code, String cacheKey) {
        try {
            redisCache.deleteObject(cacheKey + code + ":identifier");
            log.info("删除验证码成功, 验证码:{}", code);
        } catch (Exception e) {
            log.error("删除验证码异常, 验证码:{}", code, e);
        }
    }

    /**
     * 生成随机验证码
     *
     * @param length      长度
     * @param numericOnly 是否仅数字
     * @return 验证码
     */
    private static String generateRandomCode(int length, boolean numericOnly) {
        String chars = numericOnly ? NUMERIC_CHARS : CODE_CHARS;
        StringBuilder code = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(chars.length());
            code.append(chars.charAt(index));
        }

        return code.toString();
    }

    /**
     * 检查验证码是否存在（用于防止重复申请）
     *
     * @param identifier 标识符
     * @param cacheKey   缓存键前缀
     * @return true-存在，false-不存在
     */
    public static boolean hasActiveCode(String identifier, String cacheKey) {
        try {
            // 这里可以通过额外的缓存键来跟踪某个标识符是否有活跃的验证码
            String trackingKey = cacheKey + "tracking:" + identifier;
            return redisCache.hasKey(trackingKey);
        } catch (Exception e) {
            log.error("检查验证码存在性异常, 标识符:{}", identifier, e);
            return false;
        }
    }

    /**
     * 标记标识符有活跃验证码（防止重复申请）
     *
     * @param identifier    标识符
     * @param cacheKey      缓存键前缀
     * @param expireMinutes 过期时间（分钟）
     */
    public static void markActiveCode(String identifier, String cacheKey, int expireMinutes) {
        try {
            String trackingKey = cacheKey + "tracking:" + identifier;
            redisCache.setCacheObject(trackingKey, "active", expireMinutes, TimeUnit.MINUTES);
            log.info("标记活跃验证码, 标识符:{}", identifier);
        } catch (Exception e) {
            log.error("标记活跃验证码异常, 标识符:{}", identifier, e);
        }
    }

    /**
     * 清除标识符的活跃验证码标记
     *
     * @param identifier 标识符
     * @param cacheKey   缓存键前缀
     */
    public static void clearActiveCode(String identifier, String cacheKey) {
        try {
            String trackingKey = cacheKey + "tracking:" + identifier;
            redisCache.deleteObject(trackingKey);
            log.info("清除活跃验证码标记, 标识符:{}", identifier);
        } catch (Exception e) {
            log.error("清除活跃验证码标记异常, 标识符:{}", identifier, e);
        }
    }
}
