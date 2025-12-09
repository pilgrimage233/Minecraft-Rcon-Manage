package cc.endmc.server.mapper.bot;

import cc.endmc.server.domain.bot.BotGroupCommandConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 群组指令功能配置Mapper接口
 *
 * @author Memory
 * @date 2025-12-10
 */
@Mapper
public interface BotGroupCommandConfigMapper {
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
     * 删除群组指令功能配置
     *
     * @param id 群组指令功能配置主键
     * @return 结果
     */
    public int deleteBotGroupCommandConfigById(Long id);

    /**
     * 批量删除群组指令功能配置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBotGroupCommandConfigByIds(Long[] ids);

    /**
     * 根据群组ID和指令关键字查询配置
     *
     * @param groupId    群组ID
     * @param commandKey 指令关键字
     * @return 配置信息
     */
    BotGroupCommandConfig selectByGroupAndCommand(@Param("groupId") String groupId, @Param("commandKey") String commandKey);

    /**
     * 获取所有指令关键字（去重）
     *
     * @return 指令关键字列表
     */
    List<String> selectAllCommandKeys();
}
