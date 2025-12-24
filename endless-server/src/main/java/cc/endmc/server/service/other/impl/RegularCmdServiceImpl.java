package cc.endmc.server.service.other.impl;

import cc.endmc.common.exception.job.TaskException;
import cc.endmc.common.utils.DateUtils;
import cc.endmc.server.domain.other.RegularCmd;
import cc.endmc.server.mapper.other.RegularCmdMapper;
import cc.endmc.server.service.other.IRegularCmdService;
import cc.endmc.server.utils.ScheduleUtils;
import com.alibaba.fastjson2.JSON;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


/**
 * 定时命令Service业务层处理
 *
 * @author ruoyi
 * @date 2025-02-14
 */
@Service
public class RegularCmdServiceImpl implements IRegularCmdService {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private RegularCmdMapper regularCmdMapper;

    /**
     * 查询定时命令
     *
     * @param id 定时命令主键
     * @return 定时命令
     */
    @Override
    public RegularCmd selectRegularCmdById(Long id) {
        return regularCmdMapper.selectRegularCmdById(id);
    }

    /**
     * 查询定时命令列表
     *
     * @param regularCmd 定时命令
     * @return 定时命令
     */
    @Override
    public List<RegularCmd> selectRegularCmdList(RegularCmd regularCmd) {
        final List<RegularCmd> regularCmds = regularCmdMapper.selectRegularCmdList(regularCmd);
        if (regularCmds != null) {
            // 序列化历史结果
            regularCmds.forEach(obj -> {
                if (obj.getHistoryResult() != null) {
                    obj.setHistoryResult(JSON.toJSONString(obj.getHistoryResult()));
                }
            });
        }
        return regularCmds;
    }

    /**
     * 新增定时命令
     *
     * @param regularCmd 定时命令
     * @return 结果
     */
    @Override
    public int insertRegularCmd(RegularCmd regularCmd) {
        regularCmd.setCreateTime(DateUtils.getNowDate());
        regularCmd.setTaskId(UUID.randomUUID().toString());
        regularCmd.setExecuteCount(0L); // 初始化执行次数
        try {
            // 创建定时任务
            ScheduleUtils.createScheduleJob(scheduler, regularCmd);
        } catch (SchedulerException | TaskException e) {
            throw new RuntimeException("创建定时任务失败", e);
        }
        return regularCmdMapper.insertRegularCmd(regularCmd);
    }

    /**
     * 修改定时命令
     *
     * @param regularCmd 定时命令
     * @return 结果
     */
    @Override
    public int updateRegularCmd(RegularCmd regularCmd) {
        regularCmd.setUpdateTime(DateUtils.getNowDate());

        try {
            // 判断是否存在
            if (scheduler.checkExists(ScheduleUtils.getJobKey(regularCmd.getTaskId(), "DEFAULT"))) {
                // 防止创建时存在数据问题，先移除，然后在执行创建操作
                ScheduleUtils.deleteScheduleJob(scheduler, regularCmd.getTaskId(), "DEFAULT");
            }

            // 重新创建定时任务
            ScheduleUtils.createScheduleJob(scheduler, regularCmd);
        } catch (SchedulerException | TaskException e) {
            throw new RuntimeException("更新定时任务失败", e);
        }

        return regularCmdMapper.updateRegularCmd(regularCmd);
    }

    /**
     * 批量删除定时命令
     *
     * @param ids 需要删除的定时命令主键
     * @return 结果
     */
    @Override
    public int deleteRegularCmdByIds(Long[] ids) {
        for (Long id : ids) {
            final RegularCmd regularCmd = selectRegularCmdById(id);
            try {
                ScheduleUtils.deleteScheduleJob(scheduler, regularCmd.getTaskId(), "DEFAULT");
            } catch (SchedulerException e) {
                throw new RuntimeException("删除定时任务失败", e);
            }
        }
        return regularCmdMapper.deleteRegularCmdByIds(ids);
    }

    /**
     * 删除定时命令信息
     *
     * @param id 定时命令主键
     * @return 结果
     */
    @Override
    public int deleteRegularCmdById(Long id) {
        return regularCmdMapper.deleteRegularCmdById(id);
    }
}
