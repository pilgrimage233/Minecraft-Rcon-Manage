package cc.endmc.server.cache;

import cc.endmc.server.config.QuestionConfig;
import cc.endmc.server.domain.quiz.WhitelistQuizConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 问卷配置缓存管理器
 * 在应用启动时加载所有配置到内存
 *
 * @author Memory
 * @date 2026-02-04
 */
@Slf4j
@Component
public class QuizConfigCache {

    /**
     * 配置缓存 Map<configKey, WhitelistQuizConfig>
     */
    private static final Map<String, WhitelistQuizConfig> CONFIG_CACHE = new ConcurrentHashMap<>();

    /**
     * 初始化缓存
     *
     * @param configs 配置列表
     */
    public void initCache(Map<String, WhitelistQuizConfig> configs) {
        CONFIG_CACHE.clear();
        CONFIG_CACHE.putAll(configs);
        log.info("问卷配置缓存初始化完成，共加载 {} 个配置项", configs.size());
    }

    /**
     * 更新单个配置
     *
     * @param config 配置对象
     */
    public void updateConfig(WhitelistQuizConfig config) {
        if (config != null && config.getConfigKey() != null) {
            CONFIG_CACHE.put(config.getConfigKey(), config);
            log.debug("更新配置缓存: key={}, value={}", config.getConfigKey(), config.getConfigValue());
        }
    }

    /**
     * 删除配置
     *
     * @param configKey 配置键
     */
    public void removeConfig(String configKey) {
        CONFIG_CACHE.remove(configKey);
        log.debug("删除配置缓存: key={}", configKey);
    }

    /**
     * 批量删除配置
     *
     * @param configKeys 配置键列表
     */
    public void removeConfigs(String[] configKeys) {
        for (String key : configKeys) {
            CONFIG_CACHE.remove(key);
        }
        log.debug("批量删除配置缓存: count={}", configKeys.length);
    }

    /**
     * 根据配置键获取配置
     *
     * @param configKey 配置键
     * @return 配置对象，不存在返回null
     */
    public WhitelistQuizConfig getConfig(String configKey) {
        return CONFIG_CACHE.get(configKey);
    }

    /**
     * 根据配置键获取配置值
     *
     * @param configKey 配置键
     * @return 配置值，不存在返回null
     */
    public String getConfigValue(String configKey) {
        WhitelistQuizConfig config = CONFIG_CACHE.get(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 根据配置键获取配置值（带默认值）
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值，不存在返回默认值
     */
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取布尔类型配置值
     *
     * @param configKey 配置键
     * @return 布尔值，不存在或解析失败返回false
     */
    public boolean getBooleanValue(String configKey) {
        String value = getConfigValue(configKey);
        return Boolean.parseBoolean(value);
    }

    /**
     * 获取整数类型配置值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 整数值，不存在或解析失败返回默认值
     */
    public int getIntValue(String configKey, int defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("配置值解析失败: key={}, value={}, 使用默认值: {}", configKey, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 获取长整数类型配置值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 长整数值，不存在或解析失败返回默认值
     */
    public long getLongValue(String configKey, long defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("配置值解析失败: key={}, value={}, 使用默认值: {}", configKey, value, defaultValue);
            return defaultValue;
        }
    }

    /**
     * 获取所有配置
     *
     * @return 配置Map
     */
    public Map<String, WhitelistQuizConfig> getAllConfigs() {
        return new ConcurrentHashMap<>(CONFIG_CACHE);
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        CONFIG_CACHE.clear();
        log.info("问卷配置缓存已清空");
    }

    /**
     * 检查配置是否存在
     *
     * @param configKey 配置键
     * @return 是否存在
     */
    public boolean hasConfig(String configKey) {
        return CONFIG_CACHE.containsKey(configKey);
    }

    /**
     * 获取缓存大小
     *
     * @return 缓存中的配置数量
     */
    public int size() {
        return CONFIG_CACHE.size();
    }


    /**
     * 检查答题功能是否开启
     */
    public boolean isQuizEnabled() {
        return getBooleanValue(QuestionConfig.STATUS);
    }

    /**
     * 检查自动通过功能是否启用
     */
    public boolean isAutoPassEnabled() {
        return getBooleanValue(QuestionConfig.AUTO_PASSED);
    }

    /**
     * 检查是否随机抽题
     */
    public boolean isRandomQuestion() {
        return getBooleanValue(QuestionConfig.RANDOM);
    }

    /**
     * 获取通过分数
     */
    public long getPassScore() {
        return getLongValue(QuestionConfig.PASS_SCORE, 60L);
    }

    /**
     * 获取问题数量
     */
    public int getQuestionCount() {
        return getIntValue(QuestionConfig.QUESTION_COUNT, 10);
    }

    /**
     * 获取最大尝试次数
     */
    public int getMaxAttempts() {
        return getIntValue(QuestionConfig.MAX_ATTEMPTS, 3);
    }

    /**
     * 获取冷却时间（分钟）
     */
    public int getCooldownMinutes() {
        return getIntValue(QuestionConfig.COOLDOWN_MINUTES, 30);
    }

    /**
     * 获取自动移出群组天数
     */
    public int getAutoRemoveFromGroupAfterInactiveDays() {
        return getIntValue(QuestionConfig.AUTO_REMOVE_FROM_GROUP_AFTER_INACTIVE_DAYS, 30);
    }
}
