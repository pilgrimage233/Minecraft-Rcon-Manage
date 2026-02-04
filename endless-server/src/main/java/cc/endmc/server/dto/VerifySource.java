package cc.endmc.server.dto;

import lombok.Data;

/**
 * 验证来源信息
 */
@Data
public class VerifySource {
    private final String cacheKey;
    private final String source;
    private final boolean fromBot;
    private final boolean fromBatch;

    public VerifySource(String cacheKey, String source, boolean fromBot, boolean fromBatch) {
        this.cacheKey = cacheKey;
        this.source = source;
        this.fromBot = fromBot;
        this.fromBatch = fromBatch;
    }

}