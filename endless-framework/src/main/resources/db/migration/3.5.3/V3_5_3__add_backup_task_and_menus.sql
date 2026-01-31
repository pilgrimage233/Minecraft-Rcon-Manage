-- =====================================================
-- 版本: 3.5.3
-- 描述: 添加数据库定时备份任务和新增菜单
-- 作者: Memory
-- 日期: 2026-01-31
-- =====================================================

-- =====================================================
-- 1. 添加数据库定时备份任务
-- =====================================================

-- 获取当前最大的任务ID
SET @max_job_id = (SELECT IFNULL(MAX(job_id), 0)
                   FROM sys_job);

-- 插入数据库定时备份任务
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, remark)
VALUES (@max_job_id + 1, '数据库定时备份', 'SYSTEM', 'databaseBackupTask.backupDatabase', '0 0 3 * * ?', '2', '1', '0',
        'admin', now(),
        '每天凌晨3点执行数据库备份，备份包括白名单、机器人配置、服务器配置等重要数据表');


-- =====================================================
-- 2. 添加新增菜单
-- =====================================================

-- 获取当前最大的菜单ID
SET @max_menu_id = (SELECT IFNULL(MAX(menu_id), 0)
                    FROM sys_menu);

-- 环境管理菜单 (父菜单ID: 2075 - 节点管理)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 1, '环境管理', 2075, 2, 'env', 'node/env/index', null, 1, 1,
        'C', '0', '0', 'node:env:list', 'component', 'admin', now(), '环境管理菜单');

-- 环境管理查询权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 2, '环境管理查询', @max_menu_id + 1, 1, '#', '', null, 1, 0,
        'F', '0', '0', 'node:env:query', '#', 'admin', now(), '');

-- 环境管理新增权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 3, '环境管理新增', @max_menu_id + 1, 2, '#', '', null, 1, 0,
        'F', '0', '0', 'node:env:add', '#', 'admin', now(), '');

-- 环境管理修改权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 4, '环境管理修改', @max_menu_id + 1, 3, '#', '', null, 1, 0,
        'F', '0', '0', 'node:env:edit', '#', 'admin', now(), '');

-- 环境管理删除权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 5, '环境管理删除', @max_menu_id + 1, 4, '#', '', null, 1, 0,
        'F', '0', '0', 'node:env:remove', '#', 'admin', now(), '');

-- 环境管理导出权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 6, '环境管理导出', @max_menu_id + 1, 5, '#', '', null, 1, 0,
        'F', '0', '0', 'node:env:export', '#', 'admin', now(), '');

-- 指令开关菜单 (父菜单ID: 2082 - 机器人管理)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 7, '指令开关', 2082, 1, 'cmdconfig', 'bot/cmdconfig/index', null, 1, 0,
        'C', '0', '0', 'bot:cmdconfig:list', 'switch', 'admin', now(), '群组指令功能配置菜单');

-- 群组指令功能配置查询权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 8, '群组指令功能配置查询', @max_menu_id + 7, 1, '#', '', null, 1, 0,
        'F', '0', '0', 'bot:cmdconfig:query', '#', 'admin', now(), '');

-- 群组指令功能配置新增权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 9, '群组指令功能配置新增', @max_menu_id + 7, 2, '#', '', null, 1, 0,
        'F', '0', '0', 'bot:cmdconfig:add', '#', 'admin', now(), '');

-- 群组指令功能配置修改权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 10, '群组指令功能配置修改', @max_menu_id + 7, 3, '#', '', null, 1, 0,
        'F', '0', '0', 'bot:cmdconfig:edit', '#', 'admin', now(), '');

-- 群组指令功能配置删除权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 11, '群组指令功能配置删除', @max_menu_id + 7, 4, '#', '', null, 1, 0,
        'F', '0', '0', 'bot:cmdconfig:remove', '#', 'admin', now(), '');

-- 群组指令功能配置导出权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 12, '群组指令功能配置导出', @max_menu_id + 7, 5, '#', '', null, 1, 0,
        'F', '0', '0', 'bot:cmdconfig:export', '#', 'admin', now(), '');

-- 数据库备份管理菜单 (父菜单ID: 2 - 系统管理)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 13, '数据库备份', 2, 12, 'backup', 'system/backup/index', null, 1, 0,
        'C', '0', '0', 'system:backup:list', 'database', 'admin', now(), '数据库备份管理菜单');

-- 数据库备份查询权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 14, '数据库备份查询', @max_menu_id + 13, 1, '#', '', null, 1, 0,
        'F', '0', '0', 'system:backup:query', '#', 'admin', now(), '');

-- 数据库备份新增权限（手动备份）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 15, '数据库备份新增', @max_menu_id + 13, 2, '#', '', null, 1, 0,
        'F', '0', '0', 'system:backup:add', '#', 'admin', now(), '');

-- 数据库备份恢复权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 16, '数据库备份恢复', @max_menu_id + 13, 3, '#', '', null, 1, 0,
        'F', '0', '0', 'system:backup:restore', '#', 'admin', now(), '');

-- 数据库备份删除权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 17, '数据库备份删除', @max_menu_id + 13, 4, '#', '', null, 1, 0,
        'F', '0', '0', 'system:backup:remove', '#', 'admin', now(), '');


-- =====================================================
-- 备注说明
-- =====================================================

-- 【数据库定时备份任务】
-- job_name: 数据库定时备份
-- job_group: SYSTEM (系统任务组)
-- invoke_target: databaseBackupTask.backupDatabase (调用DatabaseBackupTask类的backupDatabase方法)
-- cron_expression: 0 0 3 * * ? (每天凌晨3点执行)
-- misfire_policy: 2 (立即执行)
-- concurrent: 1 (允许并发)
-- status: 0 (启用状态)
-- 
-- 备份的表包括:
-- - banlist_info (封禁列表)
-- - custom_email_templates (自定义邮件模板)
-- - history_command (历史命令)
-- - ip_limit_info (IP限制信息)
-- - node_env (节点环境)
-- - node_minecraft_server (Minecraft服务器节点)
-- - node_server (节点服务器)
-- - operator_list (管理员列表)
-- - bot_group_command_config (机器人群组命令配置)
-- - qq_bot_config (QQ机器人配置)
-- - qq_bot_manager (QQ机器人管理)
-- - qq_bot_manager_group (QQ机器人管理群组)
-- - server_info (服务器信息)
-- - sys_user (系统用户)
-- - regular_cmd (定时命令)
-- - server_command_info (服务器命令信息)
-- - whitelist_deadline_info (白名单截止信息)
-- - whitelist_info (白名单信息)
-- - whitelist_quiz_answer (白名单测验答案)
-- - whitelist_quiz_config (白名单测验配置)
-- - whitelist_quiz_question (白名单测验问题)
-- - whitelist_quiz_submission (白名单测验提交)
-- - whitelist_quiz_submission_detail (白名单测验提交详情)
--
-- 【新增菜单说明】
-- 1. 环境管理菜单 (menu_id: @max_menu_id + 1 ~ @max_menu_id + 6)
--    - 父菜单: 2075 (节点管理)
--    - 包含: 查询、新增、修改、删除、导出权限
--    - 路径: node/env/index
--
-- 2. 指令开关菜单 (menu_id: @max_menu_id + 7 ~ @max_menu_id + 12)
--    - 父菜单: 2082 (机器人管理)
--    - 包含: 查询、新增、修改、删除、导出权限
--    - 路径: bot/cmdconfig/index
--
-- 3. 数据库备份管理菜单 (menu_id: @max_menu_id + 13 ~ @max_menu_id + 17)
--    - 父菜单: 2 (系统管理)
--    - 包含: 查询、新增（手动备份）、恢复、删除权限
--    - 路径: system/backup/index

