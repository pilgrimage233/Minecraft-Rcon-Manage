-- =====================================================
-- 版本: 3.8.0
-- 描述: 新增通用投票系统（模板配置 + 投票实例 + 投票记录）
-- 作者: Memory
-- 日期: 2026-02-16
-- =====================================================

CREATE TABLE IF NOT EXISTS `vote_template`
(
    `id`                      bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_code`           varchar(64)  NOT NULL COMMENT '模板编码(唯一，如 KICK_PLAYER)',
    `template_name`           varchar(128) NOT NULL COMMENT '模板名称',
    `template_desc`           varchar(500)          DEFAULT NULL COMMENT '模板描述',
    `target_type`             varchar(32)  NOT NULL DEFAULT 'PLAYER' COMMENT '投票目标类型(PLAYER/WHITELIST/OTHER)',
    `action_type`             varchar(32)  NOT NULL DEFAULT 'RCON_COMMAND' COMMENT '执行类型(RCON_COMMAND/SYSTEM_EVENT/CUSTOM)',
    `action_command_template` varchar(500)          DEFAULT NULL COMMENT '执行命令模板(支持占位符，如 kick {targetPlayer})',
    `min_required_votes`      int          NOT NULL DEFAULT 3 COMMENT '最少通过票数',
    `vote_duration_seconds`   int          NOT NULL DEFAULT 300 COMMENT '投票持续时间(秒)',
    `need_reason`             tinyint(1)   NOT NULL DEFAULT 0 COMMENT '发起时是否必须填写原因(0否 1是)',
    `enabled`                 tinyint(1)   NOT NULL DEFAULT 1 COMMENT '是否启用(0禁用 1启用)',
    `sort_order`              int          NOT NULL DEFAULT 0 COMMENT '排序',
    `extra_config`            text                  DEFAULT NULL COMMENT '扩展配置(JSON文本)',
    `create_by`               varchar(64)           DEFAULT '' COMMENT '创建者',
    `create_time`             datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`               varchar(64)           DEFAULT '' COMMENT '更新者',
    `update_time`             datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`                  varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_vote_template_code` (`template_code`),
    KEY `idx_vote_template_enabled_sort` (`enabled`, `sort_order`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='投票模板配置表';

CREATE TABLE IF NOT EXISTS `vote_instance`
(
    `id`                  bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_id`         bigint                DEFAULT NULL COMMENT '模板ID(vote_template.id)',
    `template_code`       varchar(64)  NOT NULL COMMENT '模板编码快照',
    `template_name`       varchar(128) NOT NULL COMMENT '模板名称快照',
    `server_id`           bigint                DEFAULT NULL COMMENT '服务器ID(server_info.id)',
    `target_type`         varchar(32)  NOT NULL DEFAULT 'PLAYER' COMMENT '目标类型',
    `target_player_name`  varchar(128)          DEFAULT NULL COMMENT '目标玩家名',
    `target_whitelist_id` bigint                DEFAULT NULL COMMENT '目标白名单ID(whitelist_info.id)',
    `target_ref`          varchar(128)          DEFAULT NULL COMMENT '目标引用(兼容其他类型目标)',
    `initiator_user_id`   bigint                DEFAULT NULL COMMENT '发起人系统用户ID(sys_user.user_id)',
    `initiator_user_name` varchar(64)  NOT NULL COMMENT '发起人用户名',
    `required_votes`      int          NOT NULL COMMENT '本次投票要求票数(创建时从模板快照)',
    `agree_votes`         int          NOT NULL DEFAULT 0 COMMENT '同意票数',
    `reject_votes`        int          NOT NULL DEFAULT 0 COMMENT '反对票数',
    `status`              varchar(20)  NOT NULL DEFAULT 'ONGOING' COMMENT '状态(ONGOING/PASSED/REJECTED/CANCELLED/EXPIRED/EXECUTED)',
    `expire_time`         datetime     NOT NULL COMMENT '到期时间',
    `finished_time`       datetime              DEFAULT NULL COMMENT '结束时间',
    `execute_status`      varchar(20)           DEFAULT 'PENDING' COMMENT '执行状态(PENDING/SUCCESS/FAILED/SKIPPED)',
    `execute_result`      text                  DEFAULT NULL COMMENT '执行结果或失败原因',
    `reason`              varchar(500)          DEFAULT NULL COMMENT '发起原因',
    `extra_context`       text                  DEFAULT NULL COMMENT '扩展上下文(JSON文本)',
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_vote_instance_status_expire` (`status`, `expire_time`),
    KEY `idx_vote_instance_server_status` (`server_id`, `status`),
    KEY `idx_vote_instance_target_player` (`target_player_name`),
    KEY `idx_vote_instance_template_code` (`template_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='投票实例表';

CREATE TABLE IF NOT EXISTS `vote_record`
(
    `id`              bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `vote_id`         bigint      NOT NULL COMMENT '投票ID(vote_instance.id)',
    `voter_user_id`   bigint               DEFAULT NULL COMMENT '投票人系统用户ID(sys_user.user_id)',
    `voter_user_name` varchar(64) NOT NULL COMMENT '投票人用户名',
    `vote_decision`   tinyint     NOT NULL COMMENT '投票决策(1同意 2反对)',
    `vote_comment`    varchar(255)         DEFAULT NULL COMMENT '投票备注',
    `create_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_vote_record_vote_user` (`vote_id`, `voter_user_name`),
    KEY `idx_vote_record_vote_id` (`vote_id`),
    KEY `idx_vote_record_voter_user_id` (`voter_user_id`),
    KEY `idx_vote_record_decision` (`vote_decision`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='投票记录表';

-- 初始化默认投票模板：踢出玩家
INSERT INTO `vote_template` (`template_code`, `template_name`, `template_desc`, `target_type`, `action_type`,
                             `action_command_template`, `min_required_votes`, `vote_duration_seconds`, `need_reason`,
                             `enabled`, `sort_order`, `extra_config`, `create_by`, `remark`)
SELECT 'KICK_PLAYER',
       '投票踢出玩家',
       '玩家投票通过后执行踢出指令',
       'PLAYER',
       'RCON_COMMAND',
       'kick {targetPlayer} {reason}',
       3,
       300,
       1,
       1,
       10,
       '{"allowSelfTarget":false}',
       'admin',
       '默认模板'
WHERE NOT EXISTS (SELECT 1 FROM `vote_template` WHERE `template_code` = 'KICK_PLAYER');

-- 初始化默认投票模板：封禁玩家
INSERT INTO `vote_template` (`template_code`, `template_name`, `template_desc`, `target_type`, `action_type`,
                             `action_command_template`, `min_required_votes`, `vote_duration_seconds`, `need_reason`,
                             `enabled`, `sort_order`, `extra_config`, `create_by`, `remark`)
SELECT 'BAN_PLAYER',
       '投票封禁玩家',
       '玩家投票通过后执行封禁指令',
       'PLAYER',
       'RCON_COMMAND',
       'ban {targetPlayer} {reason}',
       5,
       600,
       1,
       1,
       20,
       '{"allowSelfTarget":false}',
       'admin',
       '默认模板'
WHERE NOT EXISTS (SELECT 1 FROM `vote_template` WHERE `template_code` = 'BAN_PLAYER');

-- 初始化默认投票模板：移出白名单
INSERT INTO `vote_template` (`template_code`, `template_name`, `template_desc`, `target_type`, `action_type`,
                             `action_command_template`, `min_required_votes`, `vote_duration_seconds`, `need_reason`,
                             `enabled`, `sort_order`, `extra_config`, `create_by`, `remark`)
SELECT 'REMOVE_WHITELIST_PLAYER',
       '投票移出白名单',
       '玩家投票通过后执行移出白名单指令',
       'WHITELIST',
       'RCON_COMMAND',
       'whitelist remove {targetPlayer}',
       5,
       600,
       1,
       1,
       30,
       '{"allowSelfTarget":false}',
       'admin',
       '默认模板'
WHERE NOT EXISTS (SELECT 1 FROM `vote_template` WHERE `template_code` = 'REMOVE_WHITELIST_PLAYER');
