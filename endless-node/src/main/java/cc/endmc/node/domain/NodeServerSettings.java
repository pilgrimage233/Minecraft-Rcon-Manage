package cc.endmc.node.domain;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 实例服务器运维策略配置对象 node_server_settings
 *
 * @author Memory
 * @date 2026-05-29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NodeServerSettings extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "节点ID")
    private Long nodeId;

    @Excel(name = "实例ID")
    private Long nodeServerId;

    @Excel(name = "节点UUID")
    private String nodeUuid;

    /** 崩溃重启 */
    @Excel(name = "崩溃重启", readConverterExp = "0=关闭,1=开启")
    private Integer crashRestartEnabled;

    @Excel(name = "崩溃重启延迟(秒)")
    private Integer crashRestartDelaySec;

    @Excel(name = "最大重试次数")
    private Integer crashRestartMaxRetry;

    /** 持久在线 */
    @Excel(name = "持久在线", readConverterExp = "0=关闭,1=开启")
    private Integer keepAliveEnabled;

    @Excel(name = "保活检查间隔(秒)")
    private Integer keepAliveCheckIntervalSec;

    /** 定时开关机 */
    @Excel(name = "定时开机", readConverterExp = "0=关闭,1=开启")
    private Integer scheduledStartEnabled;

    private String scheduledStartCron;

    @Excel(name = "定时关机", readConverterExp = "0=关闭,1=开启")
    private Integer scheduledStopEnabled;

    private String scheduledStopCron;

    /** 定时重启 */
    @Excel(name = "定时重启", readConverterExp = "0=关闭,1=开启")
    private Integer scheduledRestartEnabled;

    private String scheduledRestartCron;

    /** 优雅关闭 */
    @Excel(name = "优雅关闭超时(秒)")
    private Integer gracefulShutdownTimeoutSec;

    /** 通知 */
    private String notifyEmail;

    @Excel(name = "崩溃通知", readConverterExp = "0=关闭,1=开启")
    private Integer crashNotifyEnabled;

    @Excel(name = "备份通知", readConverterExp = "0=关闭,1=开启")
    private Integer backupNotifyEnabled;

    /** 定时备份 */
    @Excel(name = "定时备份", readConverterExp = "0=关闭,1=开启")
    private Integer backupEnabled;

    private String backupCron;

    @Excel(name = "备份保留份数")
    private Integer backupRetainCount;

    /** 磁盘告警 */
    @Excel(name = "磁盘告警", readConverterExp = "0=关闭,1=开启")
    private Integer diskAlertEnabled;

    @Excel(name = "磁盘告警阈值(GB)")
    private Integer diskAlertThresholdGb;

    /** TPS */
    @Excel(name = "TPS采集方式")
    private String tpsMode;

    @Excel(name = "TPS告警", readConverterExp = "0=关闭,1=开启")
    private Integer tpsAlertEnabled;

    private Double tpsAlertThreshold;

    private Integer sparkApiPort;

    /** 删除标志 */
    private String delFlag;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("nodeId", getNodeId())
                .append("nodeServerId", getNodeServerId())
                .append("crashRestartEnabled", getCrashRestartEnabled())
                .append("keepAliveEnabled", getKeepAliveEnabled())
                .append("scheduledRestartEnabled", getScheduledRestartEnabled())
                .append("notifyEmail", getNotifyEmail())
                .append("backupEnabled", getBackupEnabled())
                .append("diskAlertEnabled", getDiskAlertEnabled())
                .append("tpsMode", getTpsMode())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
