package com.ruoyi.server.ws;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class QQMessage {
    @JSONField(name = "self_id")
    private Long selfId;

    @JSONField(name = "user_id")
    private Long userId;

    private Long time;

    @JSONField(name = "message_id")
    private Long messageId;

    @JSONField(name = "message_seq")
    private Long messageSeq;

    @JSONField(name = "real_id")
    private Long realId;

    @JSONField(name = "message_type")
    private String messageType;

    @JSONField(name = "notice_type")
    private String noticeType;

    private Sender sender;

    @JSONField(name = "raw_message")
    private String rawMessage;

    private Integer font;

    @JSONField(name = "sub_type")
    private String subType;

    private String message;

    @JSONField(name = "message_format")
    private String messageFormat;

    @JSONField(name = "post_type")
    private String postType;

    @JSONField(name = "group_id")
    private Long groupId;

    @Data
    public static class Sender {
        @JSONField(name = "user_id")
        private Long userId;

        private String nickname;
        private String card;
        private String role;
    }
} 