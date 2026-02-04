package cc.endmc.server.domain.permission;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 白名单ID更改历史对象 whitelist_id_change_history
 *
 * @author endmc
 * @date 2026-02-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WhitelistIdChangeHistory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 旧游戏ID
     */
    @Excel(name = "旧游戏ID")
    private String oldUserName;

    /**
     * 新游戏ID
     */
    @Excel(name = "新游戏ID")
    private String newUserName;

    /**
     * 旧UUID
     */
    @Excel(name = "旧UUID")
    private String oldUserUuid;

    /**
     * 新UUID
     */
    @Excel(name = "新UUID")
    private String newUserUuid;

    /**
     * QQ号
     */
    @Excel(name = "QQ号")
    private String qqNum;

    /**
     * 更改原因
     */
    @Excel(name = "更改原因")
    private String changeReason;

    /**
     * 更改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "更改时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date changeTime;

    /**
     * IP地址
     */
    @Excel(name = "IP地址")
    private String ipAddress;

    /**
     * 状态(0-失败 1-成功)
     */
    @Excel(name = "状态", readConverterExp = "0=失败,1=成功")
    private String status;
}
