-- =====================================================
-- 版本: 3.7.0
-- 描述: 玩家上下线记录表
-- 作者: Memory
-- 日期: 2026-02-09
-- =====================================================

CREATE TABLE IF NOT EXISTS `player_online_record`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_name`    varchar(128) NOT NULL COMMENT '玩家昵称',
    `whitelist_id` bigint                DEFAULT NULL COMMENT '白名单ID',
    `login_time`   datetime     NOT NULL COMMENT '上线时间',
    `logout_time`  datetime              DEFAULT NULL COMMENT '下线时间',
    `play_minutes` int                   DEFAULT NULL COMMENT '本次游玩时长(分钟)',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`       varchar(255)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_player_online_record_user` (`user_name`),
    KEY `idx_player_online_record_whitelist` (`whitelist_id`),
    KEY `idx_player_online_record_login_time` (`login_time`),
    KEY `idx_player_online_record_logout_time` (`logout_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='玩家上下线记录表';
