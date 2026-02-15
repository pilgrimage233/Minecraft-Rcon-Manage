-- =====================================================
-- 版本: 3.7.0
-- 描述: 白名单用户登录表
-- 作者: Memory
-- 日期: 2026-02-07
-- =====================================================

CREATE TABLE IF NOT EXISTS `whitelist_user`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `whitelist_id`    bigint       NOT NULL COMMENT '白名单ID',
    `user_name`       varchar(64)  NOT NULL COMMENT '登录账号',
    `qq_num`          varchar(20)  NOT NULL COMMENT 'QQ号',
    `password`        varchar(100) NOT NULL COMMENT '密码(BCrypt)',
    `status`          char(1)      NOT NULL DEFAULT '0' COMMENT '状态(0-正常 1-停用)',
    `last_login_time` datetime              DEFAULT NULL COMMENT '最后登录时间',
    `create_by`       varchar(64)           DEFAULT '' COMMENT '创建者',
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       varchar(64)           DEFAULT '' COMMENT '更新者',
    `update_time`     datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`          varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_whitelist_user_name` (`user_name`),
    UNIQUE KEY `uk_whitelist_user_qq` (`qq_num`),
    KEY `idx_whitelist_user_whitelist_id` (`whitelist_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='白名单用户表';
