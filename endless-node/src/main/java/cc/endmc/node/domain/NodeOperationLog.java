package cc.endmc.node.domain;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 操作日志对象 node_operation_log
 *
 * @author Memory
 * @date 2025-04-24
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NodeOperationLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 节点ID
     */
    @Excel(name = "节点ID")
    private Long nodeId;

    /**
     * 操作类型（1新增节点 2修改节点 3删除节点 4下载日志 5启动游戏服务器 6停止游戏服务器 7重启游戏服务器 8强制终止游戏服务器 9新增游戏服务器 10修改游戏服务器 11删除游戏服务器）
     */
    @Excel(name = "操作类型", readConverterExp = "1=新增节点,2=修改节点,3=删除节点,4=下载日志,5=启动游戏服务器,6=停止游戏服务器,7=重启游戏服务器,8=强制终止游戏服务器,9=新增游戏服务器,1=0修改游戏服务器,1=1删除游戏服务器")
    private String operationType;

    /**
     * 操作目标类型（1节点服务器 2游戏服务器）
     */
    @Excel(name = "操作目标类型", readConverterExp = "1=节点服务器,2=游戏服务器")
    private String operationTarget;

    /**
     * 节点对象ID
     */
    @Excel(name = "节点对象ID")
    private Long nodeObjId;

    /**
     * 游戏服务器对象ID
     */
    @Excel(name = "游戏服务器对象ID")
    private Long gameServerObjId;

    /**
     * 操作名称
     */
    @Excel(name = "操作名称")
    private String operationName;

    /**
     * 方法名称
     */
    @Excel(name = "方法名称")
    private String methodName;

    /**
     * 操作参数
     */
    @Excel(name = "操作参数")
    private String operationParam;

    /**
     * 操作结果详情
     */
    @Excel(name = "操作结果详情")
    private String operationResult;

    /**
     * 执行耗时(ms)
     */
    @Excel(name = "执行耗时(ms)")
    private Long executionTime;

    /**
     * 操作者IP地址
     */
    @Excel(name = "操作者IP地址")
    private String operationIp;

    /**
     * 操作状态（0成功 1失败）
     */
    @Excel(name = "操作状态", readConverterExp = "0=成功,1=失败")
    private String status;

    /**
     * 错误消息
     */
    @Excel(name = "错误消息")
    private String errorMsg;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private String delFlag;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("nodeId", getNodeId())
                .append("operationType", getOperationType())
                .append("operationTarget", getOperationTarget())
                .append("nodeObjId", getNodeObjId())
                .append("gameServerObjId", getGameServerObjId())
                .append("operationName", getOperationName())
                .append("methodName", getMethodName())
                .append("operationParam", getOperationParam())
                .append("operationResult", getOperationResult())
                .append("executionTime", getExecutionTime())
                .append("operationIp", getOperationIp())
                .append("status", getStatus())
                .append("errorMsg", getErrorMsg())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
