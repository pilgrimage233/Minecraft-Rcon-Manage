package cc.endmc.system.domain;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;

import cc.endmc.common.core.domain.BaseEntity;
import cc.endmc.common.xss.Xss;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 通知公告表 sys_notice
 * 
 * @author ruoyi
 */
public class SysNotice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 公告ID */
    private Long noticeId;

    /** 公告标题 */
    private String noticeTitle;

    /** 公告类型（1通知 2公告） */
    private String noticeType;

    /** 公告内容 */
    private String noticeContent;

    /** 公告状态（0正常 1关闭） */
    private String status;

    /** 类型颜色（如 #3b82f6） */
    private String typeColor;

    /** 生效开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveStartTime;

    /** 生效结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveEndTime;

    /** 是否在前台展示（0否 1是） */
    private String showInFrontend;

    /** 是否置顶（0否 1是） */
    private String isPinned;

    public Long getNoticeId()
    {
        return noticeId;
    }

    public void setNoticeId(Long noticeId)
    {
        this.noticeId = noticeId;
    }

    public void setNoticeTitle(String noticeTitle)
    {
        this.noticeTitle = noticeTitle;
    }

    @Xss(message = "公告标题不能包含脚本字符")
    @NotBlank(message = "公告标题不能为空")
    @Size(min = 0, max = 50, message = "公告标题不能超过50个字符")
    public String getNoticeTitle()
    {
        return noticeTitle;
    }

    public void setNoticeType(String noticeType)
    {
        this.noticeType = noticeType;
    }

    public String getNoticeType()
    {
        return noticeType;
    }

    public void setNoticeContent(String noticeContent)
    {
        this.noticeContent = noticeContent;
    }

    public String getNoticeContent()
    {
        return noticeContent;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public String getTypeColor()
    {
        return typeColor;
    }

    public void setTypeColor(String typeColor)
    {
        this.typeColor = typeColor;
    }

    public Date getEffectiveStartTime()
    {
        return effectiveStartTime;
    }

    public void setEffectiveStartTime(Date effectiveStartTime)
    {
        this.effectiveStartTime = effectiveStartTime;
    }

    public Date getEffectiveEndTime()
    {
        return effectiveEndTime;
    }

    public void setEffectiveEndTime(Date effectiveEndTime)
    {
        this.effectiveEndTime = effectiveEndTime;
    }

    public String getShowInFrontend()
    {
        return showInFrontend;
    }

    public void setShowInFrontend(String showInFrontend)
    {
        this.showInFrontend = showInFrontend;
    }

    public String getIsPinned()
    {
        return isPinned;
    }

    public void setIsPinned(String isPinned)
    {
        this.isPinned = isPinned;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("noticeId", getNoticeId())
            .append("noticeTitle", getNoticeTitle())
            .append("noticeType", getNoticeType())
            .append("noticeContent", getNoticeContent())
            .append("status", getStatus())
            .append("typeColor", getTypeColor())
            .append("effectiveStartTime", getEffectiveStartTime())
            .append("effectiveEndTime", getEffectiveEndTime())
            .append("showInFrontend", getShowInFrontend())
            .append("isPinned", getIsPinned())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
