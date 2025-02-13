package com.ruoyi.server.domain.player;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 玩家详情对象 player_details
 *
 * @author Memory
 * @date 2024-12-31
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerDetails extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private Long id;

    /**
     * 玩家昵称
     */
    @Excel(name = "玩家昵称")
    private String userName;

    /**
     * QQ号
     */
    @Excel(name = "QQ号")
    private String qq;

    /**
     * 身份
     */
    @Excel(name = "身份")
    private String identity;
    /**
     * 最后在线时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后在线时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastOnlineTime;

    /**
     * 最后离线时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后离线时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastOfflineTime;

    /**
     * 游戏时间(分钟)
     */
    @Excel(name = "游戏时间(分钟)")
    private Long gameTime;

    /**
     * 省份
     */
    @Excel(name = "省份")
    private String province;

    /**
     * 地市
     */
    @Excel(name = "地市")
    private String city;

    /**
     * 白名单ID
     */
    private Long whitelistId;

    /**
     * 封禁ID
     */
    private Long banlistId;

    /**
     * 其他参数
     */
    private String parameters;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("userName", getUserName())
                .append("qq", getQq())
                .append("identity", getIdentity())
                .append("lastOnlineTime", getLastOnlineTime())
                .append("lastOfflineTime", getLastOfflineTime())
                .append("province", getProvince())
                .append("city", getCity())
                .append("whitelistId", getWhitelistId())
                .append("banlistId", getBanlistId())
                .append("parameters", getParameters())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .append("createBy", getCreateBy())
                .append("updateBy", getUpdateBy())
                .append("remark", getRemark())
                .toString();
    }
}
