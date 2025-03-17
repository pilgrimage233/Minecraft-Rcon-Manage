package cc.endmc.server.domain.bot;

import cc.endmc.common.annotation.Excel;
import cc.endmc.common.core.domain.BaseEntity;
import cc.endmc.common.utils.spring.SpringUtils;
import cc.endmc.server.service.bot.IQqBotManagerService;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;
import java.util.stream.Collectors;

/**
 * QQ机器人配置对象 qq_bot_config
 *
 * @author ruoyi
 * @date 2025-03-12
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QqBotConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 机器人名称
     */
    @Excel(name = "机器人名称")
    private String name;

    /**
     * 机器人QQ
     */
    @Excel(name = "机器人QQ")
    private String botQq;

    /**
     * HTTP通讯地址
     */
    private String httpUrl;

    /**
     * websocket地址
     */
    private String wsUrl;

    /**
     * 秘钥
     */
    private String token;

    /**
     * 群组
     */
    @Excel(name = "群组")
    private String groupIds;

    /**
     * 命令前缀
     */
    @Excel(name = "命令前缀")
    private String commandPrefix;

    /**
     * 机器人描述
     */
    private String description;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastLoginTime;

    /**
     * 最后心跳时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后心跳时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastHeartbeatTime;

    /**
     * 最后一次错误信息
     */
    private String errorMsg;

    /**
     * 启用状态
     */
    @Excel(name = "启用状态")
    private Long status;

    /**
     * 获取群号列表
     * 将配置中的群号字符串转换为Long类型的List
     *
     * @return 群号List
     */
    public List<Long> getGroupIdList() {
        if (groupIds == null || groupIds.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(groupIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    /**
     * 获取管理员QQ号列表
     * 从管理员表中获取该机器人的所有管理员QQ号
     *
     * @return 管理员QQ号List
     */
    public List<Long> getManagerIdList() {
        if (id == null) {
            return Collections.emptyList();
        }
        QqBotManager query = new QqBotManager();
        query.setBotId(id);
        query.setStatus(1L); // 只获取启用状态的管理员
        List<QqBotManager> managers = SpringUtils.getBean(IQqBotManagerService.class).selectQqBotManagerList(query);
        return managers.stream()
                .map(manager -> Long.parseLong(manager.getManagerQq()))
                .collect(Collectors.toList());
    }

    public List<QqBotManager> selectManagerForThisGroup(Long groupId, Long managerQq) {
        Map<String, Object> query = new HashMap<>();
        query.put("botId", id);
        query.put("groupId", groupId.toString());
        query.put("managerQq", managerQq.toString());
        return SpringUtils.getBean(IQqBotManagerService.class).selectQqBotManagerByBotIdAndManagerQqAndGroupId(query);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("name", getName())
                .append("botQq", getBotQq())
                .append("httpUrl", getHttpUrl())
                .append("wsUrl", getWsUrl())
                .append("token", getToken())
                .append("groupIds", getGroupIds())
                .append("commandPrefix", getCommandPrefix())
                .append("description", getDescription())
                .append("lastLoginTime", getLastLoginTime())
                .append("lastHeartbeatTime", getLastHeartbeatTime())
                .append("errorMsg", getErrorMsg())
                .append("status", getStatus())
                .append("createTime", getCreateTime())
                .append("createBy", getCreateBy())
                .append("updateTime", getUpdateTime())
                .append("updateBy", getUpdateBy())
                .append("remark", getRemark())
                .toString();
    }
}