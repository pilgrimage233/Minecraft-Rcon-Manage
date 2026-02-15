package cc.endmc.server.domain.player;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 玩家上下线记录对象 player_online_record
 *
 * @author Memory
 * @date 2026-02-09
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerOnlineRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 玩家昵称
     */
    @Excel(name = "玩家昵称")
    private String userName;

    /**
     * 白名单ID
     */
    @Excel(name = "白名单ID")
    private Long whitelistId;

    /**
     * 上线时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "上线时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date loginTime;

    /**
     * 下线时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "下线时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date logoutTime;

    /**
     * 本次游玩时长(分钟)
     */
    @Excel(name = "本次游玩时长(分钟)")
    private Long playMinutes;
}
