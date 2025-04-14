package cc.endmc.server.enums;

import lombok.Getter;

@Getter
public enum Identity {
    // 玩家
    PLAYER("player", "玩家"),
    // OP
    OPERATOR("operator", "OP"),
    // 封禁
    BANNED("banned", "已封禁"),
    // 其他
    OTHER("other", "其他");

    private final String value;

    private final String desc;

    Identity(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
