-- 白名单ID更改历史表
CREATE TABLE IF NOT EXISTS `whitelist_id_change_history`
(
    `id`            BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `old_user_name` VARCHAR(50) NOT NULL COMMENT '旧游戏ID',
    `new_user_name` VARCHAR(50) NOT NULL COMMENT '新游戏ID',
    `old_user_uuid` VARCHAR(100) COMMENT '旧UUID',
    `new_user_uuid` VARCHAR(100) COMMENT '新UUID',
    `qq_num`        VARCHAR(20) NOT NULL COMMENT 'QQ号',
    `change_reason` VARCHAR(500) COMMENT '更改原因',
    `change_time`   DATETIME    NOT NULL COMMENT '更改时间',
    `ip_address`    VARCHAR(50) COMMENT 'IP地址',
    `status`        CHAR(1)      DEFAULT '1' COMMENT '状态(0-失败 1-成功)',
    `create_by`     VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time`   DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_old_user_name` (`old_user_name`),
    KEY `idx_new_user_name` (`new_user_name`),
    KEY `idx_qq_num` (`qq_num`),
    KEY `idx_change_time` (`change_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='白名单ID更改历史表';
