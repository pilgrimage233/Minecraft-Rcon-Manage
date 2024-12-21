create table sys_logininfor
(
    info_id        bigint auto_increment comment '访问ID'
        primary key,
    user_name      varchar(50)  default '' null comment '用户账号',
    ipaddr         varchar(128) default '' null comment '登录IP地址',
    login_location varchar(255) default '' null comment '登录地点',
    browser        varchar(50)  default '' null comment '浏览器类型',
    os             varchar(50)  default '' null comment '操作系统',
    status         char         default '0' null comment '登录状态（0成功 1失败）',
    msg            varchar(255) default '' null comment '提示消息',
    login_time     datetime                null comment '访问时间'
)
    comment '系统访问记录';

create index idx_sys_logininfor_lt
    on sys_logininfor (login_time);

create index idx_sys_logininfor_s
    on sys_logininfor (status);

INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (100, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Mac OS X', '0', '登录成功', '2023-12-26 21:22:34');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (101, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Mac OS X', '0', '退出成功', '2023-12-26 22:10:47');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (102, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Mac OS X', '1', '验证码错误', '2023-12-26 22:10:53');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (103, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Mac OS X', '0', '登录成功', '2023-12-26 22:10:56');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (104, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Mac OS X', '1', '验证码错误', '2023-12-27 18:05:23');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (105, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Mac OS X', '0', '登录成功', '2023-12-27 18:05:26');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (106, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Mac OS X', '0', '登录成功', '2023-12-27 18:28:08');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (107, 'admin', '154.21.193.236', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-27 14:47:59');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (108, 'admin', '154.21.193.236', 'XX XX', 'Chrome 12', 'Windows 10', '0', '退出成功', '2023-12-27 14:51:46');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (109, 'admin', '154.21.193.236', ' ', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-27 14:52:04');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (110, 'admin', '154.21.193.236', ' ', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-27 16:28:27');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (111, 'admin', '112.224.162.138', '山东省 济南市', 'Chrome Mobile', 'Android 1.x', '1', '用户不存在/密码错误',
        '2023-12-27 16:34:40');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (112, 'admin', '112.224.162.138', '山东省 济南市', 'Chrome Mobile', 'Android 1.x', '1', '用户不存在/密码错误',
        '2023-12-27 16:34:57');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (113, '1732787414', '112.224.162.138', '山东省 济南市', 'Chrome Mobile', 'Android 1.x', '0', '登录成功',
        '2023-12-27 16:35:29');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (114, 'admin', '154.21.193.236', ' ', 'Chrome 12', 'Windows 10', '0', '退出成功', '2023-12-27 16:36:18');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (115, 'admin', '154.21.193.236', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-27 16:36:27');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (116, 'admin', '154.21.193.236', ' ', 'Chrome 12', 'Windows 10', '0', '退出成功', '2023-12-27 16:36:42');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (117, 'admin', '154.21.193.236', 'XX XX', 'Chrome 12', 'Windows 10', '1', '用户不存在/密码错误',
        '2023-12-27 16:36:53');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (118, 'admin', '4.2.2.2', 'XX XX', 'Chrome 12', 'Windows 10', '1', '用户不存在/密码错误', '2023-12-27 16:37:11');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (119, 'admin', '4.2.2.2', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-27 16:37:17');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (120, 'admin', '154.21.193.236', ' ', 'Mobile Safari', 'Mac OS X (iPhone)', '1', '用户不存在/密码错误',
        '2023-12-27 16:37:49');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (121, 'admin', '154.21.193.236', ' ', 'Mobile Safari', 'Mac OS X (iPhone)', '1', '用户不存在/密码错误',
        '2023-12-27 18:59:59');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (122, 'admin', '154.21.193.236', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '1', '验证码错误',
        '2023-12-27 19:00:08');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (123, 'admin', '154.21.193.236', ' ', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2023-12-27 19:00:13');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (124, 'admin', '154.21.193.236', 'XX XX', 'Chrome 12', 'Windows 10', '1', '用户不存在/密码错误',
        '2023-12-28 07:32:42');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (125, 'admin', '154.21.193.236', ' ', 'Chrome 12', 'Windows 10', '1', '验证码错误', '2023-12-28 07:32:48');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (126, 'admin', '154.21.193.236', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-28 07:32:51');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (127, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-28 08:58:01');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (128, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-28 09:57:09');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (129, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Mac OS X', '0', '登录成功', '2023-12-28 10:22:15');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (130, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Mac OS X', '1', '用户不存在/密码错误',
        '2023-12-28 10:58:58');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (131, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Mac OS X', '1', '用户不存在/密码错误',
        '2023-12-28 10:59:01');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (132, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Mac OS X', '0', '登录成功', '2023-12-28 10:59:08');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (133, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-28 11:35:18');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (134, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-28 15:29:20');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (135, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-28 16:30:00');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (136, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-28 16:30:42');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (137, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-29 07:50:54');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (138, 'showsi', '27.191.196.29', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2023-12-29 07:55:57');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (139, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-29 09:07:08');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (140, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-29 11:02:04');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (141, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-29 11:57:37');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (142, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-29 12:28:28');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (143, 'admin', '203.198.87.154', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '1', '验证码错误',
        '2023-12-29 13:24:49');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (144, 'admin', '203.198.87.154', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2023-12-29 13:24:53');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (145, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Mac OS X', '1', '用户不存在/密码错误',
        '2023-12-29 13:54:12');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (146, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Mac OS X', '1', '用户不存在/密码错误',
        '2023-12-29 13:54:15');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (147, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Mac OS X', '0', '登录成功', '2023-12-29 13:54:21');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (148, 'admin', '203.198.87.154', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '1', '用户不存在/密码错误',
        '2023-12-29 16:16:04');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (149, 'admin', '203.198.87.154', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2023-12-29 16:16:13');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (150, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '1', '验证码错误', '2023-12-30 06:12:22');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (151, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '1', '验证码已失效', '2023-12-30 06:12:23');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (152, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-30 06:12:25');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (153, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-30 12:50:29');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (154, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-31 07:16:19');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (155, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2023-12-31 11:27:15');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (156, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-01 09:47:50');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (157, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-03 15:04:10');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (158, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-05 14:42:24');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (159, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '1', '用户不存在/密码错误',
        '2024-01-06 04:23:46');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (160, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '1', '用户不存在/密码错误',
        '2024-01-06 04:23:51');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (161, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '1', '验证码错误',
        '2024-01-06 04:24:02');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (162, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-06 04:24:06');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (163, 'admin', '60.27.159.15', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '1', '用户不存在/密码错误',
        '2024-01-06 07:19:59');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (164, 'admin', '60.27.159.15', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-06 07:20:08');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (165, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-06 08:10:12');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (166, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '1', '验证码错误', '2024-01-06 15:43:20');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (167, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-06 15:43:22');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (168, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-07 05:48:27');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (169, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-09 07:28:35');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (170, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '1', '用户不存在/密码错误',
        '2024-01-09 07:44:10');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (171, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-09 07:44:19');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (172, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '1', '用户不存在/密码错误',
        '2024-01-09 10:03:10');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (173, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-09 10:03:16');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (174, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '1', '用户不存在/密码错误',
        '2024-01-09 12:28:44');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (175, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-09 12:28:51');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (176, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-09 15:36:08');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (177, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-09 17:03:24');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (178, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-10 04:57:11');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (179, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-10 08:23:47');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (180, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-10 08:26:32');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (181, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-10 12:32:04');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (182, 'showsi', '27.191.196.29', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2024-01-10 12:32:34');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (183, 'showsi', '27.191.196.29', 'XX XX', 'Chrome 10', 'Windows 10', '1', '验证码错误', '2024-01-10 12:34:44');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (184, 'showsi', '27.191.196.29', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2024-01-10 12:34:50');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (185, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '1', '验证码错误', '2024-01-10 14:27:28');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (186, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-10 14:27:32');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (187, 'showsi', '27.191.196.29', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2024-01-10 14:39:24');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (188, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-10 15:36:51');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (189, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-10 18:26:36');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (190, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-11 10:46:17');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (191, 'showsi', '27.191.196.29', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2024-01-11 15:41:22');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (192, 'showsi', '27.191.196.29', 'XX XX', 'Chrome 10', 'Windows 10', '0', '登录成功', '2024-01-11 15:55:09');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (193, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-11 15:55:13');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (194, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-11 16:35:22');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (195, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-11 18:55:41');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (196, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '1', '验证码已失效', '2024-01-12 13:57:04');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (197, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-12 13:57:10');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (198, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-12 14:30:48');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (199, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-12 15:37:50');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (200, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-12 19:20:32');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (201, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-13 05:23:50');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (202, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-13 06:13:20');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (203, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-13 08:46:37');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (204, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-13 09:34:54');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (205, 'admin', '164.70.99.9', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-13 11:07:57');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (206, 'admin', '164.70.99.9', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-13 15:08:24');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (207, 'admin', '164.70.99.9', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-13 15:58:29');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (208, 'admin', '164.70.99.9', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-13 16:48:36');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (209, 'admin', '164.70.99.9', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '1', '验证码错误',
        '2024-01-14 08:04:20');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (210, 'admin', '164.70.99.9', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-14 08:04:22');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (211, 'admin', '164.70.99.9', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-14 09:44:03');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (212, 'admin', '164.70.99.9', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-14 10:44:21');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (213, 'admin', '164.70.99.9', 'XX XX', 'Chrome 12', 'Windows 10', '1', '验证码错误', '2024-01-14 11:29:53');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (214, 'admin', '164.70.99.9', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-14 11:29:57');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (215, 'admin', '164.70.99.9', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-14 12:56:01');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (216, 'admin', '164.70.99.9', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-14 15:16:11');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (217, 'admin', '164.70.99.9', 'XX XX', 'Chrome 12', 'Windows 10', '0', '退出成功', '2024-01-14 15:38:12');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (218, 'admin', '164.70.99.9', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-14 15:38:19');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (219, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-14 19:16:31');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (220, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-15 05:17:59');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (221, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-15 05:52:58');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (222, 'admin', '203.198.87.154', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-15 08:16:19');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (223, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-15 08:22:32');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (224, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-15 09:06:49');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (225, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-15 11:18:18');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (226, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-15 12:10:09');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (227, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-15 12:43:59');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (228, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-15 12:56:30');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (229, '3096643082', '114.99.205.185', 'XX XX', 'Chrome Mobile', 'Android 1.x', '1', '用户不存在/密码错误',
        '2024-01-15 13:31:45');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (230, '3096643082', '114.99.205.185', 'XX XX', 'Chrome Mobile', 'Android 1.x', '0', '登录成功',
        '2024-01-15 13:31:59');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (231, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-15 13:38:11');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (232, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-15 15:27:06');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (233, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-15 15:54:13');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (234, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-15 18:25:03');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (235, '3096643082', '114.99.205.185', 'XX XX', 'Chrome Mobile', 'Android 1.x', '0', '登录成功',
        '2024-01-16 03:27:52');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (236, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-16 04:25:08');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (237, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Mac OS X', '1', '用户不存在/密码错误',
        '2024-01-16 04:55:09');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (238, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Mac OS X', '1', '用户不存在/密码错误',
        '2024-01-16 04:55:18');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (239, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Mac OS X', '0', '登录成功', '2024-01-16 04:55:26');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (240, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '1', '验证码错误', '2024-01-16 10:57:49');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (241, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-16 10:57:53');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (242, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-16 11:50:26');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (243, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-16 13:44:40');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (244, 'admin', '203.198.87.154', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-16 15:38:19');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (245, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-16 16:53:44');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (246, 'admin', '221.197.234.5', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-17 05:54:30');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (247, 'admin', '203.198.87.154', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-17 07:23:41');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (248, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-17 10:50:34');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (249, 'admin', '210.3.147.13', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-17 15:36:30');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (250, 'admin', '60.24.254.32', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-17 17:31:23');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (251, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-18 07:41:18');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (252, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-18 11:31:24');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (253, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-18 12:34:34');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (254, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-18 15:48:11');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (255, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-18 16:29:14');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (256, 'admin', '203.198.94.197', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-19 03:55:51');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (257, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-19 06:37:13');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (258, '3096643082', '36.62.166.152', 'XX XX', 'Chrome Mobile', 'Android 1.x', '0', '登录成功',
        '2024-01-19 10:01:34');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (259, 'admin', '60.24.254.32', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-19 10:34:54');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (260, 'admin', '60.24.254.32', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-19 11:44:29');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (261, '3096643082', '36.62.166.152', 'XX XX', 'Chrome Mobile', 'Android 1.x', '0', '登录成功',
        '2024-01-19 16:30:34');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (262, 'admin', '203.198.87.154', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '1', '用户不存在/密码错误',
        '2024-01-20 06:01:06');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (263, 'admin', '203.198.87.154', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '1', '用户不存在/密码错误',
        '2024-01-20 06:01:11');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (264, 'admin', '203.198.87.154', 'XX XX', 'Mobile Safari', 'Mac OS X (iPhone)', '0', '登录成功',
        '2024-01-20 06:01:21');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (265, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-20 07:23:29');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (266, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-20 09:14:43');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (267, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-20 16:59:45');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (268, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-20 17:38:33');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (269, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-20 18:38:47');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (270, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-21 07:33:34');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (271, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-21 14:43:46');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (272, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-22 15:41:18');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (273, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-25 09:08:27');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (274, 'admin', '203.198.87.154', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-25 15:42:15');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (275, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-26 07:57:19');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (276, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-26 09:14:13');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (277, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-26 10:34:44');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (278, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-26 12:08:39');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (279, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-27 06:14:16');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (280, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-27 07:23:51');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (281, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-27 08:08:09');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (282, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-27 09:07:27');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (283, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-27 12:13:49');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (284, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-27 13:42:06');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (285, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-27 15:39:18');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (286, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-28 07:19:50');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (287, 'admin', '203.198.94.197', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-28 12:36:29');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (288, 'admin', '210.3.147.13', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-28 14:43:30');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (289, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-29 17:13:58');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (290, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-30 08:01:58');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (291, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-30 10:57:38');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (292, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-30 13:13:23');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (293, 'admin', '1.65.168.11', 'XX XX', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-01-30 16:34:36');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (294, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '1', '验证码已失效', '2024-03-10 15:45:13');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (295, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '1', '用户不存在/密码错误',
        '2024-03-10 15:45:17');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (296, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-10 15:45:24');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (297, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-10 17:15:06');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (298, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '1', '用户不存在/密码错误',
        '2024-03-10 19:36:55');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (299, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '1', '验证码错误', '2024-03-10 19:37:02');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (300, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-10 19:37:04');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (301, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '1', '用户不存在/密码错误',
        '2024-03-10 23:09:08');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (302, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-10 23:09:15');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (303, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '1', '用户不存在/密码错误',
        '2024-03-11 14:10:14');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (304, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-11 14:10:19');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (305, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '1', '用户不存在/密码错误',
        '2024-03-11 15:24:02');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (306, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '1', '用户不存在/密码错误',
        '2024-03-11 15:24:06');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (307, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-11 15:24:10');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (308, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '1', '用户不存在/密码错误',
        '2024-03-11 16:41:08');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (309, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-11 16:41:15');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (310, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '1', '用户不存在/密码错误',
        '2024-03-11 18:56:03');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (311, 'admin', '127.0.0.1', '内网IP', 'Chrome 12', 'Windows 10', '0', '登录成功', '2024-03-11 18:56:08');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (312, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码错误', '2024-11-15 21:20:49');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (313, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2024-11-15 21:20:53');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (314, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '1', '验证码已失效', '2024-11-15 21:20:53');
INSERT INTO ruoyi.sys_logininfor (info_id, user_name, ipaddr, login_location, browser, os, status, msg, login_time)
VALUES (315, 'admin', '127.0.0.1', '内网IP', 'Chrome 13', 'Windows 10', '0', '登录成功', '2024-12-20 19:11:15');
