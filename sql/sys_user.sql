create table sys_user
(
    user_id     bigint auto_increment comment '用户ID'
        primary key,
    dept_id     bigint                   null comment '部门ID',
    user_name   varchar(30)              not null comment '用户账号',
    nick_name   varchar(30)              not null comment '用户昵称',
    user_type   varchar(2)   default '00' null comment '用户类型（00系统用户）',
    email       varchar(50)  default ''  null comment '用户邮箱',
    phonenumber varchar(11)  default ''  null comment '手机号码',
    sex         char         default '0' null comment '用户性别（0男 1女 2未知）',
    avatar      varchar(100) default ''  null comment '头像地址',
    password    varchar(100) default ''  null comment '密码',
    status      char         default '0' null comment '帐号状态（0正常 1停用）',
    del_flag    char         default '0' null comment '删除标志（0代表存在 2代表删除）',
    login_ip    varchar(128) default ''  null comment '最后登录IP',
    login_date  datetime                 null comment '最后登录时间',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500)             null comment '备注'
)
    comment '用户信息表';

insert into sys_user
values (1, 103, 'admin', '测试', '00', 'admin@mcpeach.cc', '15888888888', '1', '',
        '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), 'admin',
        sysdate(), '', null, '管理员');