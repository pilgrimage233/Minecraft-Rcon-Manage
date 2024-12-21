create table server_command_info
(
    id                            bigint auto_increment comment '主键ID'
        primary key,
    server_id                     varchar(255) null comment '服务器ID',
    online_add_whitelist_command  varchar(255) null comment '在线模式白名单添加指令',
    offline_add_whitelist_command varchar(255) null comment '离线模式白名单添加指令',
    online_rm_whitelist_command   varchar(255) null comment '在线模式白名单移除指令',
    offline_rm_whitelist_command  varchar(255) null comment '离线模式白名单移除指令',
    online_add_ban_command        varchar(255) null comment '在线模式添加封禁指令',
    offline_add_ban_command       varchar(255) null comment '离线模式添加封禁指令',
    online_rm_ban_command         varchar(255) null comment '在线模式移除封禁指令',
    offline_rm_ban_command        varchar(255) null comment '离线模式移除封禁指令',
    easyauth                      varchar(255) null comment '是否启用EasyAuthMod',
    remark                        varchar(255) null comment '备注',
    create_time                   datetime     null comment '创建时间',
    update_time                   datetime     null comment '更新时间',
    create_by                     varchar(255) null comment '创建者',
    update_by                     varchar(255) null comment '更新者'
)
    comment '指令管理表';

