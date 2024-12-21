create table sys_post
(
    post_id     bigint auto_increment comment '岗位ID'
        primary key,
    post_code   varchar(64)  not null comment '岗位编码',
    post_name   varchar(50)  not null comment '岗位名称',
    post_sort   int          not null comment '显示顺序',
    status      char         not null comment '状态（0正常 1停用）',
    create_by   varchar(64) default '' null comment '创建者',
    create_time datetime     null comment '创建时间',
    update_by   varchar(64) default '' null comment '更新者',
    update_time datetime     null comment '更新时间',
    remark      varchar(500) null comment '备注'
)
    comment '岗位信息表';

INSERT INTO ruoyi.sys_post (post_id, post_code, post_name, post_sort, status, create_by, create_time, update_by,
                            update_time, remark)
VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_post (post_id, post_code, post_name, post_sort, status, create_by, create_time, update_by,
                            update_time, remark)
VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_post (post_id, post_code, post_name, post_sort, status, create_by, create_time, update_by,
                            update_time, remark)
VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_post (post_id, post_code, post_name, post_sort, status, create_by, create_time, update_by,
                            update_time, remark)
VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2023-12-26 16:54:02', '', null, '');
