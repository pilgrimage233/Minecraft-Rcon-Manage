package cc.endmc.server.service.quiz.impl;

import java.util.List;
        import cc.endmc.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
    import java.util.ArrayList;

    import cc.endmc.common.utils.StringUtils;
    import org.springframework.transaction.annotation.Transactional;
    import cc.endmc.server.domain.quiz.WhitelistQuizSubmissionDetail;
import cc.endmc.server.mapper.quiz.WhitelistQuizSubmissionMapper;
import cc.endmc.server.domain.quiz.WhitelistQuizSubmission;
import cc.endmc.server.service.quiz.IWhitelistQuizSubmissionService;

/**
 * 答题记录Service业务层处理
 *
 * @author Memory
 * @date 2025-03-20
 */
@Service
public class WhitelistQuizSubmissionServiceImpl implements IWhitelistQuizSubmissionService {
    @Autowired
    private WhitelistQuizSubmissionMapper whitelistQuizSubmissionMapper;

    /**
     * 查询答题记录
     *
     * @param id 答题记录主键
     * @return 答题记录
     */
    @Override
    public WhitelistQuizSubmission selectWhitelistQuizSubmissionById(Long id) {
        return whitelistQuizSubmissionMapper.selectWhitelistQuizSubmissionById(id);
    }

    /**
     * 查询答题记录列表
     *
     * @param whitelistQuizSubmission 答题记录
     * @return 答题记录
     */
    @Override
    public List<WhitelistQuizSubmission> selectWhitelistQuizSubmissionList(WhitelistQuizSubmission whitelistQuizSubmission) {
        return whitelistQuizSubmissionMapper.selectWhitelistQuizSubmissionList(whitelistQuizSubmission);
    }

    /**
     * 新增答题记录
     *
     * @param whitelistQuizSubmission 答题记录
     * @return 结果
     */
        @Transactional
    @Override
    public int insertWhitelistQuizSubmission(WhitelistQuizSubmission whitelistQuizSubmission) {
                whitelistQuizSubmission.setCreateTime(DateUtils.getNowDate());
            int rows = whitelistQuizSubmissionMapper.insertWhitelistQuizSubmission(whitelistQuizSubmission);
            insertWhitelistQuizSubmissionDetail(whitelistQuizSubmission);
            return rows;
    }

    /**
     * 修改答题记录
     *
     * @param whitelistQuizSubmission 答题记录
     * @return 结果
     */
        @Transactional
    @Override
    public int updateWhitelistQuizSubmission(WhitelistQuizSubmission whitelistQuizSubmission) {
                whitelistQuizSubmission.setUpdateTime(DateUtils.getNowDate());
                whitelistQuizSubmissionMapper.deleteWhitelistQuizSubmissionDetailBySubmissionId(whitelistQuizSubmission.getId())
            ;
            insertWhitelistQuizSubmissionDetail(whitelistQuizSubmission);
        return whitelistQuizSubmissionMapper.updateWhitelistQuizSubmission(whitelistQuizSubmission);
    }

    /**
     * 批量删除答题记录
     *
     * @param ids 需要删除的答题记录主键
     * @return 结果
     */
        @Transactional
    @Override
    public int deleteWhitelistQuizSubmissionByIds(Long[] ids) {
                whitelistQuizSubmissionMapper.deleteWhitelistQuizSubmissionDetailBySubmissionIds(ids);
        return whitelistQuizSubmissionMapper.deleteWhitelistQuizSubmissionByIds(ids);
    }

    /**
     * 删除答题记录信息
     *
     * @param id 答题记录主键
     * @return 结果
     */
        @Transactional
    @Override
    public int deleteWhitelistQuizSubmissionById(Long id) {
                whitelistQuizSubmissionMapper.deleteWhitelistQuizSubmissionDetailBySubmissionId(id);
        return whitelistQuizSubmissionMapper.deleteWhitelistQuizSubmissionById(id);
    }

        /**
         * 新增白名单申请答题详情信息
         *
         * @param whitelistQuizSubmission 答题记录对象
         */
        public void insertWhitelistQuizSubmissionDetail(WhitelistQuizSubmission whitelistQuizSubmission) {
            List<WhitelistQuizSubmissionDetail> whitelistQuizSubmissionDetailList = whitelistQuizSubmission.getWhitelistQuizSubmissionDetailList();
            Long id = whitelistQuizSubmission.getId();
            if (StringUtils.isNotNull(whitelistQuizSubmissionDetailList)) {
                List<WhitelistQuizSubmissionDetail> list = new ArrayList<WhitelistQuizSubmissionDetail>();
                for (WhitelistQuizSubmissionDetail whitelistQuizSubmissionDetail :whitelistQuizSubmissionDetailList)
                {
                    whitelistQuizSubmissionDetail.setSubmissionId(id);
                    list.add(whitelistQuizSubmissionDetail);
                }
                if (list.size() > 0) {
                        whitelistQuizSubmissionMapper.batchWhitelistQuizSubmissionDetail(list);
                }
            }
        }
}
