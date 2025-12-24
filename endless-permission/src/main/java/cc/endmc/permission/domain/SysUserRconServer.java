package cc.endmc.permission.domain;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 用户-RCON服务器权限关联对象 sys_user_rcon_server
 *
 * @author Memory
 * @date 2025-12-20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysUserRconServer extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名(非数据库字段) */
    private String userName;

    /** RCON服务器ID */
    @Excel(name = "服务器ID")
    private Long serverId;

    /** 服务器名称(非数据库字段) */
    private String serverName;

    /** 权限类型(view/command/manage/admin) */
    @Excel(name = "权限类型")
    private String permissionType;

    /** 是否可执行命令 */
    @Excel(name = "可执行命令", readConverterExp = "0=否,1=是")
    private Integer canExecuteCmd;

    /** 是否可查看日志 */
    @Excel(name = "可查看日志", readConverterExp = "0=否,1=是")
    private Integer canViewLog;

    /** 是否可管理配置 */
    @Excel(name = "可管理配置", readConverterExp = "0=否,1=是")
    private Integer canManage;

    /** 允许执行的命令白名单(JSON数组) */
    private String cmdWhitelist;

    /** 禁止执行的命令黑名单(JSON数组) */
    private String cmdBlacklist;

    /** 权限过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "过期时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    /** 状态(0正常 1停用) */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
}
