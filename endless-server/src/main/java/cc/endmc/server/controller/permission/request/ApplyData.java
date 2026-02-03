package cc.endmc.server.controller.permission.request;

import cc.endmc.server.domain.permission.WhitelistInfo;
import cc.endmc.server.domain.player.PlayerDetails;
import lombok.Data;

/**
 * 申请数据
 *
 * @author Memory
 */
@Data
public class ApplyData {
    /**
     * 白名单信息
     */
    WhitelistInfo whitelistInfo;

    /**
     * 玩家详情
     */
    PlayerDetails details;

    public ApplyData(WhitelistInfo whitelistInfo, PlayerDetails details) {
        this.whitelistInfo = whitelistInfo;
        this.details = details;
    }
}