package cc.endmc.server.domain.relation;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RCON和节点实例关联对象 rcon_node_instance_relation
 *
 * @author Memory
 * @date 2025-12-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RconNodeInstanceRelation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * RCON服务器ID
     */
    @Excel(name = "RCON服务器ID")
    private Long rconServerId;

    /**
     * RCON服务器名称(非数据库字段)
     */
    private String rconServerName;

    /**
     * 节点ID
     */
    @Excel(name = "节点ID")
    private Long nodeId;

    /**
     * 节点名称(非数据库字段)
     */
    private String nodeName;

    /**
     * 实例ID
     */
    @Excel(name = "实例ID")
    private Long instanceId;

    /**
     * 实例名称(非数据库字段)
     */
    private String instanceName;

    /**
     * 状态
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
}