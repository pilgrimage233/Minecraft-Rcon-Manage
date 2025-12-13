-- 添加日志清理定时任务

-- 1. 任务日志清理任务（每天凌晨2点执行）
INSERT INTO `sys_job` (`job_name`,
                       `job_group`,
                       `invoke_target`,
                       `cron_expression`,
                       `misfire_policy`,
                       `concurrent`,
                       `status`,
                       `create_by`,
                       `create_time`,
                       `remark`)
VALUES ('任务日志清理',
        'SYSTEM',
        'logCleanupTask.cleanJobLog',
        '0 0 2 * * ?',
        '2',
        '1',
        '1',
        'admin',
        NOW(),
        '每天凌晨2点清理过期的任务日志，保留天数由配置文件决定');

-- 2. 操作日志清理任务（每天凌晨2点30分执行）
INSERT INTO `sys_job` (`job_name`,
                       `job_group`,
                       `invoke_target`,
                       `cron_expression`,
                       `misfire_policy`,
                       `concurrent`,
                       `status`,
                       `create_by`,
                       `create_time`,
                       `remark`)
VALUES ('操作日志清理',
        'SYSTEM',
        'logCleanupTask.cleanOperLog',
        '0 30 2 * * ?',
        '2',
        '1',
        '1',
        'admin',
        NOW(),
        '每天凌晨2点30分清理过期的操作日志，保留天数由配置文件决定');

-- 3. 登录日志清理任务（每天凌晨3点执行）
INSERT INTO `sys_job` (`job_name`,
                       `job_group`,
                       `invoke_target`,
                       `cron_expression`,
                       `misfire_policy`,
                       `concurrent`,
                       `status`,
                       `create_by`,
                       `create_time`,
                       `remark`)
VALUES ('登录日志清理',
        'SYSTEM',
        'logCleanupTask.cleanLoginLog',
        '0 0 3 * * ?',
        '2',
        '1',
        '1',
        'admin',
        NOW(),
        '每天凌晨3点清理过期的登录日志，保留天数由配置文件决定');

-- 4. 综合日志清理任务（每周日凌晨1点执行，可选）
INSERT INTO `sys_job` (`job_name`,
                       `job_group`,
                       `invoke_target`,
                       `cron_expression`,
                       `misfire_policy`,
                       `concurrent`,
                       `status`,
                       `create_by`,
                       `create_time`,
                       `remark`)
VALUES ('综合日志清理',
        'SYSTEM',
        'logCleanupTask.cleanAllLogs',
        '0 0 1 ? * SUN',
        '2',
        '1',
        '0',
        'admin',
        NOW(),
        '每周日凌晨1点执行综合日志清理，包含所有类型的日志清理（默认禁用，可根据需要启用）');

-- 说明：
-- cron_expression 说明：
-- '0 0 2 * * ?' = 每天凌晨2点执行
-- '0 30 2 * * ?' = 每天凌晨2点30分执行  
-- '0 0 3 * * ?' = 每天凌晨3点执行
-- '0 0 1 ? * SUN' = 每周日凌晨1点执行

-- misfire_policy 说明：
-- 1 = 立即执行
-- 2 = 执行一次
-- 3 = 放弃执行

-- concurrent 说明：
-- 0 = 允许并发
-- 1 = 禁止并发

-- status 说明：
-- 0 = 暂停
-- 1 = 正常