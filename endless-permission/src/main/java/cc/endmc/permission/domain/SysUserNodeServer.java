package cc.endmc.permission.domain;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 用户-节点服务器权限关联对象 sys_user_node_server
 *
 * @author Memory
 * @date 2025-12-20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysUserNodeServer extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名(非数据库字段) */
    private String userName;

    /** 节点ID */
    @Excel(name = "节点ID")
    private Long nodeId;

    /** 节点名称(非数据库字段) */
    private String nodeName;

    /** 权限类型(view/operate/manage/admin) */
    @Excel(name = "权限类型")
    private String permissionType;

    /** 是否可查看节点信息 */
    @Excel(name = "可查看", readConverterExp = "0=否,1=是")
    private Integer canView;

    /** 是否可操作节点 */
    @Excel(name = "可操作", readConverterExp = "0=否,1=是")
    private Integer canOperate;

    /** 是否可管理节点配置 */
    @Excel(name = "可管理", readConverterExp = "0=否,1=是")
    private Integer canManage;

    /** 是否可创建实例 */
    @Excel(name = "可创建实例", readConverterExp = "0=否,1=是")
    private Integer canCreateInstance;

    /** 权限过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "过期时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    /** 状态(0正常 1停用) */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
}
