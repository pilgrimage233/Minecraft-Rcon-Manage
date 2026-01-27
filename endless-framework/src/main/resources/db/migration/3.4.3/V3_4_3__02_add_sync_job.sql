-- 获取最新的群组成员ID定时任务ID
SET @max_job_id = (SELECT IFNULL(MAX(job_id), 0)
                   FROM sys_job);

INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, remark)
VALUES (@max_job_id, '设置群组成员ID', 'DEFAULT', 'botTask.synchronizeGroupMembersId', '0 0/1 * * * ?', '1', '0', '0',
        'admin',
        now(), '将通过白名单的用户群名片修改为【id】+ 原有昵称');
