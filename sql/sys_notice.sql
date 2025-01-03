create table sys_notice
(
    notice_id      int auto_increment comment '公告ID'
        primary key,
    notice_title   varchar(50)            not null comment '公告标题',
    notice_type    char                   not null comment '公告类型（1通知 2公告）',
    notice_content longblob               null comment '公告内容',
    status         char        default '0' null comment '公告状态（0正常 1关闭）',
    create_by      varchar(64) default '' null comment '创建者',
    create_time    datetime               null comment '创建时间',
    update_by      varchar(64) default '' null comment '更新者',
    update_time    datetime               null comment '更新时间',
    remark         varchar(255)           null comment '备注'
)
    comment '通知公告表';
