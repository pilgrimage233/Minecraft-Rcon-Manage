package cc.endmc.server.dto;

import lombok.Data;

/**
 * 自定义投票模板创建请求
 */
@Data
public class VoteTemplateCreateRequest {
    private String templateName;
    private String templateDesc;
    private String actionCommandTemplate;
    private Integer minRequiredVotes;
    private Integer voteDurationSeconds;
    private Integer needReason;
    private String targetType;
}
