package cc.endmc.server.mapper.quiz;

import cc.endmc.server.domain.quiz.WhitelistQuizConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 题库配置Mapper接口
 *
 * @author ruoyi
 * @date 2025-03-21
 */
@Mapper
public interface WhitelistQuizConfigMapper {
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
     * 删除题库配置
     *
     * @param id 题库配置主键
     * @return 结果
     */
    public int deleteWhitelistQuizConfigById(Long id);

    /**
     * 批量删除题库配置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWhitelistQuizConfigByIds(Long[] ids);
}
