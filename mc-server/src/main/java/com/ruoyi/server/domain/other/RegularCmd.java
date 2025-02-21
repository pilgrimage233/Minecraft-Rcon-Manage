package com.ruoyi.server.domain.other;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定时命令对象 regular_cmd
 *
 * @author ruoyi
 * @date 2025-02-14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RegularCmd extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 任务名称
     */
    @Excel(name = "任务名称")
    private String taskName;

    /**
     * 任务id
     */
    @Excel(name = "任务id")
    private String taskId;

    /**
     * 指令
     */
    @Excel(name = "指令")
    private String cmd;

    /**
     * 执行服务器
     */
    @Excel(name = "执行服务器")
    private String executeServer;

    /**
     * 执行结果
     */
    @Excel(name = "执行结果")
    private String result;

    /**
     * 执行历史保留次数
     */
    @Excel(name = "执行历史保留次数")
    private Long historyCount;

    /**
     * 历史结果
     */
    @Excel(name = "历史结果")
    private String historyResult;

    /**
     * Cron表达式
     */
    @Excel(name = "Cron表达式")
    private String cron;

    /**
     * 状态
     */
    @Excel(name = "状态")
    private Long status;

    /**
     * 执行次数
     */
    @Excel(name = "执行次数")
    private Long executeCount;

}
