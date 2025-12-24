package cc.endmc.server.service.quiz.impl;

import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.domain.quiz.WhitelistQuizConfig;
import cc.endmc.server.mapper.quiz.WhitelistQuizConfigMapper;
import cc.endmc.server.service.quiz.IWhitelistQuizConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 题库配置Service业务层处理
 *
 * @author ruoyi
 * @date 2025-03-21
 */
@Service
public class WhitelistQuizConfigServiceImpl implements IWhitelistQuizConfigService {
    @Autowired
    private WhitelistQuizConfigMapper whitelistQuizConfigMapper;

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
        return whitelistQuizConfigMapper.insertWhitelistQuizConfig(whitelistQuizConfig);
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
        return whitelistQuizConfigMapper.updateWhitelistQuizConfig(whitelistQuizConfig);
    }

    /**
     * 批量删除题库配置
     *
     * @param ids 需要删除的题库配置主键
     * @return 结果
     */
    @Override
    public int deleteWhitelistQuizConfigByIds(Long[] ids) {
        return whitelistQuizConfigMapper.deleteWhitelistQuizConfigByIds(ids);
    }

    /**
     * 删除题库配置信息
     *
     * @param id 题库配置主键
     * @return 结果
     */
    @Override
    public int deleteWhitelistQuizConfigById(Long id) {
        return whitelistQuizConfigMapper.deleteWhitelistQuizConfigById(id);
    }
}
