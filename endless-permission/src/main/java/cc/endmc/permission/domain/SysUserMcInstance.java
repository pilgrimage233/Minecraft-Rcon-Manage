package cc.endmc.permission.domain;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 用户-MC实例权限关联对象 sys_user_mc_instance
 *
 * @author Memory
 * @date 2025-12-20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysUserMcInstance extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名(非数据库字段) */
    private String userName;

    /** 实例ID */
    @Excel(name = "实例ID")
    private Long instanceId;

    /** 实例名称(非数据库字段) */
    private String instanceName;

    /** 所属节点ID */
    @Excel(name = "节点ID")
    private Long nodeId;

    /** 节点名称(非数据库字段) */
    private String nodeName;

    /** 权限类型(view/control/manage/admin) */
    @Excel(name = "权限类型")
    private String permissionType;

    /** 是否可查看实例 */
    @Excel(name = "可查看", readConverterExp = "0=否,1=是")
    private Integer canView;

    /** 是否可启动 */
    @Excel(name = "可启动", readConverterExp = "0=否,1=是")
    private Integer canStart;

    /** 是否可停止 */
    @Excel(name = "可停止", readConverterExp = "0=否,1=是")
    private Integer canStop;

    /** 是否可重启 */
    @Excel(name = "可重启", readConverterExp = "0=否,1=是")
    private Integer canRestart;

    /** 是否可访问控制台 */
    @Excel(name = "可访问控制台", readConverterExp = "0=否,1=是")
    private Integer canConsole;

    /** 是否可管理文件 */
    @Excel(name = "可管理文件", readConverterExp = "0=否,1=是")
    private Integer canFile;

    /** 是否可修改配置 */
    @Excel(name = "可修改配置", readConverterExp = "0=否,1=是")
    private Integer canConfig;

    /** 是否可删除实例 */
    @Excel(name = "可删除", readConverterExp = "0=否,1=是")
    private Integer canDelete;

    /** 控制台允许执行的命令白名单 */
    private String consoleCmdWhitelist;

    /** 控制台禁止执行的命令黑名单 */
    private String consoleCmdBlacklist;

    /** 允许访问的文件路径白名单 */
    private String filePathWhitelist;

    /** 禁止访问的文件路径黑名单 */
    private String filePathBlacklist;

    /** 权限过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "过期时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    /** 状态(0正常 1停用) */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
}
