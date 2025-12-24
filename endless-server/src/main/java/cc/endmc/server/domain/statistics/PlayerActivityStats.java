package cc.endmc.server.domain.statistics;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 玩家活跃度统计实体
 *
 * @author EndMC
 * @date 2024-12-15
 */
@Data
public class PlayerActivityStats {

    /** 主键ID */
    private Long id;

    /** 统计日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date statsDate;

    /** 统计类型：daily-日报, weekly-周报, monthly-月报 */
    private String statsType;

    /** 活跃玩家数量 */
    private Integer activePlayerCount;

    /** 新增玩家数量 */
    private Integer newPlayerCount;

    /** 总在线时长（分钟） */
    private Long totalOnlineMinutes;

    /** 平均在线时长（分钟） */
    private Long avgOnlineMinutes;

    /** 峰值在线人数 */
    private Integer peakOnlineCount;

    /** 峰值在线时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date peakOnlineTime;

    /** 活跃玩家列表（JSON格式） */
    private String activePlayerList;

    /** 新增玩家列表（JSON格式） */
    private String newPlayerList;

    /** 统计周期开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date periodStart;

    /** 统计周期结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date periodEnd;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 备注 */
    private String remark;
}