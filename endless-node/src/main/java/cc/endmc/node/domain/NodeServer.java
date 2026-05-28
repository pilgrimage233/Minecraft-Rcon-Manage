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
 * 节点服务器对象 node_server
 *
 * @author Memory
 * @date 2025-04-14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NodeServer extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    private Long id;

    /**
     * 节点UUID
     */
    @Excel(name = "节点UUID")
    private String uuid;

    /**
     * 节点名称
     */
    @Excel(name = "节点名称")
    private String name;

    /**
     * 服务器IP
     */
    @Excel(name = "服务器IP")
    private String ip;

    /**
     * API端口
     */
    @Excel(name = "API端口")
    private Long port;

    /**
     * 通信协议(http/https)
     */
    @Excel(name = "通信协议(http/https)")
    private String protocol;

    /**
     * 秘钥（JSON 序列化时隐藏，防止 API 响应泄露）
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String token;

    /**
     * 允许反序列化时接收 token（序列化时仍隐藏）
     */
    @com.fasterxml.jackson.annotation.JsonProperty("token")
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * 获取脱敏后的 token（用于前端展示）
     */
    @com.fasterxml.jackson.annotation.JsonProperty("tokenMasked")
    public String getTokenMasked() {
        if (getToken() == null) return null;
        return getToken().length() > 4 ? getToken().substring(0, 4) + "***" : "***";
    }

    /**
     * 状态（0正常 1离线 2故障）
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=离线,2=故障")
    private String status;

    /**
     * 最后心跳时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后心跳时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastHeartbeat;

    /**
     * 节点版本
     */
    @Excel(name = "节点版本")
    private String version;

    /**
     * 操作系统类型
     */
    @Excel(name = "操作系统类型")
    private String osType;

    /**
     * 节点描述
     */
    @Excel(name = "节点描述")
    private String description;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private String delFlag;

    @Override
    public String toString() {
        // token 脱敏：仅显示前4位 + ***
        String maskedToken = (getToken() != null && getToken().length() > 4)
                ? getToken().substring(0, 4) + "***" : "***";
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("uuid", getUuid())
                .append("name", getName())
                .append("ip", getIp())
                .append("port", getPort())
                .append("protocol", getProtocol())
                .append("token", maskedToken)
                .append("status", getStatus())
                .append("lastHeartbeat", getLastHeartbeat())
                .append("version", getVersion())
                .append("osType", getOsType())
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
