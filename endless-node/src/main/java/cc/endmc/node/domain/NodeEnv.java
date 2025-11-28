package cc.endmc.node.domain;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 节点Java多版本环境管理对象 node_env
 *
 * @author Memory
 * @date 2025-11-25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NodeEnv extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 节点ID
     */
    @Excel(name = "节点ID")
    private Long nodeId;

    /**
     * Java版本号
     */
    @Excel(name = "Java版本号")
    private String version;

    /**
     * 环境名称自定义
     */
    @Excel(name = "环境名称自定义")
    private String envName;

    /**
     * Java安装根路径（可能不是JAVA_HOME）
     */
    @Excel(name = "Java安装根路径", readConverterExp = "可=能不是JAVA_HOME")
    private String path;

    /**
     * JAVA_HOME路径
     */
    @Excel(name = "JAVA_HOME路径")
    private String javaHome;

    /**
     * bin目录路径
     */
    @Excel(name = "bin目录路径")
    private String binPath;

    /**
     * 安装类型
     */
    @Excel(name = "安装类型")
    private String type;

    /**
     * 架构
     */
    @Excel(name = "架构")
    private String arch;

    /**
     * 默认版本
     */
    @Excel(name = "默认版本")
    private Integer isDefault;

    /**
     * 路径是否有效
     */
    @Excel(name = "路径是否有效")
    private Integer valid;

    /**
     * 来源
     */
    @Excel(name = "来源")
    private String source;

    /**
     * 状态
     */
    @Excel(name = "状态")
    private Long status;

    /**
     * 节点服务器名称（关联查询字段，不存储在数据库）
     */
    private String serverName;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("nodeId", getNodeId())
                .append("version", getVersion())
                .append("envName", getEnvName())
                .append("path", getPath())
                .append("javaHome", getJavaHome())
                .append("binPath", getBinPath())
                .append("type", getType())
                .append("arch", getArch())
                .append("isDefault", getIsDefault())
                .append("valid", getValid())
                .append("source", getSource())
                .append("status", getStatus())
                .append("createTime", getCreateTime())
                .append("createBy", getCreateBy())
                .append("updateTime", getUpdateTime())
                .append("updateBy", getUpdateBy())
                .append("remark", getRemark())
                .toString();
    }
}
