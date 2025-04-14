package cc.endmc.server.mapper.quiz;

import cc.endmc.server.domain.quiz.WhitelistQuizAnswer;
import cc.endmc.server.domain.quiz.WhitelistQuizQuestion;
import cc.endmc.server.domain.quiz.vo.WhitelistQuizQuestionVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 白名单申请题库问题Mapper接口
 *
 * @author Memory
 * @date 2025-03-19
 */
@Mapper
public interface WhitelistQuizQuestionMapper {
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
     * 删除白名单申请题库问题
     *
     * @param id 白名单申请题库问题主键
     * @return 结果
     */
    public int deleteWhitelistQuizQuestionById(Long id);

    /**
     * 批量删除白名单申请题库问题
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWhitelistQuizQuestionByIds(Long[] ids);

    /**
     * 批量删除白名单申请题目答案
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWhitelistQuizAnswerByQuestionIds(Long[] ids);

    /**
     * 批量新增白名单申请题目答案
     *
     * @param whitelistQuizAnswerList 白名单申请题目答案列表
     * @return 结果
     */
    public int batchWhitelistQuizAnswer(List<WhitelistQuizAnswer> whitelistQuizAnswerList);


    /**
     * 通过白名单申请题库问题主键删除白名单申请题目答案信息
     *
     * @param id 白名单申请题库问题ID
     * @return 结果
     */
    public int deleteWhitelistQuizAnswerByQuestionId(Long id);

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
