package cc.endmc.server.service.bot;

import cc.endmc.server.domain.bot.BotGroupCommandConfig;

import java.util.List;


/**
 * 群组指令功能配置Service接口
 *
 * @author Memory
 * @date 2025-12-10
 */
public interface IBotGroupCommandConfigService {
    /**
     * 查询群组指令功能配置
     *
     * @param id 群组指令功能配置主键
     * @return 群组指令功能配置
     */
    public BotGroupCommandConfig selectBotGroupCommandConfigById(Long id);

    /**
     * 查询群组指令功能配置列表
     *
     * @param botGroupCommandConfig 群组指令功能配置
     * @return 群组指令功能配置集合
     */
    public List<BotGroupCommandConfig> selectBotGroupCommandConfigList(BotGroupCommandConfig botGroupCommandConfig);

    /**
     * 新增群组指令功能配置
     *
     * @param botGroupCommandConfig 群组指令功能配置
     * @return 结果
     */
    public int insertBotGroupCommandConfig(BotGroupCommandConfig botGroupCommandConfig);

    /**
     * 修改群组指令功能配置
     *
     * @param botGroupCommandConfig 群组指令功能配置
     * @return 结果
     */
    public int updateBotGroupCommandConfig(BotGroupCommandConfig botGroupCommandConfig);

    /**
     * 批量删除群组指令功能配置
     *
     * @param ids 需要删除的群组指令功能配置主键集合
     * @return 结果
     */
    public int deleteBotGroupCommandConfigByIds(Long[] ids);

    /**
     * 删除群组指令功能配置信息
     *
     * @param id 群组指令功能配置主键
     * @return 结果
     */
    public int deleteBotGroupCommandConfigById(Long id);

    /**
     * 检查指令在指定群组是否启用
     *
     * @param groupId    群组ID
     * @param commandKey 指令关键字
     * @return 配置信息，如果未配置则返回null
     */
    BotGroupCommandConfig checkCommandEnabled(String groupId, String commandKey);

    /**
     * 切换指令启用状态（开启/关闭）
     *
     * @param groupId    群组ID
     * @param commandKey 指令关键字
     * @param enabled    是否启用
     * @param operatorId 操作者ID
     * @return 操作结果
     */
    int toggleCommandStatus(String groupId, String commandKey, boolean enabled, String operatorId);

    /**
     * 获取所有可用的指令列表（用于帮助信息）
     *
     * @return 指令列表
     */
    List<String> getAllCommandKeys();

    /**
     * 清除指定群组的所有指令缓存
     *
     * @param groupId 群组ID
     */
    void clearGroupCache(String groupId);

    /**
     * 清除所有指令配置缓存
     */
    void clearAllCache();
}
