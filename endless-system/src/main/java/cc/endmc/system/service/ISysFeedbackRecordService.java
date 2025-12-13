package cc.endmc.system.service;

import cc.endmc.system.domain.SysFeedbackRecord;

import java.util.List;

/**
 * 用户反馈记录Service接口
 */
public interface ISysFeedbackRecordService {

    /**
     * 新增反馈记录
     */
    int insert(SysFeedbackRecord record);

    /**
     * 根据UUID查询
     */
    SysFeedbackRecord selectByUuid(String uuid);

    /**
     * 根据用户ID查询列表
     */
    List<SysFeedbackRecord> selectByUserId(Long userId);

    /**
     * 更新状态
     */
    int updateStatus(String uuid, Integer status);
}
