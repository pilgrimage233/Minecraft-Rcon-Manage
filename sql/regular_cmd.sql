create table regular_cmd
(
    id             int auto_increment comment '主键'
        primary key,
    task_name      varchar(128) not null comment '任务名称',
    task_id        varchar(64)  null comment '任务id',
    cmd            varchar(256) not null comment '指令',
    execute_server varchar(64)  null comment '执行服务器',
    result         varchar(256) null comment '执行结果',
    history_count  int          null comment '执行历史保留次数',
    history_result text         null comment '历史结果',
    cron           varchar(64)  not null comment 'Cron表达式',
    status         int          not null comment '状态',
    execute_count  int          null comment '执行次数',
    create_time    datetime     null comment '创建时间',
    update_time    datetime     null comment '更新时间',
    create_by      varchar(128) null comment '创建者',
    update_by      varchar(128) null comment '更新者',
    remark         varchar(256) null comment '备注'
)
    comment '定时命令';
