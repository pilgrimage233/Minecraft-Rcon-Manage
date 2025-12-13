package cc.endmc.system.mapper;

import cc.endmc.system.domain.SysFeedbackRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户反馈记录Mapper接口
 */
public interface SysFeedbackRecordMapper {

    /**
     * 新增反馈记录
     */
    int insert(SysFeedbackRecord record);

    /**
     * 根据UUID查询
     */
    SysFeedbackRecord selectByUuid(@Param("uuid") String uuid);

    /**
     * 根据用户ID查询列表
     */
    List<SysFeedbackRecord> selectByUserId(@Param("userId") Long userId);

    /**
     * 更新状态
     */
    int updateStatus(@Param("uuid") String uuid, @Param("status") Integer status);
}
