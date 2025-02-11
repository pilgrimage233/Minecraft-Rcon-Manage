create table history_command
(
    id           int auto_increment comment '主键ID'
        primary key,
    server_id    int                                not null comment '服务器ID',
    user         varchar(64)                        not null comment '执行用户',
    command      varchar(1000)                      not null comment '执行指令',
    execute_time datetime default CURRENT_TIMESTAMP not null comment '执行时间',
    response     text                               null comment '执行结果',
    status       varchar(8)                         null comment '执行状态',
    run_time     varchar(64)                        null comment '运行时间(毫秒值)'
)
    comment '历史命令';

create index history_command_server_id_index
    on history_command (server_id);