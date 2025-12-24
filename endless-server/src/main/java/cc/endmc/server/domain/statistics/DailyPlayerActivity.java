package cc.endmc.server.domain.statistics;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 每日玩家活跃度记录
 *
 */
@Data
public class DailyPlayerActivity {

    /** 主键ID */
    private Long id;

    /** 玩家名 */
    private String playerName;

    /** 玩家ID（关联白名单） */
    private Long playerId;

    /** 记录日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date activityDate;

    /** 当日在线时长（分钟） */
    private Long onlineMinutes;

    /** 当日登录次数 */
    private Integer loginCount;

    /** 首次登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date firstLoginTime;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    /** 最后离线时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLogoutTime;

    /** 是否为新玩家（当日首次加入） */
    private Boolean isNewPlayer;

    /** 活跃度评分（基于在线时长和登录次数） */
    private Double activityScore;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}