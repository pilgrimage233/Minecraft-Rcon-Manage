create table sys_dict_data
(
    dict_code   bigint auto_increment comment '字典编码'
        primary key,
    dict_sort   int          default 0  null comment '字典排序',
    dict_label  varchar(100) default '' null comment '字典标签',
    dict_value  varchar(100) default '' null comment '字典键值',
    dict_type   varchar(100) default '' null comment '字典类型',
    css_class   varchar(100)            null comment '样式属性（其他样式扩展）',
    list_class  varchar(100)            null comment '表格回显样式',
    is_default  char         default 'N' null comment '是否默认（Y是 N否）',
    status      char         default '0' null comment '状态（0正常 1停用）',
    create_by   varchar(64)  default '' null comment '创建者',
    create_time datetime                null comment '创建时间',
    update_by   varchar(64)  default '' null comment '更新者',
    update_time datetime                null comment '更新时间',
    remark      varchar(500)            null comment '备注'
)
    comment '字典数据表';

INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null, '性别男');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null, '性别女');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null, '性别未知');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '显示菜单');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '隐藏菜单');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '正常状态');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '停用状态');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '正常状态');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '停用状态');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '默认分组');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '系统分组');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '系统默认是');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null, '系统默认否');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '通知');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '公告');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '正常状态');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '关闭状态');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '其他操作');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '新增操作');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '修改操作');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '删除操作');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '授权操作');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '导出操作');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '导入操作');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '强退操作');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '生成操作');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '清空操作');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '正常状态');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '停用状态');
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (100, 1, '待审核', '0', 'white_examine_status', null, 'warning', 'N', '0', 'admin', '2023-12-27 18:07:29',
        'admin', '2023-12-27 18:27:23', null);
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (101, 2, '已过审', '1', 'white_examine_status', null, 'success', 'N', '0', 'admin', '2023-12-27 18:07:47',
        'admin', '2023-12-27 18:27:26', null);
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (102, 3, '已拒审', '2', 'white_examine_status', null, 'danger', 'N', '0', 'admin', '2023-12-27 18:08:15',
        'admin', '2023-12-27 18:27:30', null);
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (103, 1, '未添加', '0', 'white_add_status', null, 'info', 'N', '0', 'admin', '2023-12-27 18:40:35', 'admin',
        '2023-12-27 18:40:43', null);
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (104, 2, '已添加', '1', 'white_add_status', null, 'success', 'N', '0', 'admin', '2023-12-27 18:41:05', 'admin',
        '2024-01-14 13:38:09', null);
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (105, 3, '已删除', '2', 'white_add_status', null, 'warning', 'N', '0', 'admin', '2023-12-27 18:41:20', 'admin',
        '2023-12-27 18:41:44', null);
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (106, 4, '已封禁', '9', 'white_add_status', null, 'danger', 'N', '0', 'admin', '2023-12-27 18:42:07', 'admin',
        '2023-12-27 19:32:11', null);
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (107, 1, '正版', '1', 'online_status', null, 'primary', 'N', '0', 'admin', '2023-12-27 15:28:43', 'admin',
        '2023-12-27 15:29:00', null);
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (108, 2, '离线', '0', 'online_status', null, 'info', 'N', '0', 'admin', '2023-12-27 15:29:13', '', null, null);
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (109, 0, '停用', '0', 'server_status', null, 'danger', 'N', '0', 'admin', '2024-03-10 17:13:17', '', null, null);
INSERT INTO ruoyi.sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                                 is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (110, 0, '启用', '1', 'server_status', null, 'success', 'N', '0', 'admin', '2024-03-10 17:13:36', '', null,
        null);
