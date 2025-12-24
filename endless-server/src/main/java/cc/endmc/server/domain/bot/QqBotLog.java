package cc.endmc.server.domain.bot;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 机器人日志对象 qq_bot_log
 *
 * @author Memory
 * @date 2025-04-18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QqBotLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 关联的机器人ID
     */
    @Excel(name = "关联的机器人ID")
    private Long botId;

    /**
     * 日志类型：1=接收消息，2=发送消息，3=方法调用，4=系统事件
     */
    @Excel(name = "日志类型：1=接收消息，2=发送消息，3=方法调用，4=系统事件")
    private Long logType;

    /**
     * 消息ID
     */
    @Excel(name = "消息ID")
    private String messageId;

    /**
     * 发送者ID
     */
    @Excel(name = "发送者ID")
    private String senderId;

    /**
     * 发送者类型：user=用户，group=群组
     */
    @Excel(name = "发送者类型：user=用户，group=群组")
    private String senderType;

    /**
     * 接收者ID
     */
    @Excel(name = "接收者ID")
    private String receiverId;

    /**
     * 接收者类型：user=用户，group=群组
     */
    @Excel(name = "接收者类型：user=用户，group=群组")
    private String receiverType;

    /**
     * 消息内容
     */
    @Excel(name = "消息内容")
    private String messageContent;

    /**
     * 消息类型：text=文本，image=图片，voice=语音，file=文件等
     */
    @Excel(name = "消息类型：text=文本，image=图片，voice=语音，file=文件等")
    private String messageType;

    /**
     * 调用的方法名称
     */
    @Excel(name = "调用的方法名称")
    private String methodName;

    /**
     * 方法参数(JSON格式)
     */
    @Excel(name = "方法参数(JSON格式)")
    private String methodParams;

    /**
     * 方法执行结果
     */
    @Excel(name = "方法执行结果")
    private String methodResult;

    /**
     * 方法执行时间(毫秒)
     */
    @Excel(name = "方法执行时间(毫秒)")
    private Long executionTime;

    /**
     * 错误信息
     */
    @Excel(name = "错误信息")
    private String errorMessage;

    /**
     * 错误堆栈信息
     */
    @Excel(name = "错误堆栈信息")
    private String stackTrace;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("botId", getBotId())
                .append("logType", getLogType())
                .append("messageId", getMessageId())
                .append("senderId", getSenderId())
                .append("senderType", getSenderType())
                .append("receiverId", getReceiverId())
                .append("receiverType", getReceiverType())
                .append("messageContent", getMessageContent())
                .append("messageType", getMessageType())
                .append("methodName", getMethodName())
                .append("methodParams", getMethodParams())
                .append("methodResult", getMethodResult())
                .append("executionTime", getExecutionTime())
                .append("errorMessage", getErrorMessage())
                .append("stackTrace", getStackTrace())
                .append("createTime", getCreateTime())
                .toString();
    }
}
