-- Description: 系统菜单数据初始化脚本
DELETE
FROM sys_menu;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1, '系统管理', 0, 5, 'system', null, '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2023-12-26 16:54:02',
        'admin', '2025-03-04 23:09:22', '系统管理目录');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2, '系统监控', 0, 4, 'monitor', null, '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', '2023-12-26 16:54:02',
        'admin', '2025-03-12 15:55:10', '系统监控目录');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (3, '系统工具', 0, 6, 'tool', null, '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2023-12-26 16:54:02', 'admin',
        '2025-03-12 15:55:25', '系统工具目录');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user',
        'admin', '2023-12-26 16:54:02', '', null, '用户管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples',
        'admin', '2023-12-26 16:54:02', '', null, '角色管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table',
        'admin', '2023-12-26 16:54:02', '', null, '菜单管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', 1, 0, 'C', '1', '0', 'system:dept:list', 'tree',
        'admin', '2023-12-26 16:54:02', 'admin', '2023-12-26 21:37:32', '部门管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', 1, 0, 'C', '1', '0', 'system:post:list', 'post',
        'admin', '2023-12-26 16:54:02', 'admin', '2023-12-26 21:37:37', '岗位管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', 1, 0, 'C', '0', '0', 'system:dict:list', 'dict',
        'admin', '2023-12-26 16:54:02', '', null, '字典管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit',
        'admin', '2023-12-26 16:54:02', '', null, '参数设置菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', 1, 0, 'C', '0', '0', 'system:notice:list',
        'message', 'admin', '2023-12-26 16:54:02', '', null, '通知公告菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (108, '日志管理', 1, 9, 'log', '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', '2023-12-26 16:54:02', '', null,
        '日志管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', 1, 0, 'C', '0', '0', 'monitor:online:list',
        'online', 'admin', '2023-12-26 16:54:02', '', null, '在线用户菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (110, '定时任务', 2, 2, 'job', 'monitor/job/index', '', 1, 0, 'C', '0', '0', 'monitor:job:list', 'job', 'admin',
        '2023-12-26 16:54:02', '', null, '定时任务菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (111, '数据监控', 2, 3, 'druid', 'monitor/druid/index', '', 1, 0, 'C', '0', '0', 'monitor:druid:list', 'druid',
        'admin', '2023-12-26 16:54:02', '', null, '数据监控菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (112, '服务监控', 2, 4, 'server', 'monitor/server/index', '', 1, 0, 'C', '0', '0', 'monitor:server:list',
        'server', 'admin', '2023-12-26 16:54:02', '', null, '服务监控菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (113, '缓存监控', 2, 5, 'cache', 'monitor/cache/index', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis',
        'admin', '2023-12-26 16:54:02', '', null, '缓存监控菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (114, '缓存列表', 2, 6, 'cacheList', 'monitor/cache/list', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',
        'redis-list', 'admin', '2023-12-26 16:54:02', '', null, '缓存列表菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (115, '表单构建', 3, 1, 'build', 'tool/build/index', '', 1, 0, 'C', '0', '0', 'tool:build:list', 'build',
        'admin', '2023-12-26 16:54:02', '', null, '表单构建菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (116, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin',
        '2023-12-26 16:54:02', '', null, '代码生成菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (117, '系统接口', 3, 3, 'swagger', 'tool/swagger/index', '', 1, 0, 'C', '0', '0', 'tool:swagger:list', 'swagger',
        'admin', '2023-12-26 16:54:02', '', null, '系统接口菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list',
        'form', 'admin', '2023-12-26 16:54:02', '', null, '操作日志菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', 1, 0, 'C', '0', '0',
        'monitor:logininfor:list', 'logininfor', 'admin', '2023-12-26 16:54:02', '', null, '登录日志菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1000, '用户查询', 100, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1001, '用户新增', 100, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1002, '用户修改', 100, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1003, '用户删除', 100, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1004, '用户导出', 100, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1005, '用户导入', 100, 6, '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1006, '重置密码', 100, 7, '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1007, '角色查询', 101, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1008, '角色新增', 101, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1009, '角色修改', 101, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1010, '角色删除', 101, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1011, '角色导出', 101, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1012, '菜单查询', 102, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1013, '菜单新增', 102, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1014, '菜单修改', 102, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1015, '菜单删除', 102, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1016, '部门查询', 103, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1017, '部门新增', 103, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1018, '部门修改', 103, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1019, '部门删除', 103, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1020, '岗位查询', 104, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1021, '岗位新增', 104, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1022, '岗位修改', 104, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1023, '岗位删除', 104, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1024, '岗位导出', 104, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1025, '字典查询', 105, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1026, '字典新增', 105, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1027, '字典修改', 105, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1028, '字典删除', 105, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1029, '字典导出', 105, 5, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1030, '参数查询', 106, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1031, '参数新增', 106, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1032, '参数修改', 106, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1033, '参数删除', 106, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1034, '参数导出', 106, 5, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1035, '公告查询', 107, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1036, '公告新增', 107, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1037, '公告修改', 107, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1038, '公告删除', 107, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1039, '操作查询', 500, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1040, '操作删除', 500, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1041, '日志导出', 500, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1042, '登录查询', 501, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1043, '登录删除', 501, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1044, '日志导出', 501, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1045, '账户解锁', 501, 4, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1046, '在线查询', 109, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1047, '批量强退', 109, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1048, '单条强退', 109, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1049, '任务查询', 110, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1050, '任务新增', 110, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1051, '任务修改', 110, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1052, '任务删除', 110, 4, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1053, '状态修改', 110, 5, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1054, '任务导出', 110, 6, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1055, '生成查询', 116, 1, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1056, '生成修改', 116, 2, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1057, '生成删除', 116, 3, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1058, '导入代码', 116, 4, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1059, '预览代码', 116, 5, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1060, '生成代码', 116, 6, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2000, '服务器管理', 0, 1, 'mc', null, null, 1, 0, 'M', '0', '0', '', 'server', 'admin', '2023-12-26 21:28:37',
        'admin', '2023-12-26 21:56:23', '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2009, '白名单管理', 2000, 1, 'whitelist', 'mc/whitelist/index', null, 1, 0, 'C', '0', '0', 'mc:whitelist:list',
        'peoples', 'admin', '2023-12-26 22:02:01', 'admin', '2023-12-26 22:56:52', '白名单菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2010, '白名单查询', 2009, 1, '#', '', null, 1, 0, 'F', '0', '0', 'mc:whitelist:query', '#', 'admin',
        '2023-12-26 22:02:01', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2011, '白名单新增', 2009, 2, '#', '', null, 1, 0, 'F', '0', '0', 'mc:whitelist:add', '#', 'admin',
        '2023-12-26 22:02:01', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2012, '白名单修改', 2009, 3, '#', '', null, 1, 0, 'F', '0', '0', 'mc:whitelist:edit', '#', 'admin',
        '2023-12-26 22:02:01', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2013, '白名单删除', 2009, 4, '#', '', null, 1, 0, 'F', '0', '0', 'mc:whitelist:remove', '#', 'admin',
        '2023-12-26 22:02:01', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2014, '白名单导出', 2009, 5, '#', '', null, 1, 0, 'F', '0', '0', 'mc:whitelist:export', '#', 'admin',
        '2023-12-26 22:02:01', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2021, '服务器信息', 2000, 1, 'serverlist', 'server/serverlist/index', null, 1, 0, 'C', '0', '0',
        'server:serverlist:list', 'server', 'admin', '2024-03-10 16:00:54', 'admin', '2024-03-10 16:01:38',
        '服务器信息菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2022, '服务器信息查询', 2021, 1, '#', '', null, 1, 0, 'F', '0', '0', 'server:serverlist:query', '#', 'admin',
        '2024-03-10 16:00:54', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2023, '服务器信息新增', 2021, 2, '#', '', null, 1, 0, 'F', '0', '0', 'server:serverlist:add', '#', 'admin',
        '2024-03-10 16:00:54', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2024, '服务器信息修改', 2021, 3, '#', '', null, 1, 0, 'F', '0', '0', 'server:serverlist:edit', '#', 'admin',
        '2024-03-10 16:00:54', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2025, '服务器信息删除', 2021, 4, '#', '', null, 1, 0, 'F', '0', '0', 'server:serverlist:remove', '#', 'admin',
        '2024-03-10 16:00:54', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2026, '服务器信息导出', 2021, 5, '#', '', null, 1, 0, 'F', '0', '0', 'server:serverlist:export', '#', 'admin',
        '2024-03-10 16:00:54', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2033, 'IP限流', 2000, 1, 'limit', 'ipinfo/limit/index', null, 1, 0, 'C', '0', '0', 'ipinfo:limit:list',
        'number', 'admin', '2024-12-20 19:25:20', 'admin', '2024-12-20 19:25:56', 'IP限流菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2034, 'IP限流查询', 2033, 1, '#', '', null, 1, 0, 'F', '0', '0', 'ipinfo:limit:query', '#', 'admin',
        '2024-12-20 19:25:20', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2035, 'IP限流新增', 2033, 2, '#', '', null, 1, 0, 'F', '0', '0', 'ipinfo:limit:add', '#', 'admin',
        '2024-12-20 19:25:20', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2036, 'IP限流修改', 2033, 3, '#', '', null, 1, 0, 'F', '0', '0', 'ipinfo:limit:edit', '#', 'admin',
        '2024-12-20 19:25:20', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2037, 'IP限流删除', 2033, 4, '#', '', null, 1, 0, 'F', '0', '0', 'ipinfo:limit:remove', '#', 'admin',
        '2024-12-20 19:25:20', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2038, 'IP限流导出', 2033, 5, '#', '', null, 1, 0, 'F', '0', '0', 'ipinfo:limit:export', '#', 'admin',
        '2024-12-20 19:25:20', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2039, '封禁管理', 2000, 1, 'banlist', 'mc/banlist/index', null, 1, 0, 'C', '0', '0', 'mc:banlist:list', 'lock',
        'admin', '2024-12-20 19:26:57', 'admin', '2024-12-20 19:28:40', '封禁管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2040, '封禁管理查询', 2039, 1, '#', '', null, 1, 0, 'F', '0', '0', 'mc:banlist:query', '#', 'admin',
        '2024-12-20 19:26:57', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2041, '封禁管理新增', 2039, 2, '#', '', null, 1, 0, 'F', '0', '0', 'mc:banlist:add', '#', 'admin',
        '2024-12-20 19:26:57', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2042, '封禁管理修改', 2039, 3, '#', '', null, 1, 0, 'F', '0', '0', 'mc:banlist:edit', '#', 'admin',
        '2024-12-20 19:26:57', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2043, '封禁管理删除', 2039, 4, '#', '', null, 1, 0, 'F', '0', '0', 'mc:banlist:remove', '#', 'admin',
        '2024-12-20 19:26:57', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2044, '封禁管理导出', 2039, 5, '#', '', null, 1, 0, 'F', '0', '0', 'mc:banlist:export', '#', 'admin',
        '2024-12-20 19:26:57', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2045, '指令管理', 2000, 1, 'command', 'mc/command/index', null, 1, 0, 'C', '0', '0', 'mc:command:list', 'code',
        'admin', '2024-12-20 19:29:44', 'admin', '2024-12-20 19:30:35', '指令管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2046, '指令管理查询', 2045, 1, '#', '', null, 1, 0, 'F', '0', '0', 'mc:command:query', '#', 'admin',
        '2024-12-20 19:29:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2047, '指令管理新增', 2045, 2, '#', '', null, 1, 0, 'F', '0', '0', 'mc:command:add', '#', 'admin',
        '2024-12-20 19:29:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2048, '指令管理修改', 2045, 3, '#', '', null, 1, 0, 'F', '0', '0', 'mc:command:edit', '#', 'admin',
        '2024-12-20 19:29:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2049, '指令管理删除', 2045, 4, '#', '', null, 1, 0, 'F', '0', '0', 'mc:command:remove', '#', 'admin',
        '2024-12-20 19:29:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2050, '指令管理导出', 2045, 5, '#', '', null, 1, 0, 'F', '0', '0', 'mc:command:export', '#', 'admin',
        '2024-12-20 19:29:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2057, '玩家详情', 2000, 1, 'details', 'player/details/index', null, 1, 0, 'C', '0', '0', 'player:details:list',
        'list', 'admin', '2024-12-31 03:34:39', 'admin', '2024-12-31 03:46:23', '玩家详情菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2058, '玩家详情查询', 2057, 1, '#', '', null, 1, 0, 'F', '0', '0', 'player:details:query', '#', 'admin',
        '2024-12-31 03:34:39', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2059, '玩家详情新增', 2057, 2, '#', '', null, 1, 0, 'F', '0', '0', 'player:details:add', '#', 'admin',
        '2024-12-31 03:34:39', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2060, '玩家详情修改', 2057, 3, '#', '', null, 1, 0, 'F', '0', '0', 'player:details:edit', '#', 'admin',
        '2024-12-31 03:34:39', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2061, '玩家详情删除', 2057, 4, '#', '', null, 1, 0, 'F', '0', '0', 'player:details:remove', '#', 'admin',
        '2024-12-31 03:34:39', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2062, '玩家详情导出', 2057, 5, '#', '', null, 1, 0, 'F', '0', '0', 'player:details:export', '#', 'admin',
        '2024-12-31 03:34:39', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2063, '管理员名单', 2000, 1, 'operator', 'player/operator/index', null, 1, 0, 'C', '0', '0',
        'player:operator:list', 'user', 'admin', '2025-01-11 12:20:35', 'admin', '2025-01-11 12:21:19',
        '管理员名单菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2064, '管理员名单查询', 2063, 1, '#', '', null, 1, 0, 'F', '0', '0', 'player:operator:query', '#', 'admin',
        '2025-01-11 12:20:35', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2065, '管理员名单新增', 2063, 2, '#', '', null, 1, 0, 'F', '0', '0', 'player:operator:add', '#', 'admin',
        '2025-01-11 12:20:35', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2066, '管理员名单修改', 2063, 3, '#', '', null, 1, 0, 'F', '0', '0', 'player:operator:edit', '#', 'admin',
        '2025-01-11 12:20:35', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2067, '管理员名单删除', 2063, 4, '#', '', null, 1, 0, 'F', '0', '0', 'player:operator:remove', '#', 'admin',
        '2025-01-11 12:20:35', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2068, '管理员名单导出', 2063, 5, '#', '', null, 1, 0, 'F', '0', '0', 'player:operator:export', '#', 'admin',
        '2025-01-11 12:20:35', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2069, '定时命令', 2000, 1, 'regular', 'regular/command/index', null, 1, 0, 'C', '0', '0',
        'regular:command:list', 'time', 'admin', '2025-02-14 23:47:25', 'admin', '2025-02-14 23:54:39', '定时命令菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2070, '定时命令查询', 2069, 1, '#', '', null, 1, 0, 'F', '0', '0', 'regular:command:query', '#', 'admin',
        '2025-02-14 23:47:26', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2071, '定时命令新增', 2069, 2, '#', '', null, 1, 0, 'F', '0', '0', 'regular:command:add', '#', 'admin',
        '2025-02-14 23:47:26', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2072, '定时命令修改', 2069, 3, '#', '', null, 1, 0, 'F', '0', '0', 'regular:command:edit', '#', 'admin',
        '2025-02-14 23:47:26', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2073, '定时命令删除', 2069, 4, '#', '', null, 1, 0, 'F', '0', '0', 'regular:command:remove', '#', 'admin',
        '2025-02-14 23:47:26', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2074, '定时命令导出', 2069, 5, '#', '', null, 1, 0, 'F', '0', '0', 'regular:command:export', '#', 'admin',
        '2025-02-14 23:47:26', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2075, '节点管理', 0, 2, '/nodelist', null, null, 1, 0, 'M', '0', '0', null, 'cascader', 'admin',
        '2025-03-04 23:10:32', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2082, '机器人管理', 0, 3, '/bot', null, null, 1, 0, 'M', '0', '0', '', 'qq', 'admin', '2025-03-12 15:54:38',
        'admin', '2025-03-12 15:55:01', '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2083, '机器人配置', 2082, 1, 'config', 'bot/config/index', null, 1, 0, 'C', '0', '0', 'bot:config:list', 'dict',
        'admin', '2025-03-12 16:16:35', 'admin', '2025-03-13 18:04:35', 'QQ机器人配置菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2084, 'QQ机器人配置查询', 2083, 1, '#', '', null, 1, 0, 'F', '0', '0', 'bot:config:query', '#', 'admin',
        '2025-03-12 16:16:36', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2085, 'QQ机器人配置新增', 2083, 2, '#', '', null, 1, 0, 'F', '0', '0', 'bot:config:add', '#', 'admin',
        '2025-03-12 16:16:36', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2086, 'QQ机器人配置修改', 2083, 3, '#', '', null, 1, 0, 'F', '0', '0', 'bot:config:edit', '#', 'admin',
        '2025-03-12 16:16:36', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2087, 'QQ机器人配置删除', 2083, 4, '#', '', null, 1, 0, 'F', '0', '0', 'bot:config:remove', '#', 'admin',
        '2025-03-12 16:16:36', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2088, 'QQ机器人配置导出', 2083, 5, '#', '', null, 1, 0, 'F', '0', '0', 'bot:config:export', '#', 'admin',
        '2025-03-12 16:16:36', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2089, '机器人管理员', 2082, 1, 'manager', 'bot/manager/index', null, 1, 0, 'C', '1', '0', 'bot:manager:list',
        'peoples', 'admin', '2025-03-13 18:03:44', 'admin', '2025-03-15 14:10:11', 'QQ机器人管理员菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2090, 'QQ机器人管理员查询', 2089, 1, '#', '', null, 1, 0, 'F', '0', '0', 'bot:manager:query', '#', 'admin',
        '2025-03-13 18:03:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2091, 'QQ机器人管理员新增', 2089, 2, '#', '', null, 1, 0, 'F', '0', '0', 'bot:manager:add', '#', 'admin',
        '2025-03-13 18:03:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2092, 'QQ机器人管理员修改', 2089, 3, '#', '', null, 1, 0, 'F', '0', '0', 'bot:manager:edit', '#', 'admin',
        '2025-03-13 18:03:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2093, 'QQ机器人管理员删除', 2089, 4, '#', '', null, 1, 0, 'F', '0', '0', 'bot:manager:remove', '#', 'admin',
        '2025-03-13 18:03:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2094, 'QQ机器人管理员导出', 2089, 5, '#', '', null, 1, 0, 'F', '0', '0', 'bot:manager:export', '#', 'admin',
        '2025-03-13 18:03:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2095, '题目管理', 0, 2, '/quiz', null, null, 1, 0, 'M', '0', '0', null, 'question', 'admin',
        '2025-03-19 19:15:37', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2096, '题库管理', 2095, 1, 'question', 'quiz/question/index', null, 1, 0, 'C', '0', '0', 'quiz:question:list',
        'documentation', 'admin', '2025-03-19 19:59:19', 'admin', '2025-03-20 15:33:15', '白名单申请题库问题菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2097, '白名单申请题库问题查询', 2096, 1, '#', '', null, 1, 0, 'F', '0', '0', 'quiz:question:query', '#',
        'admin', '2025-03-19 19:59:20', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2098, '白名单申请题库问题新增', 2096, 2, '#', '', null, 1, 0, 'F', '0', '0', 'quiz:question:add', '#', 'admin',
        '2025-03-19 19:59:20', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2099, '白名单申请题库问题修改', 2096, 3, '#', '', null, 1, 0, 'F', '0', '0', 'quiz:question:edit', '#', 'admin',
        '2025-03-19 19:59:20', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2100, '白名单申请题库问题删除', 2096, 4, '#', '', null, 1, 0, 'F', '0', '0', 'quiz:question:remove', '#',
        'admin', '2025-03-19 19:59:20', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2101, '白名单申请题库问题导出', 2096, 5, '#', '', null, 1, 0, 'F', '0', '0', 'quiz:question:export', '#',
        'admin', '2025-03-19 19:59:20', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2102, '答题记录', 2095, 1, 'submission', 'quiz/submission/index', null, 1, 0, 'C', '0', '0',
        'quiz:submission:list', 'log', 'admin', '2025-03-20 15:37:24', 'admin', '2025-03-20 15:37:54', '答题记录菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2103, '答题记录查询', 2102, 1, '#', '', null, 1, 0, 'F', '0', '0', 'quiz:submission:query', '#', 'admin',
        '2025-03-20 15:37:24', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2104, '答题记录新增', 2102, 2, '#', '', null, 1, 0, 'F', '0', '0', 'quiz:submission:add', '#', 'admin',
        '2025-03-20 15:37:24', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2105, '答题记录修改', 2102, 3, '#', '', null, 1, 0, 'F', '0', '0', 'quiz:submission:edit', '#', 'admin',
        '2025-03-20 15:37:24', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2106, '答题记录删除', 2102, 4, '#', '', null, 1, 0, 'F', '0', '0', 'quiz:submission:remove', '#', 'admin',
        '2025-03-20 15:37:25', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2107, '答题记录导出', 2102, 5, '#', '', null, 1, 0, 'F', '0', '0', 'quiz:submission:export', '#', 'admin',
        '2025-03-20 15:37:25', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2108, '题库配置', 2095, 1, 'config', 'quiz/config/index', null, 1, 0, 'C', '0', '0', 'quiz:config:list',
        'system', 'admin', '2025-03-21 00:53:59', 'admin', '2025-03-21 00:57:00', '题库配置菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2109, '题库配置查询', 2108, 1, '#', '', null, 1, 0, 'F', '0', '0', 'quiz:config:query', '#', 'admin',
        '2025-03-21 00:53:59', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2110, '题库配置新增', 2108, 2, '#', '', null, 1, 0, 'F', '0', '0', 'quiz:config:add', '#', 'admin',
        '2025-03-21 00:54:00', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2111, '题库配置修改', 2108, 3, '#', '', null, 1, 0, 'F', '0', '0', 'quiz:config:edit', '#', 'admin',
        '2025-03-21 00:54:00', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2112, '题库配置删除', 2108, 4, '#', '', null, 1, 0, 'F', '0', '0', 'quiz:config:remove', '#', 'admin',
        '2025-03-21 00:54:00', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2113, '题库配置导出', 2108, 5, '#', '', null, 1, 0, 'F', '0', '0', 'quiz:config:export', '#', 'admin',
        '2025-03-21 00:54:00', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2114, '节点服务器', 2075, 1, 'server', 'node/server/index', null, 1, 0, 'C', '0', '0', 'node:server:list',
        'server', 'admin', '2025-04-14 16:02:15', 'admin', '2025-04-14 16:16:13', '节点服务器菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2115, '节点服务器查询', 2114, 1, '#', '', null, 1, 0, 'F', '0', '0', 'node:server:query', '#', 'admin',
        '2025-04-14 16:02:16', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2116, '节点服务器新增', 2114, 2, '#', '', null, 1, 0, 'F', '0', '0', 'node:server:add', '#', 'admin',
        '2025-04-14 16:02:16', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2117, '节点服务器修改', 2114, 3, '#', '', null, 1, 0, 'F', '0', '0', 'node:server:edit', '#', 'admin',
        '2025-04-14 16:02:16', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2118, '节点服务器删除', 2114, 4, '#', '', null, 1, 0, 'F', '0', '0', 'node:server:remove', '#', 'admin',
        '2025-04-14 16:02:16', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2119, '节点服务器导出', 2114, 5, '#', '', null, 1, 0, 'F', '0', '0', 'node:server:export', '#', 'admin',
        '2025-04-14 16:02:16', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2120, '消息日志', 2082, 1, 'log', 'bot/log/index', null, 1, 0, 'C', '0', '0', 'bot:log:list', 'log', 'admin',
        '2025-04-18 16:50:13', 'admin', '2025-04-18 17:36:38', '机器人日志菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2121, '机器人日志查询', 2120, 1, '#', '', null, 1, 0, 'F', '0', '0', 'bot:log:query', '#', 'admin',
        '2025-04-18 16:50:13', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2122, '机器人日志新增', 2120, 2, '#', '', null, 1, 0, 'F', '0', '0', 'bot:log:add', '#', 'admin',
        '2025-04-18 16:50:13', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2123, '机器人日志修改', 2120, 3, '#', '', null, 1, 0, 'F', '0', '0', 'bot:log:edit', '#', 'admin',
        '2025-04-18 16:50:13', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2124, '机器人日志删除', 2120, 4, '#', '', null, 1, 0, 'F', '0', '0', 'bot:log:remove', '#', 'admin',
        '2025-04-18 16:50:13', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2125, '机器人日志导出', 2120, 5, '#', '', null, 1, 0, 'F', '0', '0', 'bot:log:export', '#', 'admin',
        '2025-04-18 16:50:13', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2131, '操作日志', 2075, 99, 'log', 'node/log/index', null, 1, 0, 'C', '0', '0', 'node:log:list', 'log', 'admin',
        '2025-04-24 23:19:08', 'admin', '2025-04-24 23:20:00', '操作日志菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2132, '操作日志查询', 2131, 1, '#', '', null, 1, 0, 'F', '0', '0', 'node:log:query', '#', 'admin',
        '2025-04-24 23:19:09', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2133, '操作日志新增', 2131, 2, '#', '', null, 1, 0, 'F', '0', '0', 'node:log:add', '#', 'admin',
        '2025-04-24 23:19:09', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2134, '操作日志修改', 2131, 3, '#', '', null, 1, 0, 'F', '0', '0', 'node:log:edit', '#', 'admin',
        '2025-04-24 23:19:09', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2135, '操作日志删除', 2131, 4, '#', '', null, 1, 0, 'F', '0', '0', 'node:log:remove', '#', 'admin',
        '2025-04-24 23:19:09', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2136, '操作日志导出', 2131, 5, '#', '', null, 1, 0, 'F', '0', '0', 'node:log:export', '#', 'admin',
        '2025-04-24 23:19:09', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2137, '时限管理', 2000, 1, 'deadline', 'mc/deadline/index', null, 1, 0, 'C', '0', '0', 'mc:deadline:list',
        'time-range', 'admin', '2025-08-15 02:40:30', 'admin', '2025-08-15 02:41:47', '时限管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2138, '时限管理查询', 2137, 1, '#', '', null, 1, 0, 'F', '0', '0', 'mc:deadline:query', '#', 'admin',
        '2025-08-15 02:40:30', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2139, '时限管理新增', 2137, 2, '#', '', null, 1, 0, 'F', '0', '0', 'mc:deadline:add', '#', 'admin',
        '2025-08-15 02:40:30', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2140, '时限管理修改', 2137, 3, '#', '', null, 1, 0, 'F', '0', '0', 'mc:deadline:edit', '#', 'admin',
        '2025-08-15 02:40:30', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2141, '时限管理删除', 2137, 4, '#', '', null, 1, 0, 'F', '0', '0', 'mc:deadline:remove', '#', 'admin',
        '2025-08-15 02:40:30', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2142, '时限管理导出', 2137, 5, '#', '', null, 1, 0, 'F', '0', '0', 'mc:deadline:export', '#', 'admin',
        '2025-08-15 02:40:30', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2143, '邮件管理', 0, 2, 'email', null, null, 1, 0, 'M', '0', '0', '', 'email', 'admin', '2025-10-03 01:55:03',
        'admin', '2025-10-03 01:55:17', '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2144, '邮件模板', 2143, 1, 'templates', 'email/templates/index', null, 1, 0, 'C', '0', '0',
        'email:templates:list', 'documentation', 'admin', '2025-10-03 02:16:01', 'admin', '2025-10-07 06:02:20',
        '自定义邮件通知模板菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2145, '自定义邮件通知模板查询', 2144, 1, '#', '', null, 1, 0, 'F', '0', '0', 'email:templates:query', '#',
        'admin', '2025-10-03 02:16:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2146, '自定义邮件通知模板新增', 2144, 2, '#', '', null, 1, 0, 'F', '0', '0', 'email:templates:add', '#',
        'admin', '2025-10-03 02:16:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2147, '自定义邮件通知模板修改', 2144, 3, '#', '', null, 1, 0, 'F', '0', '0', 'email:templates:edit', '#',
        'admin', '2025-10-03 02:16:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2148, '自定义邮件通知模板删除', 2144, 4, '#', '', null, 1, 0, 'F', '0', '0', 'email:templates:remove', '#',
        'admin', '2025-10-03 02:16:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2149, '自定义邮件通知模板导出', 2144, 5, '#', '', null, 1, 0, 'F', '0', '0', 'email:templates:export', '#',
        'admin', '2025-10-03 02:16:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2150, '实例管理', 2075, 1, 'mcs', 'node/mcs/index', null, 1, 0, 'C', '1', '0', 'node:mcs:list', '#', 'admin',
        '2025-10-28 21:14:01', 'admin', '2025-10-29 19:17:31', '实例管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2151, '实例管理查询', 2150, 1, '#', '', null, 1, 0, 'F', '0', '0', 'node:mcs:query', '#', 'admin',
        '2025-10-28 21:14:01', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2152, '实例管理新增', 2150, 2, '#', '', null, 1, 0, 'F', '0', '0', 'node:mcs:add', '#', 'admin',
        '2025-10-28 21:14:01', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2153, '实例管理修改', 2150, 3, '#', '', null, 1, 0, 'F', '0', '0', 'node:mcs:edit', '#', 'admin',
        '2025-10-28 21:14:01', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2154, '实例管理删除', 2150, 4, '#', '', null, 1, 0, 'F', '0', '0', 'node:mcs:remove', '#', 'admin',
        '2025-10-28 21:14:01', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (2155, '实例管理导出', 2150, 5, '#', '', null, 1, 0, 'F', '0', '0', 'node:mcs:export', '#', 'admin',
        '2025-10-28 21:14:01', '', null, '');
