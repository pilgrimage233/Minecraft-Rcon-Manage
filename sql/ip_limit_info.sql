create table ip_limit_info
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    uuid        varchar(255) null comment '随机UUID',
    ip          varchar(255) null comment 'IP地址',
    user_agent  varchar(255) null comment 'UA标识',
    count       bigint       null comment '请求次数',
    province    varchar(255) null comment '省',
    city        varchar(255) null comment '地市',
    county      varchar(255) null comment '区县',
    longitude   varchar(255) null comment '经度',
    latitude    varchar(255) null comment '纬度',
    body_params text         null comment '请求参数',
    remark      varchar(255) null comment '备注',
    create_time datetime     null comment '创建时间',
    update_time datetime     null comment '更新时间',
    create_by   varchar(255) null comment '创建者',
    update_by   varchar(255) null comment '更新者'
)
    comment 'IP限流表';

