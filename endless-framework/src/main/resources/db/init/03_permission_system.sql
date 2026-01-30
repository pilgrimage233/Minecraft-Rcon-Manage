SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 用户-RCON服务器权限关联表
-- 控制用户对特定RCON服务器的访问权限
-- =====================================================
DROP TABLE IF EXISTS `sys_user_rcon_server`;
CREATE TABLE `sys_user_rcon_server`
(
    `id`              bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         bigint      NOT NULL COMMENT '用户ID(关联sys_user)',
    `server_id`       bigint      NOT NULL COMMENT 'RCON服务器ID(关联server_info)',
    `permission_type` varchar(20) NOT NULL DEFAULT 'view' COMMENT '权限类型(view/command/manage/admin)',
    `can_execute_cmd` tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可执行命令(0否 1是)',
    `can_view_log`    tinyint(1)  NOT NULL DEFAULT 1 COMMENT '是否可查看日志(0否 1是)',
    `can_manage`      tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可管理配置(0否 1是)',
    `cmd_whitelist`   text        NULL COMMENT '允许执行的命令白名单(JSON数组)',
    `cmd_blacklist`   text        NULL COMMENT '禁止执行的命令黑名单(JSON数组)',
    `expire_time`     datetime    NULL COMMENT '权限过期时间(NULL表示永不过期)',
    `status`          char(1)     NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
    `create_by`       varchar(64)          DEFAULT '' COMMENT '创建者',
    `create_time`     datetime             DEFAULT NULL COMMENT '创建时间',
    `update_by`       varchar(64)          DEFAULT '' COMMENT '更新者',
    `update_time`     datetime             DEFAULT NULL COMMENT '更新时间',
    `remark`          varchar(500)         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_server` (`user_id`, `server_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_server_id` (`server_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户-RCON服务器权限关联表';


-- =====================================================
-- 2. 用户-节点服务器权限关联表
-- 控制用户对特定节点端的访问权限
-- =====================================================
DROP TABLE IF EXISTS `sys_user_node_server`;
CREATE TABLE `sys_user_node_server`
(
    `id`                  bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`             bigint      NOT NULL COMMENT '用户ID(关联sys_user)',
    `node_id`             bigint      NOT NULL COMMENT '节点ID(关联node_server)',
    `permission_type`     varchar(20) NOT NULL DEFAULT 'view' COMMENT '权限类型(view/operate/manage/admin)',
    `can_view`            tinyint(1)  NOT NULL DEFAULT 1 COMMENT '是否可查看节点信息(0否 1是)',
    `can_operate`         tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可操作节点(0否 1是)',
    `can_manage`          tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可管理节点配置(0否 1是)',
    `can_create_instance` tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可创建实例(0否 1是)',
    `expire_time`         datetime    NULL COMMENT '权限过期时间(NULL表示永不过期)',
    `status`              char(1)     NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
    `create_by`           varchar(64)          DEFAULT '' COMMENT '创建者',
    `create_time`         datetime             DEFAULT NULL COMMENT '创建时间',
    `update_by`           varchar(64)          DEFAULT '' COMMENT '更新者',
    `update_time`         datetime             DEFAULT NULL COMMENT '更新时间',
    `remark`              varchar(500)         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_node` (`user_id`, `node_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_node_id` (`node_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户-节点服务器权限关联表';


-- =====================================================
-- 3. 用户-游戏服务器实例权限关联表
-- 控制用户对特定MC服务器实例的访问权限(最细粒度)
-- =====================================================
DROP TABLE IF EXISTS `sys_user_mc_instance`;
CREATE TABLE `sys_user_mc_instance`
(
    `id`                    bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`               bigint      NOT NULL COMMENT '用户ID(关联sys_user)',
    `instance_id`           bigint      NOT NULL COMMENT '实例ID(关联node_minecraft_server)',
    `node_id`               bigint      NOT NULL COMMENT '所属节点ID(冗余字段,便于查询)',
    `permission_type`       varchar(20) NOT NULL DEFAULT 'view' COMMENT '权限类型(view/control/manage/admin)',
    `can_view`              tinyint(1)  NOT NULL DEFAULT 1 COMMENT '是否可查看实例(0否 1是)',
    `can_start`             tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可启动(0否 1是)',
    `can_stop`              tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可停止(0否 1是)',
    `can_restart`           tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可重启(0否 1是)',
    `can_console`           tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可访问控制台(0否 1是)',
    `can_file`              tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可管理文件(0否 1是)',
    `can_config`            tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可修改配置(0否 1是)',
    `can_delete`            tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可删除实例(0否 1是)',
    `console_cmd_whitelist` text        NULL COMMENT '控制台允许执行的命令白名单(JSON数组)',
    `console_cmd_blacklist` text        NULL COMMENT '控制台禁止执行的命令黑名单(JSON数组)',
    `file_path_whitelist`   text        NULL COMMENT '允许访问的文件路径白名单(JSON数组)',
    `file_path_blacklist`   text        NULL COMMENT '禁止访问的文件路径黑名单(JSON数组)',
    `expire_time`           datetime    NULL COMMENT '权限过期时间(NULL表示永不过期)',
    `status`                char(1)     NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
    `create_by`             varchar(64)          DEFAULT '' COMMENT '创建者',
    `create_time`           datetime             DEFAULT NULL COMMENT '创建时间',
    `update_by`             varchar(64)          DEFAULT '' COMMENT '更新者',
    `update_time`           datetime             DEFAULT NULL COMMENT '更新时间',
    `remark`                varchar(500)         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_instance` (`user_id`, `instance_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_instance_id` (`instance_id`),
    KEY `idx_node_id` (`node_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户-游戏服务器实例权限关联表';


-- =====================================================
-- 4. 角色-RCON服务器权限关联表
-- 通过角色批量授权RCON服务器权限
-- =====================================================
DROP TABLE IF EXISTS `sys_role_rcon_server`;
CREATE TABLE `sys_role_rcon_server`
(
    `id`              bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id`         bigint      NOT NULL COMMENT '角色ID(关联sys_role)',
    `server_id`       bigint      NOT NULL COMMENT 'RCON服务器ID(关联server_info)',
    `permission_type` varchar(20) NOT NULL DEFAULT 'view' COMMENT '权限类型',
    `can_execute_cmd` tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可执行命令',
    `can_view_log`    tinyint(1)  NOT NULL DEFAULT 1 COMMENT '是否可查看日志',
    `can_manage`      tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可管理配置',
    `cmd_whitelist`   text        NULL COMMENT '允许执行的命令白名单',
    `cmd_blacklist`   text        NULL COMMENT '禁止执行的命令黑名单',
    `create_by`       varchar(64)          DEFAULT '' COMMENT '创建者',
    `create_time`     datetime             DEFAULT NULL COMMENT '创建时间',
    `update_by`       varchar(64)          DEFAULT '' COMMENT '更新者',
    `update_time`     datetime             DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_server` (`role_id`, `server_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_server_id` (`server_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='角色-RCON服务器权限关联表';

-- =====================================================
-- 5. 角色-节点服务器权限关联表
-- =====================================================
DROP TABLE IF EXISTS `sys_role_node_server`;
CREATE TABLE `sys_role_node_server`
(
    `id`                  bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id`             bigint      NOT NULL COMMENT '角色ID(关联sys_role)',
    `node_id`             bigint      NOT NULL COMMENT '节点ID(关联node_server)',
    `permission_type`     varchar(20) NOT NULL DEFAULT 'view' COMMENT '权限类型',
    `can_view`            tinyint(1)  NOT NULL DEFAULT 1 COMMENT '是否可查看',
    `can_operate`         tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可操作',
    `can_manage`          tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可管理',
    `can_create_instance` tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可创建实例',
    `create_by`           varchar(64)          DEFAULT '' COMMENT '创建者',
    `create_time`         datetime             DEFAULT NULL COMMENT '创建时间',
    `update_by`           varchar(64)          DEFAULT '' COMMENT '更新者',
    `update_time`         datetime             DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_node` (`role_id`, `node_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_node_id` (`node_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='角色-节点服务器权限关联表';


-- =====================================================
-- 6. 角色-游戏服务器实例权限关联表
-- =====================================================
DROP TABLE IF EXISTS `sys_role_mc_instance`;
CREATE TABLE `sys_role_mc_instance`
(
    `id`                    bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id`               bigint      NOT NULL COMMENT '角色ID(关联sys_role)',
    `instance_id`           bigint      NOT NULL COMMENT '实例ID(关联node_minecraft_server)',
    `node_id`               bigint      NOT NULL COMMENT '所属节点ID',
    `permission_type`       varchar(20) NOT NULL DEFAULT 'view' COMMENT '权限类型',
    `can_view`              tinyint(1)  NOT NULL DEFAULT 1 COMMENT '是否可查看',
    `can_start`             tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可启动',
    `can_stop`              tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可停止',
    `can_restart`           tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可重启',
    `can_console`           tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可访问控制台',
    `can_file`              tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可管理文件',
    `can_config`            tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可修改配置',
    `can_delete`            tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可删除',
    `console_cmd_whitelist` text        NULL COMMENT '控制台命令白名单',
    `console_cmd_blacklist` text        NULL COMMENT '控制台命令黑名单',
    `create_by`             varchar(64)          DEFAULT '' COMMENT '创建者',
    `create_time`           datetime             DEFAULT NULL COMMENT '创建时间',
    `update_by`             varchar(64)          DEFAULT '' COMMENT '更新者',
    `update_time`           datetime             DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_instance` (`role_id`, `instance_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_instance_id` (`instance_id`),
    KEY `idx_node_id` (`node_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='角色-游戏服务器实例权限关联表';

-- =====================================================
-- 7. 资源权限操作日志表
-- 记录所有资源权限相关的操作
-- =====================================================
DROP TABLE IF EXISTS `sys_resource_permission_log`;
CREATE TABLE `sys_resource_permission_log`
(
    `id`                bigint       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id`           bigint       NOT NULL COMMENT '操作用户ID',
    `user_name`         varchar(64)  NOT NULL COMMENT '操作用户名',
    `resource_type`     varchar(20)  NOT NULL COMMENT '资源类型(rcon_server/node_server/mc_instance)',
    `resource_id`       bigint       NOT NULL COMMENT '资源ID',
    `resource_name`     varchar(255) NULL COMMENT '资源名称',
    `action_type`       varchar(20)  NOT NULL COMMENT '操作类型(grant/revoke/modify/access)',
    `target_type`       varchar(10)  NOT NULL COMMENT '目标类型(user/role)',
    `target_id`         bigint       NOT NULL COMMENT '目标ID(用户ID或角色ID)',
    `target_name`       varchar(64)  NULL COMMENT '目标名称',
    `permission_detail` text         NULL COMMENT '权限详情(JSON格式)',
    `ip_address`        varchar(128) NULL COMMENT '操作IP',
    `status`            char(1)      NOT NULL DEFAULT '0' COMMENT '操作状态(0成功 1失败)',
    `error_msg`         varchar(500) NULL COMMENT '错误信息',
    `create_time`       datetime     NOT NULL COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_resource` (`resource_type`, `resource_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_action_type` (`action_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='资源权限操作日志表';


-- =====================================================
-- 8. 权限模板表
-- 预定义的权限模板,便于快速授权
-- =====================================================
DROP TABLE IF EXISTS `sys_permission_template`;
CREATE TABLE `sys_permission_template`
(
    `id`                bigint       NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `template_name`     varchar(64)  NOT NULL COMMENT '模板名称',
    `template_key`      varchar(64)  NOT NULL COMMENT '模板标识',
    `resource_type`     varchar(20)  NOT NULL COMMENT '资源类型(rcon_server/node_server/mc_instance)',
    `permission_config` text         NOT NULL COMMENT '权限配置(JSON格式)',
    `description`       varchar(500) NULL COMMENT '模板描述',
    `is_system`         tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否系统内置(0否 1是)',
    `status`            char(1)      NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
    `create_by`         varchar(64)           DEFAULT '' COMMENT '创建者',
    `create_time`       datetime              DEFAULT NULL COMMENT '创建时间',
    `update_by`         varchar(64)           DEFAULT '' COMMENT '更新者',
    `update_time`       datetime              DEFAULT NULL COMMENT '更新时间',
    `remark`            varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_key` (`template_key`),
    KEY `idx_resource_type` (`resource_type`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='权限模板表';

-- =====================================================
-- 9. 字典数据 - 权限类型
-- =====================================================
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('RCON服务器权限类型', 'rcon_permission_type', '0', 'admin', NOW(), 'RCON服务器权限类型'),
       ('节点服务器权限类型', 'node_permission_type', '0', 'admin', NOW(), '节点服务器权限类型'),
       ('MC实例权限类型', 'instance_permission_type', '0', 'admin', NOW(), 'MC实例权限类型'),
       ('资源类型', 'resource_type', '0', 'admin', NOW(), '资源权限管理的资源类型'),
       ('权限操作类型', 'permission_action_type', '0', 'admin', NOW(), '权限操作类型');


INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`,
                             `is_default`, `status`, `create_by`, `create_time`, `remark`)
VALUES
-- RCON服务器权限类型
(1, '仅查看', 'view', 'rcon_permission_type', '', 'info', 'N', '0', 'admin', NOW(), '仅可查看服务器信息'),
(2, '执行命令', 'command', 'rcon_permission_type', '', 'primary', 'N', '0', 'admin', NOW(), '可执行RCON命令'),
(3, '管理配置', 'manage', 'rcon_permission_type', '', 'warning', 'N', '0', 'admin', NOW(), '可管理服务器配置'),
(4, '完全控制', 'admin', 'rcon_permission_type', '', 'danger', 'N', '0', 'admin', NOW(), '完全控制权限'),
-- 节点服务器权限类型
(1, '仅查看', 'view', 'node_permission_type', '', 'info', 'N', '0', 'admin', NOW(), '仅可查看节点信息'),
(2, '操作节点', 'operate', 'node_permission_type', '', 'primary', 'N', '0', 'admin', NOW(), '可操作节点'),
(3, '管理节点', 'manage', 'node_permission_type', '', 'warning', 'N', '0', 'admin', NOW(), '可管理节点配置'),
(4, '完全控制', 'admin', 'node_permission_type', '', 'danger', 'N', '0', 'admin', NOW(), '完全控制权限'),
-- MC实例权限类型
(1, '仅查看', 'view', 'instance_permission_type', '', 'info', 'N', '0', 'admin', NOW(), '仅可查看实例信息'),
(2, '控制实例', 'control', 'instance_permission_type', '', 'primary', 'N', '0', 'admin', NOW(), '可启停实例'),
(3, '管理实例', 'manage', 'instance_permission_type', '', 'warning', 'N', '0', 'admin', NOW(), '可管理实例配置'),
(4, '完全控制', 'admin', 'instance_permission_type', '', 'danger', 'N', '0', 'admin', NOW(), '完全控制权限'),
-- 资源类型
(1, 'RCON服务器', 'rcon_server', 'resource_type', '', 'primary', 'N', '0', 'admin', NOW(), 'RCON服务器'),
(2, '节点服务器', 'node_server', 'resource_type', '', 'success', 'N', '0', 'admin', NOW(), '节点服务器'),
(3, 'MC实例', 'mc_instance', 'resource_type', '', 'warning', 'N', '0', 'admin', NOW(), 'Minecraft服务器实例'),
-- 权限操作类型
(1, '授权', 'grant', 'permission_action_type', '', 'success', 'N', '0', 'admin', NOW(), '授予权限'),
(2, '撤销', 'revoke', 'permission_action_type', '', 'danger', 'N', '0', 'admin', NOW(), '撤销权限'),
(3, '修改', 'modify', 'permission_action_type', '', 'warning', 'N', '0', 'admin', NOW(), '修改权限'),
(4, '访问', 'access', 'permission_action_type', '', 'info', 'N', '0', 'admin', NOW(), '访问资源');


-- =====================================================
-- 10. 预置权限模板数据
-- =====================================================
INSERT INTO `sys_permission_template` (`template_name`, `template_key`, `resource_type`, `permission_config`,
                                       `description`, `is_system`, `status`, `create_by`, `create_time`)
VALUES
-- RCON服务器权限模板
('RCON只读权限', 'rcon_readonly', 'rcon_server',
 '{"permission_type":"view","can_execute_cmd":false,"can_view_log":true,"can_manage":false}',
 '仅可查看RCON服务器信息和日志', 1, '0', 'admin', NOW()),
('RCON操作员权限', 'rcon_operator', 'rcon_server',
 '{"permission_type":"command","can_execute_cmd":true,"can_view_log":true,"can_manage":false}',
 '可执行RCON命令和查看日志', 1, '0', 'admin', NOW()),
('RCON管理员权限', 'rcon_admin', 'rcon_server',
 '{"permission_type":"admin","can_execute_cmd":true,"can_view_log":true,"can_manage":true}',
 'RCON服务器完全控制权限', 1, '0', 'admin', NOW()),
-- 节点服务器权限模板
('节点只读权限', 'node_readonly', 'node_server',
 '{"permission_type":"view","can_view":true,"can_operate":false,"can_manage":false,"can_create_instance":false}',
 '仅可查看节点信息', 1, '0', 'admin', NOW()),
('节点操作员权限', 'node_operator', 'node_server',
 '{"permission_type":"operate","can_view":true,"can_operate":true,"can_manage":false,"can_create_instance":false}',
 '可操作节点但不能管理配置', 1, '0', 'admin', NOW()),
('节点管理员权限', 'node_admin', 'node_server',
 '{"permission_type":"admin","can_view":true,"can_operate":true,"can_manage":true,"can_create_instance":true}',
 '节点完全控制权限', 1, '0', 'admin', NOW()),
-- MC实例权限模板
('实例只读权限', 'instance_readonly', 'mc_instance',
 '{"permission_type":"view","can_view":true,"can_start":false,"can_stop":false,"can_restart":false,"can_console":false,"can_file":false,"can_config":false,"can_delete":false}',
 '仅可查看实例信息', 1, '0', 'admin', NOW()),
('实例操作员权限', 'instance_operator', 'mc_instance',
 '{"permission_type":"control","can_view":true,"can_start":true,"can_stop":true,"can_restart":true,"can_console":true,"can_file":false,"can_config":false,"can_delete":false}',
 '可启停实例和访问控制台', 1, '0', 'admin', NOW()),
('实例管理员权限', 'instance_manager', 'mc_instance',
 '{"permission_type":"manage","can_view":true,"can_start":true,"can_stop":true,"can_restart":true,"can_console":true,"can_file":true,"can_config":true,"can_delete":false}',
 '可管理实例但不能删除', 1, '0', 'admin', NOW()),
('实例完全控制', 'instance_admin', 'mc_instance',
 '{"permission_type":"admin","can_view":true,"can_start":true,"can_stop":true,"can_restart":true,"can_console":true,"can_file":true,"can_config":true,"can_delete":true}',
 '实例完全控制权限', 1, '0', 'admin', NOW());


-- =====================================================
-- 11. 菜单数据 - 资源权限管理
-- 注意: menu_id需要根据实际情况调整,避免冲突
-- =====================================================

-- 资源权限管理目录 (假设menu_id从3100开始)
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`,
                        `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`,
                        `remark`)
VALUES (3100, '资源权限', 0, 3, 'resource-permission', NULL, '', 1, 0, 'M', '0', '0', '', 'lock', 'admin', NOW(),
        '资源权限管理目录');

-- RCON服务器权限管理
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`,
                        `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`,
                        `remark`)
VALUES (3101, 'RCON权限', 3100, 1, 'rcon-permission', 'permission/rcon/index', '', 1, 0, 'C', '0', '0',
        'permission:rcon:list', 'server', 'admin', NOW(), 'RCON服务器权限管理'),
       (3102, 'RCON权限查询', 3101, 1, '', '', '', 1, 0, 'F', '0', '0', 'permission:rcon:query', '#', 'admin', NOW(),
        ''),
       (3103, 'RCON权限新增', 3101, 2, '', '', '', 1, 0, 'F', '0', '0', 'permission:rcon:add', '#', 'admin', NOW(), ''),
       (3104, 'RCON权限修改', 3101, 3, '', '', '', 1, 0, 'F', '0', '0', 'permission:rcon:edit', '#', 'admin', NOW(),
        ''),
       (3105, 'RCON权限删除', 3101, 4, '', '', '', 1, 0, 'F', '0', '0', 'permission:rcon:remove', '#', 'admin', NOW(),
        '');

-- 节点服务器权限管理
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`,
                        `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`,
                        `remark`)
VALUES (3110, '节点权限', 3100, 2, 'node-permission', 'permission/node/index', '', 1, 0, 'C', '0', '0',
        'permission:node:list', 'tree', 'admin', NOW(), '节点服务器权限管理'),
       (3111, '节点权限查询', 3110, 1, '', '', '', 1, 0, 'F', '0', '0', 'permission:node:query', '#', 'admin', NOW(),
        ''),
       (3112, '节点权限新增', 3110, 2, '', '', '', 1, 0, 'F', '0', '0', 'permission:node:add', '#', 'admin', NOW(), ''),
       (3113, '节点权限修改', 3110, 3, '', '', '', 1, 0, 'F', '0', '0', 'permission:node:edit', '#', 'admin', NOW(),
        ''),
       (3114, '节点权限删除', 3110, 4, '', '', '', 1, 0, 'F', '0', '0', 'permission:node:remove', '#', 'admin', NOW(),
        '');

-- MC实例权限管理
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`,
                        `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`,
                        `remark`)
VALUES (3120, '实例权限', 3100, 3, 'instance-permission', 'permission/instance/index', '', 1, 0, 'C', '0', '0',
        'permission:instance:list', 'component', 'admin', NOW(), 'MC实例权限管理'),
       (3121, '实例权限查询', 3120, 1, '', '', '', 1, 0, 'F', '0', '0', 'permission:instance:query', '#', 'admin',
        NOW(), ''),
       (3122, '实例权限新增', 3120, 2, '', '', '', 1, 0, 'F', '0', '0', 'permission:instance:add', '#', 'admin', NOW(),
        ''),
       (3123, '实例权限修改', 3120, 3, '', '', '', 1, 0, 'F', '0', '0', 'permission:instance:edit', '#', 'admin', NOW(),
        ''),
       (3124, '实例权限删除', 3120, 4, '', '', '', 1, 0, 'F', '0', '0', 'permission:instance:remove', '#', 'admin',
        NOW(), '');

-- 权限模板管理
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`,
                        `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`,
                        `remark`)
VALUES (3130, '权限模板', 3100, 4, 'permission-template', 'permission/template/index', '', 1, 0, 'C', '0', '0',
        'permission:template:list', 'documentation', 'admin', NOW(), '权限模板管理'),
       (3131, '模板查询', 3130, 1, '', '', '', 1, 0, 'F', '0', '0', 'permission:template:query', '#', 'admin', NOW(),
        ''),
       (3132, '模板新增', 3130, 2, '', '', '', 1, 0, 'F', '0', '0', 'permission:template:add', '#', 'admin', NOW(), ''),
       (3133, '模板修改', 3130, 3, '', '', '', 1, 0, 'F', '0', '0', 'permission:template:edit', '#', 'admin', NOW(),
        ''),
       (3134, '模板删除', 3130, 4, '', '', '', 1, 0, 'F', '0', '0', 'permission:template:remove', '#', 'admin', NOW(),
        '');

-- 权限日志
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`,
                        `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`,
                        `remark`)
VALUES (3140, '权限日志', 3100, 5, 'permission-log', 'permission/log/index', '', 1, 0, 'C', '0', '0',
        'permission:log:list', 'log', 'admin', NOW(), '资源权限操作日志'),
       (3141, '日志查询', 3140, 1, '', '', '', 1, 0, 'F', '0', '0', 'permission:log:query', '#', 'admin', NOW(), ''),
       (3142, '日志删除', 3140, 2, '', '', '', 1, 0, 'F', '0', '0', 'permission:log:remove', '#', 'admin', NOW(), ''),
       (3143, '日志导出', 3140, 3, '', '', '', 1, 0, 'F', '0', '0', 'permission:log:export', '#', 'admin', NOW(), '');

-- =====================================================
-- 权限系统设计说明
-- =====================================================
-- 
-- 1. 权限层级结构:
--    - RCON服务器权限: 控制对RCON服务器的访问和命令执行
--    - 节点服务器权限: 控制对节点端的访问和管理
--    - MC实例权限: 最细粒度,控制对具体游戏服务器实例的操作
--
-- 2. 权限来源:
--    - 用户直接授权: 直接给用户分配资源权限
--    - 角色继承: 通过角色间接获得资源权限
--    - 权限合并: 用户最终权限 = 直接权限 OR 角色权限 (取并集)
--
-- 3. 权限类型:
--    - view: 仅查看
--    - command/operate/control: 可执行操作
--    - manage: 可管理配置
--    - admin: 完全控制
--
-- 4. 特殊功能:
--    - 命令白名单/黑名单: 精细控制可执行的命令
--    - 文件路径白名单/黑名单: 精细控制可访问的文件
--    - 权限过期时间: 支持临时授权
--    - 权限模板: 快速批量授权
--
-- 5. 使用示例:
--    -- 给用户授予RCON服务器操作权限
--    INSERT INTO sys_user_rcon_server (user_id, server_id, permission_type, can_execute_cmd, can_view_log)
--    VALUES (2, 1, 'command', 1, 1);
--
--    -- 给角色授予节点管理权限
--    INSERT INTO sys_role_node_server (role_id, node_id, permission_type, can_view, can_operate, can_manage)
--    VALUES (2, 1, 'manage', 1, 1, 1);
--
--    -- 检查用户权限
--    CALL sp_check_rcon_permission(2, 1, 'command', @has_perm);
--    SELECT @has_perm;
--
-- =====================================================
