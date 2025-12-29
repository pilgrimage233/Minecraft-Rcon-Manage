DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`
(
    `table_id`          bigint                                                         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `table_name`        varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '表名称',
    `table_comment`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '表描述',
    `sub_table_name`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT NULL COMMENT '关联子表的表名',
    `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT NULL COMMENT '子表关联的外键名',
    `class_name`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '实体类名称',
    `tpl_category`      varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
    `tpl_web_type`      varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
    `package_name`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '生成包路径',
    `module_name`       varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT NULL COMMENT '生成模块名',
    `business_name`     varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT NULL COMMENT '生成业务名',
    `function_name`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT NULL COMMENT '生成功能名',
    `function_author`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT NULL COMMENT '生成功能作者',
    `gen_type`          char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci       NULL DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
    `gen_path`          varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
    `options`           varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '其它生成选项',
    `create_by`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT '' COMMENT '创建者',
    `create_time`       datetime                                                       NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT '' COMMENT '更新者',
    `update_time`       datetime                                                       NULL DEFAULT NULL COMMENT '更新时间',
    `remark`            varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`table_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表'
  ROW_FORMAT = DYNAMIC;


DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column`
(
    `column_id`      bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '编号',
    `table_id`       bigint                                                        NULL DEFAULT NULL COMMENT '归属表编号',
    `column_name`    varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列名称',
    `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列描述',
    `column_type`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列类型',
    `java_type`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA类型',
    `java_field`     varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA字段名',
    `is_pk`          char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT NULL COMMENT '是否主键（1是）',
    `is_increment`   char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT NULL COMMENT '是否自增（1是）',
    `is_required`    char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT NULL COMMENT '是否必填（1是）',
    `is_insert`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT NULL COMMENT '是否为插入字段（1是）',
    `is_edit`        char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT NULL COMMENT '是否编辑字段（1是）',
    `is_list`        char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT NULL COMMENT '是否列表字段（1是）',
    `is_query`       char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT NULL COMMENT '是否查询字段（1是）',
    `query_type`     varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
    `html_type`      varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
    `dict_type`      varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
    `sort`           int                                                           NULL DEFAULT NULL COMMENT '排序',
    `create_by`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '创建者',
    `create_time`    datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '更新者',
    `update_time`    datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`column_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表字段'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars`
(
    `sched_name`    varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
    `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '日历名称',
    `calendar`      blob                                                          NOT NULL COMMENT '存放持久化calendar对象',
    PRIMARY KEY (`sched_name`, `calendar_name`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '日历信息表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_calendars
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers`
(
    `sched_name`        varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
    `entry_id`          varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '调度器实例id',
    `trigger_name`      varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
    `trigger_group`     varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
    `instance_name`     varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度器实例名',
    `fired_time`        bigint                                                        NOT NULL COMMENT '触发的时间',
    `sched_time`        bigint                                                        NOT NULL COMMENT '定时器制定的时间',
    `priority`          int                                                           NOT NULL COMMENT '优先级',
    `state`             varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '状态',
    `job_name`          varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务名称',
    `job_group`         varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务组名',
    `is_nonconcurrent`  varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT NULL COMMENT '是否并发',
    `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT NULL COMMENT '是否接受恢复执行',
    PRIMARY KEY (`sched_name`, `entry_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '已触发的触发器表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_fired_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks`
(
    `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
    `lock_name`  varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '悲观锁名称',
    PRIMARY KEY (`sched_name`, `lock_name`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '存储的悲观锁信息表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_locks
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps`
(
    `sched_name`    varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
    `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
    PRIMARY KEY (`sched_name`, `trigger_group`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '暂停的触发器表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_paused_trigger_grps
-- ----------------------------


-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state`
(
    `sched_name`        varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
    `instance_name`     varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '实例名称',
    `last_checkin_time` bigint                                                        NOT NULL COMMENT '上次检查时间',
    `checkin_interval`  bigint                                                        NOT NULL COMMENT '检查间隔时间',
    PRIMARY KEY (`sched_name`, `instance_name`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '调度器状态表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_scheduler_state
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`
(
    `config_id`    int                                                           NOT NULL AUTO_INCREMENT COMMENT '参数主键',
    `config_name`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数名称',
    `config_key`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键名',
    `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键值',
    `config_type`  char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
    `create_by`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '创建者',
    `create_time`  datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '更新者',
    `update_time`  datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `remark`       varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '参数配置表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config`
VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2023-12-26 16:54:02', '',
        NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO `sys_config`
VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2023-12-26 16:54:02', '', NULL,
        '初始化密码 123456');
INSERT INTO `sys_config`
VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2023-12-26 16:54:02', '', NULL,
        '深色主题theme-dark，浅色主题theme-light');
INSERT INTO `sys_config`
VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', '2023-12-26 16:54:02', '', NULL,
        '是否开启验证码功能（true开启，false关闭）');
INSERT INTO `sys_config`
VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2023-12-26 16:54:02',
        '', NULL, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO `sys_config`
VALUES (6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2023-12-26 16:54:02', '', NULL,
        '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------

-- ----------------------------
-- Records of server_info
-- ----------------------------

DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`
(
    `dept_id`     bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '部门id',
    `parent_id`   bigint                                                       NULL DEFAULT 0 COMMENT '父部门id',
    `ancestors`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '祖级列表',
    `dept_name`   varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
    `order_num`   int                                                          NULL DEFAULT 0 COMMENT '显示顺序',
    `leader`      varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '负责人',
    `phone`       varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
    `email`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
    `status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci     NULL DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
    `del_flag`    char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci     NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                     NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                     NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '部门表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept`
VALUES (100, 0, '0', '若依科技', 0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2023-12-26 16:54:02', '',
        NULL);
INSERT INTO `sys_dept`
VALUES (101, 100, '0,100', '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', NULL);
INSERT INTO `sys_dept`
VALUES (102, 100, '0,100', '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', NULL);
INSERT INTO `sys_dept`
VALUES (103, 101, '0,100,101', '研发部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', NULL);
INSERT INTO `sys_dept`
VALUES (104, 101, '0,100,101', '市场部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', NULL);
INSERT INTO `sys_dept`
VALUES (105, 101, '0,100,101', '测试部门', 3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', NULL);
INSERT INTO `sys_dept`
VALUES (106, 101, '0,100,101', '财务部门', 4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', NULL);
INSERT INTO `sys_dept`
VALUES (107, 101, '0,100,101', '运维部门', 5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', NULL);
INSERT INTO `sys_dept`
VALUES (108, 102, '0,100,102', '市场部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', NULL);
INSERT INTO `sys_dept`
VALUES (109, 102, '0,100,102', '财务部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin',
        '2023-12-26 16:54:02', '', NULL);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------

create table sys_job
(
    job_id          bigint auto_increment comment '任务ID',
    job_name        varchar(64)  default ''        not null comment '任务名称',
    job_group       varchar(64)  default 'DEFAULT' not null comment '任务组名',
    invoke_target   varchar(500)                   not null comment '调用目标字符串',
    cron_expression varchar(255) default ''        null comment 'cron执行表达式',
    misfire_policy  varchar(20)  default '3'       null comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
    concurrent      char         default '1'       null comment '是否并发执行（0允许 1禁止）',
    status          char         default '0'       null comment '状态（0正常 1暂停）',
    create_by       varchar(64)  default ''        null comment '创建者',
    create_time     datetime                       null comment '创建时间',
    update_by       varchar(64)  default ''        null comment '更新者',
    update_time     datetime                       null comment '更新时间',
    remark          varchar(500) default ''        null comment '备注信息',
    primary key (job_id, job_name, job_group)
)
    comment '定时任务调度表' charset = utf8mb4
                             row_format = DYNAMIC;

INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (4, '同步白名单', 'DEFAULT', 'whiteListTask.syncWhitelistByServerId(\'all\')', '30 1 0/1 * * ?', '1', '1', '0',
        'admin', '2024-12-21 18:56:29', 'admin', '2025-12-16 14:43:42', '');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (5, '刷新Rcon缓存', 'DEFAULT', 'rconTask.refreshMapCache', '10 1 0/1 * * ?', '1', '1', '0', 'admin',
        '2024-12-21 18:58:15', 'admin', '2025-01-03 02:26:12', '');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (6, '同步ID', 'DEFAULT', 'onlineTask.syncUserNameForUuid', '0 0 0 1/1 * ?', '1', '1', '0', 'admin',
        '2024-12-21 19:00:00', '', '2024-12-21 19:00:03', '');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (7, '重建redis缓存', 'DEFAULT', 'rconTask.refreshRedisCache', '0 0 1 1/2 * ?', '1', '1', '0', 'admin',
        '2024-12-23 16:34:41', '', '2024-12-23 16:34:43', '');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (9, '玩家监控', 'DEFAULT', 'onlineTask.monitor', '0/30 * * * * ?', '1', '1', '0', 'admin', '2025-01-02 05:14:41',
        'admin', '2025-12-15 16:17:45', '');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (10, '指令重试', 'DEFAULT', 'onlineTask.commandRetry', '0 0/1 * * * ?', '1', '1', '0', 'admin',
        '2025-03-07 18:25:35', 'admin', '2025-03-07 18:27:34', '');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (11, '退群监控', 'DEFAULT', 'botTask.monitorWhiteList', '0 0/5 * * * ?', '1', '1', '0', 'admin',
        '2025-03-09 19:05:55', '', '2025-03-27 21:03:57', '');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (12, '白名单时限监控', 'DEFAULT', 'whiteListTask.checkWhitelistExpiry', '10 0/1 * * * ?', '1', '1', '0', 'admin',
        '2025-08-18 22:06:06', 'admin', '2025-08-18 22:58:22', '');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (13, '更新检测', 'DEFAULT', 'botTask.checkUpdate', '0 0/5 * * * ?', '1', '1', '0', 'admin',
        '2025-08-22 23:19:56', '', '2025-08-22 23:20:06', '');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (14, '任务日志清理', 'SYSTEM', 'logCleanupTask.cleanJobLog', '0 0 2 * * ?', '2', '1', '0', 'admin',
        '2025-12-13 08:13:49', '', '2025-12-16 18:47:53', '每天凌晨2点清理过期的任务日志，保留天数由配置文件决定');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (15, '操作日志清理', 'SYSTEM', 'logCleanupTask.cleanOperLog', '0 30 2 * * ?', '2', '1', '1', 'admin',
        '2025-12-13 08:13:49', '', null, '每天凌晨2点30分清理过期的操作日志，保留天数由配置文件决定');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (16, '登录日志清理', 'SYSTEM', 'logCleanupTask.cleanLoginLog', '0 0 3 * * ?', '2', '1', '1', 'admin',
        '2025-12-13 08:13:49', '', null, '每天凌晨3点清理过期的登录日志，保留天数由配置文件决定');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (17, '综合日志清理', 'SYSTEM', 'logCleanupTask.cleanAllLogs', '0 0 1 ? * 1', '2', '1', '0', 'admin',
        '2025-12-13 08:13:49', 'admin', '2025-12-15 13:02:03',
        '每周日凌晨1点执行综合日志清理，包含所有类型的日志清理（默认禁用，可根据需要启用）');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (18, '每日玩家活跃度报告', 'REPORT', 'playerReportTask.generateDailyReport', '0 0 8 * * ?', '2', '1', '0',
        'admin', '2025-12-15 12:30:47', '', '2025-12-17 09:17:15', '每天早上8点生成昨日玩家活跃度报告');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (19, '每周玩家活跃度报告', 'REPORT', 'playerReportTask.generateWeeklyReport', '0 0 9 ? * 2', '2', '1', '1',
        'admin', '2025-12-15 12:30:47', 'admin', '2025-12-16 18:15:23', '每周一早上9点生成上周玩家活跃度报告');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (20, '每月玩家活跃度报告', 'REPORT', 'playerReportTask.generateMonthlyReport', '0 0 10 1 * ?', '2', '1', '1',
        'admin', '2025-12-15 12:30:47', '', '2025-12-16 18:15:24', '每月1号早上10点生成上月玩家活跃度报告');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (21, '每日数据同步任务', 'SYNC', 'dataSyncTask.dailyDataSync', '0 0 2 * * ?', '2', '1', '0', 'admin',
        '2025-12-15 12:30:47', '', null, '每天凌晨2点将Redis数据同步到数据库');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (22, '清理过期缓存任务', 'SYNC', 'dataSyncTask.cleanExpiredCache', '0 0 3 * * ?', '2', '1', '0', 'admin',
        '2025-12-15 12:30:47', '', null, '每天凌晨3点清理Redis过期缓存');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (23, '数据备份任务', 'SYNC', 'dataSyncTask.backupData', '0 0 4 ? * 1', '2', '1', '0', 'admin',
        '2025-12-15 12:30:47', 'admin', '2025-12-15 13:02:20', '每周日凌晨4点执行数据备份');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (24, '修复缺失数据任务', 'SYNC', 'dataSyncTask.repairMissingData', '0 0 1 * * ?', '2', '1', '0', 'admin',
        '2025-12-15 12:30:47', '', null, '每天凌晨1点检查并修复缺失数据');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                     status, create_by, create_time, update_by, update_time, remark)
VALUES (25, '清理过期资源权限', 'DEFAULT', 'permissionTask.cleanExpiredPermissions', '0 0 2 * * ?', '1', '1', '0',
        'admin', '2025-12-20 01:40:32', '', null, '每天凌晨2点清理过期的资源权限');

create table sys_oper_log
(
    oper_id        bigint auto_increment comment '日志主键'
        primary key,
    title          varchar(50)   default '' null comment '模块标题',
    business_type  int           default 0  null comment '业务类型（0其它 1新增 2修改 3删除）',
    method         varchar(100)  default '' null comment '方法名称',
    request_method varchar(10)   default '' null comment '请求方式',
    operator_type  int           default 0  null comment '操作类别（0其它 1后台用户 2手机端用户）',
    oper_name      varchar(50)   default '' null comment '操作人员',
    dept_name      varchar(50)   default '' null comment '部门名称',
    oper_url       varchar(255)  default '' null comment '请求URL',
    oper_ip        varchar(128)  default '' null comment '主机地址',
    oper_location  varchar(255)  default '' null comment '操作地点',
    oper_param     varchar(2000) default '' null comment '请求参数',
    json_result    varchar(2000) default '' null comment '返回参数',
    status         int           default 0  null comment '操作状态（0正常 1异常）',
    error_msg      varchar(2000) default '' null comment '错误消息',
    oper_time      datetime                 null comment '操作时间',
    cost_time      bigint        default 0  null comment '消耗时间'
)
    comment '操作日志记录' charset = utf8mb4
                           row_format = DYNAMIC;

create index idx_sys_oper_log_bt
    on sys_oper_log (business_type);

create index idx_sys_oper_log_ot
    on sys_oper_log (oper_time);

create index idx_sys_oper_log_s
    on sys_oper_log (status);

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor`
(
    `info_id`        bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '访问ID',
    `user_name`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '用户账号',
    `ipaddr`         varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录IP地址',
    `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录地点',
    `browser`        varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '浏览器类型',
    `os`             varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '操作系统',
    `status`         char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
    `msg`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '提示消息',
    `login_time`     datetime                                                      NULL DEFAULT NULL COMMENT '访问时间',
    PRIMARY KEY (`info_id`) USING BTREE,
    INDEX `idx_sys_logininfor_lt` (`login_time` ASC) USING BTREE,
    INDEX `idx_sys_logininfor_s` (`status` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '系统访问记录'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`
(
    `menu_id`     bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `menu_name`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '菜单名称',
    `parent_id`   bigint                                                        NULL DEFAULT 0 COMMENT '父菜单ID',
    `order_num`   int                                                           NULL DEFAULT 0 COMMENT '显示顺序',
    `path`        varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '路由地址',
    `component`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件路径',
    `query`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由参数',
    `is_frame`    int                                                           NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
    `is_cache`    int                                                           NULL DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
    `menu_type`   char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
    `visible`     char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
    `status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
    `perms`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识',
    `icon`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#' COMMENT '菜单图标',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '菜单权限表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------

DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`
(
    `notice_id`      int                                                           NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `notice_title`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '公告标题',
    `notice_type`    char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NOT NULL COMMENT '公告类型（1通知 2公告）',
    `notice_content` longblob                                                      NULL COMMENT '公告内容',
    `status`         char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
    `create_by`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '创建者',
    `create_time`    datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '更新者',
    `update_time`    datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `remark`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '通知公告表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`
(
    `oper_id`        bigint                                                         NOT NULL AUTO_INCREMENT COMMENT '日志主键',
    `title`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT '' COMMENT '模块标题',
    `business_type`  int                                                            NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
    `method`         varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '方法名称',
    `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT '' COMMENT '请求方式',
    `operator_type`  int                                                            NULL DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
    `oper_name`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT '' COMMENT '操作人员',
    `dept_name`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT '' COMMENT '部门名称',
    `oper_url`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '请求URL',
    `oper_ip`        varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '主机地址',
    `oper_location`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '操作地点',
    `oper_param`     varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求参数',
    `json_result`    varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '返回参数',
    `status`         int                                                            NULL DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
    `error_msg`      varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '错误消息',
    `oper_time`      datetime                                                       NULL DEFAULT NULL COMMENT '操作时间',
    `cost_time`      bigint                                                         NULL DEFAULT 0 COMMENT '消耗时间',
    PRIMARY KEY (`oper_id`) USING BTREE,
    INDEX `idx_sys_oper_log_bt` (`business_type` ASC) USING BTREE,
    INDEX `idx_sys_oper_log_ot` (`oper_time` ASC) USING BTREE,
    INDEX `idx_sys_oper_log_s` (`status` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '操作日志记录'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`
(
    `post_id`     bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
    `post_code`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '岗位编码',
    `post_name`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '岗位名称',
    `post_sort`   int                                                           NOT NULL COMMENT '显示顺序',
    `status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NOT NULL COMMENT '状态（0正常 1停用）',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`post_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '岗位信息表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post`
VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2023-12-26 16:54:02', '', NULL, '');
INSERT INTO `sys_post`
VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2023-12-26 16:54:02', '', NULL, '');
INSERT INTO `sys_post`
VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2023-12-26 16:54:02', '', NULL, '');
INSERT INTO `sys_post`
VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2023-12-26 16:54:02', '', NULL, '');


-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`
(
    `role_id`             bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`           varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '角色名称',
    `role_key`            varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
    `role_sort`           int                                                           NOT NULL COMMENT '显示顺序',
    `data_scope`          char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
    `menu_check_strictly` tinyint(1)                                                    NULL DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
    `dept_check_strictly` tinyint(1)                                                    NULL DEFAULT 1 COMMENT '部门树选择项是否关联显示',
    `status`              char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NOT NULL COMMENT '角色状态（0正常 1停用）',
    `del_flag`            char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    `create_by`           varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '创建者',
    `create_time`         datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`           varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '更新者',
    `update_time`         datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `remark`              varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '角色信息表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role`
VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2023-12-26 16:54:02', '', NULL, '超级管理员');
INSERT INTO `sys_role`
VALUES (2, '普通角色', 'common', 2, '2', 1, 1, '0', '0', 'admin', '2023-12-26 16:54:02', 'admin', '2023-12-27 16:38:22',
        '普通角色');

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`
(
    `role_id` bigint NOT NULL COMMENT '角色ID',
    `dept_id` bigint NOT NULL COMMENT '部门ID',
    PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '角色和部门关联表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
INSERT INTO `sys_role_dept`
VALUES (2, 100);
INSERT INTO `sys_role_dept`
VALUES (2, 101);
INSERT INTO `sys_role_dept`
VALUES (2, 105);


-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`
(
    `role_id` bigint NOT NULL COMMENT '角色ID',
    `menu_id` bigint NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '角色和菜单关联表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu`
VALUES (2, 2);
INSERT INTO `sys_role_menu`
VALUES (2, 109);
INSERT INTO `sys_role_menu`
VALUES (2, 110);
INSERT INTO `sys_role_menu`
VALUES (2, 111);
INSERT INTO `sys_role_menu`
VALUES (2, 112);
INSERT INTO `sys_role_menu`
VALUES (2, 113);
INSERT INTO `sys_role_menu`
VALUES (2, 114);
INSERT INTO `sys_role_menu`
VALUES (2, 1046);
INSERT INTO `sys_role_menu`
VALUES (2, 1047);
INSERT INTO `sys_role_menu`
VALUES (2, 1048);
INSERT INTO `sys_role_menu`
VALUES (2, 1049);
INSERT INTO `sys_role_menu`
VALUES (2, 1050);
INSERT INTO `sys_role_menu`
VALUES (2, 1051);
INSERT INTO `sys_role_menu`
VALUES (2, 1052);
INSERT INTO `sys_role_menu`
VALUES (2, 1053);
INSERT INTO `sys_role_menu`
VALUES (2, 1054);
INSERT INTO `sys_role_menu`
VALUES (2, 2000);
INSERT INTO `sys_role_menu`
VALUES (2, 2009);
INSERT INTO `sys_role_menu`
VALUES (2, 2010);
INSERT INTO `sys_role_menu`
VALUES (2, 2011);
INSERT INTO `sys_role_menu`
VALUES (2, 2012);
INSERT INTO `sys_role_menu`
VALUES (2, 2013);
INSERT INTO `sys_role_menu`
VALUES (2, 2014);


-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `user_id`     bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `dept_id`     bigint                                                        NULL DEFAULT NULL COMMENT '部门ID',
    `user_name`   varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '用户账号',
    `nick_name`   varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '用户昵称',
    `user_type`   varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL DEFAULT '00' COMMENT '用户类型（00系统用户）',
    `email`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '用户邮箱',
    `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '手机号码',
    `sex`         char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
    `avatar`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '头像地址',
    `password`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码',
    `status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
    `del_flag`    char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    `login_ip`    varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '最后登录IP',
    `login_date`  datetime                                                      NULL DEFAULT NULL COMMENT '最后登录时间',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户信息表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
insert into `sys_user`
values (1, 103, 'admin', 'Endless', '00', 'Endless@163.com', '15888888888', '1', '',
        '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), 'admin',
        sysdate(), '', null, '管理员');


-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`
(
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `post_id` bigint NOT NULL COMMENT '岗位ID',
    PRIMARY KEY (`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户与岗位关联表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post`
VALUES (1, 1);
INSERT INTO `sys_user_post`
VALUES (2, 2);
INSERT INTO `sys_user_post`
VALUES (3, 1);
INSERT INTO `sys_user_post`
VALUES (5, 4);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`
(
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `role_id` bigint NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户和角色关联表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role`
VALUES (1, 1);
INSERT INTO `sys_user_role`
VALUES (2, 2);
INSERT INTO `sys_user_role`
VALUES (3, 2);
INSERT INTO `sys_user_role`
VALUES (5, 2);


create table sys_dict_data
(
    dict_code   bigint auto_increment comment '字典编码'
        primary key,
    dict_sort   int          default 0   null comment '字典排序',
    dict_label  varchar(100) default ''  null comment '字典标签',
    dict_value  varchar(100) default ''  null comment '字典键值',
    dict_type   varchar(100) default ''  null comment '字典类型',
    css_class   varchar(100)             null comment '样式属性（其他样式扩展）',
    list_class  varchar(100)             null comment '表格回显样式',
    is_default  char         default 'N' null comment '是否默认（Y是 N否）',
    status      char         default '0' null comment '状态（0正常 1停用）',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500)             null comment '备注'
)
    comment '字典数据表' charset = utf8mb4
                         row_format = DYNAMIC;

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null, '性别男');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null, '性别女');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null, '性别未知');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '显示菜单');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '隐藏菜单');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '正常状态');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '停用状态');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '正常状态');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '停用状态');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '默认分组');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '系统分组');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '系统默认是');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null, '系统默认否');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '通知');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '公告');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '正常状态');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '关闭状态');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '其他操作');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '新增操作');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '修改操作');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '删除操作');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '授权操作');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '导出操作');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '导入操作');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '强退操作');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '生成操作');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '清空操作');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '正常状态');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', null,
        '停用状态');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (100, 1, '待审核', '0', 'white_examine_status', null, 'warning', 'N', '0', 'admin', '2023-12-27 18:07:29',
        'admin', '2023-12-27 18:27:23', null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (101, 2, '已过审', '1', 'white_examine_status', null, 'success', 'N', '0', 'admin', '2023-12-27 18:07:47',
        'admin', '2023-12-27 18:27:26', null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (102, 3, '已拒审', '2', 'white_examine_status', null, 'danger', 'N', '0', 'admin', '2023-12-27 18:08:15',
        'admin', '2023-12-27 18:27:30', null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (103, 1, '未添加', '0', 'white_add_status', null, 'info', 'N', '0', 'admin', '2023-12-27 18:40:35', 'admin',
        '2023-12-27 18:40:43', null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (104, 2, '已添加', '1', 'white_add_status', null, 'success', 'N', '0', 'admin', '2023-12-27 18:41:05', 'admin',
        '2024-01-14 13:38:09', null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (105, 3, '已删除', '2', 'white_add_status', null, 'warning', 'N', '0', 'admin', '2023-12-27 18:41:20', 'admin',
        '2023-12-27 18:41:44', null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (106, 4, '已封禁', '9', 'white_add_status', null, 'danger', 'N', '0', 'admin', '2023-12-27 18:42:07', 'admin',
        '2023-12-27 19:32:11', null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (107, 1, '正版', '1', 'online_status', null, 'primary', 'N', '0', 'admin', '2023-12-27 15:28:43', 'admin',
        '2023-12-27 15:29:00', null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (108, 2, '离线', '0', 'online_status', null, 'info', 'N', '0', 'admin', '2023-12-27 15:29:13', '', null, null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (109, 0, '停用', '0', 'server_status', null, 'danger', 'N', '0', 'admin', '2024-03-10 17:13:17', '', null, null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (110, 0, '启用', '1', 'server_status', null, 'success', 'N', '0', 'admin', '2024-03-10 17:13:36', '', null,
        null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (111, 0, '已封禁', '1', 'ban_status', null, 'danger', 'N', '0', 'admin', '2024-12-23 17:43:23', '', null, null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (112, 0, '已解封', '0', 'ban_status', null, 'warning', 'N', '0', 'admin', '2024-12-23 17:47:28', '', null, null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (113, 0, '玩家', 'player', 'player_identity', null, 'primary', 'N', '0', 'admin', '2024-12-31 04:06:48', 'admin',
        '2024-12-31 04:09:36', null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (114, 0, '管理', 'operator', 'player_identity', null, 'success', 'N', '0', 'admin', '2024-12-31 04:07:52',
        'admin', '2024-12-31 04:08:16', null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (115, 0, '封禁', 'banned', 'player_identity', null, 'danger', 'N', '0', 'admin', '2024-12-31 04:09:02', 'admin',
        '2024-12-31 04:09:15', null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (116, 0, '启用', '1', 'op_status', null, 'success', 'N', '0', 'admin', '2025-01-12 10:25:26', '', null, null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (117, 0, '停用', '0', 'op_status', null, 'warning', 'N', '0', 'admin', '2025-01-12 10:25:38', '', null, null);
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (118, 1, '仅查看', 'view', 'rcon_permission_type', '', 'info', 'N', '0', 'admin', '2025-12-20 01:40:32', '',
        null, '仅可查看服务器信息');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (119, 2, '执行命令', 'command', 'rcon_permission_type', '', 'primary', 'N', '0', 'admin', '2025-12-20 01:40:32',
        '', null, '可执行RCON命令');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (120, 3, '管理配置', 'manage', 'rcon_permission_type', '', 'warning', 'N', '0', 'admin', '2025-12-20 01:40:32',
        '', null, '可管理服务器配置');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (121, 4, '完全控制', 'admin', 'rcon_permission_type', '', 'danger', 'N', '0', 'admin', '2025-12-20 01:40:32', '',
        null, '完全控制权限');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (122, 1, '仅查看', 'view', 'node_permission_type', '', 'info', 'N', '0', 'admin', '2025-12-20 01:40:32', '',
        null, '仅可查看节点信息');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (123, 2, '操作节点', 'operate', 'node_permission_type', '', 'primary', 'N', '0', 'admin', '2025-12-20 01:40:32',
        '', null, '可操作节点');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (124, 3, '管理节点', 'manage', 'node_permission_type', '', 'warning', 'N', '0', 'admin', '2025-12-20 01:40:32',
        '', null, '可管理节点配置');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (125, 4, '完全控制', 'admin', 'node_permission_type', '', 'danger', 'N', '0', 'admin', '2025-12-20 01:40:32', '',
        null, '完全控制权限');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (126, 1, '仅查看', 'view', 'instance_permission_type', '', 'info', 'N', '0', 'admin', '2025-12-20 01:40:32', '',
        null, '仅可查看实例信息');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (127, 2, '控制实例', 'control', 'instance_permission_type', '', 'primary', 'N', '0', 'admin',
        '2025-12-20 01:40:32', '', null, '可启停实例');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (128, 3, '管理实例', 'manage', 'instance_permission_type', '', 'warning', 'N', '0', 'admin',
        '2025-12-20 01:40:32', '', null, '可管理实例配置');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (129, 4, '完全控制', 'admin', 'instance_permission_type', '', 'danger', 'N', '0', 'admin', '2025-12-20 01:40:32',
        '', null, '完全控制权限');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (130, 1, 'RCON服务器', 'rcon_server', 'resource_type', '', 'primary', 'N', '0', 'admin', '2025-12-20 01:40:32',
        '', null, 'RCON服务器');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (131, 2, '节点服务器', 'node_server', 'resource_type', '', 'success', 'N', '0', 'admin', '2025-12-20 01:40:32',
        '', null, '节点服务器');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (132, 3, 'MC实例', 'mc_instance', 'resource_type', '', 'warning', 'N', '0', 'admin', '2025-12-20 01:40:32', '',
        null, 'Minecraft服务器实例');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (133, 1, '授权', 'grant', 'permission_action_type', '', 'success', 'N', '0', 'admin', '2025-12-20 01:40:32', '',
        null, '授予权限');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (134, 2, '撤销', 'revoke', 'permission_action_type', '', 'danger', 'N', '0', 'admin', '2025-12-20 01:40:32', '',
        null, '撤销权限');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (135, 3, '修改', 'modify', 'permission_action_type', '', 'warning', 'N', '0', 'admin', '2025-12-20 01:40:32', '',
        null, '修改权限');
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
                           is_default, status, create_by, create_time, update_by, update_time, remark)
VALUES (136, 4, '访问', 'access', 'permission_action_type', '', 'info', 'N', '0', 'admin', '2025-12-20 01:40:32', '',
        null, '访问资源');


create table sys_dict_type
(
    dict_id     bigint auto_increment comment '字典主键'
        primary key,
    dict_name   varchar(100) default ''  null comment '字典名称',
    dict_type   varchar(100) default ''  null comment '字典类型',
    status      char         default '0' null comment '状态（0正常 1停用）',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500)             null comment '备注',
    constraint dict_type
        unique (dict_type)
)
    comment '字典类型表' charset = utf8mb4
                         row_format = DYNAMIC;

INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2023-12-26 16:54:02', '', null, '用户性别列表');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2023-12-26 16:54:02', '', null, '菜单状态列表');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2023-12-26 16:54:02', '', null, '系统开关列表');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2023-12-26 16:54:02', '', null, '任务状态列表');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2023-12-26 16:54:02', '', null, '任务分组列表');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2023-12-26 16:54:02', '', null, '系统是否列表');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2023-12-26 16:54:02', '', null, '通知类型列表');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2023-12-26 16:54:02', '', null, '通知状态列表');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2023-12-26 16:54:02', '', null, '操作类型列表');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2023-12-26 16:54:02', '', null, '登录状态列表');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (100, '审核状态', 'white_examine_status', '0', 'admin', '2023-12-27 18:07:01', '', null, null);
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (101, '添加状态', 'white_add_status', '0', 'admin', '2023-12-27 18:38:34', 'admin', '2023-12-27 18:43:28', null);
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (102, '正版标识', 'online_status', '0', 'admin', '2023-12-27 15:28:19', '', null, null);
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (103, '服务器开关状态', 'server_status', '0', 'admin', '2024-03-10 17:12:54', '', null, null);
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (104, '封禁状态', 'ban_status', '0', 'admin', '2024-12-23 17:41:42', '', null, null);
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (105, '玩家身份组', 'player_identity', '0', 'admin', '2024-12-31 04:06:12', '', null, null);
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (106, '管理员状态', 'op_status', '0', 'admin', '2025-01-12 10:25:05', '', null, null);
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (107, 'RCON服务器权限类型', 'rcon_permission_type', '0', 'admin', '2025-12-20 01:40:32', '', null,
        'RCON服务器权限类型');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (108, '节点服务器权限类型', 'node_permission_type', '0', 'admin', '2025-12-20 01:40:32', '', null,
        '节点服务器权限类型');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (109, 'MC实例权限类型', 'instance_permission_type', '0', 'admin', '2025-12-20 01:40:32', '', null,
        'MC实例权限类型');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (110, '资源类型', 'resource_type', '0', 'admin', '2025-12-20 01:40:32', '', null, '资源权限管理的资源类型');
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time,
                           remark)
VALUES (111, '权限操作类型', 'permission_action_type', '0', 'admin', '2025-12-20 01:40:32', '', null, '权限操作类型');


