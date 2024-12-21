create table sys_job
(
    job_id          bigint auto_increment comment '任务ID',
    job_name        varchar(64)  default ''        not null comment '任务名称',
    job_group       varchar(64)  default 'DEFAULT' not null comment '任务组名',
    invoke_target   varchar(500)                   not null comment '调用目标字符串',
    cron_expression varchar(255) default ''  null comment 'cron执行表达式',
    misfire_policy  varchar(20)  default '3' null comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
    concurrent      char         default '1' null comment '是否并发执行（0允许 1禁止）',
    status          char         default '0' null comment '状态（0正常 1暂停）',
    create_by       varchar(64)  default ''  null comment '创建者',
    create_time     datetime                 null comment '创建时间',
    update_by       varchar(64)  default ''  null comment '更新者',
    update_time     datetime                 null comment '更新时间',
    remark          varchar(500) default ''  null comment '备注信息',
    primary key (job_id, job_name, job_group)
)
    comment '定时任务调度表';

INSERT INTO ruoyi.sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES (1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams', '0/10 * * * * ?', '3', '1', '1', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES (2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')', '0/15 * * * * ?', '3', '1', '1', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO ruoyi.sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES (3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)', '0/20 * * * * ?',
        '3', '1', '1', 'admin', '2023-12-26 16:54:02', '', null, '');
