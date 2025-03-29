package cc.endmc.server.service.quiz;

import java.util.List;

import cc.endmc.server.domain.quiz.WhitelistQuizConfig;

/**
 * 题库配置Service接口
 *
 * @author ruoyi
 * @date 2025-03-21
 */
public interface IWhitelistQuizConfigService {
    /**
     * 查询题库配置
     *
     * @param id 题库配置主键
     * @return 题库配置
     */
    public WhitelistQuizConfig selectWhitelistQuizConfigById(Long id);

    /**
     * 查询题库配置列表
     *
     * @param whitelistQuizConfig 题库配置
     * @return 题库配置集合
     */
    public List<WhitelistQuizConfig> selectWhitelistQuizConfigList(WhitelistQuizConfig whitelistQuizConfig);

    /**
     * 新增题库配置
     *
     * @param whitelistQuizConfig 题库配置
     * @return 结果
     */
    public int insertWhitelistQuizConfig(WhitelistQuizConfig whitelistQuizConfig);

    /**
     * 修改题库配置
     *
     * @param whitelistQuizConfig 题库配置
     * @return 结果
     */
    public int updateWhitelistQuizConfig(WhitelistQuizConfig whitelistQuizConfig);

    /**
     * 批量删除题库配置
     *
     * @param ids 需要删除的题库配置主键集合
     * @return 结果
     */
    public int deleteWhitelistQuizConfigByIds(Long[] ids);

    /**
     * 删除题库配置信息
     *
     * @param id 题库配置主键
     * @return 结果
     */
    public int deleteWhitelistQuizConfigById(Long id);
}
