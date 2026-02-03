package cc.endmc.server.controller.permission.request;

import lombok.Data;

/**
 * 验证来源信息
 *
 * @author Memory
 */
@Data
public class VerifySource {

    /**
     * 缓存键
     */
    String cacheKey;

    /**
     * 来源描述
     */
    String source;

    /**
     * 是否来自机器人
     */
    boolean isFromBot;

    /**
     * 是否来自批处理
     */
    boolean isFromBatch;

    public VerifySource(String cacheKey, String source, boolean isFromBot, boolean isFromBatch) {
        this.cacheKey = cacheKey;
        this.source = source;
        this.isFromBot = isFromBot;
        this.isFromBatch = isFromBatch;
    }
}
