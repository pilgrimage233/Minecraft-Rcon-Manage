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
VALUES (@max_id + 1, 'auto_remove_from_group_after_inactive_days', '30', '长时间未答卷自动踢出群聊天数(设置0为禁用)',
        null, '',
        now(), '0');
INSERT INTO whitelist_quiz_config (id, config_key, config_value, description, remark, create_by, create_time,
                                   del_flag)
VALUES (@max_id + 2, 'require_quiz_after_long_term_inactive_days', '60', '长期未登录要求重新答卷天数(设置0为禁用)',
        null, '',
        now(), '0');
