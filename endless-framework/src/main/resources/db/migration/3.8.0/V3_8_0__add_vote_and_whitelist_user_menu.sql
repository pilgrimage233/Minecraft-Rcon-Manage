-- =====================================================
-- 描述: 服务器管理功能菜单 - 版本3.8.0升级脚本（增量脚本，可重复执行）
-- 日期: 2026-02-09
-- =====================================================

-- 1) 白名单投票菜单与按钮权限（独立页面，挂在"服务器管理"菜单下）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '白名单投票',
       p.menu_id,
       5,
       'whitelistVote',
       'mc/whitelistVote/index',
       null,
       1,
       0,
       'C',
       '0',
       '0',
       'mc:whitelist:vote:view',
       'form',
       'admin',
       NOW(),
       'admin',
       NOW(),
       '白名单投票管理菜单'
FROM (SELECT menu_id FROM sys_menu WHERE path = 'mc' AND menu_type = 'M' LIMIT 1) p
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'whitelistVote' AND menu_type = 'C');

UPDATE sys_menu child
    JOIN (SELECT menu_id FROM sys_menu WHERE perms = 'mc:whitelist:vote:view' AND menu_type = 'C' LIMIT 1) parent
SET child.parent_id = parent.menu_id,
    child.order_num = CASE child.perms
                          WHEN 'mc:whitelist:vote:list' THEN 1
                          WHEN 'mc:whitelist:vote:create' THEN 2
                          WHEN 'mc:whitelist:vote:cast' THEN 3
                          WHEN 'mc:whitelist:vote:template:add' THEN 4
                          ELSE child.order_num
        END
WHERE child.perms IN (
                      'mc:whitelist:vote:list',
                      'mc:whitelist:vote:create',
                      'mc:whitelist:vote:cast',
                      'mc:whitelist:vote:template:add'
    );

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '白名单投票查询',
       p.menu_id,
       1,
       '#',
       '',
       null,
       1,
       0,
       'F',
       '0',
       '0',
       'mc:whitelist:vote:list',
       '#',
       'admin',
       NOW(),
       'admin',
       NOW(),
       '白名单投票查询权限'
FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mc:whitelist:vote:view' AND menu_type = 'C' LIMIT 1) p
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mc:whitelist:vote:list');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '白名单投票发起',
       p.menu_id,
       2,
       '#',
       '',
       null,
       1,
       0,
       'F',
       '0',
       '0',
       'mc:whitelist:vote:create',
       '#',
       'admin',
       NOW(),
       'admin',
       NOW(),
       '白名单投票发起权限'
FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mc:whitelist:vote:view' AND menu_type = 'C' LIMIT 1) p
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mc:whitelist:vote:create');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '白名单投票跟投',
       p.menu_id,
       3,
       '#',
       '',
       null,
       1,
       0,
       'F',
       '0',
       '0',
       'mc:whitelist:vote:cast',
       '#',
       'admin',
       NOW(),
       'admin',
       NOW(),
       '白名单投票跟投权限'
FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mc:whitelist:vote:view' AND menu_type = 'C' LIMIT 1) p
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mc:whitelist:vote:cast');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '白名单投票模板新增',
       p.menu_id,
       4,
       '#',
       '',
       null,
       1,
       0,
       'F',
       '0',
       '0',
       'mc:whitelist:vote:template:add',
       '#',
       'admin',
       NOW(),
       'admin',
       NOW(),
       '白名单投票模板新增权限'
FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mc:whitelist:vote:view' AND menu_type = 'C' LIMIT 1) p
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mc:whitelist:vote:template:add');

-- 2) 白名单用户监控菜单（用于等级/头衔/发起权限管理 - 挂在服务器管理菜单下）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '白名单用户监控',
       p.menu_id,
       6,
       'whitelistUser',
       'monitor/whitelistUser/index',
       null,
       1,
       0,
       'C',
       '0',
       '0',
       'monitor:whitelist-user:list',
       'peoples',
       'admin',
       NOW(),
       'admin',
       NOW(),
       '白名单用户在线与角色管理'
FROM (SELECT menu_id FROM sys_menu WHERE path = 'mc' AND menu_type = 'M' LIMIT 1) p
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'monitor:whitelist-user:list');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '白名单用户强退',
       p.menu_id,
       1,
       '#',
       '',
       null,
       1,
       0,
       'F',
       '0',
       '0',
       'monitor:whitelist-user:forceLogout',
       '#',
       'admin',
       NOW(),
       'admin',
       NOW(),
       '白名单用户强退权限'
FROM (SELECT menu_id FROM sys_menu WHERE perms = 'monitor:whitelist-user:list' LIMIT 1) p
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'monitor:whitelist-user:forceLogout');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type,
                      visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '白名单用户角色修改',
       p.menu_id,
       2,
       '#',
       '',
       null,
       1,
       0,
       'F',
       '0',
       '0',
       'monitor:whitelist-user:role',
       '#',
       'admin',
       NOW(),
       'admin',
       NOW(),
       '白名单用户角色权限'
FROM (SELECT menu_id FROM sys_menu WHERE perms = 'monitor:whitelist-user:list' LIMIT 1) p
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'monitor:whitelist-user:role');
