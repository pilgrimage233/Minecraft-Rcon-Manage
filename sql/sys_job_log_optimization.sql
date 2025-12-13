ALTER TABLE `sys_job_log`
    ADD INDEX `idx_create_time` (`create_time`);
ALTER TABLE `sys_job_log`
    ADD INDEX `idx_job_name_group` (`job_name`, `job_group`);
ALTER TABLE `sys_job_log`
    ADD INDEX `idx_status_create_time` (`status`, `create_time`);

DELETE
FROM `sys_job_log`
WHERE `create_time` < DATE_SUB(NOW(), INTERVAL 30 DAY);
