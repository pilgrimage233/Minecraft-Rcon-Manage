package cc.endmc.server.domain.permission;

import cc.endmc.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 白名单用户隐私设置
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WhitelistUserPrivacy extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long whitelistId;

    private Integer showQq;
    private Integer showCity;
    private Integer showLastOnline;
    private Integer showGameTime;
    private Integer showNameHistory;
    private Integer showQuizResult;
    private Integer showUuid;
}
