create table public_server_command
(
    id             bigint auto_increment comment '主键ID'
        primary key,
    server_id      bigint       null comment '服务器ID',
    command        varchar(255) null comment '指令',
    status         bigint       null comment '启用状态',
    vague_matching bigint       null comment '模糊匹配',
    remark         varchar(255) null comment '备注',
    create_time    datetime     null comment '创建时间',
    update_time    datetime     null comment '更新时间',
    create_by      varchar(255) null comment '创建者',
    update_by      varchar(255) null comment '更新者'
)
    comment '公开命令表';

