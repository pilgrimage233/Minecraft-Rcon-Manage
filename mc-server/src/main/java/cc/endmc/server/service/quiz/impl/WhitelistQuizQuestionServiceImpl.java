package cc.endmc.server.service.quiz.impl;

import java.util.List;
        import cc.endmc.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
    import java.util.ArrayList;

    import cc.endmc.common.utils.StringUtils;
    import org.springframework.transaction.annotation.Transactional;
    import cc.endmc.server.domain.quiz.WhitelistQuizAnswer;
import cc.endmc.server.domain.quiz.vo.WhitelistQuizQuestionVo;
import cc.endmc.server.mapper.quiz.WhitelistQuizQuestionMapper;
import cc.endmc.server.domain.quiz.WhitelistQuizQuestion;
import cc.endmc.server.service.quiz.IWhitelistQuizQuestionService;

/**
 * 白名单申请题库问题Service业务层处理
 *
 * @author Memory
 * @date 2025-03-19
 */
@Service
public class WhitelistQuizQuestionServiceImpl implements IWhitelistQuizQuestionService {
    @Autowired
    private WhitelistQuizQuestionMapper whitelistQuizQuestionMapper;

    /**
     * 查询白名单申请题库问题
     *
     * @param id 白名单申请题库问题主键
     * @return 白名单申请题库问题
     */
    @Override
    public WhitelistQuizQuestion selectWhitelistQuizQuestionById(Long id) {
        return whitelistQuizQuestionMapper.selectWhitelistQuizQuestionById(id);
    }

    /**
     * 查询白名单申请题库问题列表
     *
     * @param whitelistQuizQuestion 白名单申请题库问题
     * @return 白名单申请题库问题
     */
    @Override
    public List<WhitelistQuizQuestion> selectWhitelistQuizQuestionList(WhitelistQuizQuestion whitelistQuizQuestion) {
        return whitelistQuizQuestionMapper.selectWhitelistQuizQuestionList(whitelistQuizQuestion);
    }

    /**
     * 查询白名单申请题库问题VO列表
     *
     * @param whitelistQuizQuestion 白名单申请题库问题
     * @return 白名单申请题库问题VO集合
     */
    @Override
    public List<WhitelistQuizQuestionVo> selectWhitelistQuizQuestionVoList(WhitelistQuizQuestion whitelistQuizQuestion) {
        return whitelistQuizQuestionMapper.selectWhitelistQuizQuestionVoList(whitelistQuizQuestion);
    }

    /**
     * 新增白名单申请题库问题
     *
     * @param whitelistQuizQuestion 白名单申请题库问题
     * @return 结果
     */
        @Transactional
    @Override
    public int insertWhitelistQuizQuestion(WhitelistQuizQuestion whitelistQuizQuestion) {
                whitelistQuizQuestion.setCreateTime(DateUtils.getNowDate());
            int rows = whitelistQuizQuestionMapper.insertWhitelistQuizQuestion(whitelistQuizQuestion);
            insertWhitelistQuizAnswer(whitelistQuizQuestion);
            return rows;
    }

    /**
     * 修改白名单申请题库问题
     *
     * @param whitelistQuizQuestion 白名单申请题库问题
     * @return 结果
     */
        @Transactional
    @Override
    public int updateWhitelistQuizQuestion(WhitelistQuizQuestion whitelistQuizQuestion) {
                whitelistQuizQuestion.setUpdateTime(DateUtils.getNowDate());
                whitelistQuizQuestionMapper.deleteWhitelistQuizAnswerByQuestionId(whitelistQuizQuestion.getId())
            ;
            insertWhitelistQuizAnswer(whitelistQuizQuestion);
        return whitelistQuizQuestionMapper.updateWhitelistQuizQuestion(whitelistQuizQuestion);
    }

    /**
     * 批量删除白名单申请题库问题
     *
     * @param ids 需要删除的白名单申请题库问题主键
     * @return 结果
     */
        @Transactional
    @Override
    public int deleteWhitelistQuizQuestionByIds(Long[] ids) {
                whitelistQuizQuestionMapper.deleteWhitelistQuizAnswerByQuestionIds(ids);
        return whitelistQuizQuestionMapper.deleteWhitelistQuizQuestionByIds(ids);
    }

    /**
     * 删除白名单申请题库问题信息
     *
     * @param id 白名单申请题库问题主键
     * @return 结果
     */
        @Transactional
    @Override
    public int deleteWhitelistQuizQuestionById(Long id) {
                whitelistQuizQuestionMapper.deleteWhitelistQuizAnswerByQuestionId(id);
        return whitelistQuizQuestionMapper.deleteWhitelistQuizQuestionById(id);
    }

        /**
         * 新增白名单申请题目答案信息
         *
         * @param whitelistQuizQuestion 白名单申请题库问题对象
         */
        public void insertWhitelistQuizAnswer(WhitelistQuizQuestion whitelistQuizQuestion) {
            List<WhitelistQuizAnswer> whitelistQuizAnswerList = whitelistQuizQuestion.getWhitelistQuizAnswerList();
            Long id = whitelistQuizQuestion.getId();
            if (StringUtils.isNotNull(whitelistQuizAnswerList)) {
                List<WhitelistQuizAnswer> list = new ArrayList<WhitelistQuizAnswer>();
                for (WhitelistQuizAnswer whitelistQuizAnswer :whitelistQuizAnswerList)
                {
                    whitelistQuizAnswer.setQuestionId(id);
                    list.add(whitelistQuizAnswer);
                }
                if (list.size() > 0) {
                        whitelistQuizQuestionMapper.batchWhitelistQuizAnswer(list);
                }
            }
        }

    /**
     * 根据ID列表查询白名单申请题库问题
     *
     * @param ids 白名单申请题库问题ID列表
     * @return 白名单申请题库问题集合
     */
    @Override
    public List<WhitelistQuizQuestion> selectWhitelistQuizQuestionByIds(List<Integer> ids) {
        return whitelistQuizQuestionMapper.selectWhitelistQuizQuestionByIds(ids);
    }
}
