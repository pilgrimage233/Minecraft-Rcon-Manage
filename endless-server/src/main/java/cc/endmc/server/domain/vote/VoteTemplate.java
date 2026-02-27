package cc.endmc.server.domain.vote;

import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 投票模板配置对象 vote_template
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VoteTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String templateCode;
    private String templateName;
    private String templateDesc;
    private String targetType;
    private String actionType;
    private String actionCommandTemplate;
    private Integer minRequiredVotes;
    private Integer voteDurationSeconds;
    private Integer needReason;
    private Integer enabled;
    private Integer sortOrder;
    private String extraConfig;
}
