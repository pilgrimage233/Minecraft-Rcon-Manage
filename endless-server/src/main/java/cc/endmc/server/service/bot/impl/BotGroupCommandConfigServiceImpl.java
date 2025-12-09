package cc.endmc.server.service.bot.impl;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.common.constant.CacheKey;
import cc.endmc.server.domain.bot.BotGroupCommandConfig;
import cc.endmc.server.mapper.bot.BotGroupCommandConfigMapper;
import cc.endmc.server.service.bot.IBotGroupCommandConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 群组指令功能配置Service业务层处理
 *
 * @author Memory
 * @date 2025-12-10
 */
@Service
public class BotGroupCommandConfigServiceImpl implements IBotGroupCommandConfigService {
    /**
     * 缓存过期时间（小时）
     */
    private static final int CACHE_EXPIRE_HOURS = 24;
    @Autowired
    private BotGroupCommandConfigMapper botGroupCommandConfigMapper;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询群组指令功能配置
     *
     * @param id 群组指令功能配置主键
     * @return 群组指令功能配置
     */
    @Override
    public BotGroupCommandConfig selectBotGroupCommandConfigById(Long id) {
        return botGroupCommandConfigMapper.selectBotGroupCommandConfigById(id);
    }

    /**
     * 查询群组指令功能配置列表
     *
     * @param botGroupCommandConfig 群组指令功能配置
     * @return 群组指令功能配置
     */
    @Override
    public List<BotGroupCommandConfig> selectBotGroupCommandConfigList(BotGroupCommandConfig botGroupCommandConfig) {
        return botGroupCommandConfigMapper.selectBotGroupCommandConfigList(botGroupCommandConfig);
    }

    /**
     * 新增群组指令功能配置
     *
     * @param botGroupCommandConfig 群组指令功能配置
     * @return 结果
     */
    @Override
    public int insertBotGroupCommandConfig(BotGroupCommandConfig botGroupCommandConfig) {
        botGroupCommandConfig.setCreateTime(DateUtils.getNowDate());
        return botGroupCommandConfigMapper.insertBotGroupCommandConfig(botGroupCommandConfig);
    }

    /**
     * 修改群组指令功能配置
     *
     * @param botGroupCommandConfig 群组指令功能配置
     * @return 结果
     */
    @Override
    public int updateBotGroupCommandConfig(BotGroupCommandConfig botGroupCommandConfig) {
        botGroupCommandConfig.setUpdateTime(DateUtils.getNowDate());
        int result = botGroupCommandConfigMapper.updateBotGroupCommandConfig(botGroupCommandConfig);

        if (result > 0 && botGroupCommandConfig.getGroupId() != null && botGroupCommandConfig.getCommandKey() != null) {
            clearCache(botGroupCommandConfig.getGroupId(), botGroupCommandConfig.getCommandKey());
        }

        return result;
    }

    /**
     * 批量删除群组指令功能配置
     *
     * @param ids 需要删除的群组指令功能配置主键
     * @return 结果
     */
    @Override
    public int deleteBotGroupCommandConfigByIds(Long[] ids) {
        for (Long id : ids) {
            BotGroupCommandConfig config = botGroupCommandConfigMapper.selectBotGroupCommandConfigById(id);
            if (config != null && config.getGroupId() != null && config.getCommandKey() != null) {
                clearCache(config.getGroupId(), config.getCommandKey());
            }
        }

        return botGroupCommandConfigMapper.deleteBotGroupCommandConfigByIds(ids);
    }

    /**
     * 删除群组指令功能配置信息
     *
     * @param id 群组指令功能配置主键
     * @return 结果
     */
    @Override
    public int deleteBotGroupCommandConfigById(Long id) {
        BotGroupCommandConfig config = botGroupCommandConfigMapper.selectBotGroupCommandConfigById(id);
        int result = botGroupCommandConfigMapper.deleteBotGroupCommandConfigById(id);

        if (result > 0 && config != null && config.getGroupId() != null && config.getCommandKey() != null) {
            clearCache(config.getGroupId(), config.getCommandKey());
        }

        return result;
    }

    /**
     * 检查指令在指定群组是否启用
     * 优先查询群组特定配置，如果没有则查询默认配置
     * 使用 Redis 缓存减少数据库查询
     *
     * @param groupId    群组ID
     * @param commandKey 指令关键字
     * @return 配置信息，如果未配置则返回null
     */
    @Override
    public BotGroupCommandConfig checkCommandEnabled(String groupId, String commandKey) {

        String cacheKey = getCacheKey(groupId, commandKey);

        BotGroupCommandConfig cachedConfig = redisCache.getCacheObject(cacheKey);
        if (cachedConfig != null) {
            return cachedConfig;
        }

        BotGroupCommandConfig config = botGroupCommandConfigMapper.selectByGroupAndCommand(groupId, commandKey);
        if (config != null) {
            redisCache.setCacheObject(cacheKey, config, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            return config;
        }

        BotGroupCommandConfig defaultConfig = botGroupCommandConfigMapper.selectByGroupAndCommand("default", commandKey);
        if (defaultConfig != null) {
            redisCache.setCacheObject(cacheKey, defaultConfig, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }

        return defaultConfig;
    }

    /**
     * 生成缓存 key
     *
     * @param groupId    群组ID
     * @param commandKey 指令关键字
     * @return 缓存 key
     */
    private String getCacheKey(String groupId, String commandKey) {
        return CacheKey.COMMAND_INFO_KEY + ":" + groupId + ":" + commandKey;
    }

    /**
     * 清除指定群组和指令的缓存
     *
     * @param groupId    群组ID
     * @param commandKey 指令关键字
     */
    private void clearCache(String groupId, String commandKey) {
        String cacheKey = getCacheKey(groupId, commandKey);
        redisCache.deleteObject(cacheKey);
    }

    /**
     * 切换指令启用状态（开启/关闭）
     *
     * @param groupId    群组ID
     * @param commandKey 指令关键字
     * @param enabled    是否启用
     * @param operatorId 操作者ID
     * @return 操作结果
     */
    @Override
    public int toggleCommandStatus(String groupId, String commandKey, boolean enabled, String operatorId) {
        int result;

        BotGroupCommandConfig existConfig = botGroupCommandConfigMapper.selectByGroupAndCommand(groupId, commandKey);

        if (existConfig != null) {
            // 更新现有配置
            existConfig.setIsEnabled(enabled ? 1 : 0);
            existConfig.setUpdateBy(operatorId);
            existConfig.setUpdateTime(DateUtils.getNowDate());
            result = botGroupCommandConfigMapper.updateBotGroupCommandConfig(existConfig);
        } else {
            // 查询默认配置获取指令信息
            BotGroupCommandConfig defaultConfig = botGroupCommandConfigMapper.selectByGroupAndCommand("default", commandKey);
            if (defaultConfig == null) {
                // 指令不存在
                return -1;
            }

            BotGroupCommandConfig newConfig = new BotGroupCommandConfig();
            newConfig.setGroupId(groupId);
            newConfig.setCommandKey(commandKey);
            newConfig.setCommandName(defaultConfig.getCommandName());
            newConfig.setCommandCategory(defaultConfig.getCommandCategory());
            newConfig.setIsEnabled(enabled ? 1 : 0);
            newConfig.setCreateBy(operatorId);
            newConfig.setCreateTime(DateUtils.getNowDate());
            result = botGroupCommandConfigMapper.insertBotGroupCommandConfig(newConfig);
        }

        if (result > 0) {
            clearCache(groupId, commandKey);
        }

        return result;
    }

    /**
     * 获取所有可用的指令列表
     *
     * @return 指令列表
     */
    @Override
    public List<String> getAllCommandKeys() {
        return botGroupCommandConfigMapper.selectAllCommandKeys();
    }

    /**
     * 清除指定群组的所有指令缓存
     *
     * @param groupId 群组ID
     */
    @Override
    public void clearGroupCache(String groupId) {
        // 获取所有指令关键字
        List<String> commandKeys = getAllCommandKeys();
        for (String commandKey : commandKeys) {
            clearCache(groupId, commandKey);
        }
    }

    /**
     * 清除所有指令配置缓存
     * 使用模糊匹配删除所有相关缓存
     */
    @Override
    public void clearAllCache() {
        Collection<String> keys = redisCache.keys(CacheKey.COMMAND_INFO_KEY + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisCache.deleteObject(keys);
        }
    }
}
