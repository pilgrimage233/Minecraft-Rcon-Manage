INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                      remark)
VALUES (2082, '机器人管理', 0, 3, '/bot', null, null, 1, 0, 'M', '0', '0', '', 'qq', 'admin', '2025-03-12 15:54:38',
        'admin', '2025-03-12 15:55:01', '');

-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, update_by, update_time, remark)
values ('QQ机器人配置', '2082', '1', 'config', 'bot/config/index', 1, 0, 'C', '0', '0', 'bot:config:list', '#', 'admin',
        sysdate(), '', null, 'QQ机器人配置菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, update_by, update_time, remark)
values ('QQ机器人配置查询', @parentId, '1', '#', '', 1, 0, 'F', '0', '0', 'bot:config:query', '#', 'admin', sysdate(),
        '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, update_by, update_time, remark)
values ('QQ机器人配置新增', @parentId, '2', '#', '', 1, 0, 'F', '0', '0', 'bot:config:add', '#', 'admin', sysdate(), '',
        null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, update_by, update_time, remark)
values ('QQ机器人配置修改', @parentId, '3', '#', '', 1, 0, 'F', '0', '0', 'bot:config:edit', '#', 'admin', sysdate(),
        '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, update_by, update_time, remark)
values ('QQ机器人配置删除', @parentId, '4', '#', '', 1, 0, 'F', '0', '0', 'bot:config:remove', '#', 'admin', sysdate(),
        '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, update_by, update_time, remark)
values ('QQ机器人配置导出', @parentId, '5', '#', '', 1, 0, 'F', '0', '0', 'bot:config:export', '#', 'admin', sysdate(),
        '', null, '');

-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, update_by, update_time, remark)
values ('QQ机器人管理员', '2082', '1', 'manager', 'bot/manager/index', 1, 0, 'C', '0', '0', 'bot:manager:list', '#',
        'admin', sysdate(), '', null, 'QQ机器人管理员菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, update_by, update_time, remark)
values ('QQ机器人管理员查询', @parentId, '1', '#', '', 1, 0, 'F', '0', '0', 'bot:manager:query', '#', 'admin',
        sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, update_by, update_time, remark)
values ('QQ机器人管理员新增', @parentId, '2', '#', '', 1, 0, 'F', '0', '0', 'bot:manager:add', '#', 'admin', sysdate(),
        '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, update_by, update_time, remark)
values ('QQ机器人管理员修改', @parentId, '3', '#', '', 1, 0, 'F', '0', '0', 'bot:manager:edit', '#', 'admin', sysdate(),
        '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, update_by, update_time, remark)
values ('QQ机器人管理员删除', @parentId, '4', '#', '', 1, 0, 'F', '0', '0', 'bot:manager:remove', '#', 'admin',
        sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status,
                      perms, icon, create_by, create_time, update_by, update_time, remark)
values ('QQ机器人管理员导出', @parentId, '5', '#', '', 1, 0, 'F', '0', '0', 'bot:manager:export', '#', 'admin',
        sysdate(), '', null, '');
