package cc.endmc.permission.domain;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限模板对象 sys_permission_template
 *
 * @author Memory
 * @date 2025-12-20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysPermissionTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 模板ID */
    private Long id;

    /** 模板名称 */
    @Excel(name = "模板名称")
    private String templateName;

    /** 模板标识 */
    @Excel(name = "模板标识")
    private String templateKey;

    /** 资源类型(rcon_server/node_server/mc_instance) */
    @Excel(name = "资源类型")
    private String resourceType;

    /** 权限配置(JSON格式) */
    private String permissionConfig;

    /** 模板描述 */
    @Excel(name = "模板描述")
    private String description;

    /** 是否系统内置(0否 1是) */
    @Excel(name = "系统内置", readConverterExp = "0=否,1=是")
    private Integer isSystem;

    /** 状态(0正常 1停用) */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
}
