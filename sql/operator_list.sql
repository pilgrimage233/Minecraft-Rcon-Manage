create table operator_list
(
    id          int auto_increment
        primary key,
    user_name   varchar(128) not null comment '玩家昵称',
    uuid        varchar(64)  not null,
    status      int          null comment '状态',
    parameter   varchar(256) null comment '其他参数',
    create_time datetime     null comment '创建时间',
    update_time datetime     null comment '更新时间',
    create_by   varchar(128) null comment '创建者',
    update_by   varchar(128) null comment '更新者',
    remark      varchar(128) null comment '备注'
)
    comment '管理员列表' collate = utf8_german2_ci;

create index operator_list_id_index
    on operator_list (id);

create index operator_list_user_name_index
    on operator_list (user_name);

