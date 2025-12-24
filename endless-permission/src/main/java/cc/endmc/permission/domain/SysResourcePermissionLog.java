package cc.endmc.permission.domain;

import cc.endmc.common.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 资源权限操作日志对象 sys_resource_permission_log
 *
 * @author Memory
 * @date 2025-12-20
 */
@Data
public class SysResourcePermissionLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long id;

    /** 操作用户ID */
    @Excel(name = "操作用户ID")
    private Long userId;

    /** 操作用户名 */
    @Excel(name = "操作用户名")
    private String userName;

    /** 资源类型(rcon_server/node_server/mc_instance) */
    @Excel(name = "资源类型")
    private String resourceType;

    /** 资源ID */
    @Excel(name = "资源ID")
    private Long resourceId;

    /** 资源名称 */
    @Excel(name = "资源名称")
    private String resourceName;

    /** 操作类型(grant/revoke/modify/access) */
    @Excel(name = "操作类型")
    private String actionType;

    /** 目标类型(user/role) */
    @Excel(name = "目标类型")
    private String targetType;

    /** 目标ID */
    @Excel(name = "目标ID")
    private Long targetId;

    /** 目标名称 */
    @Excel(name = "目标名称")
    private String targetName;

    /** 权限详情(JSON格式) */
    private String permissionDetail;

    /** 操作IP */
    @Excel(name = "操作IP")
    private String ipAddress;

    /** 操作状态(0成功 1失败) */
    @Excel(name = "状态", readConverterExp = "0=成功,1=失败")
    private String status;

    /** 错误信息 */
    private String errorMsg;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
