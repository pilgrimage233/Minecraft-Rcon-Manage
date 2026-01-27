-- 添加白名单ID字段及索引到测验提交表
alter table whitelist_quiz_submission
    add whitelist_id int null comment '白名单ID' after id;

create index idx_whitelist_id
    on whitelist_quiz_submission (whitelist_id desc);


SET @max_id = (SELECT IFNULL(MAX(id), 0)
               FROM whitelist_quiz_config);

-- 插入新的测验配置项
INSERT INTO whitelist_quiz_config (id, config_key, config_value, description, remark, create_by, create_time,
                                   del_flag)
VALUES (@max_id + 1, 'auto_remove_from_group_after_inactive_days', '0', '长时间未答卷自动踢出群聊天数(设置0为禁用)',
        null, '',
        now(), '0');
INSERT INTO whitelist_quiz_config (id, config_key, config_value, description, remark, create_by, create_time,
                                   del_flag)
VALUES (@max_id + 2, 'require_quiz_after_long_term_inactive_days', '60', '长期未登录要求重新答卷天数(设置0为禁用)',
        null, '',
        now(), '0');


-- 获取最新的群组成员ID定时任务ID
SET @max_job_id = (SELECT IFNULL(MAX(job_id), 0)
                   FROM sys_job);

INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, remark)
VALUES (@max_job_id, '设置群组成员ID', 'DEFAULT', 'botTask.synchronizeGroupMembersId', '0 0/1 * * * ?', '1', '0', '0',
        'admin',
        now(), '将通过白名单的用户群名片修改为【id】+ 原有昵称');


-- 更新现有的测验提交记录，关联对应的白名单ID
-- 通过玩家UUID匹配白名单记录
UPDATE whitelist_quiz_submission wqs
    INNER JOIN whitelist_info wi ON wqs.player_uuid = wi.user_uuid
SET wqs.whitelist_id = wi.id
WHERE wqs.whitelist_id IS NULL
  AND wi.user_uuid IS NOT NULL
  AND wi.user_uuid != '';

-- 对于无法通过UUID匹配的记录，尝试通过玩家名称匹配
UPDATE whitelist_quiz_submission wqs
    INNER JOIN whitelist_info wi ON wqs.player_name = wi.user_name
SET wqs.whitelist_id = wi.id
WHERE wqs.whitelist_id IS NULL
  AND wi.user_name IS NOT NULL
  AND wi.user_name != '';
