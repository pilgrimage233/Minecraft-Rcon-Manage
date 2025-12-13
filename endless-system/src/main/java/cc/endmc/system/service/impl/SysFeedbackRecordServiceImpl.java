package cc.endmc.system.service.impl;

import cc.endmc.system.domain.SysFeedbackRecord;
import cc.endmc.system.mapper.SysFeedbackRecordMapper;
import cc.endmc.system.service.ISysFeedbackRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户反馈记录Service实现
 */
@Service
public class SysFeedbackRecordServiceImpl implements ISysFeedbackRecordService {

    @Autowired
    private SysFeedbackRecordMapper feedbackRecordMapper;

    @Override
    public int insert(SysFeedbackRecord record) {
        return feedbackRecordMapper.insert(record);
    }

    @Override
    public SysFeedbackRecord selectByUuid(String uuid) {
        return feedbackRecordMapper.selectByUuid(uuid);
    }

    @Override
    public List<SysFeedbackRecord> selectByUserId(Long userId) {
        return feedbackRecordMapper.selectByUserId(userId);
    }

    @Override
    public int updateStatus(String uuid, Integer status) {
        return feedbackRecordMapper.updateStatus(uuid, status);
    }
}
