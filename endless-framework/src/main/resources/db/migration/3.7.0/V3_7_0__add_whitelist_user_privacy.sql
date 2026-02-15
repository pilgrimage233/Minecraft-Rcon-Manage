-- =====================================================
-- 版本: 3.7.0
-- 描述: 白名单用户隐私设置
-- 作者: Memory
-- 日期: 2026-02-09
-- =====================================================

CREATE TABLE IF NOT EXISTS `whitelist_user_privacy`
(
    `id`                bigint     NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `whitelist_id`      bigint     NOT NULL COMMENT '白名单ID',
    `show_qq`           tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否展示QQ号',
    `show_city`         tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否展示城市',
    `show_last_online`  tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否展示最后上线时间',
    `show_game_time`    tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否展示游戏时间',
    `show_name_history` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否展示历史名称',
    `show_quiz_result`  tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否展示答题信息',
    `show_uuid`         tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否展示UUID',
    `create_by`         varchar(64)         DEFAULT '' COMMENT '创建者',
    `create_time`       datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`         varchar(64)         DEFAULT '' COMMENT '更新者',
    `update_time`       datetime            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`            varchar(500)        DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_whitelist_privacy_whitelist_id` (`whitelist_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='白名单用户隐私设置表';
