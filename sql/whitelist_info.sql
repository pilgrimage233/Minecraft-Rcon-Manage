create table whitelist_info
(
    id            int auto_increment comment '主键ID'
        primary key,
    time        datetime    null comment '申请时间',
    user_name     varchar(256) null comment '游戏名称',
    user_uuid     varchar(256) null,
    online_flag int         null comment '正版标识',
    qq_num      varchar(64) null comment '用户QQ号',
    remark        varchar(256) null comment '描述',
    review_users  varchar(256) null comment '审核用户',
    status      varchar(32) null comment '审核状态',
    add_state   varchar(64) null comment '添加状态',
    add_time    datetime    null comment '添加时间',
    remove_reason varchar(256) null comment '移除原因',
    remove_time datetime    null comment '移除时间',
    create_by     varchar(128) null comment '创建人',
    update_by     varchar(128) null comment '更新者',
    create_time datetime    null comment '创建时间',
    update_time datetime    null comment '更新时间',
    servers       varchar(256) null
)
    collate = utf8_bin;

create index whitelist_info_qq_num_index
    on whitelist_info (qq_num);

create index whitelist_info_user_name_index
    on whitelist_info (user_name);

