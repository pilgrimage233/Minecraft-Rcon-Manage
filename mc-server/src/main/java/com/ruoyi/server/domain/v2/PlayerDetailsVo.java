package com.ruoyi.server.domain.v2;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class PlayerDetailsVo {

    private Long id;

    /**
     * 玩家昵称
     */
    private String userName;

    /**
     * QQ号
     */
    private String qq;

    /**
     * 身份
     */
    private String identity;
    /**
     * 最后在线时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastOnlineTime;

    /**
     * 最后离线时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastOfflineTime;

    /**
     * 游戏时间(分钟)
     */
    private Long gameTime;

}
