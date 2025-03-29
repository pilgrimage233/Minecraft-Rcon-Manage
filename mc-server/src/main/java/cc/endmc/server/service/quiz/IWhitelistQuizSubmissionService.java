package cc.endmc.server.service.quiz;

import java.util.List;

import cc.endmc.server.domain.quiz.WhitelistQuizSubmission;

/**
 * 答题记录Service接口
 *
 * @author Memory
 * @date 2025-03-20
 */
public interface IWhitelistQuizSubmissionService {
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
     * 批量删除答题记录
     *
     * @param ids 需要删除的答题记录主键集合
     * @return 结果
     */
    public int deleteWhitelistQuizSubmissionByIds(Long[] ids);

    /**
     * 删除答题记录信息
     *
     * @param id 答题记录主键
     * @return 结果
     */
    public int deleteWhitelistQuizSubmissionById(Long id);
}
