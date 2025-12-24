-- 用户反馈记录表（本地存储，通过UUID关联远程反馈系统）
CREATE TABLE IF NOT EXISTS `sys_feedback_record`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `uuid`          varchar(64)  NOT NULL COMMENT '反馈UUID（关联远程反馈系统）',
    `user_id`       bigint      DEFAULT NULL COMMENT '用户ID',
    `user_name`     varchar(64) DEFAULT NULL COMMENT '用户名',
    `feedback_type` int          NOT NULL COMMENT '反馈类型：1-Bug反馈 2-功能建议 3-使用问题 4-其他',
    `title`         varchar(200) NOT NULL COMMENT '反馈标题',
    `status`        int         DEFAULT 0 COMMENT '状态：0-待处理 1-处理中 2-已解决 3-已关闭',
    `create_time`   datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_uuid` (`uuid`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户反馈记录表';
