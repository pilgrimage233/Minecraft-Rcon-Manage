package cc.endmc.node.domain;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 实例管理对象 node_minecraft_server
 *
 * @author ruoyi
 * @date 2025-10-28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NodeMinecraftServer extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 游戏服务器ID
     */
    private Long id;

    /**
     * 所属节点ID
     */
    @Excel(name = "所属节点ID")
    private Long nodeId;

    /**
     * 节点UUID
     */
    @Excel(name = "节点UUID")
    private String nodeUuid;

    /**
     * 关联节点实例ID
     */
    @Excel(name = "关联节点实例ID")
    private Integer nodeInstancesId;

    /**
     * 服务器名称
     */
    @Excel(name = "服务器名称")
    private String name;

    /**
     * 服务端所在目录
     */
    @Excel(name = "服务端所在目录")
    private String serverPath;

    /**
     * 启动命令
     */
    @Excel(name = "启动命令")
    private String startStr;

    /**
     * Java路径
     */
    @Excel(name = "Java路径")
    private String javaPath;

    /**
     * Java环境ID
     */
    @Excel(name = "Java环境ID")
    private String javaEnvId;

    /**
     * 最大堆内存(XMX)
     */
    @Excel(name = "最大堆内存(XMX)")
    private String jvmXmx;

    /**
     * 最小堆内存(XMS)
     */
    @Excel(name = "最小堆内存(XMS)")
    private String jvmXms;

    /**
     * 其他JVM参数
     */
    @Excel(name = "其他JVM参数")
    private String jvmArgs;

    /**
     * 核心类型(如：Paper、Spigot、Bukkit等)
     */
    @Excel(name = "核心类型(如：Paper、Spigot、Bukkit等)")
    private String coreType;

    /**
     * 核心版本
     */
    @Excel(name = "核心版本")
    private String version;

    /**
     * 服务器状态（0未启动 1运行中 2已停止 3异常）
     */
    @Excel(name = "服务器状态", readConverterExp = "0=未启动,1=运行中,2=已停止,3=异常")
    private String status;

    /**
     * 最后启动时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后启动时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastStartTime;

    /**
     * 最后停止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后停止时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastStopTime;

    /**
     * 服务器描述
     */
    @Excel(name = "服务器描述")
    private String description;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private String delFlag;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("nodeId", getNodeId())
                .append("nodeUuid", getNodeUuid())
                .append("name", getName())
                .append("serverPath", getServerPath())
                .append("startStr", getStartStr())
                .append("jvmXmx", getJvmXmx())
                .append("jvmXms", getJvmXms())
                .append("jvmArgs", getJvmArgs())
                .append("coreType", getCoreType())
                .append("version", getVersion())
                .append("status", getStatus())
                .append("lastStartTime", getLastStartTime())
                .append("lastStopTime", getLastStopTime())
                .append("description", getDescription())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
