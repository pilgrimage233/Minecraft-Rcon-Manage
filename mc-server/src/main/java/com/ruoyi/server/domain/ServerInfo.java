package com.ruoyi.server.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 服务器信息对象 server_info
 *
 * @author ruoyi
 * @date 2024-03-10
 */
@Data
public class ServerInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 随机UUID
     */
    @Excel(name = "随机UUID")
    private String uuid;

    /**
     * 服务器名称标签
     */
    @Excel(name = "服务器名称标签")
    private String nameTag;

    /**
     * 服务器IP
     */
    @Excel(name = "服务器IP")
    private String ip;

    /**
     * RCON远程端口号
     */
    @Excel(name = "RCON远程端口号")
    private Long rconPort;

    /**
     * 远程密码/MD5加密
     */
    @Excel(name = "远程密码/MD5加密")
    private String rconPassword;

    /**
     * 启用状态
     */
    @Excel(name = "启用状态")
    private Long status;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("uuid", getUuid())
                .append("nameTag", getNameTag())
                .append("ip", getIp())
                .append("rconPort", getRconPort())
                .append("rconPassword", getRconPassword())
                .append("updateTime", getUpdateTime())
                .append("updateBy", getUpdateBy())
                .append("status", getStatus())
                .append("remark", getRemark())
                .toString();
    }
}
