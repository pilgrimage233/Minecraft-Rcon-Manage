create table banlist_info
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    white_id    bigint       null comment '关联白名单ID',
    user_name   varchar(255) null comment '用户名称',
    state       bigint       null comment '封禁状态',
    reason      varchar(255) null comment '封禁原因',
    remark      varchar(255) null comment '备注',
    create_time datetime     null comment '创建时间',
    update_time datetime     null comment '更新时间',
    create_by   varchar(255) null comment '创建者',
    update_by   varchar(255) null comment '更新者'
)
    comment '封禁管理表';

