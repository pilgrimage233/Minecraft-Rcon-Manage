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
VALUES (4, '同步白名单', 'DEFAULT', 'whiteListTask.syncWhitelistByServerId(\'1\')', '30 1 0/1 * * ?', '1', '1', '0',
        'admin', '2024-12-21 18:56:29', 'admin', '2025-01-03 02:25:32', '');
INSERT INTO ruoyi.sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES (5, '刷新Rcon缓存', 'DEFAULT', 'rconTask.refreshMapCache', '10 1 0/1 * * ?', '1', '1', '0', 'admin',
        '2024-12-21 18:58:15', 'admin', '2025-01-03 02:26:12', '');
INSERT INTO ruoyi.sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES (6, '同步ID', 'DEFAULT', 'onlineTask.syncUserNameForUuid', '0 0 0 1/1 * ?', '1', '1', '0', 'admin',
        '2024-12-21 19:00:00', '', '2024-12-21 19:00:03', '');
INSERT INTO ruoyi.sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES (7, '重建redis缓存', 'DEFAULT', 'rconTask.refreshRedisCache', '0 0 1 1/2 * ?', '1', '1', '0', 'admin',
        '2024-12-23 16:34:41', '', '2024-12-23 16:34:43', '');
INSERT INTO ruoyi.sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES (8, '心跳检测', 'DEFAULT', 'rconTask.heartBeat', '0 0/3 * * * ?', '1', '1', '1', 'admin', '2024-12-25 23:41:20',
        'admin', '2025-01-03 02:26:19', '');
INSERT INTO ruoyi.sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES (9, '玩家监控', 'DEFAULT', 'onlineTask.monitor', '0 0/1 * * * ?', '1', '1', '0', 'admin', '2025-01-02 05:14:41',
        '', '2025-01-02 22:56:40', '');
