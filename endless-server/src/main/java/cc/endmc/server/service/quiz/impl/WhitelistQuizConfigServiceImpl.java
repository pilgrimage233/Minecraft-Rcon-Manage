package cc.endmc.server.service.quiz.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.cache.QuizConfigCache;
import cc.endmc.server.domain.quiz.WhitelistQuizConfig;
import cc.endmc.server.mapper.quiz.WhitelistQuizConfigMapper;
import cc.endmc.server.service.quiz.IWhitelistQuizConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 题库配置Service业务层处理
 *
 * @author ruoyi
 * @date 2025-03-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhitelistQuizConfigServiceImpl implements IWhitelistQuizConfigService {

    private final WhitelistQuizConfigMapper whitelistQuizConfigMapper;
    private final QuizConfigCache quizConfigCache;

    /**
     * 查询题库配置
     *
     * @param id 题库配置主键
     * @return 题库配置
     */
    @Override
    public WhitelistQuizConfig selectWhitelistQuizConfigById(Long id) {
        return whitelistQuizConfigMapper.selectWhitelistQuizConfigById(id);
    }

    /**
     * 查询题库配置列表
     *
     * @param whitelistQuizConfig 题库配置
     * @return 题库配置
     */
    @Override
    public List<WhitelistQuizConfig> selectWhitelistQuizConfigList(WhitelistQuizConfig whitelistQuizConfig) {
        return whitelistQuizConfigMapper.selectWhitelistQuizConfigList(whitelistQuizConfig);
    }

    /**
     * 新增题库配置
     *
     * @param whitelistQuizConfig 题库配置
     * @return 结果
     */
    @Override
    public int insertWhitelistQuizConfig(WhitelistQuizConfig whitelistQuizConfig) {
        whitelistQuizConfig.setCreateTime(DateUtils.getNowDate());
        int result = whitelistQuizConfigMapper.insertWhitelistQuizConfig(whitelistQuizConfig);

        // 更新缓存
        if (result > 0) {
            quizConfigCache.updateConfig(whitelistQuizConfig);
            log.info("新增配置并更新缓存: key={}, value={}",
                    whitelistQuizConfig.getConfigKey(), whitelistQuizConfig.getConfigValue());
        }

        return result;
    }

    /**
     * 修改题库配置
     *
     * @param whitelistQuizConfig 题库配置
     * @return 结果
     */
    @Override
    public int updateWhitelistQuizConfig(WhitelistQuizConfig whitelistQuizConfig) {
        whitelistQuizConfig.setUpdateTime(DateUtils.getNowDate());
        int result = whitelistQuizConfigMapper.updateWhitelistQuizConfig(whitelistQuizConfig);

        // 更新缓存
        if (result > 0) {
            quizConfigCache.updateConfig(whitelistQuizConfig);
            log.info("修改配置并更新缓存: key={}, value={}",
                    whitelistQuizConfig.getConfigKey(), whitelistQuizConfig.getConfigValue());
        }

        return result;
    }

    /**
     * 批量删除题库配置
     *
     * @param ids 需要删除的题库配置主键
     * @return 结果
     */
    @Override
    public int deleteWhitelistQuizConfigByIds(Long[] ids) {
        // 先查询要删除的配置键
        String[] configKeys = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            WhitelistQuizConfig config = whitelistQuizConfigMapper.selectWhitelistQuizConfigById(ids[i]);
            if (config != null) {
                configKeys[i] = config.getConfigKey();
            }
        }

        int result = whitelistQuizConfigMapper.deleteWhitelistQuizConfigByIds(ids);

        // 更新缓存
        if (result > 0) {
            quizConfigCache.removeConfigs(configKeys);
            log.info("批量删除配置并更新缓存: count={}", result);
        }

        return result;
    }

    /**
     * 删除题库配置信息
     *
     * @param id 题库配置主键
     * @return 结果
     */
    @Override
    public int deleteWhitelistQuizConfigById(Long id) {
        // 先查询要删除的配置键
        WhitelistQuizConfig config = whitelistQuizConfigMapper.selectWhitelistQuizConfigById(id);

        int result = whitelistQuizConfigMapper.deleteWhitelistQuizConfigById(id);

        // 更新缓存
        if (result > 0 && config != null) {
            quizConfigCache.removeConfig(config.getConfigKey());
            log.info("删除配置并更新缓存: key={}", config.getConfigKey());
        }

        return result;
    }

    /**
     * 初始化配置缓存
     * 在应用启动时调用，加载所有配置到缓存
     */
    @Override
    public void initConfigCache() {
        try {
            List<WhitelistQuizConfig> allConfigs = whitelistQuizConfigMapper.selectWhitelistQuizConfigList(new WhitelistQuizConfig());

            Map<String, WhitelistQuizConfig> configMap = new HashMap<>();
            for (WhitelistQuizConfig config : allConfigs) {
                if (config.getConfigKey() != null) {
                    configMap.put(config.getConfigKey(), config);
                }
            }

            quizConfigCache.initCache(configMap);
            log.info("问卷配置缓存初始化成功，共加载 {} 个配置项", configMap.size());
        } catch (Exception e) {
            log.error("问卷配置缓存初始化失败", e);
        }
    }
}
