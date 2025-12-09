-- ----------------------------
-- 群组指令功能配置表
-- 用于管理每个群组中每个功能指令的独立开关
-- ----------------------------
DROP TABLE IF EXISTS `bot_group_command_config`;
CREATE TABLE `bot_group_command_config`
(
    `id`               bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `group_id`         varchar(50) NOT NULL COMMENT '群组ID',
    `command_key`      varchar(50) NOT NULL COMMENT '指令关键字（主命令名称）',
    `command_name`     varchar(100)         DEFAULT NULL COMMENT '指令显示名称',
    `command_category` varchar(50)          DEFAULT NULL COMMENT '指令分类（user/admin/super）',
    `is_enabled`       tinyint(1)  NOT NULL DEFAULT 1 COMMENT '是否启用（0=禁用，1=启用）',
    `disabled_message` varchar(500)         DEFAULT NULL COMMENT '禁用时的提示消息',
    `create_time`      datetime             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`        varchar(64)          DEFAULT NULL COMMENT '创建者',
    `update_by`        varchar(64)          DEFAULT NULL COMMENT '更新者',
    `remark`           varchar(500)         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_command` (`group_id`, `command_key`) COMMENT '群组+指令唯一索引',
    KEY `idx_group_id` (`group_id`) COMMENT '群组ID索引',
    KEY `idx_command_key` (`command_key`) COMMENT '指令关键字索引',
    KEY `idx_is_enabled` (`is_enabled`) COMMENT '启用状态索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='群组指令功能配置表';

-- ----------------------------
-- 初始化默认指令配置数据
-- 注意：这里只是示例数据，实际使用时需要根据具体群组ID进行配置
-- ----------------------------

-- 普通用户指令
INSERT INTO `bot_group_command_config` (`group_id`, `command_key`, `command_name`, `command_category`, `is_enabled`,
                                        `remark`)
VALUES ('default', 'help', '帮助信息', 'user', 1, '显示帮助信息'),
       ('default', '白名单申请', '白名单申请', 'user', 1, '申请白名单'),
       ('default', '查询白名单', '查询白名单', 'user', 1, '查询自己的白名单状态'),
       ('default', '查询玩家', '查询玩家', 'user', 1, '查询指定玩家信息'),
       ('default', '查询在线', '查询在线', 'user', 1, '查询所有服务器在线玩家'),
       ('default', '查询服务器', '查询服务器', 'user', 1, '查询服务器列表'),
       ('default', 'test', '测试连通', 'user', 1, '测试服务器连通性');

-- 管理员指令
INSERT INTO `bot_group_command_config` (`group_id`, `command_key`, `command_name`, `command_category`, `is_enabled`,
                                        `remark`)
VALUES ('default', '过审', '白名单审核通过', 'admin', 1, '通过白名单申请'),
       ('default', '拒审', '白名单审核拒绝', 'admin', 1, '拒绝白名单申请'),
       ('default', '封禁', '封禁玩家', 'admin', 1, '封禁玩家'),
       ('default', '解封', '解封玩家', 'admin', 1, '解除玩家封禁'),
       ('default', '发送指令', 'RCON指令', 'admin', 1, '发送RCON指令'),
       ('default', '运行状态', '主机状态', 'admin', 1, '查看主机运行状态'),
       ('default', '刷新连接', '刷新连接', 'admin', 1, '刷新RCON连接'),
       ('default', '测试连接', '测试连接', 'admin', 1, '测试RCON连接'),
       ('default', '实例列表', '实例列表', 'admin', 1, '查看游戏服务器实例'),
       ('default', '启动实例', '启动实例', 'admin', 1, '启动实例'),
       ('default', '停止实例', '停止实例', 'admin', 1, '停止实例'),
       ('default', '重启实例', '重启实例', 'admin', 1, '重启实例'),
       ('default', '实例状态', '实例状态', 'admin', 1, '查看实例状态'),
       ('default', '实例日志', '实例日志', 'admin', 1, '查看实例日志'),
       ('default', '实例命令', '实例命令', 'admin', 1, '发送实例命令'),
       ('default', '节点状态', '节点状态', 'admin', 1, '查看节点服务器状态');

-- 超级管理员指令
INSERT INTO `bot_group_command_config` (`group_id`, `command_key`, `command_name`, `command_category`, `is_enabled`,
                                        `remark`)
VALUES ('default', '添加管理', '添加管理员', 'super', 1, '添加普通管理员'),
       ('default', '添加超管', '添加超级管理员', 'super', 1, '添加超级管理员');

-- 功能开关指令（这些指令不受开关限制）
INSERT INTO `bot_group_command_config` (`group_id`, `command_key`, `command_name`, `command_category`, `is_enabled`,
                                        `remark`)
VALUES ('default', '关闭', '关闭功能', 'admin', 1, '关闭指定功能（不可被关闭）'),
       ('default', '开启', '开启功能', 'admin', 1, '开启指定功能（不可被关闭）'),
       ('default', '功能列表', '功能列表', 'user', 1, '查看所有功能及状态（不可被关闭）');

-- 系统通知功能（非指令类功能）
INSERT INTO `bot_group_command_config` (`group_id`, `command_key`, `command_name`, `command_category`, `is_enabled`,
                                        `remark`)
VALUES ('default', '玩家上线通知', '玩家上线通知', 'system', 1, '玩家加入游戏时发送通知'),
       ('default', '玩家下线通知', '玩家下线通知', 'system', 1, '玩家离开游戏时发送通知');


-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('指令开关', @parentId, 1, 'cmdconfig', 'bot/cmdconfig/index', null, 1, 0, 'C', '0', '0',
        'bot:cmdconfig:list',
        'switch', 'admin', sysdate(), 'admin', null, '群组指令功能配置菜单');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('群组指令功能配置查询', @parentId, 1, '#', '', null, 1, 0, 'F', '0', '0', 'bot:cmdconfig:query', '#',
        'admin',
        sysdate(), '', null, '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('群组指令功能配置新增', @parentId, 2, '#', '', null, 1, 0, 'F', '0', '0', 'bot:cmdconfig:add', '#',
        'admin',
        sysdate(), '', null, '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('群组指令功能配置修改', @parentId, 3, '#', '', null, 1, 0, 'F', '0', '0', 'bot:cmdconfig:edit', '#',
        'admin',
        sysdate(), '', null, '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('群组指令功能配置删除', @parentId, 4, '#', '', null, 1, 0, 'F', '0', '0', 'bot:cmdconfig:remove', '#',
        'admin',
        sysdate(), '', null, '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('群组指令功能配置导出', @parentId, 5, '#', '', null, 1, 0, 'F', '0', '0', 'bot:cmdconfig:export', '#',
        'admin',
        sysdate(), '', null, '');

-- ----------------------------
-- 使用说明
-- ----------------------------
-- 1. 'default' 群组ID表示默认配置，当某个群组没有特定配置时，使用默认配置
-- 2. 为特定群组配置时，将 'default' 替换为实际的群组ID
-- 3. is_enabled: 1=启用，0=禁用
-- 4. disabled_message: 当功能被禁用时，向用户显示的提示消息
-- 5. command_category: user=普通用户, admin=管理员, super=超级管理员

-- ----------------------------
-- 示例：为特定群组禁用某些功能
-- ----------------------------
-- 例如：禁用群组 123456789 的封禁功能
-- INSERT INTO `bot_group_command_config` (`group_id`, `command_key`, `command_name`, `command_category`, `is_enabled`, `disabled_message`, `remark`)
-- VALUES ('123456789', '封禁', '封禁玩家', 'admin', 0, '该群组已禁用封禁功能', '特定群组配置');
