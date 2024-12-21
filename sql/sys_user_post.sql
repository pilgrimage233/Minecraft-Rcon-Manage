create table sys_user_post
(
    user_id bigint not null comment '用户ID',
    post_id bigint not null comment '岗位ID',
    primary key (user_id, post_id)
)
    comment '用户与岗位关联表';

INSERT INTO ruoyi.sys_user_post (user_id, post_id)
VALUES (1, 1);
INSERT INTO ruoyi.sys_user_post (user_id, post_id)
VALUES (2, 2);
INSERT INTO ruoyi.sys_user_post (user_id, post_id)
VALUES (3, 1);
INSERT INTO ruoyi.sys_user_post (user_id, post_id)
VALUES (5, 4);
