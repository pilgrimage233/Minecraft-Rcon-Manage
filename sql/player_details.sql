create table player_details
(
    id                int auto_increment
        primary key,
    user_name         varchar(128) not null comment '玩家昵称',
    qq                varchar(64)  null comment 'QQ号',
    identity          varchar(64)  not null comment '身份',
    last_online_time  datetime     null comment '最后上线时间',
    last_offline_time datetime     null comment '最后离线时间',
    province          varchar(128) null comment '省份',
    city              varchar(128) null comment '地市',
    whitelist_id      int          null comment '白名单ID',
    banlist_id        int          null comment '封禁ID',
    parameters        varchar(256) null comment '其他参数',
    create_time       datetime     not null comment '创建时间',
    update_time       datetime     null comment '更新时间',
    create_by         varchar(64)  not null comment '创建者',
    update_by         varchar(64)  null comment '更新者',
    remark            varchar(256) null comment '备注'
)
    comment '玩家信息' charset = utf8;

create index player_details_id_whitelist_id_index
    on player_details (id, whitelist_id);
