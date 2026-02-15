-- =====================================================
-- 版本: 3.7.0
-- 描述: 白名单用户登录监控菜单
-- 作者: Memory
-- 日期: 2026-02-07
-- =====================================================

-- 获取当前最大的菜单ID
SET @max_menu_id = (SELECT IFNULL(MAX(menu_id), 0)
                    FROM sys_menu);

-- 白名单用户监控菜单 (父菜单ID: 2 - 系统监控)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 1, '白名单用户', 2, 7, 'whitelistUser', 'monitor/whitelistUser/index', null, 1, 0,
        'C', '0', '0', 'monitor:whitelist-user:list', 'user', 'admin', now(), '白名单用户登录监控菜单');

-- 白名单用户强退权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (@max_menu_id + 2, '白名单用户强退', @max_menu_id + 1, 1, '#', '', null, 1, 0,
        'F', '0', '0', 'monitor:whitelist-user:forceLogout', '#', 'admin', now(), '');
