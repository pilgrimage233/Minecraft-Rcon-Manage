package com.ruoyi.server.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 历史命令对象 history_command
 *
 * @author ruoyi
 * @date 2025-02-11
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class HistoryCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 服务器ID
     */
    @Excel(name = "服务器ID")
    private Long serverId;

    /**
     * 执行用户
     */
    @Excel(name = "执行用户")
    private String user;

    /**
     * 执行指令
     */
    @Excel(name = "执行指令")
    private String command;

    /**
     * 执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "执行时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date executeTime;

    /**
     * 执行结果
     */
    @Excel(name = "执行结果")
    private String response;

    /**
     * 执行状态
     */
    @Excel(name = "执行状态")
    private String status;

    /**
     * 运行时间(毫秒值)
     */
    @Excel(name = "运行时间(毫秒值)")
    private String runTime;

}
