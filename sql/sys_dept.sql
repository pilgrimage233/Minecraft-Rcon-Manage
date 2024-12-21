create table sys_dept
(
    dept_id     bigint auto_increment comment '部门id'
        primary key,
    parent_id   bigint      default 0  null comment '父部门id',
    ancestors   varchar(50) default '' null comment '祖级列表',
    dept_name   varchar(30) default '' null comment '部门名称',
    order_num   int         default 0  null comment '显示顺序',
    leader      varchar(20)            null comment '负责人',
    phone       varchar(11)            null comment '联系电话',
    email       varchar(50)            null comment '邮箱',
    status      char        default '0' null comment '部门状态（0正常 1停用）',
    del_flag    char        default '0' null comment '删除标志（0代表存在 2代表删除）',
    create_by   varchar(64) default '' null comment '创建者',
    create_time datetime               null comment '创建时间',
    update_by   varchar(64) default '' null comment '更新者',
    update_time datetime               null comment '更新时间'
)
    comment '部门表';

INSERT INTO ruoyi.sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag,
                            create_by, create_time, update_by, update_time)
VALUES (100, 0, '0', '若依科技', 0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2023-12-26 16:54:02', '',
        null);
INSERT INTO ruoyi.sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag,
                            create_by, create_time, update_by, update_time)
VALUES (101, 100, '0,100', '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', null);
INSERT INTO ruoyi.sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag,
                            create_by, create_time, update_by, update_time)
VALUES (102, 100, '0,100', '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', null);
INSERT INTO ruoyi.sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag,
                            create_by, create_time, update_by, update_time)
VALUES (103, 101, '0,100,101', '研发部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', null);
INSERT INTO ruoyi.sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag,
                            create_by, create_time, update_by, update_time)
VALUES (104, 101, '0,100,101', '市场部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', null);
INSERT INTO ruoyi.sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag,
                            create_by, create_time, update_by, update_time)
VALUES (105, 101, '0,100,101', '测试部门', 3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', null);
INSERT INTO ruoyi.sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag,
                            create_by, create_time, update_by, update_time)
VALUES (106, 101, '0,100,101', '财务部门', 4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', null);
INSERT INTO ruoyi.sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag,
                            create_by, create_time, update_by, update_time)
VALUES (107, 101, '0,100,101', '运维部门', 5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', null);
INSERT INTO ruoyi.sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag,
                            create_by, create_time, update_by, update_time)
VALUES (108, 102, '0,100,102', '市场部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', null);
INSERT INTO ruoyi.sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag,
                            create_by, create_time, update_by, update_time)
VALUES (109, 102, '0,100,102', '财务部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', null);
