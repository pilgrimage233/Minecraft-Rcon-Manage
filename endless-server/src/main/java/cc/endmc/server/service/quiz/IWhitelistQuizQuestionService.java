package cc.endmc.server.service.quiz;

import cc.endmc.server.domain.quiz.WhitelistQuizQuestion;
import cc.endmc.server.domain.quiz.vo.WhitelistQuizQuestionVo;

import java.util.List;

/**
 * 白名单申请题库问题Service接口
 *
 * @author Memory
 * @date 2025-03-19
 */
public interface IWhitelistQuizQuestionService {
    /**
     * 查询白名单申请题库问题
     *
     * @param id 白名单申请题库问题主键
     * @return 白名单申请题库问题
     */
    public WhitelistQuizQuestion selectWhitelistQuizQuestionById(Long id);

    /**
     * 查询白名单申请题库问题列表
     *
     * @param whitelistQuizQuestion 白名单申请题库问题
     * @return 白名单申请题库问题集合
     */
    public List<WhitelistQuizQuestion> selectWhitelistQuizQuestionList(WhitelistQuizQuestion whitelistQuizQuestion);

    /**
     * 新增白名单申请题库问题
     *
     * @param whitelistQuizQuestion 白名单申请题库问题
     * @return 结果
     */
    public int insertWhitelistQuizQuestion(WhitelistQuizQuestion whitelistQuizQuestion);

    /**
     * 修改白名单申请题库问题
     *
     * @param whitelistQuizQuestion 白名单申请题库问题
     * @return 结果
     */
    public int updateWhitelistQuizQuestion(WhitelistQuizQuestion whitelistQuizQuestion);

    /**
     * 批量删除白名单申请题库问题
     *
     * @param ids 需要删除的白名单申请题库问题主键集合
     * @return 结果
     */
    public int deleteWhitelistQuizQuestionByIds(Long[] ids);

    /**
     * 删除白名单申请题库问题信息
     *
     * @param id 白名单申请题库问题主键
     * @return 结果
     */
    public int deleteWhitelistQuizQuestionById(Long id);

    /**
     * 查询白名单申请题库问题VO列表
     *
     * @param whitelistQuizQuestion 白名单申请题库问题
     * @return 白名单申请题库问题VO集合
     */
    public List<WhitelistQuizQuestionVo> selectWhitelistQuizQuestionVoList(WhitelistQuizQuestion whitelistQuizQuestion);

    /**
     * 根据ID列表查询白名单申请题库问题
     *
     * @param ids 白名单申请题库问题ID列表
     * @return 白名单申请题库问题集合
     */
    public List<WhitelistQuizQuestion> selectWhitelistQuizQuestionByIds(List<Integer> ids);
}
