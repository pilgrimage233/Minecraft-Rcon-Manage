create table server_info
(
    id            int auto_increment comment '主键ID'
        primary key,
    uuid          varchar(64)  not null comment '随机UUID',
    name_tag      varchar(128) not null comment '服务器名称标签',
    ip            varchar(128) not null comment '服务器IP',
    rcon_port     int          not null comment 'RCON远程端口号',
    rcon_password varchar(256) not null comment '远程密码/MD5加密',
    create_time datetime null comment '创建时间',
    create_by     varchar(128) null comment '创建者',
    update_time datetime null comment '更新时间',
    update_by     varchar(128) null comment '更新者',
    status        int          not null comment '启用状态',
    remark      text     null comment '描述'
)
    comment '服务器信息' collate = utf8mb4_bin;

create index server_info_id_index
    on server_info (id);

create index server_info_name_tag_index
    on server_info (name_tag);

