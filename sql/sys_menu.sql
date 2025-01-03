create table sys_menu
(
    menu_id     bigint auto_increment comment '菜单ID'
        primary key,
    menu_name   varchar(50)             not null comment '菜单名称',
    parent_id   bigint       default 0  null comment '父菜单ID',
    order_num   int          default 0  null comment '显示顺序',
    path        varchar(200) default '' null comment '路由地址',
    component   varchar(255)            null comment '组件路径',
    query       varchar(255)            null comment '路由参数',
    is_frame    int          default 1  null comment '是否为外链（0是 1否）',
    is_cache    int          default 0  null comment '是否缓存（0缓存 1不缓存）',
    menu_type   char         default '' null comment '菜单类型（M目录 C菜单 F按钮）',
    visible     char         default '0' null comment '菜单状态（0显示 1隐藏）',
    status      char         default '0' null comment '菜单状态（0正常 1停用）',
    perms       varchar(100)            null comment '权限标识',
    icon        varchar(100) default '#' null comment '菜单图标',
    create_by   varchar(64)  default '' null comment '创建者',
    create_time datetime                null comment '创建时间',
    update_by   varchar(64)  default '' null comment '更新者',
    update_time datetime                null comment '更新时间',
    remark      varchar(500) default '' null comment '备注'
)
    comment '菜单权限表';

INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1, '系统管理', 0, 1, 'system', null, '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2023-12-26 16:54:02', '',
        null, '系统管理目录');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2, '系统监控', 0, 2, 'monitor', null, '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', '2023-12-26 16:54:02',
        '', null, '系统监控目录');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (3, '系统工具', 0, 3, 'tool', null, '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2023-12-26 16:54:02', '',
        null, '系统工具目录');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (4, '若依官网', 0, 4, 'http://ruoyi.vip', null, '', 0, 0, 'M', '1', '1', '', 'guide', 'admin',
        '2023-12-26 16:54:02', 'admin', '2023-12-26 21:38:21', '若依官网地址');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user',
        'admin', '2023-12-26 16:54:02', '', null, '用户管理菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples',
        'admin', '2023-12-26 16:54:02', '', null, '角色管理菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table',
        'admin', '2023-12-26 16:54:02', '', null, '菜单管理菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', 1, 0, 'C', '1', '0', 'system:dept:list', 'tree',
        'admin', '2023-12-26 16:54:02', 'admin', '2023-12-26 21:37:32', '部门管理菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', 1, 0, 'C', '1', '0', 'system:post:list', 'post',
        'admin', '2023-12-26 16:54:02', 'admin', '2023-12-26 21:37:37', '岗位管理菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', 1, 0, 'C', '0', '0', 'system:dict:list', 'dict',
        'admin', '2023-12-26 16:54:02', '', null, '字典管理菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit',
        'admin', '2023-12-26 16:54:02', '', null, '参数设置菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', 1, 0, 'C', '0', '0', 'system:notice:list',
        'message', 'admin', '2023-12-26 16:54:02', '', null, '通知公告菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (108, '日志管理', 1, 9, 'log', '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', '2023-12-26 16:54:02', '', null,
        '日志管理菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', 1, 0, 'C', '0', '0', 'monitor:online:list',
        'online', 'admin', '2023-12-26 16:54:02', '', null, '在线用户菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (110, '定时任务', 2, 2, 'job', 'monitor/job/index', '', 1, 0, 'C', '0', '0', 'monitor:job:list', 'job', 'admin',
        '2023-12-26 16:54:02', '', null, '定时任务菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (111, '数据监控', 2, 3, 'druid', 'monitor/druid/index', '', 1, 0, 'C', '0', '0', 'monitor:druid:list', 'druid',
        'admin', '2023-12-26 16:54:02', '', null, '数据监控菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (112, '服务监控', 2, 4, 'server', 'monitor/server/index', '', 1, 0, 'C', '0', '0', 'monitor:server:list',
        'server', 'admin', '2023-12-26 16:54:02', '', null, '服务监控菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (113, '缓存监控', 2, 5, 'cache', 'monitor/cache/index', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis',
        'admin', '2023-12-26 16:54:02', '', null, '缓存监控菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (114, '缓存列表', 2, 6, 'cacheList', 'monitor/cache/list', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',
        'redis-list', 'admin', '2023-12-26 16:54:02', '', null, '缓存列表菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (115, '表单构建', 3, 1, 'build', 'tool/build/index', '', 1, 0, 'C', '0', '0', 'tool:build:list', 'build',
        'admin', '2023-12-26 16:54:02', '', null, '表单构建菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (116, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin',
        '2023-12-26 16:54:02', '', null, '代码生成菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (117, '系统接口', 3, 3, 'swagger', 'tool/swagger/index', '', 1, 0, 'C', '0', '0', 'tool:swagger:list', 'swagger',
        'admin', '2023-12-26 16:54:02', '', null, '系统接口菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list',
        'form', 'admin', '2023-12-26 16:54:02', '', null, '操作日志菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', 1, 0, 'C', '0', '0',
        'monitor:logininfor:list', 'logininfor', 'admin', '2023-12-26 16:54:02', '', null, '登录日志菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1000, '用户查询', 100, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1001, '用户新增', 100, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1002, '用户修改', 100, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1003, '用户删除', 100, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1004, '用户导出', 100, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1005, '用户导入', 100, 6, '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1006, '重置密码', 100, 7, '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1007, '角色查询', 101, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1008, '角色新增', 101, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1009, '角色修改', 101, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1010, '角色删除', 101, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1011, '角色导出', 101, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1012, '菜单查询', 102, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1013, '菜单新增', 102, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1014, '菜单修改', 102, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1015, '菜单删除', 102, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1016, '部门查询', 103, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1017, '部门新增', 103, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1018, '部门修改', 103, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1019, '部门删除', 103, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1020, '岗位查询', 104, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1021, '岗位新增', 104, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1022, '岗位修改', 104, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1023, '岗位删除', 104, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1024, '岗位导出', 104, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1025, '字典查询', 105, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1026, '字典新增', 105, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1027, '字典修改', 105, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1028, '字典删除', 105, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1029, '字典导出', 105, 5, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1030, '参数查询', 106, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1031, '参数新增', 106, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1032, '参数修改', 106, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1033, '参数删除', 106, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1034, '参数导出', 106, 5, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1035, '公告查询', 107, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1036, '公告新增', 107, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1037, '公告修改', 107, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1038, '公告删除', 107, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1039, '操作查询', 500, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1040, '操作删除', 500, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1041, '日志导出', 500, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1042, '登录查询', 501, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1043, '登录删除', 501, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1044, '日志导出', 501, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1045, '账户解锁', 501, 4, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1046, '在线查询', 109, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1047, '批量强退', 109, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1048, '单条强退', 109, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1049, '任务查询', 110, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1050, '任务新增', 110, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1051, '任务修改', 110, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1052, '任务删除', 110, 4, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1053, '状态修改', 110, 5, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1054, '任务导出', 110, 6, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1055, '生成查询', 116, 1, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1056, '生成修改', 116, 2, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1057, '生成删除', 116, 3, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1058, '导入代码', 116, 4, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1059, '预览代码', 116, 5, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1060, '生成代码', 116, 6, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2000, '服务器管理', 0, 1, 'mc', null, null, 1, 0, 'M', '0', '0', '', 'server', 'admin', '2023-12-26 21:28:37',
        'admin', '2023-12-26 21:56:23', '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2009, '白名单管理', 2000, 1, 'whitelist', 'mc/whitelist/index', null, 1, 0, 'C', '0', '0', 'mc:whitelist:list',
        'peoples', 'admin', '2023-12-26 22:02:01', 'admin', '2023-12-26 22:56:52', '白名单菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2010, '白名单查询', 2009, 1, '#', '', null, 1, 0, 'F', '0', '0', 'mc:whitelist:query', '#', 'admin',
        '2023-12-26 22:02:01', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2011, '白名单新增', 2009, 2, '#', '', null, 1, 0, 'F', '0', '0', 'mc:whitelist:add', '#', 'admin',
        '2023-12-26 22:02:01', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2012, '白名单修改', 2009, 3, '#', '', null, 1, 0, 'F', '0', '0', 'mc:whitelist:edit', '#', 'admin',
        '2023-12-26 22:02:01', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2013, '白名单删除', 2009, 4, '#', '', null, 1, 0, 'F', '0', '0', 'mc:whitelist:remove', '#', 'admin',
        '2023-12-26 22:02:01', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2014, '白名单导出', 2009, 5, '#', '', null, 1, 0, 'F', '0', '0', 'mc:whitelist:export', '#', 'admin',
        '2023-12-26 22:02:01', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2021, '服务器信息', 2000, 1, 'serverlist', 'server/serverlist/index', null, 1, 0, 'C', '0', '0',
        'server:serverlist:list', 'server', 'admin', '2024-03-10 16:00:54', 'admin', '2024-03-10 16:01:38',
        '服务器信息菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2022, '服务器信息查询', 2021, 1, '#', '', null, 1, 0, 'F', '0', '0', 'server:serverlist:query', '#', 'admin',
        '2024-03-10 16:00:54', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2023, '服务器信息新增', 2021, 2, '#', '', null, 1, 0, 'F', '0', '0', 'server:serverlist:add', '#', 'admin',
        '2024-03-10 16:00:54', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2024, '服务器信息修改', 2021, 3, '#', '', null, 1, 0, 'F', '0', '0', 'server:serverlist:edit', '#', 'admin',
        '2024-03-10 16:00:54', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2025, '服务器信息删除', 2021, 4, '#', '', null, 1, 0, 'F', '0', '0', 'server:serverlist:remove', '#', 'admin',
        '2024-03-10 16:00:54', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2026, '服务器信息导出', 2021, 5, '#', '', null, 1, 0, 'F', '0', '0', 'server:serverlist:export', '#', 'admin',
        '2024-03-10 16:00:54', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2033, 'IP限流', 2000, 1, 'limit', 'ipinfo/limit/index', null, 1, 0, 'C', '0', '0', 'ipinfo:limit:list',
        'number', 'admin', '2024-12-20 19:25:20', 'admin', '2024-12-20 19:25:56', 'IP限流菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2034, 'IP限流查询', 2033, 1, '#', '', null, 1, 0, 'F', '0', '0', 'ipinfo:limit:query', '#', 'admin',
        '2024-12-20 19:25:20', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2035, 'IP限流新增', 2033, 2, '#', '', null, 1, 0, 'F', '0', '0', 'ipinfo:limit:add', '#', 'admin',
        '2024-12-20 19:25:20', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2036, 'IP限流修改', 2033, 3, '#', '', null, 1, 0, 'F', '0', '0', 'ipinfo:limit:edit', '#', 'admin',
        '2024-12-20 19:25:20', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2037, 'IP限流删除', 2033, 4, '#', '', null, 1, 0, 'F', '0', '0', 'ipinfo:limit:remove', '#', 'admin',
        '2024-12-20 19:25:20', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2038, 'IP限流导出', 2033, 5, '#', '', null, 1, 0, 'F', '0', '0', 'ipinfo:limit:export', '#', 'admin',
        '2024-12-20 19:25:20', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2039, '封禁管理', 2000, 1, 'banlist', 'mc/banlist/index', null, 1, 0, 'C', '0', '0', 'mc:banlist:list', 'lock',
        'admin', '2024-12-20 19:26:57', 'admin', '2024-12-20 19:28:40', '封禁管理菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2040, '封禁管理查询', 2039, 1, '#', '', null, 1, 0, 'F', '0', '0', 'mc:banlist:query', '#', 'admin',
        '2024-12-20 19:26:57', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2041, '封禁管理新增', 2039, 2, '#', '', null, 1, 0, 'F', '0', '0', 'mc:banlist:add', '#', 'admin',
        '2024-12-20 19:26:57', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2042, '封禁管理修改', 2039, 3, '#', '', null, 1, 0, 'F', '0', '0', 'mc:banlist:edit', '#', 'admin',
        '2024-12-20 19:26:57', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2043, '封禁管理删除', 2039, 4, '#', '', null, 1, 0, 'F', '0', '0', 'mc:banlist:remove', '#', 'admin',
        '2024-12-20 19:26:57', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2044, '封禁管理导出', 2039, 5, '#', '', null, 1, 0, 'F', '0', '0', 'mc:banlist:export', '#', 'admin',
        '2024-12-20 19:26:57', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2045, '指令管理', 2000, 1, 'command', 'mc/command/index', null, 1, 0, 'C', '0', '0', 'mc:command:list', 'code',
        'admin', '2024-12-20 19:29:44', 'admin', '2024-12-20 19:30:35', '指令管理菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2046, '指令管理查询', 2045, 1, '#', '', null, 1, 0, 'F', '0', '0', 'mc:command:query', '#', 'admin',
        '2024-12-20 19:29:44', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2047, '指令管理新增', 2045, 2, '#', '', null, 1, 0, 'F', '0', '0', 'mc:command:add', '#', 'admin',
        '2024-12-20 19:29:44', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2048, '指令管理修改', 2045, 3, '#', '', null, 1, 0, 'F', '0', '0', 'mc:command:edit', '#', 'admin',
        '2024-12-20 19:29:44', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2049, '指令管理删除', 2045, 4, '#', '', null, 1, 0, 'F', '0', '0', 'mc:command:remove', '#', 'admin',
        '2024-12-20 19:29:44', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2050, '指令管理导出', 2045, 5, '#', '', null, 1, 0, 'F', '0', '0', 'mc:command:export', '#', 'admin',
        '2024-12-20 19:29:44', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2057, '玩家详情', 2000, 1, 'details', 'player/details/index', null, 1, 0, 'C', '0', '0', 'player:details:list',
        'list', 'admin', '2024-12-31 03:34:39', 'admin', '2024-12-31 03:46:23', '玩家详情菜单');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2058, '玩家详情查询', 2057, 1, '#', '', null, 1, 0, 'F', '0', '0', 'player:details:query', '#', 'admin',
        '2024-12-31 03:34:39', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2059, '玩家详情新增', 2057, 2, '#', '', null, 1, 0, 'F', '0', '0', 'player:details:add', '#', 'admin',
        '2024-12-31 03:34:39', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2060, '玩家详情修改', 2057, 3, '#', '', null, 1, 0, 'F', '0', '0', 'player:details:edit', '#', 'admin',
        '2024-12-31 03:34:39', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2061, '玩家详情删除', 2057, 4, '#', '', null, 1, 0, 'F', '0', '0', 'player:details:remove', '#', 'admin',
        '2024-12-31 03:34:39', '', null, '');
INSERT INTO ruoyi.sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2062, '玩家详情导出', 2057, 5, '#', '', null, 1, 0, 'F', '0', '0', 'player:details:export', '#', 'admin',
        '2024-12-31 03:34:39', '', null, '');
