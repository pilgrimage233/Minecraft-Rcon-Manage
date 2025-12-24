package cc.endmc.server.mapper.quiz;

import cc.endmc.server.domain.quiz.WhitelistQuizSubmission;
import cc.endmc.server.domain.quiz.WhitelistQuizSubmissionDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 答题记录Mapper接口
 *
 * @author Memory
 * @date 2025-03-20
 */
@Mapper
public interface WhitelistQuizSubmissionMapper {
    /**
     * 查询答题记录
     *
     * @param id 答题记录主键
     * @return 答题记录
     */
    public WhitelistQuizSubmission selectWhitelistQuizSubmissionById(Long id);

    /**
     * 查询答题记录列表
     *
     * @param whitelistQuizSubmission 答题记录
     * @return 答题记录集合
     */
    public List<WhitelistQuizSubmission> selectWhitelistQuizSubmissionList(WhitelistQuizSubmission whitelistQuizSubmission);

    /**
     * 新增答题记录
     *
     * @param whitelistQuizSubmission 答题记录
     * @return 结果
     */
    public int insertWhitelistQuizSubmission(WhitelistQuizSubmission whitelistQuizSubmission);

    /**
     * 修改答题记录
     *
     * @param whitelistQuizSubmission 答题记录
     * @return 结果
     */
    public int updateWhitelistQuizSubmission(WhitelistQuizSubmission whitelistQuizSubmission);

    /**
     * 删除答题记录
     *
     * @param id 答题记录主键
     * @return 结果
     */
    public int deleteWhitelistQuizSubmissionById(Long id);

    /**
     * 根据问题ID删除答题记录
     *
     * @param id 问题ID
     * @return 结果
     */
    public int deleteWhitelistQuizSubmissionDetailByQuestionId(Long id);

    /**
     * 批量删除答题记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWhitelistQuizSubmissionByIds(Long[] ids);

    /**
     * 批量删除白名单申请答题详情
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWhitelistQuizSubmissionDetailBySubmissionIds(Long[] ids);

    /**
     * 批量新增白名单申请答题详情
     *
     * @param whitelistQuizSubmissionDetailList 白名单申请答题详情列表
     * @return 结果
     */
    public int batchWhitelistQuizSubmissionDetail(List<WhitelistQuizSubmissionDetail> whitelistQuizSubmissionDetailList);


    /**
     * 通过答题记录主键删除白名单申请答题详情信息
     *
     * @param id 答题记录ID
     * @return 结果
     */
    public int deleteWhitelistQuizSubmissionDetailBySubmissionId(Long id);
}
