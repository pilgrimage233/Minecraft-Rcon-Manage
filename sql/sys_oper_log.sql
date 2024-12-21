create table sys_oper_log
(
    oper_id        bigint auto_increment comment '日志主键'
        primary key,
    title          varchar(50)   default '' null comment '模块标题',
    business_type int    default 0 null comment '业务类型（0其它 1新增 2修改 3删除）',
    method         varchar(100)  default '' null comment '方法名称',
    request_method varchar(10)   default '' null comment '请求方式',
    operator_type int    default 0 null comment '操作类别（0其它 1后台用户 2手机端用户）',
    oper_name      varchar(50)   default '' null comment '操作人员',
    dept_name      varchar(50)   default '' null comment '部门名称',
    oper_url       varchar(255)  default '' null comment '请求URL',
    oper_ip        varchar(128)  default '' null comment '主机地址',
    oper_location  varchar(255)  default '' null comment '操作地点',
    oper_param     varchar(2000) default '' null comment '请求参数',
    json_result    varchar(2000) default '' null comment '返回参数',
    status        int    default 0 null comment '操作状态（0正常 1异常）',
    error_msg      varchar(2000) default '' null comment '错误消息',
    oper_time     datetime         null comment '操作时间',
    cost_time     bigint default 0 null comment '消耗时间'
)
    comment '操作日志记录';

create index idx_sys_oper_log_bt
    on sys_oper_log (business_type);

create index idx_sys_oper_log_ot
    on sys_oper_log (oper_time);

create index idx_sys_oper_log_s
    on sys_oper_log (status);

INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (100, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin',
        '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{"tables":"whitelist_info"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:25:13', 185);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (101, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"createBy":"admin","icon":"online","isCache":"0","isFrame":"1","menuName":"白名单管理","menuType":"M","orderNum":1,"params":{},"parentId":0,"path":"/Whitelist","status":"0","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:28:37', 23);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (102, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"createTime":"2023-12-26 21:28:37","icon":"online","isCache":"0","isFrame":"1","menuId":2000,"menuName":"服务器管理","menuType":"M","orderNum":1,"params":{},"parentId":0,"path":"/server","perms":"","status":"0","updateBy":"admin","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:29:26', 16);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (103, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"createTime":"2023-12-26 21:28:37","icon":"server","isCache":"0","isFrame":"1","menuId":2000,"menuName":"服务器管理","menuType":"M","orderNum":1,"params":{},"parentId":0,"path":"/server","perms":"","status":"0","updateBy":"admin","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:29:39', 20);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (104, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"createBy":"admin","icon":"peoples","isCache":"0","isFrame":"1","menuName":"白名单管理","menuType":"M","orderNum":1,"params":{},"parentId":2000,"path":"/Whitelist","status":"0","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:30:29', 14);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (105, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门',
        '/tool/gen', '127.0.0.1', '内网IP',
        '{"businessName":"Whitelist","className":"WhitelistInfo","columns":[{"capJavaField":"Id","columnComment":"主键ID","columnId":1,"columnName":"id","columnType":"int(11)","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":false,"htmlType":"input","increment":true,"insert":true,"isIncrement":"1","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"Long","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":1,"updateBy":"","usableColumn":false},{"capJavaField":"Time","columnComment":"申请时间","columnId":2,"columnName":"time","columnType":"datetime","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":true,"htmlType":"datetime","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"time","javaType":"Date","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":1,"updateBy":"","usableColumn":false},{"capJavaField":"UserName","columnComment":"游戏名称","columnId":3,"columnName":"user_name","columnType":"varchar(256)","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"userName","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":3,"superColumn":false,"tableId":1,"updateBy":"","usableColumn":false},{"capJavaField":"UserUuid","columnId":4,"columnName":"user_uuid","columnType":"varchar(256)","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"userUuid","javaType":"St',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:31:46', 114);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (106, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin',
        '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"whitelist_info"}', null, 0, null,
        '2023-12-26 21:31:52', 196);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (107, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/103', '127.0.0.1', '内网IP', '{}', '{"msg":"存在子菜单,不允许删除","code":601}', 0,
        null, '2023-12-26 21:37:04', 9);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (108, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/104', '127.0.0.1', '内网IP', '{}', '{"msg":"存在子菜单,不允许删除","code":601}', 0,
        null, '2023-12-26 21:37:07', 11);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (109, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/104', '127.0.0.1', '内网IP', '{}', '{"msg":"存在子菜单,不允许删除","code":601}', 0,
        null, '2023-12-26 21:37:20', 5);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (110, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"component":"system/dept/index","createTime":"2023-12-26 16:54:02","icon":"tree","isCache":"0","isFrame":"1","menuId":103,"menuName":"部门管理","menuType":"C","orderNum":4,"params":{},"parentId":1,"path":"dept","perms":"system:dept:list","query":"","status":"0","updateBy":"admin","visible":"1"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:37:32', 23);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (111, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"component":"system/post/index","createTime":"2023-12-26 16:54:02","icon":"post","isCache":"0","isFrame":"1","menuId":104,"menuName":"岗位管理","menuType":"C","orderNum":5,"params":{},"parentId":1,"path":"post","perms":"system:post:list","query":"","status":"0","updateBy":"admin","visible":"1"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:37:37', 12);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (112, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/4', '127.0.0.1', '内网IP', '{}', '{"msg":"菜单已分配,不允许删除","code":601}', 0,
        null, '2023-12-26 21:37:58', 14);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (113, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"createTime":"2023-12-26 16:54:02","icon":"guide","isCache":"0","isFrame":"0","menuId":4,"menuName":"若依官网","menuType":"M","orderNum":4,"params":{},"parentId":0,"path":"http://ruoyi.vip","perms":"","query":"","status":"1","updateBy":"admin","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:38:03', 11);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (114, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/4', '127.0.0.1', '内网IP', '{}', '{"msg":"菜单已分配,不允许删除","code":601}', 0,
        null, '2023-12-26 21:38:05', 8);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (115, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/4', '127.0.0.1', '内网IP', '{}', '{"msg":"菜单已分配,不允许删除","code":601}', 0,
        null, '2023-12-26 21:38:14', 15);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (116, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"createTime":"2023-12-26 16:54:02","icon":"guide","isCache":"0","isFrame":"0","menuId":4,"menuName":"若依官网","menuType":"M","orderNum":4,"params":{},"parentId":0,"path":"http://ruoyi.vip","perms":"","query":"","status":"1","updateBy":"admin","visible":"1"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:38:21', 12);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (117, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/4', '127.0.0.1', '内网IP', '{}', '{"msg":"菜单已分配,不允许删除","code":601}', 0,
        null, '2023-12-26 21:38:23', 8);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (118, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/4', '127.0.0.1', '内网IP', '{}', '{"msg":"菜单已分配,不允许删除","code":601}', 0,
        null, '2023-12-26 21:38:28', 8);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (119, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"createTime":"2023-12-26 21:30:29","icon":"peoples","isCache":"0","isFrame":"1","menuId":2001,"menuName":"白名单管理","menuType":"C","orderNum":1,"params":{},"parentId":2000,"path":"/Whitelist","perms":"","status":"0","updateBy":"admin","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:43:00', 13);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (120, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"component":"mc/Whitelist/index","createTime":"2023-12-26 21:30:29","icon":"peoples","isCache":"0","isFrame":"1","menuId":2001,"menuName":"白名单管理","menuType":"C","orderNum":1,"params":{},"parentId":2000,"path":"/Whitelist","perms":"mc:Whitelist:list","status":"0","updateBy":"admin","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:43:58', 15);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (121, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/2001', '127.0.0.1', '内网IP', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-26 21:45:31', 27);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (122, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门',
        '/tool/gen', '127.0.0.1', '内网IP',
        '{"businessName":"Whitelist","className":"WhitelistInfo","columns":[{"capJavaField":"Id","columnComment":"主键ID","columnId":1,"columnName":"id","columnType":"int(11)","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":false,"htmlType":"input","increment":true,"insert":true,"isIncrement":"1","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"Long","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":1,"updateBy":"","updateTime":"2023-12-26 21:31:46","usableColumn":false},{"capJavaField":"Time","columnComment":"申请时间","columnId":2,"columnName":"time","columnType":"datetime","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":true,"htmlType":"datetime","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"time","javaType":"Date","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":1,"updateBy":"","updateTime":"2023-12-26 21:31:46","usableColumn":false},{"capJavaField":"UserName","columnComment":"游戏名称","columnId":3,"columnName":"user_name","columnType":"varchar(256)","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"userName","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":3,"superColumn":false,"tableId":1,"updateBy":"","updateTime":"2023-12-26 21:31:46","usableColumn":false},{"capJavaField":"UserUuid","columnId":4,"columnName":"user_uuid","columnType":"varchar(256)","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","i',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:45:44', 108);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (123, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门',
        '/tool/gen', '127.0.0.1', '内网IP',
        '{"businessName":"Whitelist","className":"WhitelistInfo","columns":[{"capJavaField":"Id","columnComment":"主键ID","columnId":1,"columnName":"id","columnType":"int(11)","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":false,"htmlType":"input","increment":true,"insert":true,"isIncrement":"1","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"Long","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":1,"updateBy":"","updateTime":"2023-12-26 21:45:43","usableColumn":false},{"capJavaField":"Time","columnComment":"申请时间","columnId":2,"columnName":"time","columnType":"datetime","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":true,"htmlType":"datetime","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"time","javaType":"Date","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":1,"updateBy":"","updateTime":"2023-12-26 21:45:43","usableColumn":false},{"capJavaField":"UserName","columnComment":"游戏名称","columnId":3,"columnName":"user_name","columnType":"varchar(256)","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"userName","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":3,"superColumn":false,"tableId":1,"updateBy":"","updateTime":"2023-12-26 21:45:43","usableColumn":false},{"capJavaField":"UserUuid","columnId":4,"columnName":"user_uuid","columnType":"varchar(256)","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","i',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:45:59', 100);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (124, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin',
        '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"whitelist_info"}', null, 0, null,
        '2023-12-26 21:46:01', 98);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (125, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"createBy":"admin","icon":"peoples","isCache":"0","isFrame":"1","menuName":"白名单管理","menuType":"M","orderNum":1,"params":{},"parentId":2000,"path":"mc/Whitelist","status":"0","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:48:59', 18);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (126, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"component":"mc/Whitelist/index","createTime":"2023-12-26 21:48:59","icon":"peoples","isCache":"0","isFrame":"1","menuId":2002,"menuName":"白名单管理","menuType":"C","orderNum":1,"params":{},"parentId":2000,"path":"whitelist","perms":"mc:Whitelist:list","status":"0","updateBy":"admin","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:51:33', 19);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (127, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/2002', '127.0.0.1', '内网IP', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-26 21:52:28', 21);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (128, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门',
        '/tool/gen', '127.0.0.1', '内网IP',
        '{"businessName":"whitelist","className":"WhitelistInfo","columns":[{"capJavaField":"Id","columnComment":"主键ID","columnId":1,"columnName":"id","columnType":"int(11)","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":false,"htmlType":"input","increment":true,"insert":true,"isIncrement":"1","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"Long","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":1,"updateBy":"","updateTime":"2023-12-26 21:45:59","usableColumn":false},{"capJavaField":"Time","columnComment":"申请时间","columnId":2,"columnName":"time","columnType":"datetime","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":true,"htmlType":"datetime","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"time","javaType":"Date","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":1,"updateBy":"","updateTime":"2023-12-26 21:45:59","usableColumn":false},{"capJavaField":"UserName","columnComment":"游戏名称","columnId":3,"columnName":"user_name","columnType":"varchar(256)","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"userName","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":3,"superColumn":false,"tableId":1,"updateBy":"","updateTime":"2023-12-26 21:45:59","usableColumn":false},{"capJavaField":"UserUuid","columnId":4,"columnName":"user_uuid","columnType":"varchar(256)","createBy":"admin","createTime":"2023-12-26 21:25:13","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","i',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:53:08', 90);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (129, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin',
        '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"whitelist_info"}', null, 0, null,
        '2023-12-26 21:53:18', 108);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (130, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"createTime":"2023-12-26 21:28:37","icon":"server","isCache":"0","isFrame":"1","menuId":2000,"menuName":"服务器管理","menuType":"M","orderNum":1,"params":{},"parentId":0,"path":"server","perms":"","status":"0","updateBy":"admin","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:56:06', 19);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (131, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"createTime":"2023-12-26 21:28:37","icon":"server","isCache":"0","isFrame":"1","menuId":2000,"menuName":"服务器管理","menuType":"M","orderNum":1,"params":{},"parentId":0,"path":"mc","perms":"","status":"0","updateBy":"admin","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:56:23', 13);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (132, '代码生成', 3, 'com.ruoyi.generator.controller.GenController.remove()', 'DELETE', 1, 'admin', '研发部门',
        '/tool/gen/1', '127.0.0.1', '内网IP', '{}', '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:56:54',
        18);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (133, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin',
        '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{"tables":"whitelist_info"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:56:57', 110);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (134, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门',
        '/tool/gen', '127.0.0.1', '内网IP',
        '{"businessName":"whitelist","className":"WhitelistInfo","columns":[{"capJavaField":"Id","columnComment":"主键ID","columnId":14,"columnName":"id","columnType":"int(11)","createBy":"admin","createTime":"2023-12-26 21:56:56","dictType":"","edit":false,"htmlType":"input","increment":true,"insert":true,"isIncrement":"1","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"Long","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":2,"updateBy":"","usableColumn":false},{"capJavaField":"Time","columnComment":"申请时间","columnId":15,"columnName":"time","columnType":"datetime","createBy":"admin","createTime":"2023-12-26 21:56:56","dictType":"","edit":true,"htmlType":"datetime","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"time","javaType":"Date","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":2,"updateBy":"","usableColumn":false},{"capJavaField":"UserName","columnComment":"游戏名称","columnId":16,"columnName":"user_name","columnType":"varchar(256)","createBy":"admin","createTime":"2023-12-26 21:56:56","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"userName","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":3,"superColumn":false,"tableId":2,"updateBy":"","usableColumn":false},{"capJavaField":"UserUuid","columnId":17,"columnName":"user_uuid","columnType":"varchar(256)","createBy":"admin","createTime":"2023-12-26 21:56:57","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"userUuid","javaType"',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 21:57:57', 79);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (135, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin',
        '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"whitelist_info"}', null, 0, null,
        '2023-12-26 21:57:59', 96);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (136, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门',
        '/tool/gen', '127.0.0.1', '内网IP',
        '{"businessName":"whitelist","className":"WhitelistInfo","columns":[{"capJavaField":"Id","columnComment":"主键ID","columnId":14,"columnName":"id","columnType":"int(11)","createBy":"admin","createTime":"2023-12-26 21:56:56","dictType":"","edit":false,"htmlType":"input","increment":true,"insert":true,"isIncrement":"1","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"Long","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":2,"updateBy":"","updateTime":"2023-12-26 21:57:57","usableColumn":false},{"capJavaField":"Time","columnComment":"申请时间","columnId":15,"columnName":"time","columnType":"datetime","createBy":"admin","createTime":"2023-12-26 21:56:56","dictType":"","edit":true,"htmlType":"datetime","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"time","javaType":"Date","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":2,"updateBy":"","updateTime":"2023-12-26 21:57:57","usableColumn":false},{"capJavaField":"UserName","columnComment":"游戏名称","columnId":16,"columnName":"user_name","columnType":"varchar(256)","createBy":"admin","createTime":"2023-12-26 21:56:56","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"userName","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":3,"superColumn":false,"tableId":2,"updateBy":"","updateTime":"2023-12-26 21:57:57","usableColumn":false},{"capJavaField":"UserUuid","columnId":17,"columnName":"user_uuid","columnType":"varchar(256)","createBy":"admin","createTime":"2023-12-26 21:56:57","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 22:00:11', 93);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (137, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin',
        '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"whitelist_info"}', null, 0, null,
        '2023-12-26 22:00:13', 258);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (138, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/2009', '127.0.0.1', '内网IP', '{}', '{"msg":"存在子菜单,不允许删除","code":601}', 0,
        null, '2023-12-26 22:03:28', 20);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (139, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"component":"mc/whitelist/index","createTime":"2023-12-26 22:02:01","icon":"#","isCache":"1","isFrame":"1","menuId":2009,"menuName":"白名单","menuType":"C","orderNum":1,"params":{},"parentId":2000,"path":"whitelist","perms":"mc:whitelist:list","status":"1","visible":"1"}',
        '{"msg":"修改菜单\'白名单\'失败，菜单名称已存在","code":500}', 0, null, '2023-12-26 22:04:08', 18);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (140, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"component":"mc/whitelist/index","createTime":"2023-12-26 22:02:01","icon":"#","isCache":"1","isFrame":"1","menuId":2009,"menuName":"白名单","menuType":"C","orderNum":1,"params":{},"parentId":2000,"path":"whitelist","perms":"mc:whitelist:list","status":"1","visible":"1"}',
        '{"msg":"修改菜单\'白名单\'失败，菜单名称已存在","code":500}', 0, null, '2023-12-26 22:04:09', 16);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (141, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"component":"mc/whitelist/index","createTime":"2023-12-26 22:02:01","icon":"#","isCache":"0","isFrame":"1","menuId":2009,"menuName":"白名单","menuType":"C","orderNum":1,"params":{},"parentId":2000,"path":"whitelist","perms":"mc:whitelist:list","status":"1","visible":"0"}',
        '{"msg":"修改菜单\'白名单\'失败，菜单名称已存在","code":500}', 0, null, '2023-12-26 22:04:14', 5);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (142, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/2004', '127.0.0.1', '内网IP', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-26 22:04:24', 25);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (143, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/2005', '127.0.0.1', '内网IP', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-26 22:04:28', 25);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (144, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/2006', '127.0.0.1', '内网IP', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-26 22:04:30', 18);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (145, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/2007', '127.0.0.1', '内网IP', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-26 22:04:31', 16);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (146, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/2008', '127.0.0.1', '内网IP', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-26 22:04:34', 153);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (147, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/menu/2003', '127.0.0.1', '内网IP', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-26 22:04:35', 16);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (148, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"component":"mc/whitelist/index","createTime":"2023-12-26 22:02:01","icon":"peoples","isCache":"0","isFrame":"1","menuId":2009,"menuName":"白名单管理","menuType":"C","orderNum":1,"params":{},"parentId":2000,"path":"whitelist","perms":"mc:whitelist:list","status":"0","updateBy":"admin","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-26 22:56:52', 37);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (149, '字典类型', 1, 'com.ruoyi.web.controller.system.SysDictTypeController.add()', 'POST', 1, 'admin',
        '研发部门', '/system/dict/type', '127.0.0.1', '内网IP',
        '{"createBy":"admin","dictName":"审核状态","dictType":"white_examine_status","params":{},"status":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:07:01', 29);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (150, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","default":false,"dictLabel":"待审核","dictSort":0,"dictType":"white_examine_status","dictValue":"0","listClass":"warning","params":{},"status":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:07:29', 31);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (151, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","default":false,"dictLabel":"已过审","dictSort":0,"dictType":"white_examine_status","dictValue":"1","listClass":"success","params":{},"status":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:07:47', 27);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (152, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","default":false,"dictLabel":"2","dictSort":0,"dictType":"white_examine_status","dictValue":"已拒审","listClass":"danger","params":{},"status":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:08:15', 27);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (153, '字典数据', 2, 'com.ruoyi.web.controller.system.SysDictDataController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2023-12-27 18:08:15","default":false,"dictCode":102,"dictLabel":"已拒审","dictSort":0,"dictType":"white_examine_status","dictValue":"2","isDefault":"N","listClass":"danger","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:08:24', 19);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (154, '字典数据', 2, 'com.ruoyi.web.controller.system.SysDictDataController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2023-12-27 18:08:15","default":false,"dictCode":102,"dictLabel":"已拒审","dictSort":1,"dictType":"white_examine_status","dictValue":"2","isDefault":"N","listClass":"danger","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:24:14', 39);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (155, '字典数据', 2, 'com.ruoyi.web.controller.system.SysDictDataController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2023-12-27 18:07:47","default":false,"dictCode":101,"dictLabel":"已过审","dictSort":2,"dictType":"white_examine_status","dictValue":"1","isDefault":"N","listClass":"success","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:24:19', 16);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (156, '字典数据', 2, 'com.ruoyi.web.controller.system.SysDictDataController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2023-12-27 18:08:15","default":false,"dictCode":102,"dictLabel":"已拒审","dictSort":2,"dictType":"white_examine_status","dictValue":"2","isDefault":"N","listClass":"danger","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:24:28', 31);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (157, '字典数据', 2, 'com.ruoyi.web.controller.system.SysDictDataController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2023-12-27 18:07:47","default":false,"dictCode":101,"dictLabel":"已过审","dictSort":1,"dictType":"white_examine_status","dictValue":"1","isDefault":"N","listClass":"success","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:24:36', 31);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (158, '字典数据', 2, 'com.ruoyi.web.controller.system.SysDictDataController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2023-12-27 18:07:29","default":false,"dictCode":100,"dictLabel":"待审核","dictSort":1,"dictType":"white_examine_status","dictValue":"0","isDefault":"N","listClass":"warning","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:27:23', 34);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (159, '字典数据', 2, 'com.ruoyi.web.controller.system.SysDictDataController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2023-12-27 18:07:47","default":false,"dictCode":101,"dictLabel":"已过审","dictSort":2,"dictType":"white_examine_status","dictValue":"1","isDefault":"N","listClass":"success","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:27:26', 120);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (160, '字典数据', 2, 'com.ruoyi.web.controller.system.SysDictDataController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2023-12-27 18:08:15","default":false,"dictCode":102,"dictLabel":"已拒审","dictSort":3,"dictType":"white_examine_status","dictValue":"2","isDefault":"N","listClass":"danger","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:27:30', 23);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (161, '字典类型', 9, 'com.ruoyi.web.controller.system.SysDictTypeController.refreshCache()', 'DELETE', 1,
        'admin', '研发部门', '/system/dict/type/refreshCache', '127.0.0.1', '内网IP', '{}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:28:38', 28);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (162, '字典类型', 1, 'com.ruoyi.web.controller.system.SysDictTypeController.add()', 'POST', 1, 'admin',
        '研发部门', '/system/dict/type', '127.0.0.1', '内网IP',
        '{"createBy":"admin","dictName":"添加状态","dictType":"whitelist_add_status","params":{},"status":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:38:34', 22);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (163, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","default":false,"dictLabel":"未添加","dictSort":0,"dictType":"whitelist_add_status","dictValue":"0","listClass":"info","params":{},"status":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:40:35', 18);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (164, '字典数据', 2, 'com.ruoyi.web.controller.system.SysDictDataController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2023-12-27 18:40:35","default":false,"dictCode":103,"dictLabel":"未添加","dictSort":1,"dictType":"whitelist_add_status","dictValue":"0","isDefault":"N","listClass":"info","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:40:43', 13);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (165, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","default":false,"dictLabel":"已添加","dictSort":2,"dictType":"whitelist_add_status","dictValue":"1","listClass":"success","params":{},"status":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:41:05', 19);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (166, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","default":false,"dictLabel":"已删除","dictSort":2,"dictType":"whitelist_add_status","dictValue":"2","listClass":"warning","params":{},"status":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:41:20', 22);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (167, '字典数据', 2, 'com.ruoyi.web.controller.system.SysDictDataController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2023-12-27 18:41:20","default":false,"dictCode":105,"dictLabel":"已删除","dictSort":3,"dictType":"whitelist_add_status","dictValue":"2","isDefault":"N","listClass":"warning","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:41:44', 14);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (168, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","default":false,"dictLabel":"已封禁","dictSort":4,"dictType":"whitelist_add_status","dictValue":"9","listClass":"danger","params":{},"status":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:42:07', 13);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (169, '字典类型', 9, 'com.ruoyi.web.controller.system.SysDictTypeController.refreshCache()', 'DELETE', 1,
        'admin', '研发部门', '/system/dict/type/refreshCache', '127.0.0.1', '内网IP', '{}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:42:12', 19);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (170, '字典类型', 2, 'com.ruoyi.web.controller.system.SysDictTypeController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/type', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2023-12-27 18:38:34","dictId":101,"dictName":"添加状态","dictType":"white_add_status","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:43:28', 54);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (171, '字典类型', 9, 'com.ruoyi.web.controller.system.SysDictTypeController.refreshCache()', 'DELETE', 1,
        'admin', '研发部门', '/system/dict/type/refreshCache', '127.0.0.1', '内网IP', '{}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:43:29', 12);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (172, '字典类型', 9, 'com.ruoyi.web.controller.system.SysDictTypeController.refreshCache()', 'DELETE', 1,
        'admin', '研发部门', '/system/dict/type/refreshCache', '127.0.0.1', '内网IP', '{}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:43:45', 18);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (173, '字典类型', 9, 'com.ruoyi.web.controller.system.SysDictTypeController.refreshCache()', 'DELETE', 1,
        'admin', '研发部门', '/system/dict/type/refreshCache', '127.0.0.1', '内网IP', '{}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 18:43:58', 19);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (174, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"false","addTime":"2023-12-27","id":100,"onlineFlag":1,"params":{},"qqNum":"184232","remark":"1","status":"0","userName":"kissthefire","userUuid":"f377675f-d4e4-495b-a004-2d14d3e24955"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 19:27:34', 35);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (175, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"false","addTime":"2023-12-27","id":100,"onlineFlag":1,"params":{},"qqNum":"184231","remark":"1","status":"0","userName":"kissthefire","userUuid":"f377675f-d4e4-495b-a004-2d14d3e24955"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 19:27:38', 8);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (176, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"false","addTime":"2023-12-27","id":100,"onlineFlag":1,"params":{},"qqNum":"184231","remark":"1","status":"1","userName":"kissthefire","userUuid":"f377675f-d4e4-495b-a004-2d14d3e24955"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 19:29:59', 10);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (177, '字典数据', 2, 'com.ruoyi.web.controller.system.SysDictDataController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2023-12-27 18:42:07","default":false,"dictCode":106,"dictLabel":"已移除","dictSort":4,"dictType":"white_add_status","dictValue":"9","isDefault":"N","listClass":"danger","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 19:32:04', 25);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (178, '字典数据', 2, 'com.ruoyi.web.controller.system.SysDictDataController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2023-12-27 18:42:07","default":false,"dictCode":106,"dictLabel":"已封禁","dictSort":4,"dictType":"white_add_status","dictValue":"9","isDefault":"N","listClass":"danger","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 19:32:11', 20);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (179, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"2","addTime":"2023-12-27","id":100,"onlineFlag":1,"params":{},"qqNum":"184231","remark":"1","removeReason":"test","removeTime":"2023-12-27","reviewUsers":"admin","status":"1","userName":"kissthefire","userUuid":"f377675f-d4e4-495b-a004-2d14d3e24955"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 19:45:08', 24);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (180, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/100', '127.0.0.1', '内网IP', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-27 19:45:17', 14);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (181, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"0","id":101,"onlineFlag":1,"params":{},"qqNum":"2873336923","remark":"111","removeTime":"2023-12-27","reviewUsers":"admin","status":"1","time":"2023-12-27","userName":"kissthefire","userUuid":"f377675f-d4e4-495b-a004-2d14d3e24955"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 19:50:03', 186);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (182, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/101', '127.0.0.1', '内网IP', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-27 19:51:07', 10);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (183, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.resetPwd()', 'PUT', 1, 'admin',
        '研发部门', '/system/user/resetPwd', '154.21.193.236', 'XX XX',
        '{"admin":false,"params":{},"updateBy":"admin","userId":2}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-27 14:51:28', 141);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (184, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/1', '154.21.193.236', ' ', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-27 15:04:16', 64);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (185, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '154.21.193.236', 'XX XX',
        '{"addState":"0","id":3,"onlineFlag":0,"params":{},"qqNum":"666666","remark":"","reviewUsers":"admin","status":"1","time":"2023-12-27","userName":"test","userUuid":"a65bcd64-cf11-4bd1-b10e-37ab70a80c4b"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 15:14:14', 216);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (186, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '154.21.193.236', 'XX XX',
        '{"addState":"2","id":3,"onlineFlag":0,"params":{},"qqNum":"666666","remark":"","removeTime":"2023-12-27","reviewUsers":"admin","status":"1","time":"2023-12-27","userName":"test","userUuid":"a65bcd64-cf11-4bd1-b10e-37ab70a80c4b"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 15:14:42', 218);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (187, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/3', '154.21.193.236', ' ', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-27 15:23:21', 61);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (188, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/2', '154.21.193.236', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-27 15:23:22', 22);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (189, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '154.21.193.236', ' ',
        '{"addState":"1","id":5,"onlineFlag":0,"params":{},"qqNum":"666666","remark":"1","reviewUsers":"admin","status":"1","time":"2023-12-27","userName":"test","userUuid":"623b6d2d-e4fa-4cad-9d77-c22376485ae1"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 15:23:48', 249);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (190, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '154.21.193.236', 'XX XX',
        '{"addState":"1","id":5,"onlineFlag":0,"params":{},"qqNum":"666666","remark":"1","removeTime":"2023-12-27","reviewUsers":"admin","status":"1","time":"2023-12-27","userName":"test","userUuid":"623b6d2d-e4fa-4cad-9d77-c22376485ae1"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 15:24:14', 433);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (191, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '154.21.193.236', 'XX XX',
        '{"addState":"1","id":5,"onlineFlag":0,"params":{},"qqNum":"666666","remark":"1","removeTime":"2023-12-27","reviewUsers":"admin","status":"1","time":"2023-12-27","userName":"test","userUuid":"623b6d2d-e4fa-4cad-9d77-c22376485ae1"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 15:27:32', 330);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (192, '字典类型', 1, 'com.ruoyi.web.controller.system.SysDictTypeController.add()', 'POST', 1, 'admin',
        '研发部门', '/system/dict/type', '154.21.193.236', 'XX XX',
        '{"createBy":"admin","dictName":"正版标识","dictType":"online_status","params":{},"status":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 15:28:20', 50);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (193, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin',
        '研发部门', '/system/dict/data', '154.21.193.236', ' ',
        '{"createBy":"admin","default":false,"dictLabel":"1","dictSort":0,"dictType":"online_status","dictValue":"正版","listClass":"primary","params":{},"status":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 15:28:44', 56);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (194, '字典数据', 2, 'com.ruoyi.web.controller.system.SysDictDataController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/data', '154.21.193.236', 'XX XX',
        '{"createBy":"admin","createTime":"2023-12-27 07:28:43","default":false,"dictCode":107,"dictLabel":"正版","dictSort":1,"dictType":"online_status","dictValue":"1","isDefault":"N","listClass":"primary","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 15:29:02', 50);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (195, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin',
        '研发部门', '/system/dict/data', '154.21.193.236', ' ',
        '{"createBy":"admin","default":false,"dictLabel":"离线","dictSort":2,"dictType":"online_status","dictValue":"0","listClass":"info","params":{},"status":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 15:29:15', 34);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (196, '字典类型', 9, 'com.ruoyi.web.controller.system.SysDictTypeController.refreshCache()', 'DELETE', 1,
        'admin', '研发部门', '/system/dict/type/refreshCache', '154.21.193.236', 'XX XX', '{}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 15:29:24', 68);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (197, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '154.21.193.236', 'XX XX',
        '{"addState":"1","addTime":"2023-12-27","id":5,"onlineFlag":0,"params":{},"qqNum":"666666","remark":"1","removeTime":"2023-12-27","reviewUsers":"admin","status":"1","time":"2023-12-27","userName":"test","userUuid":"623b6d2d-e4fa-4cad-9d77-c22376485ae1"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 15:35:34', 390);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (198, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/5', '154.21.193.236', ' ', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-27 15:35:51', 43);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (199, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/4', '154.21.193.236', ' ', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-27 15:36:16', 26);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (200, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '154.21.193.236', ' ',
        '{"addState":"1","addTime":"2023-12-27","id":6,"onlineFlag":0,"params":{},"qqNum":"11111","remark":"1","reviewUsers":"admin","status":"1","time":"2023-12-27","userName":"test","userUuid":"a5642281-ed3f-4854-977e-ec9f9c19e1be"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 15:36:35', 234);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (201, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/6', '154.21.193.236', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-27 15:56:13', 57);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (202, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '154.21.193.236', 'XX XX',
        '{"addState":"1","addTime":"2023-12-27","id":7,"onlineFlag":0,"params":{},"qqNum":"44444","remark":"1","reviewUsers":"admin","status":"1","time":"2023-12-27","userName":"test","userUuid":"bd98a0ca-390a-4a75-aa98-d7ce53906cec"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 15:56:42', 257);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (203, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '154.21.193.236', 'XX XX',
        '{"addState":"2","addTime":"2023-12-27","id":7,"onlineFlag":0,"params":{},"qqNum":"44444","remark":"1","removeTime":"2023-12-27","reviewUsers":"admin","status":"0","time":"2023-12-27","userName":"test","userUuid":"bd98a0ca-390a-4a75-aa98-d7ce53906cec"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 15:56:55', 133);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (204, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/7', '154.21.193.236', ' ', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-27 15:59:07', 35);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (205, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '154.21.193.236', ' ',
        '{"addState":"1","addTime":"2023-12-27","id":8,"onlineFlag":0,"params":{},"qqNum":"11111","remark":"","reviewUsers":"admin","status":"1","time":"2023-12-27","userName":"test","userUuid":"7e5fcdf3-dd2b-4a62-9bcd-e7f00dc54a7f"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 15:59:47', 233);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (206, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '154.21.193.236', 'XX XX',
        '{"addState":"2","addTime":"2023-12-27","id":8,"onlineFlag":0,"params":{},"qqNum":"11111","remark":"","removeTime":"2023-12-27","reviewUsers":"admin","status":"0","time":"2023-12-27","userName":"test","userUuid":"7e5fcdf3-dd2b-4a62-9bcd-e7f00dc54a7f"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 16:00:04', 133);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (207, '用户管理', 1, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '研发部门',
        '/system/user', '154.21.193.236', ' ',
        '{"admin":false,"createBy":"admin","deptId":100,"nickName":"s瘦司","params":{},"phonenumber":"17777777777","postIds":[1],"roleIds":[2],"sex":"1","status":"0","userId":3,"userName":"showsi"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 16:29:59', 276);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (208, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/role', '154.21.193.236', 'XX XX',
        '{"admin":false,"createTime":"2023-12-26 08:54:02","dataScope":"2","delFlag":"0","deptCheckStrictly":true,"flag":false,"menuCheckStrictly":true,"menuIds":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2000,2009,2010,2011,2012,2013,2014,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,116,1055,1056,1057,1058,1059,1060,117],"params":{},"remark":"普通角色","roleId":2,"roleKey":"common","roleName":"普通角色","roleSort":2,"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 16:30:30', 99);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (209, '用户管理', 1, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '研发部门',
        '/system/user', '154.21.193.236', 'XX XX',
        '{"admin":false,"createBy":"admin","deptId":100,"nickName":"刈曲","params":{},"phonenumber":"18888888888","postIds":[4],"roleIds":[2],"sex":"0","status":"0","userId":4,"userName":"1732787414"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 16:33:34', 170);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (210, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/role', '4.2.2.2', 'XX XX',
        '{"admin":false,"createTime":"2023-12-26 08:54:02","dataScope":"2","delFlag":"0","deptCheckStrictly":true,"flag":false,"menuCheckStrictly":true,"menuIds":[2000,2009,2010,2011,2012,2013,2014,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114],"params":{},"remark":"普通角色","roleId":2,"roleKey":"common","roleName":"普通角色","roleSort":2,"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-27 16:38:23', 67);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (211, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '1.65.168.11', 'XX XX',
        '{"addState":"0","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","status":"1","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        null, 1, 'Expected 4 bytes but received -1', '2023-12-28 09:57:21', 26);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (212, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '1.65.168.11', 'XX XX',
        '{"addState":"0","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","status":"1","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        null, 1, 'Failed to write 46 bytes', '2023-12-28 09:57:24', 2);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (213, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '1.65.168.11', 'XX XX',
        '{"addState":"0","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","status":"1","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        null, 1, 'Failed to write 46 bytes', '2023-12-28 09:57:32', 1);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (214, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '1.65.168.11', 'XX XX',
        '{"addState":"1","addTime":"2023-12-28","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","reviewUsers":"admin","status":"1","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-28 10:02:13', 315);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (215, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '1.65.168.11', 'XX XX',
        '{"addState":"2","addTime":"2023-12-28","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeTime":"2023-12-28","reviewUsers":"admin","status":"0","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-28 10:25:56', 190);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (216, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '1.65.168.11', 'XX XX',
        '{"addState":"0","addTime":"2023-12-27","id":8,"onlineFlag":0,"params":{},"qqNum":"11111","remark":"","removeTime":"2023-12-27","reviewUsers":"admin","status":"0","time":"2023-12-27","userName":"test","userUuid":"7e5fcdf3-dd2b-4a62-9bcd-e7f00dc54a7f"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-28 10:59:16', 33);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (217, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '1.65.168.11', 'XX XX',
        '{"addState":"1","addTime":"2023-12-28","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeTime":"2023-12-28","reviewUsers":"admin","status":"1","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-28 11:02:12', 235);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (218, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '1.65.168.11', 'XX XX',
        '{"addState":"2","addTime":"2023-12-28","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"ces","removeTime":"2023-12-28","reviewUsers":"admin","status":"0","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-28 11:07:30', 139);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (219, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '1.65.168.11', 'XX XX',
        '{"addState":"1","addTime":"2023-12-28","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"ces","removeTime":"2023-12-28","reviewUsers":"admin","status":"1","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-28 11:07:38', 234);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (220, '用户管理', 2, 'com.ruoyi.web.controller.system.SysUserController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/user', '1.65.168.11', 'XX XX',
        '{"admin":false,"avatar":"","createBy":"admin","createTime":"2023-12-27 08:29:57","delFlag":"0","dept":{"ancestors":"0","children":[],"deptId":100,"deptName":"若依科技","leader":"若依","orderNum":0,"params":{},"parentId":0,"status":"0"},"deptId":100,"email":"","loginIp":"","nickName":"瘦司","params":{},"phonenumber":"17777777777","postIds":[1],"roleIds":[2],"roles":[{"admin":false,"dataScope":"2","deptCheckStrictly":false,"flag":false,"menuCheckStrictly":false,"params":{},"roleId":2,"roleKey":"common","roleName":"普通角色","roleSort":2,"status":"0"}],"sex":"1","status":"0","updateBy":"admin","userId":3,"userName":"showsi"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-29 07:54:58', 468);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (221, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '1.65.168.11', 'XX XX',
        '{"addState":"2","addTime":"2023-12-28","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"ces","removeTime":"2023-12-29","reviewUsers":"admin","status":"0","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-29 07:56:15', 144);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (222, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '1.65.168.11', 'XX XX',
        '{"addState":"1","addTime":"2023-12-29","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"ces","removeTime":"2023-12-29","reviewUsers":"admin","status":"1","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-29 07:56:56', 218);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (223, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'showsi', '若依科技',
        '/mc/whitelist', '27.191.196.29', 'XX XX',
        '{"addState":"1","addTime":"2023-12-29","id":10,"onlineFlag":0,"params":{},"qqNum":"1729886917","remark":"","reviewUsers":"showsi","status":"1","time":"2023-12-29","userName":"asdfkll","userUuid":"5d69af1b-70c7-431f-bd14-24a8fe2f725f"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-29 07:57:06', 227);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (224, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'showsi', '若依科技',
        '/mc/whitelist', '27.191.196.29', 'XX XX',
        '{"addState":"1","addTime":"2023-12-29","id":8,"onlineFlag":0,"params":{},"qqNum":"11111","remark":"","removeTime":"2023-12-27","reviewUsers":"showsi","status":"1","time":"2023-12-27","userName":"test","userUuid":"7e5fcdf3-dd2b-4a62-9bcd-e7f00dc54a7f"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-29 07:57:12', 220);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (225, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/8', '1.65.168.11', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-29 11:07:51', 36);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (226, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"2","addTime":"2023-12-29","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"ces","removeTime":"2023-12-29","reviewUsers":"admin","status":"0","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-29 11:57:54', 126);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (227, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"1","addTime":"2023-12-29","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2023-12-29","reviewUsers":"admin","status":"1","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-29 11:58:01', 227);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (228, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/12', '203.198.87.154', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-29 16:17:15', 40);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (229, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"1","addTime":"2023-12-29","id":11,"onlineFlag":0,"params":{},"qqNum":"2225626081","remark":"我是粉丝","reviewUsers":"admin","status":"1","time":"2023-12-29","userName":"lron_man","userUuid":"d725662e-2650-41e3-bac3-fc0dcc732e60"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-29 16:20:39', 2066);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (230, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"1","addTime":"2023-12-29","id":13,"onlineFlag":0,"params":{},"qqNum":"996111175","remark":"","reviewUsers":"admin","status":"1","time":"2023-12-29","userName":"testa","userUuid":"361dd7ab-d82e-42e3-967d-170390f695b5"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-29 16:20:42', 609);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (231, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"2","addTime":"2023-12-29","id":13,"onlineFlag":0,"params":{},"qqNum":"996111175","remark":"","removeTime":"2023-12-29","reviewUsers":"admin","status":"0","time":"2023-12-29","userName":"testa","userUuid":"361dd7ab-d82e-42e3-967d-170390f695b5"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-29 16:21:57', 212);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (232, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/13', '203.198.87.154', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2023-12-30 06:12:37', 29);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (233, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"2","addTime":"2023-12-29","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2023-12-30","reviewUsers":"admin","status":"0","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        null, 1, 'Expected 4 bytes but received -1', '2023-12-30 12:50:40', 9);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (234, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"2","addTime":"2023-12-29","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2023-12-30","reviewUsers":"admin","status":"0","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        null, 1, 'Failed to write 43 bytes', '2023-12-30 12:50:42', 0);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (235, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"2","addTime":"2023-12-29","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2023-12-30","reviewUsers":"admin","status":"0","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        null, 1, 'Failed to write 43 bytes', '2023-12-30 12:50:57', 0);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (236, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"0","addTime":"2023-12-29","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2023-12-29","reviewUsers":"admin","status":"1","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        null, 1, 'Failed to write 46 bytes', '2023-12-30 12:51:01', 0);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (237, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"0","addTime":"2023-12-29","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2023-12-29","reviewUsers":"admin","status":"2","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-30 12:51:03', 21);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (238, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"2","addTime":"2023-12-29","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2023-12-30","reviewUsers":"admin","status":"0","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        null, 1, 'Failed to write 43 bytes', '2023-12-30 12:51:07', 1);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (239, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"0","addTime":"2023-12-29","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2023-12-29","reviewUsers":"admin","status":"1","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        null, 1, 'Failed to write 46 bytes', '2023-12-30 12:51:10', 1);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (240, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"1","addTime":"2023-12-30","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2023-12-29","reviewUsers":"admin","status":"1","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-30 12:52:51', 284);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (241, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"1","addTime":"2023-12-31","id":14,"onlineFlag":1,"params":{},"qqNum":"2328834354","remark":"想玩（老实）","reviewUsers":"admin","status":"1","time":"2023-12-30","userName":"wdsaj1","userUuid":"586f6210-992f-4ac0-bf35-d4b4b71b75bf"}',
        '{"msg":"操作成功","code":200}', 0, null, '2023-12-31 07:16:31', 133);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (242, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/18', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-03 15:26:33', 58);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (243, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/16', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-03 15:26:35', 24);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (244, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-03","id":15,"onlineFlag":1,"params":{},"qqNum":"3076274097","remark":"单人档玩的有些许无聊，想体验体验服务器的乐趣哈哈哈哈","reviewUsers":"admin","status":"1","time":"2024-01-02","userName":"HeXCong","userUuid":"7f87814a-549b-4c50-a33b-341b90eaf92f"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-03 15:26:44', 194);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (245, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-03","id":17,"onlineFlag":1,"params":{},"qqNum":"2750172318","remark":"玩","reviewUsers":"admin","status":"1","time":"2024-01-03","userName":"MrHNan","userUuid":"bf030b4b-0c56-4d5e-8ccb-1c9eb5335e90"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-03 15:26:48', 126);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (246, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"0","id":19,"onlineFlag":0,"params":{},"qqNum":"1120967093","remark":"要和up一起玩","status":"2","time":"2024-01-03","userName":"波奇boki","userUuid":"4d3926d7-1008-40ef-b3e2-bfa3def85012"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-03 15:26:56', 27);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (247, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/20', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-06 04:24:45', 34);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (248, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/23', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-06 04:25:07', 19);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (249, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/24', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-06 04:25:15', 32);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (250, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/30', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-06 04:25:51', 23);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (251, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/29', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-06 04:25:53', 20);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (252, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/28', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-06 04:25:55', 17);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (253, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/27', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-06 04:25:56', 25);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (254, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/26', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-06 04:25:58', 17);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (255, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/25', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-06 04:26:05', 17);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (256, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '60.27.159.15', 'XX XX',
        '{"addState":"1","addTime":"2024-01-06","id":31,"onlineFlag":1,"params":{},"qqNum":"1493249389","remark":"因为管理员有神权","reviewUsers":"admin","status":"1","time":"2024-01-06","userName":"travelblacktea","userUuid":"35a0237b-bb53-4106-babe-897981e61c3f"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-06 07:20:25', 282);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (257, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '60.27.159.15', 'XX XX',
        '{"addState":"1","addTime":"2024-01-06","id":22,"onlineFlag":1,"params":{},"qqNum":"2350495127","remark":"因为要进服务器","reviewUsers":"admin","status":"1","time":"2024-01-04","userName":"zhifeijijijiji","userUuid":"a0f5400b-421d-44ff-8614-8cdb462337c7"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-06 07:20:28', 145);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (258, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '60.27.159.15', 'XX XX',
        '{"addState":"1","addTime":"2024-01-06","id":21,"onlineFlag":0,"params":{},"qqNum":"1781914235","remark":"萌新，虽有正版，但我不会，所以玩离线","reviewUsers":"admin","status":"1","time":"2024-01-03","userName":"whitecat","userUuid":"5a33c5ee-42fd-4eb7-b9e2-2ea9b5fee618"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-06 07:20:37', 414);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (259, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/33', '60.27.159.15', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-06 07:24:02', 14);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (260, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/32', '60.27.159.15', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-06 07:24:04', 16);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (261, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-09","id":34,"onlineFlag":1,"params":{},"qqNum":"3099643082","remark":"原因当然是和别人一起玩MC啦","reviewUsers":"admin","status":"1","time":"2024-01-07","userName":"DaCapo_anew","userUuid":"e272b757-173c-4c3e-8f19-248a0e3a93b5"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-09 07:31:40', 177);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (262, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-09","id":35,"onlineFlag":0,"params":{},"qqNum":"838518228","remark":"看视频想玩","reviewUsers":"admin","status":"1","time":"2024-01-07","userName":"单方块空岛生存","userUuid":"682b1873-04ce-4a16-88f2-be6f9396f21c"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-09 07:31:44', 255);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (263, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-09","id":36,"onlineFlag":1,"params":{},"qqNum":"1549293957","remark":"虚无世界3服务器\\n","reviewUsers":"admin","status":"1","time":"2024-01-08","userName":"CNHAX_chen","userUuid":"af290a56-f3c7-4a53-965f-f859dcbf42f9"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-09 07:31:48', 142);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (264, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-09","id":37,"onlineFlag":1,"params":{},"qqNum":"1940777187","remark":"想找人一起玩","reviewUsers":"admin","status":"1","time":"2024-01-09","userName":"Cymbal0209","userUuid":"e6fc81dc-c910-4334-bd5e-31ea29484479"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-09 07:31:51', 135);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (265, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/39', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-09 07:57:54', 27);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (266, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/38', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-09 07:57:56', 16);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (267, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"2","addTime":"2023-12-30","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2024-01-09","reviewUsers":"admin","status":"0","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-09 17:06:04', 168);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (268, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"0","id":41,"onlineFlag":0,"params":{},"qqNum":"341354183","remark":"一起玩","status":"1","time":"2024-01-10","userName":"zhichi_jntm","userUuid":"1639f59c-af19-4903-9d55-c8b05aea2048"}',
        null, 1, 'Expected 4 bytes but received -1', '2024-01-10 08:24:32', 18);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (269, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"0","id":41,"onlineFlag":0,"params":{},"qqNum":"341354183","remark":"一起玩","status":"1","time":"2024-01-10","userName":"zhichi_jntm","userUuid":"1639f59c-af19-4903-9d55-c8b05aea2048"}',
        null, 1, 'Failed to write 49 bytes', '2024-01-10 08:24:33', 1);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (270, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"0","id":41,"onlineFlag":0,"params":{},"qqNum":"341354183","remark":"一起玩","status":"1","time":"2024-01-10","userName":"zhichi_jntm","userUuid":"1639f59c-af19-4903-9d55-c8b05aea2048"}',
        null, 1, 'Failed to write 49 bytes', '2024-01-10 08:24:35', 0);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (271, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-10","id":41,"onlineFlag":0,"params":{},"qqNum":"341354183","remark":"一起玩","reviewUsers":"admin","status":"1","time":"2024-01-10","userName":"zhichi_jntm","userUuid":"1639f59c-af19-4903-9d55-c8b05aea2048"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-10 08:26:53', 287);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (272, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"2","addTime":"2023-12-31","id":14,"onlineFlag":1,"params":{},"qqNum":"2328834354","remark":"想玩（老实）","removeTime":"2024-01-10","reviewUsers":"admin","status":"0","time":"2023-12-30","userName":"wdsaj1","userUuid":"586f6210-992f-4ac0-bf35-d4b4b71b75bf"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-10 08:45:07', 160);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (273, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"0","addTime":"2023-12-31","id":14,"onlineFlag":1,"params":{},"qqNum":"2328834354","remark":"想玩（老实）","removeTime":"2024-01-10","reviewUsers":"admin","status":"0","time":"2023-12-30","userName":"wdsaj1","userUuid":"586f6210-992f-4ac0-bf35-d4b4b71b75bf"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-10 08:45:12', 49);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (274, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-10","id":14,"onlineFlag":1,"params":{},"qqNum":"2328834354","remark":"想玩（老实）","removeTime":"2024-01-10","reviewUsers":"admin","status":"1","time":"2023-12-30","userName":"wdsaj1","userUuid":"586f6210-992f-4ac0-bf35-d4b4b71b75bf"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-10 08:45:17', 132);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (275, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/43', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-10 12:32:19', 40);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (276, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/44', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-10 12:32:21', 20);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (277, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'showsi', '若依科技',
        '/mc/whitelist', '27.191.196.29', 'XX XX',
        '{"addState":"1","addTime":"2024-01-10","id":40,"onlineFlag":0,"params":{},"qqNum":"99999","remark":"1","reviewUsers":"showsi","status":"1","time":"2024-01-09","userName":"gameggg","userUuid":"0263f468-ff87-4e83-8167-a4f4006ba3e0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-10 12:33:58', 240);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (278, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'showsi', '若依科技',
        '/mc/whitelist', '27.191.196.29', 'XX XX',
        '{"addState":"1","addTime":"2024-01-10","id":42,"onlineFlag":1,"params":{},"qqNum":"2761866885","remark":"肯定是想进服务器玩喽","reviewUsers":"showsi","status":"1","time":"2024-01-10","userName":"beijibing233","userUuid":"8b3d2132-0bf5-4efe-aa2f-227f28d776e4"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-10 12:34:08', 126);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (279, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/45', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-10 12:45:45', 46);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (280, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/46', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-10 13:07:19', 26);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (281, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'showsi', '若依科技',
        '/mc/whitelist', '27.191.196.29', 'XX XX',
        '{"addState":"1","addTime":"2024-01-10","id":47,"onlineFlag":1,"params":{},"qqNum":"421405626","remark":"冲冲冲~","reviewUsers":"showsi","status":"1","time":"2024-01-10","userName":"Talos1031","userUuid":"4cd214f0-cf2d-4c55-8986-8c81306c8203"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-10 14:39:39', 234);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (282, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/40', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-11 10:47:05', 53);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (283, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/48', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-11 10:48:32', 29);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (284, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'showsi', '若依科技',
        '/mc/whitelist', '27.191.196.29', 'XX XX',
        '{"addState":"0","id":49,"onlineFlag":1,"params":{},"qqNum":"435753710","remark":"老年人想找个养老院安享晚年生活","reviewUsers":"showsi","status":"1","time":"2024-01-11","userName":"K1tani","userUuid":"289d4dfc-547a-4e92-ae25-42e8d4604ba2"}',
        null, 1, 'Expected 4 bytes but received -1', '2024-01-11 15:41:34', 53);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (285, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'showsi', '若依科技',
        '/mc/whitelist', '27.191.196.29', 'XX XX',
        '{"addState":"0","id":49,"onlineFlag":1,"params":{},"qqNum":"435753710","remark":"老年人想找个养老院安享晚年生活","reviewUsers":"showsi","status":"1","time":"2024-01-11","userName":"K1tani","userUuid":"289d4dfc-547a-4e92-ae25-42e8d4604ba2"}',
        null, 1, 'Failed to write 38 bytes', '2024-01-11 15:41:36', 4);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (286, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'showsi', '若依科技',
        '/mc/whitelist', '27.191.196.29', 'XX XX',
        '{"addState":"0","id":49,"onlineFlag":1,"params":{},"qqNum":"435753710","remark":"老年人想找个养老院安享晚年生活","reviewUsers":"showsi","status":"1","time":"2024-01-11","userName":"K1tani","userUuid":"289d4dfc-547a-4e92-ae25-42e8d4604ba2"}',
        null, 1, 'Failed to write 38 bytes', '2024-01-11 15:41:40', 1);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (287, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'showsi', '若依科技',
        '/mc/whitelist', '27.191.196.29', 'XX XX',
        '{"addState":"0","id":49,"onlineFlag":1,"params":{},"qqNum":"435753710","remark":"老年人想找个养老院安享晚年生活","reviewUsers":"showsi","status":"1","time":"2024-01-11","userName":"K1tani","userUuid":"289d4dfc-547a-4e92-ae25-42e8d4604ba2"}',
        null, 1, 'Failed to write 38 bytes', '2024-01-11 15:42:01', 0);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (288, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'showsi', '若依科技',
        '/mc/whitelist', '27.191.196.29', 'XX XX',
        '{"addState":"0","id":49,"onlineFlag":1,"params":{},"qqNum":"435753710","remark":"老年人想找个养老院安享晚年生活","reviewUsers":"showsi","status":"1","time":"2024-01-11","userName":"K1tani","userUuid":"289d4dfc-547a-4e92-ae25-42e8d4604ba2"}',
        null, 1, 'Failed to write 38 bytes', '2024-01-11 15:42:12', 3);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (289, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'showsi', '若依科技',
        '/mc/whitelist', '27.191.196.29', 'XX XX',
        '{"addState":"0","id":49,"onlineFlag":1,"params":{},"qqNum":"435753710","remark":"老年人想找个养老院安享晚年生活","reviewUsers":"showsi","status":"1","time":"2024-01-11","userName":"K1tani","userUuid":"289d4dfc-547a-4e92-ae25-42e8d4604ba2"}',
        null, 1, 'Failed to write 38 bytes', '2024-01-11 15:42:13', 2);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (290, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'showsi', '若依科技',
        '/mc/whitelist', '27.191.196.29', 'XX XX',
        '{"addState":"0","id":49,"onlineFlag":1,"params":{},"qqNum":"435753710","remark":"老年人想找个养老院安享晚年生活","reviewUsers":"showsi","status":"1","time":"2024-01-11","userName":"K1tani","userUuid":"289d4dfc-547a-4e92-ae25-42e8d4604ba2"}',
        null, 1, 'Failed to write 38 bytes', '2024-01-11 15:42:34', 1);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (291, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'showsi', '若依科技',
        '/mc/whitelist', '27.191.196.29', 'XX XX',
        '{"addState":"0","id":49,"onlineFlag":1,"params":{},"qqNum":"435753710","remark":"老年人想找个养老院安享晚年生活","reviewUsers":"showsi","status":"1","time":"2024-01-11","userName":"K1tani","userUuid":"289d4dfc-547a-4e92-ae25-42e8d4604ba2"}',
        null, 1, 'Failed to write 38 bytes', '2024-01-11 15:42:55', 0);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (292, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-11","id":49,"onlineFlag":1,"params":{},"qqNum":"435753710","remark":"老年人想找个养老院安享晚年生活","reviewUsers":"admin","status":"1","time":"2024-01-11","userName":"K1tani","userUuid":"289d4dfc-547a-4e92-ae25-42e8d4604ba2"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-11 15:56:32', 171);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (293, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-12","id":50,"onlineFlag":1,"params":{},"qqNum":"3541493171","remark":"想加入进来","reviewUsers":"admin","status":"1","time":"2024-01-12","userName":"Tsubakiwataru","userUuid":"302e6e66-507f-4c14-b6ed-41d92e0bca54"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-12 14:31:00', 182);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (294, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"2","addTime":"2024-01-10","id":14,"onlineFlag":1,"params":{},"qqNum":"2328834354","remark":"想玩（老实）","removeTime":"2024-01-12","reviewUsers":"admin","status":"0","time":"2023-12-30","userName":"wdsaj1","userUuid":"586f6210-992f-4ac0-bf35-d4b4b71b75bf"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-12 19:20:47', 173);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (295, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-12","id":14,"onlineFlag":1,"params":{},"qqNum":"2328834354","remark":"想玩（老实）","removeTime":"2024-01-12","reviewUsers":"admin","status":"1","time":"2023-12-30","userName":"wdsaj1","userUuid":"586f6210-992f-4ac0-bf35-d4b4b71b75bf"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-12 19:21:01', 144);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (296, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"2","addTime":"2023-12-30","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2024-01-12","reviewUsers":"admin","status":"0","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-12 19:27:40', 140);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (297, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-12","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2024-01-12","reviewUsers":"admin","status":"1","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-12 19:27:44', 242);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (298, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-13","id":51,"onlineFlag":1,"params":{},"qqNum":"1321064453","remark":"","reviewUsers":"admin","status":"1","time":"2024-01-13","userName":"ninjiaapua","userUuid":"e058840d-c4cc-4753-a7ce-a1e3026b2f07"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-13 05:24:26', 161);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (299, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"0","id":53,"onlineFlag":0,"params":{},"qqNum":"2873336923","remark":"","reviewUsers":"admin","status":"1","time":"2024-01-13","userName":"emailTest","userUuid":"aee11882-cba9-4bbf-9532-f0a9e8b78287"}',
        '{"msg":"添加白名单失败,请联系管理员!","code":500}', 0, null, '2024-01-13 08:46:56', 2351);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (300, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"0","id":52,"onlineFlag":1,"params":{},"qqNum":"1078422426","remark":"想一起玩服务器","reviewUsers":"admin","status":"1","time":"2024-01-13","userName":"b1b2bu","userUuid":"3f0c3aee-1dcd-4a06-81fa-49a0a3da6c71"}',
        '{"msg":"添加白名单失败,请联系管理员!","code":500}', 0, null, '2024-01-13 08:47:55', 524);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (301, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/53', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-13 09:35:55', 40);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (302, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-13","id":52,"onlineFlag":1,"params":{},"qqNum":"1078422426","remark":"想一起玩服务器","reviewUsers":"admin","status":"1","time":"2024-01-13","userName":"b1b2bu","userUuid":"3f0c3aee-1dcd-4a06-81fa-49a0a3da6c71"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-13 09:36:02', 1943);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (303, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '164.70.99.9', 'XX XX',
        '{"addState":"1","addTime":"2024-01-13","id":54,"onlineFlag":0,"params":{},"qqNum":"2697089140","remark":"进不去游戏","reviewUsers":"admin","status":"1","time":"2024-01-13","userName":"CChuan","userUuid":"94dcfa55-1afe-466c-9f4d-59467d9c3766"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-13 09:53:24', 1460);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (304, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '164.70.99.9', 'XX XX',
        '{"addState":"1","addTime":"2024-01-14","id":55,"onlineFlag":0,"params":{},"qqNum":"1695976142","remark":"大学生暑假想和朋友一块玩服务器，看到抖音里的视频，对贵服务器感兴趣，想来玩玩","reviewUsers":"admin","status":"1","time":"2024-01-14","userName":"Kong_Cheng","userUuid":"32e81971-4406-41ae-a46b-964ac3631d79"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-14 10:45:26', 1915);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (305, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '164.70.99.9', 'XX XX',
        '{"addState":"1","addTime":"2024-01-14","id":56,"onlineFlag":0,"params":{},"qqNum":"834789621","remark":"嘻嘻，朋友邀请一起玩","reviewUsers":"admin","status":"1","time":"2024-01-14","userName":"Ayuee","userUuid":"10abedd2-a007-40dd-8ac7-474442f7602b"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-14 12:56:28', 1980);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (306, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '164.70.99.9', 'XX XX',
        '{"addState":"1","addTime":"2024-01-14","id":57,"onlineFlag":0,"params":{},"qqNum":"2246233068","remark":"大学生放假了，和朋友一块来玩服务器，朋友介绍来一起的","reviewUsers":"admin","status":"1","time":"2024-01-14","userName":"backlight","userUuid":"4adf8764-e612-4ac5-a869-5fcebcc6d333"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-14 12:56:45', 1449);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (307, '字典数据', 2, 'com.ruoyi.web.controller.system.SysDictDataController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/data', '164.70.99.9', 'XX XX',
        '{"createBy":"admin","createTime":"2023-12-27 10:41:05","default":false,"dictCode":104,"dictLabel":"已添加","dictSort":2,"dictType":"white_add_status","dictValue":"1","isDefault":"N","listClass":"default","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-14 13:37:58', 48);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (308, '字典数据', 2, 'com.ruoyi.web.controller.system.SysDictDataController.edit()', 'PUT', 1, 'admin',
        '研发部门', '/system/dict/data', '164.70.99.9', 'XX XX',
        '{"createBy":"admin","createTime":"2023-12-27 10:41:05","default":false,"dictCode":104,"dictLabel":"已添加","dictSort":2,"dictType":"white_add_status","dictValue":"1","isDefault":"N","listClass":"success","params":{},"status":"0","updateBy":"admin"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-14 13:38:09', 40);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (309, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '164.70.99.9', 'XX XX',
        '{"addState":"2","addTime":"2024-01-12","id":14,"onlineFlag":1,"params":{},"qqNum":"2328834354","remark":"想玩（老实）","removeTime":"2024-01-14","reviewUsers":"admin","status":"0","time":"2023-12-30","userName":"wdsaj1","userUuid":"586f6210-992f-4ac0-bf35-d4b4b71b75bf"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-14 15:59:16', 1630);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (310, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '164.70.99.9', 'XX XX',
        '{"addState":"1","addTime":"2024-01-14","id":14,"onlineFlag":1,"params":{},"qqNum":"2328834354","remark":"想玩（老实）","removeTime":"2024-01-14","reviewUsers":"admin","status":"1","time":"2023-12-30","userName":"wdsaj1","userUuid":"586f6210-992f-4ac0-bf35-d4b4b71b75bf"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-14 15:59:22', 1254);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (311, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"1","addTime":"2024-01-15","id":58,"onlineFlag":0,"params":{},"qqNum":"4324324234","remark":"","reviewUsers":"admin","status":"1","time":"2024-01-15","userName":"GG-BBO","userUuid":"44a00924-6552-452e-8258-d428d4a10cee"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-15 08:17:42', 2018);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (312, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"1","addTime":"2024-01-15","id":58,"onlineFlag":0,"params":{},"qqNum":"4324324234","remark":"","reviewUsers":"admin","status":"1","time":"2024-01-15","userName":"GG_BBO","userUuid":"44a00924-6552-452e-8258-d428d4a10cee"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-15 08:22:51', 1687);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (313, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"1","addTime":"2024-01-15","id":58,"onlineFlag":0,"params":{},"qqNum":"4324324234","remark":"","reviewUsers":"admin","status":"1","time":"2024-01-15","userName":"GG_BBO","userUuid":"44a00924-6552-452e-8258-d428d4a10cee"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-15 08:23:16', 1427);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (314, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"1","addTime":"2024-01-15","id":59,"onlineFlag":1,"params":{},"qqNum":"2896109695","remark":"自己一个人玩比较孤独，想和大家一起玩","reviewUsers":"admin","status":"1","time":"2024-01-15","userName":"Di_jiang_","userUuid":"af24032b-2f3a-4fe0-a153-9e8df849dd8c"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-15 11:18:39', 1989);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (315, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"2","addTime":"2024-01-12","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2024-01-15","reviewUsers":"admin","status":"0","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-15 12:18:14', 1568);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (316, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"1","addTime":"2024-01-15","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2024-01-15","reviewUsers":"admin","status":"1","time":"2023-12-28","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-15 12:18:44', 1447);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (317, '用户管理', 1, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '研发部门',
        '/system/user', '203.198.87.154', 'XX XX',
        '{"admin":false,"deptId":100,"email":"3096643082@qq.com","nickName":"DaCapo_anew","params":{},"phonenumber":"18888888888","postIds":[4],"roleIds":[2],"sex":"0","status":"0","userName":"3096643082"}',
        '{"msg":"新增用户\'3096643082\'失败，手机号码已存在","code":500}', 0, null, '2024-01-15 12:44:57', 35);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (318, '用户管理', 1, 'com.ruoyi.web.controller.system.SysUserController.add()', 'POST', 1, 'admin', '研发部门',
        '/system/user', '203.198.87.154', 'XX XX',
        '{"admin":false,"createBy":"admin","deptId":100,"email":"3096643082@qq.com","nickName":"DaCapo_anew","params":{},"phonenumber":"18888888887","postIds":[4],"roleIds":[2],"sex":"0","status":"0","userId":5,"userName":"3096643082"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-15 12:45:02', 289);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (319, '用户管理', 3, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/system/user/4', '203.198.87.154', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-15 12:45:19', 53);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (320, '白名单', 5, 'com.ruoyi.server.controller.WhitelistInfoController.export()', 'POST', 1, 'admin',
        '研发部门', '/mc/whitelist/export', '203.198.87.154', 'XX XX', '{"pageSize":"10","pageNum":"3"}', null, 0, null,
        '2024-01-15 18:30:33', 2254);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (321, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"1","addTime":"2024-01-16","id":42,"onlineFlag":1,"params":{},"qqNum":"2761866885","remark":"肯定是想进服务器玩喽","reviewUsers":"admin","status":"1","time":"2024-01-10","userName":"Tsing_Yi321","userUuid":"8b3d2132-0bf5-4efe-aa2f-227f28d776e4"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-16 04:26:09', 2527);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (322, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"1","addTime":"2024-01-16","id":42,"onlineFlag":1,"params":{},"qqNum":"2761866885","remark":"肯定是想进服务器玩喽","reviewUsers":"admin","status":"1","time":"2024-01-10","userName":"Tsing_Yi321","userUuid":"8b3d2132-0bf5-4efe-aa2f-227f28d776e4"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-16 04:26:57', 1293);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (323, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"1","addTime":"2024-01-16","id":60,"onlineFlag":0,"params":{},"qqNum":"1990189528","remark":"想和大家一起玩","reviewUsers":"admin","status":"1","time":"2024-01-16","userName":"muqing","userUuid":"e3fba7c8-5f33-4371-b81f-ca3298e331de"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-16 10:58:28', 2194);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (324, '角色管理', 5, 'com.ruoyi.web.controller.system.SysRoleController.export()', 'POST', 1, 'admin',
        '研发部门', '/system/role/export', '203.198.87.154', 'XX XX', '{"pageSize":"10","pageNum":"1"}', null, 0, null,
        '2024-01-16 16:54:01', 10956);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (325, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/61', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-18 07:57:29', 280);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (326, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-18","id":62,"onlineFlag":0,"params":{},"qqNum":"1158360909","remark":"","reviewUsers":"admin","status":"1","time":"2024-01-18","userName":"lihuan","userUuid":"526eb6fb-96cf-49cf-94b8-70396c09afa0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-18 11:31:44', 2348);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (327, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-18","id":63,"onlineFlag":1,"params":{},"qqNum":"2016938653","remark":"","reviewUsers":"admin","status":"1","time":"2024-01-18","userName":"chenpingan12","userUuid":"adb9bf5e-768a-4493-8e80-99d3504bcf8d"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-18 15:48:22', 1706);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (328, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, '3096643082',
        '若依科技', '/mc/whitelist', '36.62.166.152', 'XX XX',
        '{"addState":"1","addTime":"2024-01-19","id":64,"onlineFlag":1,"params":{},"qqNum":"1839622383","remark":"玩","reviewUsers":"3096643082","status":"1","time":"2024-01-19","userName":"taizediyixian","userUuid":"3e2f566f-46d5-4c77-a224-29ba4b69af57"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-19 10:04:43', 1755);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (329, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, '3096643082',
        '若依科技', '/mc/whitelist', '36.62.166.152', 'XX XX',
        '{"addState":"1","addTime":"2024-01-19","id":64,"onlineFlag":1,"params":{},"qqNum":"1839622383","remark":"玩","reviewUsers":"3096643082","status":"1","time":"2024-01-19","userName":"taizediyixian","userUuid":"3e2f566f-46d5-4c77-a224-29ba4b69af57"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-19 10:04:44', 1223);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (330, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, '3096643082',
        '若依科技', '/mc/whitelist', '36.62.166.152', 'XX XX',
        '{"addState":"1","addTime":"2024-01-19","id":65,"onlineFlag":0,"params":{},"qqNum":"2902568437","remark":"想玩\\n","reviewUsers":"3096643082","status":"1","time":"2024-01-19","userName":"Lieqqq","userUuid":"8efe6581-58db-4633-8055-3d19edcbcfc7"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-19 16:30:58', 2120);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (331, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, '3096643082',
        '若依科技', '/mc/whitelist', '36.62.166.152', 'XX XX',
        '{"addState":"1","addTime":"2024-01-19","id":66,"onlineFlag":1,"params":{},"qqNum":"3819079588","remark":"一起玩","reviewUsers":"3096643082","status":"1","time":"2024-01-19","userName":"ShuBeiQiQi","userUuid":"f13c8e4b-3f68-4b06-a36e-c99fb9284663"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-19 16:31:02', 1211);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (332, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"1","addTime":"2024-01-20","id":67,"onlineFlag":1,"params":{},"qqNum":"2993353689","remark":"想玩","reviewUsers":"admin","status":"1","time":"2024-01-20","userName":"Skiff_Wu","userUuid":"e5d11fdd-9966-454b-a6bd-3521a2bfeb85"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-20 17:00:00', 1879);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (333, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.87.154', 'XX XX',
        '{"addState":"1","addTime":"2024-01-20","id":68,"onlineFlag":0,"params":{},"qqNum":"123412331","remark":"","reviewUsers":"admin","status":"1","time":"2024-01-20","userName":"loseruuu","userUuid":"f007f782-4f07-4188-90a5-a0b9a55056eb"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-20 17:39:19', 2097);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (334, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/68', '203.198.87.154', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-21 07:33:45', 131);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (335, '白名单', 3, 'com.ruoyi.server.controller.WhitelistInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/mc/whitelist/69', '203.198.94.197', 'XX XX', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-01-26 10:35:22', 75);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (336, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-27","id":70,"onlineFlag":1,"params":{},"qqNum":"211946985","remark":"玩服务器","reviewUsers":"admin","status":"1","time":"2024-01-27","userName":"ERKE6001","userUuid":"3b4650d5-b345-4f06-aaf8-9f0ef465b01d"}',
        null, 1, 'nested exception is org.apache.ibatis.exceptions.PersistenceException:
### Error updating database.  Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt
### The error may exist in URL [jar:file:/www/ruoyi/ruoyi-admin.jar!/BOOT-INF/lib/mc-server-3.8.7.jar!/mapper/mc/WhitelistInfoMapper.xml]
### The error may involve com.ruoyi.server.mapper.WhitelistInfoMapper.updateWhitelistInfo
### The error occurred while executing an update
### Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt',
        '2024-01-27 06:15:51', 80082);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (337, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-27","id":70,"onlineFlag":1,"params":{},"qqNum":"211946985","remark":"玩服务器","reviewUsers":"admin","status":"1","time":"2024-01-27","userName":"ERKE6001","userUuid":"3b4650d5-b345-4f06-aaf8-9f0ef465b01d"}',
        null, 1, 'nested exception is org.apache.ibatis.exceptions.PersistenceException:
### Error updating database.  Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt
### The error may exist in URL [jar:file:/www/ruoyi/ruoyi-admin.jar!/BOOT-INF/lib/mc-server-3.8.7.jar!/mapper/mc/WhitelistInfoMapper.xml]
### The error may involve com.ruoyi.server.mapper.WhitelistInfoMapper.updateWhitelistInfo
### The error occurred while executing an update
### Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt',
        '2024-01-27 06:15:51', 72743);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (338, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-27","id":70,"onlineFlag":1,"params":{},"qqNum":"211946985","remark":"玩服务器","reviewUsers":"admin","status":"1","time":"2024-01-27","userName":"ERKE6001","userUuid":"3b4650d5-b345-4f06-aaf8-9f0ef465b01d"}',
        null, 1, 'nested exception is org.apache.ibatis.exceptions.PersistenceException:
### Error updating database.  Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt
### The error may exist in URL [jar:file:/www/ruoyi/ruoyi-admin.jar!/BOOT-INF/lib/mc-server-3.8.7.jar!/mapper/mc/WhitelistInfoMapper.xml]
### The error may involve com.ruoyi.server.mapper.WhitelistInfoMapper.updateWhitelistInfo
### The error occurred while executing an update
### Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt',
        '2024-01-27 06:15:51', 45690);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (339, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-27","id":70,"onlineFlag":1,"params":{},"qqNum":"211946985","remark":"玩服务器","reviewUsers":"admin","status":"1","time":"2024-01-27","userName":"ERKE6001","userUuid":"3b4650d5-b345-4f06-aaf8-9f0ef465b01d"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-27 06:17:36', 1597);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (340, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-27","id":71,"onlineFlag":0,"params":{},"qqNum":"2723786253","remark":"woyaowanzhenban","reviewUsers":"admin","status":"1","time":"2024-01-27","userName":"12378","userUuid":"583ddc78-1fcf-4b72-87e9-745b6b692a9c"}',
        null, 1, 'nested exception is org.apache.ibatis.exceptions.PersistenceException:
### Error updating database.  Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt
### The error may exist in URL [jar:file:/www/ruoyi/ruoyi-admin.jar!/BOOT-INF/lib/mc-server-3.8.7.jar!/mapper/mc/WhitelistInfoMapper.xml]
### The error may involve com.ruoyi.server.mapper.WhitelistInfoMapper.updateWhitelistInfo
### The error occurred while executing an update
### Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt',
        '2024-01-27 08:23:50', 399829);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (341, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-27","id":71,"onlineFlag":0,"params":{},"qqNum":"2723786253","remark":"woyaowanzhenban","reviewUsers":"admin","status":"1","time":"2024-01-27","userName":"12378","userUuid":"583ddc78-1fcf-4b72-87e9-745b6b692a9c"}',
        null, 1, 'nested exception is org.apache.ibatis.exceptions.PersistenceException:
### Error updating database.  Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt
### The error may exist in URL [jar:file:/www/ruoyi/ruoyi-admin.jar!/BOOT-INF/lib/mc-server-3.8.7.jar!/mapper/mc/WhitelistInfoMapper.xml]
### The error may involve com.ruoyi.server.mapper.WhitelistInfoMapper.updateWhitelistInfo
### The error occurred while executing an update
### Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt',
        '2024-01-27 08:23:50', 202965);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (342, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-27","id":71,"onlineFlag":0,"params":{},"qqNum":"2723786253","remark":"woyaowanzhenban","reviewUsers":"admin","status":"1","time":"2024-01-27","userName":"12378","userUuid":"583ddc78-1fcf-4b72-87e9-745b6b692a9c"}',
        null, 1, 'nested exception is org.apache.ibatis.exceptions.PersistenceException:
### Error updating database.  Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt
### The error may exist in URL [jar:file:/www/ruoyi/ruoyi-admin.jar!/BOOT-INF/lib/mc-server-3.8.7.jar!/mapper/mc/WhitelistInfoMapper.xml]
### The error may involve com.ruoyi.server.mapper.WhitelistInfoMapper.updateWhitelistInfo
### The error occurred while executing an update
### Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt',
        '2024-01-27 08:23:50', 389938);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (343, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-27","id":71,"onlineFlag":0,"params":{},"qqNum":"2723786253","remark":"woyaowanzhenban","reviewUsers":"admin","status":"1","time":"2024-01-27","userName":"12378","userUuid":"583ddc78-1fcf-4b72-87e9-745b6b692a9c"}',
        null, 1, 'nested exception is org.apache.ibatis.exceptions.PersistenceException:
### Error updating database.  Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt
### The error may exist in URL [jar:file:/www/ruoyi/ruoyi-admin.jar!/BOOT-INF/lib/mc-server-3.8.7.jar!/mapper/mc/WhitelistInfoMapper.xml]
### The error may involve com.ruoyi.server.mapper.WhitelistInfoMapper.updateWhitelistInfo
### The error occurred while executing an update
### Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt',
        '2024-01-27 08:23:50', 395369);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (344, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-27","id":71,"onlineFlag":0,"params":{},"qqNum":"2723786253","remark":"woyaowanzhenban","reviewUsers":"admin","status":"1","time":"2024-01-27","userName":"12378","userUuid":"583ddc78-1fcf-4b72-87e9-745b6b692a9c"}',
        null, 1, 'nested exception is org.apache.ibatis.exceptions.PersistenceException:
### Error updating database.  Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt
### The error may exist in URL [jar:file:/www/ruoyi/ruoyi-admin.jar!/BOOT-INF/lib/mc-server-3.8.7.jar!/mapper/mc/WhitelistInfoMapper.xml]
### The error may involve com.ruoyi.server.mapper.WhitelistInfoMapper.updateWhitelistInfo
### The error occurred while executing an update
### Cause: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection; nested exception is java.sql.SQLException: interrupt',
        '2024-01-27 08:23:50', 387830);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (345, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '203.198.94.197', 'XX XX',
        '{"addState":"1","addTime":"2024-01-27","id":71,"onlineFlag":0,"params":{},"qqNum":"2723786253","remark":"woyaowanzhenban","reviewUsers":"admin","status":"1","time":"2024-01-27","userName":"12378","userUuid":"583ddc78-1fcf-4b72-87e9-745b6b692a9c"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-27 08:24:32', 1741);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (346, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '1.65.168.11', 'XX XX',
        '{"addState":"1","addTime":"2024-01-29","id":72,"onlineFlag":1,"params":{},"qqNum":"3464859080","remark":"想玩联机","reviewUsers":"admin","status":"1","time":"2024-01-29","userName":"YeZi0614","userUuid":"975efeea-9730-443c-baa9-772c69dd9489"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-29 17:30:08', 945897);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (347, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '1.65.168.11', 'XX XX',
        '{"addState":"1","addTime":"2024-01-30","id":73,"onlineFlag":0,"params":{},"qqNum":"205450565","remark":"玩乌托邦\\n","reviewUsers":"admin","status":"1","time":"2024-01-30","userName":"chazixu","userUuid":"dae4cfb6-24dd-41be-a118-55d8550930d8"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-01-30 11:13:31', 946877);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (348, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin',
        '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{"tables":"server_info"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-10 15:46:36', 70);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (349, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门',
        '/tool/gen', '127.0.0.1', '内网IP',
        '{"businessName":"serverlist","className":"ServerInfo","columns":[{"capJavaField":"Id","columnComment":"主键ID","columnId":27,"columnName":"id","columnType":"int","createBy":"admin","createTime":"2024-03-10 15:46:36","dictType":"","edit":false,"htmlType":"input","increment":true,"insert":true,"isIncrement":"1","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"Long","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":3,"updateBy":"","usableColumn":false},{"capJavaField":"Uuid","columnComment":"随机UUID","columnId":28,"columnName":"uuid","columnType":"varchar(64)","createBy":"admin","createTime":"2024-03-10 15:46:36","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"1","javaField":"uuid","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":true,"sort":2,"superColumn":false,"tableId":3,"updateBy":"","usableColumn":false},{"capJavaField":"NameTag","columnComment":"服务器名称标签","columnId":29,"columnName":"name_tag","columnType":"varchar(128)","createBy":"admin","createTime":"2024-03-10 15:46:36","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"1","javaField":"nameTag","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":true,"sort":3,"superColumn":false,"tableId":3,"updateBy":"","usableColumn":false},{"capJavaField":"Ip","columnComment":"服务器IP","columnId":30,"columnName":"ip","columnType":"varchar(128)","createBy":"admin","createTime":"2024-03-10 15:46:36","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"1","javaField":"ip","javaType":',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-10 15:49:10', 31);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (350, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin',
        '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"server_info"}', null, 0, null,
        '2024-03-10 15:49:16', 165);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (351, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门',
        '/tool/gen', '127.0.0.1', '内网IP',
        '{"businessName":"serverlist","className":"ServerInfo","columns":[{"capJavaField":"Id","columnComment":"主键ID","columnId":27,"columnName":"id","columnType":"int","createBy":"admin","createTime":"2024-03-10 15:46:36","dictType":"","edit":false,"htmlType":"input","increment":true,"insert":true,"isIncrement":"1","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"Long","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":3,"updateBy":"","updateTime":"2024-03-10 15:49:10","usableColumn":false},{"capJavaField":"Uuid","columnComment":"随机UUID","columnId":28,"columnName":"uuid","columnType":"varchar(64)","createBy":"admin","createTime":"2024-03-10 15:46:36","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"1","javaField":"uuid","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":true,"sort":2,"superColumn":false,"tableId":3,"updateBy":"","updateTime":"2024-03-10 15:49:10","usableColumn":false},{"capJavaField":"NameTag","columnComment":"服务器名称标签","columnId":29,"columnName":"name_tag","columnType":"varchar(128)","createBy":"admin","createTime":"2024-03-10 15:46:36","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"1","javaField":"nameTag","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":true,"sort":3,"superColumn":false,"tableId":3,"updateBy":"","updateTime":"2024-03-10 15:49:10","usableColumn":false},{"capJavaField":"Ip","columnComment":"服务器IP","columnId":30,"columnName":"ip","columnType":"varchar(128)","createBy":"admin","createTime":"2024-03-10 15:46:36","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncremen',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-10 15:57:12', 55);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (352, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin',
        '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"server_info"}', null, 0, null,
        '2024-03-10 15:57:16', 152);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (353, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"component":"server/serverlist/index","createTime":"2024-03-10 16:00:54","icon":"server","isCache":"0","isFrame":"1","menuId":2021,"menuName":"服务器信息","menuType":"C","orderNum":1,"params":{},"parentId":2000,"path":"serverlist","perms":"server:serverlist:list","status":"0","updateBy":"admin","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-10 16:01:38', 27);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (354, '服务器信息', 1, 'com.ruoyi.server.controller.ServerInfoController.add()', 'POST', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"id":1,"ip":"1.1.1.1","nameTag":"test","params":{},"rconPassword":"032e7adc3adb4f1dad0d19cc5c14494d","rconPort":25545,"remark":"111","status":1,"uuid":"333039b2-5783-4d7d-b385-4e327581774d"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-10 16:59:43', 33);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (355, '服务器信息', 2, 'com.ruoyi.server.controller.ServerInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"id":1,"ip":"1.1.1.1","nameTag":"test","params":{},"rconPassword":"e08c64b0c61b93dd3274a9a2911cde20","rconPort":25545,"remark":"111","status":0,"updateBy":"admin","updateTime":"2024-03-10 17:05:33","uuid":"333039b2-5783-4d7d-b385-4e327581774d"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-10 17:05:33', 12);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (356, '服务器信息', 1, 'com.ruoyi.server.controller.ServerInfoController.add()', 'POST', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2024-03-10 17:06:56","ip":"host.mcpeach.cc","nameTag":"生电","params":{},"rconPassword":"ef973c3c23300e242b7257298b4f9d7b","rconPort":25575,"remark":"111111","status":0,"updateBy":"admin","updateTime":"2024-03-10 17:06:56","uuid":"7a9662fb-c250-4ea1-bdf2-53d52644f3a9"}',
        null, 1,
        'nested exception is org.apache.ibatis.reflection.ReflectionException: There is no getter for property named \'creatTime\' in \'class com.ruoyi.server.domain.ServerInfo\'',
        '2024-03-10 17:06:56', 10);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (357, '服务器信息', 1, 'com.ruoyi.server.controller.ServerInfoController.add()', 'POST', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2024-03-10 17:06:59","ip":"host.mcpeach.cc","nameTag":"生电","params":{},"rconPassword":"ef973c3c23300e242b7257298b4f9d7b","rconPort":25575,"remark":"111111","status":0,"updateBy":"admin","updateTime":"2024-03-10 17:06:59","uuid":"32214b02-a128-4a35-8921-707ada44ec1e"}',
        null, 1,
        'nested exception is org.apache.ibatis.reflection.ReflectionException: There is no getter for property named \'creatTime\' in \'class com.ruoyi.server.domain.ServerInfo\'',
        '2024-03-10 17:06:59', 3);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (358, '服务器信息', 1, 'com.ruoyi.server.controller.ServerInfoController.add()', 'POST', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2024-03-10 17:07:26","ip":"host.mcpeach.cc","nameTag":"生电","params":{},"rconPassword":"ef973c3c23300e242b7257298b4f9d7b","rconPort":25575,"remark":"111111","status":0,"updateBy":"admin","updateTime":"2024-03-10 17:07:26","uuid":"42b456f6-bc36-49b6-9bcf-5a98ed342b07"}',
        null, 1,
        'nested exception is org.apache.ibatis.reflection.ReflectionException: There is no getter for property named \'creatTime\' in \'class com.ruoyi.server.domain.ServerInfo\'',
        '2024-03-10 17:07:26', 3);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (359, '服务器信息', 1, 'com.ruoyi.server.controller.ServerInfoController.add()', 'POST', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2024-03-10 17:07:59","ip":"host.mcpeach.cc","nameTag":"生电","params":{},"rconPassword":"ef973c3c23300e242b7257298b4f9d7b","rconPort":25575,"remark":"111111","status":0,"updateBy":"admin","updateTime":"2024-03-10 17:07:59","uuid":"da7b48ca-9f49-420f-95ea-11c412b7e2b1"}',
        null, 1,
        'nested exception is org.apache.ibatis.reflection.ReflectionException: There is no getter for property named \'creatTime\' in \'class com.ruoyi.server.domain.ServerInfo\'',
        '2024-03-10 17:07:59', 3);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (360, '服务器信息', 1, 'com.ruoyi.server.controller.ServerInfoController.add()', 'POST', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2024-03-10 17:08:06","ip":"host.mcpeach.cc","nameTag":"生电","params":{},"rconPassword":"ef973c3c23300e242b7257298b4f9d7b","rconPort":25575,"remark":"111111","status":1,"updateBy":"admin","updateTime":"2024-03-10 17:08:06","uuid":"8d198829-bc18-481a-a3fd-4134eb3a1a81"}',
        null, 1,
        'nested exception is org.apache.ibatis.reflection.ReflectionException: There is no getter for property named \'creatTime\' in \'class com.ruoyi.server.domain.ServerInfo\'',
        '2024-03-10 17:08:06', 2);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (361, '服务器信息', 1, 'com.ruoyi.server.controller.ServerInfoController.add()', 'POST', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2024-03-10 17:08:32","ip":"1.1.1.1","nameTag":"生电","params":{},"rconPassword":"c4ca4238a0b923820dcc509a6f75849b","rconPort":255575,"remark":"1","status":1,"updateBy":"admin","updateTime":"2024-03-10 17:08:32","uuid":"474882e5-7b18-4efb-8674-8a9d2de7c1f4"}',
        null, 1,
        'nested exception is org.apache.ibatis.reflection.ReflectionException: There is no getter for property named \'creatTime\' in \'class com.ruoyi.server.domain.ServerInfo\'',
        '2024-03-10 17:08:32', 2);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (362, '服务器信息', 1, 'com.ruoyi.server.controller.ServerInfoController.add()', 'POST', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2024-03-10 17:08:51","ip":"1","nameTag":"111","params":{},"rconPassword":"c4ca4238a0b923820dcc509a6f75849b","rconPort":1,"remark":"1","status":1,"updateBy":"admin","updateTime":"2024-03-10 17:08:51","uuid":"7f6ae628-5027-450d-89d3-5a7c0505fde4"}',
        null, 1,
        'nested exception is org.apache.ibatis.reflection.ReflectionException: There is no getter for property named \'creatTime\' in \'class com.ruoyi.server.domain.ServerInfo\'',
        '2024-03-10 17:08:51', 2);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (363, '服务器信息', 1, 'com.ruoyi.server.controller.ServerInfoController.add()', 'POST', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2024-03-10 17:09:21","ip":"1","nameTag":"111","params":{},"rconPassword":"c4ca4238a0b923820dcc509a6f75849b","rconPort":1,"remark":"1","status":1,"updateBy":"admin","updateTime":"2024-03-10 17:09:21","uuid":"d3290aa3-e4ec-4235-9078-97e4b51ee40f"}',
        null, 1,
        'nested exception is org.apache.ibatis.reflection.ReflectionException: There is no getter for property named \'creatTime\' in \'class com.ruoyi.server.domain.ServerInfo\'',
        '2024-03-10 17:09:21', 25);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (364, '服务器信息', 3, 'com.ruoyi.server.controller.ServerInfoController.remove()', 'DELETE', 1, 'admin',
        '研发部门', '/server/serverlist/1', '127.0.0.1', '内网IP', '{}', '{"msg":"操作成功","code":200}', 0, null,
        '2024-03-10 17:10:23', 19);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (365, '服务器信息', 1, 'com.ruoyi.server.controller.ServerInfoController.add()', 'POST', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2024-03-10 17:10:51","id":2,"ip":"host.mcpeach.cc","nameTag":"生电","params":{},"rconPassword":"ef973c3c23300e242b7257298b4f9d7b","rconPort":25575,"remark":"test","status":0,"updateBy":"admin","updateTime":"2024-03-10 17:10:51","uuid":"5ca1f4fb-42c3-4a58-8729-4bc589ddefca"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-10 17:10:51', 19);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (366, '字典类型', 1, 'com.ruoyi.web.controller.system.SysDictTypeController.add()', 'POST', 1, 'admin',
        '研发部门', '/system/dict/type', '127.0.0.1', '内网IP',
        '{"createBy":"admin","dictName":"服务器开关状态","dictType":"server_status","params":{},"status":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-10 17:12:54', 16);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (367, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","default":false,"dictLabel":"停用","dictSort":0,"dictType":"server_status","dictValue":"0","listClass":"danger","params":{},"status":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-10 17:13:17', 14);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (368, '字典数据', 1, 'com.ruoyi.web.controller.system.SysDictDataController.add()', 'POST', 1, 'admin',
        '研发部门', '/system/dict/data', '127.0.0.1', '内网IP',
        '{"createBy":"admin","default":false,"dictLabel":"启用","dictSort":0,"dictType":"server_status","dictValue":"1","listClass":"success","params":{},"status":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-10 17:13:36', 14);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (369, '服务器信息', 5, 'com.ruoyi.server.controller.ServerInfoController.export()', 'POST', 1, 'admin',
        '研发部门', '/server/serverlist/export', '127.0.0.1', '内网IP', '{"pageSize":"10","pageNum":"1"}', null, 0,
        null, '2024-03-10 20:52:04', 773);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (370, '服务器信息', 2, 'com.ruoyi.server.controller.ServerInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2024-03-10 17:10:52","id":2,"ip":"host.mcpeach.cc","nameTag":"生电","params":{},"rconPassword":"20021129","rconPort":25575,"remark":"test","status":1,"updateBy":"admin","updateTime":"2024-03-10 23:09:33","uuid":"5ca1f4fb-42c3-4a58-8729-4bc589ddefca"}',
        null, 1,
        'nested exception is org.apache.ibatis.reflection.ReflectionException: There is no getter for property named \'creatTime\' in \'class com.ruoyi.server.domain.ServerInfo\'',
        '2024-03-10 23:09:33', 6);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (371, '服务器信息', 2, 'com.ruoyi.server.controller.ServerInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2024-03-10 17:10:52","id":2,"ip":"host.mcpeach.cc","nameTag":"生电","params":{},"rconPassword":"20021129","rconPort":25575,"remark":"test","status":1,"updateBy":"admin","updateTime":"2024-03-10 23:09:36","uuid":"5ca1f4fb-42c3-4a58-8729-4bc589ddefca"}',
        null, 1,
        'nested exception is org.apache.ibatis.reflection.ReflectionException: There is no getter for property named \'creatTime\' in \'class com.ruoyi.server.domain.ServerInfo\'',
        '2024-03-10 23:09:36', 1);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (372, '服务器信息', 2, 'com.ruoyi.server.controller.ServerInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2024-03-10 17:10:52","id":2,"ip":"host.mcpeach.cc","nameTag":"生电","params":{},"rconPassword":"20021129","rconPort":25575,"remark":"test","status":1,"updateBy":"admin","updateTime":"2024-03-10 23:11:01","uuid":"5ca1f4fb-42c3-4a58-8729-4bc589ddefca"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-10 23:11:04', 2679);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (373, '服务器信息', 2, 'com.ruoyi.server.controller.ServerInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2024-03-10 17:10:52","id":2,"ip":"host.mcpeach.cc","nameTag":"生电","params":{},"rconPassword":"20021129","rconPort":25575,"remark":"test","status":0,"updateBy":"admin","updateTime":"2024-03-10 23:20:26","uuid":"5ca1f4fb-42c3-4a58-8729-4bc589ddefca"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-10 23:20:26', 15);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (374, '服务器信息', 2, 'com.ruoyi.server.controller.ServerInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2024-03-10 17:10:52","id":2,"ip":"host.mcpeach.cc","nameTag":"生电","params":{},"rconPassword":"20021129","rconPort":25575,"remark":"test","status":1,"updateBy":"admin","updateTime":"2024-03-10 23:20:43","uuid":"5ca1f4fb-42c3-4a58-8729-4bc589ddefca"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-10 23:20:43', 5);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (375, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"1","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2024-01-15","reviewUsers":"admin","status":"1","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 14:10:39","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 14:10:41', 1764);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (376, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"1","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2024-01-15","reviewUsers":"admin","status":"1","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 14:13:39","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 14:13:40', 1003);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (377, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"1","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2024-01-15","reviewUsers":"admin","servers":"2","status":"1","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 14:14:56","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 14:14:57', 1029);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (378, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"1","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2024-01-15","reviewUsers":"admin","servers":"all,2","status":"1","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 14:17:45","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 14:17:46', 1152);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (379, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"1","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2024-01-15","reviewUsers":"admin","servers":"","status":"1","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 14:43:37","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 14:43:39', 1212);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (380, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"1","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2024-01-15","reviewUsers":"admin","servers":"","status":"1","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 14:45:37","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 14:45:38', 989);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (381, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"1","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"66666","remark":"","removeReason":"","removeTime":"2024-01-15","reviewUsers":"admin","servers":"all,2","status":"1","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 14:47:21","userName":"GoodGame","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 14:47:22', 1076);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (382, '服务器信息', 1, 'com.ruoyi.server.controller.ServerInfoController.add()', 'POST', 1, 'admin', '研发部门',
        '/server/serverlist', '127.0.0.1', '内网IP',
        '{"createBy":"admin","createTime":"2024-03-11 16:45:16","id":3,"ip":"host.mcpeach.cc","nameTag":"BCG","params":{},"rconPassword":"20021129","rconPort":6667,"remark":"BCGGGGGG","status":1,"updateBy":"admin","updateTime":"2024-03-11 16:45:16","uuid":"c9a0dc45-9e7d-4d63-ae9d-69ca3dedc895"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 16:45:16', 38);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (383, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"1","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"2873336923","remark":"","removeReason":"","removeTime":"2024-01-15","reviewUsers":"admin","servers":"all,2,3","status":"1","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 16:52:36","userName":"im_rbq","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 16:52:37', 1690);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (384, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"2","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"2873336923","remark":"","removeReason":"","removeTime":"2024-03-11","reviewUsers":"admin","servers":"all,2,3","status":"0","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 16:54:23","userName":"im_rbq","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 16:54:24', 976);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (385, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"1","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"2873336923","remark":"","removeReason":"","removeTime":"2024-03-11","reviewUsers":"admin","servers":"all,2,3","status":"1","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 16:57:07","userName":"im_rbq","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 16:57:08', 35);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (386, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"2","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"2873336923","remark":"","removeReason":"test","removeTime":"2024-03-11","reviewUsers":"admin","servers":"all,2,3","status":"0","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 17:02:31","userName":"im_rbq","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 17:02:32', 30);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (387, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"1","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"2873336923","remark":"","removeReason":"test","removeTime":"2024-03-11","reviewUsers":"admin","servers":"2","status":"1","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 17:03:39","userName":"im_rbq","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 17:03:39', 9);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (388, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"1","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"2873336923","remark":"","removeReason":"test","removeTime":"2024-03-11","reviewUsers":"admin","servers":"all,2,3","status":"1","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 17:09:45","userName":"im_rbq","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 17:09:45', 29);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (389, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"1","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"2873336923","remark":"","removeReason":"test","removeTime":"2024-03-11","reviewUsers":"admin","servers":"all,2,3","status":"1","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 19:14:14","userName":"im_rbq","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 19:14:14', 29);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (390, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"1","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"2873336923","remark":"","removeReason":"test","removeTime":"2024-03-11","reviewUsers":"admin","servers":"all,2,3","status":"1","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 19:18:03","userName":"im_rbq","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 19:18:03', 22);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (391, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"1","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"2873336923","remark":"","removeReason":"test","removeTime":"2024-03-11","reviewUsers":"admin","servers":"all,2,3","status":"1","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 19:19:14","userName":"im_rbq","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 19:19:14', 14);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (392, '白名单', 2, 'com.ruoyi.server.controller.WhitelistInfoController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/mc/whitelist', '127.0.0.1', '内网IP',
        '{"addState":"1","addTime":"2024-03-11","id":9,"onlineFlag":0,"params":{},"qqNum":"2873336923","remark":"","removeReason":"test","removeTime":"2024-03-11","reviewUsers":"admin","servers":"3","status":"1","time":"2023-12-28","updateBy":"admin","updateTime":"2024-03-11 19:20:05","userName":"im_rbq","userUuid":"601288f2-3729-4a95-a92d-fb534f54cb63"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-03-11 19:20:05', 8);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (393, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin',
        '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{"tables":"ip_limit_info"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-12-20 19:11:37', 131);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (394, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin',
        '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"ip_limit_info"}', null, 0, null,
        '2024-12-20 19:11:39', 379);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (395, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"component":"system/info/index","createTime":"2024-12-20 19:12:10","icon":"number","isCache":"0","isFrame":"1","menuId":2027,"menuName":"IP限流","menuType":"C","orderNum":1,"params":{},"parentId":2000,"path":"info","perms":"system:info:list","status":"0","updateBy":"admin","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-12-20 19:14:23', 12);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (396, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin',
        '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{"tables":"banlist_info"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-12-20 19:15:07', 40);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (397, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门',
        '/tool/gen', '127.0.0.1', '内网IP',
        '{"businessName":"limit","className":"IpLimitInfo","columns":[{"capJavaField":"Id","columnComment":"主键ID","columnId":39,"columnName":"id","columnType":"bigint(20)","createBy":"admin","createTime":"2024-12-20 19:11:37","dictType":"","edit":false,"htmlType":"input","increment":true,"insert":true,"isIncrement":"1","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"Long","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":4,"updateBy":"","usableColumn":false},{"capJavaField":"Uuid","columnComment":"随机UUID","columnId":40,"columnName":"uuid","columnType":"varchar(255)","createBy":"admin","createTime":"2024-12-20 19:11:37","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"uuid","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":4,"updateBy":"","usableColumn":false},{"capJavaField":"Ip","columnComment":"IP地址","columnId":41,"columnName":"ip","columnType":"varchar(255)","createBy":"admin","createTime":"2024-12-20 19:11:37","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"ip","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":4,"updateBy":"","usableColumn":false},{"capJavaField":"UserAgent","columnComment":"UA标识","columnId":42,"columnName":"user_agent","columnType":"varchar(255)","createBy":"admin","createTime":"2024-12-20 19:11:37","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"userAgent","ja',
        '{"msg":"操作成功","code":200}', 0, null, '2024-12-20 19:24:18', 39);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (398, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin',
        '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"ip_limit_info"}', null, 0, null,
        '2024-12-20 19:24:21', 63);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (399, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"component":"ipinfo/limit/index","createTime":"2024-12-20 19:25:20","icon":"number","isCache":"0","isFrame":"1","menuId":2033,"menuName":"IP限流","menuType":"C","orderNum":1,"params":{},"parentId":2000,"path":"limit","perms":"ipinfo:limit:list","status":"0","updateBy":"admin","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-12-20 19:25:56', 7);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (400, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门',
        '/tool/gen', '127.0.0.1', '内网IP',
        '{"businessName":"banlist","className":"BanlistInfo","columns":[{"capJavaField":"Id","columnComment":"主键ID","columnId":55,"columnName":"id","columnType":"bigint(20)","createBy":"admin","createTime":"2024-12-20 19:15:07","dictType":"","edit":false,"htmlType":"input","increment":true,"insert":true,"isIncrement":"1","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"Long","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":5,"updateBy":"","usableColumn":false},{"capJavaField":"WhiteId","columnComment":"关联白名单ID","columnId":56,"columnName":"white_id","columnType":"bigint(20)","createBy":"admin","createTime":"2024-12-20 19:15:07","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"whiteId","javaType":"Long","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":5,"updateBy":"","usableColumn":false},{"capJavaField":"UserName","columnComment":"用户名称","columnId":57,"columnName":"user_name","columnType":"varchar(255)","createBy":"admin","createTime":"2024-12-20 19:15:07","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"userName","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"LIKE","required":false,"sort":3,"superColumn":false,"tableId":5,"updateBy":"","usableColumn":false},{"capJavaField":"State","columnComment":"封禁状态","columnId":58,"columnName":"state","columnType":"bigint(20)","createBy":"admin","createTime":"2024-12-20 19:15:07","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaFie',
        '{"msg":"操作成功","code":200}', 0, null, '2024-12-20 19:26:41', 34);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (401, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin',
        '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"banlist_info"}', null, 0, null,
        '2024-12-20 19:26:43', 50);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (402, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"component":"mc/banlist/index","createTime":"2024-12-20 19:26:57","icon":"lock","isCache":"0","isFrame":"1","menuId":2039,"menuName":"封禁管理","menuType":"C","orderNum":1,"params":{},"parentId":2000,"path":"banlist","perms":"mc:banlist:list","status":"0","updateBy":"admin","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-12-20 19:28:40', 8);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (403, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin',
        '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{"tables":"server_command_info"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-12-20 19:28:58', 45);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (404, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门',
        '/tool/gen', '127.0.0.1', '内网IP',
        '{"businessName":"command","className":"ServerCommandInfo","columns":[{"capJavaField":"Id","columnComment":"主键ID","columnId":65,"columnName":"id","columnType":"bigint(20)","createBy":"admin","createTime":"2024-12-20 19:28:58","dictType":"","edit":false,"htmlType":"input","increment":true,"insert":true,"isIncrement":"1","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"Long","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":6,"updateBy":"","usableColumn":false},{"capJavaField":"ServerId","columnComment":"服务器ID","columnId":66,"columnName":"server_id","columnType":"varchar(255)","createBy":"admin","createTime":"2024-12-20 19:28:58","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"serverId","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":6,"updateBy":"","usableColumn":false},{"capJavaField":"OnlineAddWhitelistCommand","columnComment":"在线模式白名单添加指令","columnId":67,"columnName":"online_add_whitelist_command","columnType":"varchar(255)","createBy":"admin","createTime":"2024-12-20 19:28:58","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"onlineAddWhitelistCommand","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":6,"updateBy":"","usableColumn":false},{"capJavaField":"OfflineAddWhitelistCommand","columnComment":"离线模式白名单添加指令","columnId":68,"columnName":"offline_add_whitelist_command","columnType":"varchar(255)","createBy":"admin","createTime":"2024-12-20 19:28:58","dictType":"","edit":true,"htmlType":"input","increment":false,',
        '{"msg":"操作成功","code":200}', 0, null, '2024-12-20 19:29:20', 42);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (405, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门',
        '/tool/gen', '127.0.0.1', '内网IP',
        '{"businessName":"command","className":"ServerCommandInfo","columns":[{"capJavaField":"Id","columnComment":"主键ID","columnId":65,"columnName":"id","columnType":"bigint(20)","createBy":"admin","createTime":"2024-12-20 19:28:58","dictType":"","edit":false,"htmlType":"input","increment":true,"insert":true,"isIncrement":"1","isInsert":"1","isPk":"1","isRequired":"0","javaField":"id","javaType":"Long","list":false,"params":{},"pk":true,"query":false,"queryType":"EQ","required":false,"sort":1,"superColumn":false,"tableId":6,"updateBy":"","updateTime":"2024-12-20 19:29:20","usableColumn":false},{"capJavaField":"ServerId","columnComment":"服务器ID","columnId":66,"columnName":"server_id","columnType":"varchar(255)","createBy":"admin","createTime":"2024-12-20 19:28:58","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"serverId","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":2,"superColumn":false,"tableId":6,"updateBy":"","updateTime":"2024-12-20 19:29:20","usableColumn":false},{"capJavaField":"OnlineAddWhitelistCommand","columnComment":"在线模式白名单添加指令","columnId":67,"columnName":"online_add_whitelist_command","columnType":"varchar(255)","createBy":"admin","createTime":"2024-12-20 19:28:58","dictType":"","edit":true,"htmlType":"input","increment":false,"insert":true,"isEdit":"1","isIncrement":"0","isInsert":"1","isList":"1","isPk":"0","isQuery":"1","isRequired":"0","javaField":"onlineAddWhitelistCommand","javaType":"String","list":true,"params":{},"pk":false,"query":true,"queryType":"EQ","required":false,"sort":3,"superColumn":false,"tableId":6,"updateBy":"","updateTime":"2024-12-20 19:29:20","usableColumn":false},{"capJavaField":"OfflineAddWhitelistCommand","columnComment":"离线模式白名单添加指令","columnId":68,"columnName":"offline_add_whitelist_command","columnType":"varchar(255)","createBy":"',
        '{"msg":"操作成功","code":200}', 0, null, '2024-12-20 19:29:31', 34);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (406, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin',
        '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{"tables":"server_command_info"}', null, 0, null,
        '2024-12-20 19:29:33', 62);
INSERT INTO ruoyi.sys_oper_log (oper_id, title, business_type, method, request_method, operator_type, oper_name,
                                dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg,
                                oper_time, cost_time)
VALUES (407, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门',
        '/system/menu', '127.0.0.1', '内网IP',
        '{"children":[],"component":"mc/command/index","createTime":"2024-12-20 19:29:44","icon":"code","isCache":"0","isFrame":"1","menuId":2045,"menuName":"指令管理","menuType":"C","orderNum":1,"params":{},"parentId":2000,"path":"command","perms":"mc:command:list","status":"0","updateBy":"admin","visible":"0"}',
        '{"msg":"操作成功","code":200}', 0, null, '2024-12-20 19:30:35', 5);
