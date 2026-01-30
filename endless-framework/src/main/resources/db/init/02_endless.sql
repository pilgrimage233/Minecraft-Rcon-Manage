CREATE TABLE IF NOT EXISTS banlist_info
(
    id          bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    white_id    bigint                                                        NULL DEFAULT NULL COMMENT '关联白名单ID',
    user_name   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名称',
    state       bigint                                                        NULL DEFAULT NULL COMMENT '封禁状态',
    reason      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封禁原因',
    remark      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    create_time datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    create_by   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
    update_by   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '封禁管理表'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS history_command
(
    id           int                                                            NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    server_id    int                                                            NOT NULL COMMENT '服务器ID',
    user         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NOT NULL COMMENT '执行用户',
    command      varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行指令',
    execute_time datetime                                                       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    response     text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          NULL COMMENT '执行结果',
    status       varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci    NULL     DEFAULT NULL COMMENT '执行状态',
    run_time     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL     DEFAULT NULL COMMENT '运行时间(毫秒值)',
    PRIMARY KEY (id) USING BTREE,
    INDEX history_command_server_id_index (server_id ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '历史命令'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS ip_limit_info
(
    id          bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    uuid        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '随机UUID',
    ip          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP地址',
    user_agent  varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'UA标识',
    count       bigint                                                        NULL DEFAULT NULL COMMENT '请求次数',
    province    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '省',
    city        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地市',
    county      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '区县',
    longitude   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '经度',
    latitude    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '纬度',
    body_params text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         NULL COMMENT '请求参数',
    remark      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    create_time datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    create_by   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
    update_by   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = 'IP限流表'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS operator_list
(
    id          int                                                           NOT NULL AUTO_INCREMENT,
    user_name   varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_german2_ci NOT NULL COMMENT '玩家昵称',
    status      int                                                           NULL DEFAULT NULL COMMENT '状态',
    parameter   varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_german2_ci NULL DEFAULT NULL COMMENT '其他参数',
    create_time datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    create_by   varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_german2_ci NULL DEFAULT NULL COMMENT '创建者',
    update_by   varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_german2_ci NULL DEFAULT NULL COMMENT '更新者',
    remark      varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_german2_ci NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id) USING BTREE,
    INDEX operator_list_id_index (id ASC) USING BTREE,
    INDEX operator_list_user_name_index (user_name ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_german2_ci COMMENT = '管理员列表'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS player_details
(
    id                int                                                           NOT NULL AUTO_INCREMENT,
    user_name         varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '玩家昵称',
    qq                varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL DEFAULT NULL COMMENT 'QQ号',
    identity          varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NOT NULL COMMENT '身份',
    last_online_time  datetime                                                      NULL DEFAULT NULL COMMENT '最后上线时间',
    last_offline_time datetime                                                      NULL DEFAULT NULL COMMENT '最后离线时间',
    game_time         int                                                           NULL DEFAULT NULL COMMENT '游戏时间',
    province          varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '省份',
    city              varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '地市',
    whitelist_id      int                                                           NULL DEFAULT NULL COMMENT '白名单ID',
    banlist_id        int                                                           NULL DEFAULT NULL COMMENT '封禁ID',
    parameters        varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '其他参数',
    create_time       datetime                                                      NOT NULL COMMENT '创建时间',
    update_time       datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    create_by         varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NOT NULL COMMENT '创建者',
    update_by         varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL DEFAULT NULL COMMENT '更新者',
    remark            varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id) USING BTREE,
    INDEX player_details_id_whitelist_id_index (id ASC, whitelist_id ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_general_ci COMMENT = '玩家信息'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS public_server_command
(
    id             bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    server_id      bigint                                                        NULL DEFAULT NULL COMMENT '服务器ID',
    command        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '指令',
    status         bigint                                                        NULL DEFAULT NULL COMMENT '启用状态',
    vague_matching bigint                                                        NULL DEFAULT NULL COMMENT '模糊匹配',
    remark         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    create_time    datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    update_time    datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    create_by      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
    update_by      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '公开命令表'
  ROW_FORMAT = DYNAMIC;


CREATE TABLE IF NOT EXISTS regular_cmd
(
    id             int                                                           NOT NULL AUTO_INCREMENT COMMENT '主键',
    task_name      varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
    task_id        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '任务id',
    cmd            varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '指令',
    execute_server varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '执行服务器',
    result         varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行结果',
    history_count  int                                                           NULL DEFAULT NULL COMMENT '执行历史保留次数',
    history_result text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         NULL COMMENT '历史结果',
    cron           varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT 'Cron表达式',
    status         int                                                           NOT NULL COMMENT '状态',
    execute_count  int                                                           NULL DEFAULT NULL COMMENT '执行次数',
    create_time    datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    update_time    datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    create_by      varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
    update_by      varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
    remark         varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '定时命令'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS server_command_info
(
    id                            bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    server_id                     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务器ID',
    online_add_whitelist_command  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '在线模式白名单添加指令',
    offline_add_whitelist_command varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '离线模式白名单添加指令',
    online_rm_whitelist_command   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '在线模式白名单移除指令',
    offline_rm_whitelist_command  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '离线模式白名单移除指令',
    online_add_ban_command        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '在线模式添加封禁指令',
    offline_add_ban_command       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '离线模式添加封禁指令',
    online_rm_ban_command         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '在线模式移除封禁指令',
    offline_rm_ban_command        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '离线模式移除封禁指令',
    easyauth                      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否启用EasyAuthMod',
    remark                        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    create_time                   datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    update_time                   datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    create_by                     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
    update_by                     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (id) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '指令管理表'
  ROW_FORMAT = DYNAMIC;

create table IF NOT EXISTS server_info
(
    id                int auto_increment comment '主键ID'
        primary key,
    uuid              varchar(64)  not null comment '随机UUID',
    name_tag          varchar(128) not null comment '服务器名称标签',
    play_address      varchar(128) not null comment '游玩地址',
    play_address_port int          not null comment '地址端口号(默认25565）',
    server_version    varchar(64)  null comment '服务器版本',
    server_core       varchar(64)  null comment '服务器核心',
    ip                varchar(128) not null comment 'RCON远程地址',
    rcon_port         int          not null comment 'RCON远程端口号',
    rcon_password     varchar(256) not null comment '远程密码/MD5加密',
    create_time       datetime     null comment '创建时间',
    create_by         varchar(128) null comment '创建者',
    update_time       datetime     null comment '更新时间',
    update_by         varchar(128) null comment '更新者',
    status            int          not null comment '启用状态',
    remark            text         null comment '描述'
)
    comment '服务器信息' collate = utf8mb4_bin;

create index server_info_id_index
    on server_info (id);

CREATE TABLE IF NOT EXISTS whitelist_info
(
    id            int                                                    NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    time          datetime                                               NULL DEFAULT NULL COMMENT '申请时间',
    user_name     varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '游戏名称',
    user_uuid     varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
    online_flag   int                                                    NULL DEFAULT NULL COMMENT '正版标识',
    qq_num        varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL COMMENT '用户QQ号',
    remark        varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '描述',
    review_users  varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '审核用户',
    status        varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL COMMENT '审核状态',
    add_state     varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL COMMENT '添加状态',
    add_time      datetime                                               NULL DEFAULT NULL COMMENT '添加时间',
    remove_reason varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '移除原因',
    remove_time   datetime                                               NULL DEFAULT NULL COMMENT '移除时间',
    create_by     varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '创建人',
    update_by     varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '更新者',
    create_time   datetime                                               NULL DEFAULT NULL COMMENT '创建时间',
    update_time   datetime                                               NULL DEFAULT NULL COMMENT '更新时间',
    servers       varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
    PRIMARY KEY (id) USING BTREE,
    INDEX whitelist_info_qq_num_index (qq_num ASC) USING BTREE,
    INDEX whitelist_info_user_name_index (user_name ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_bin
  ROW_FORMAT = DYNAMIC;

create table IF NOT EXISTS whitelist_deadline_info
(
    id           int auto_increment
        primary key,
    whitelist_id int         not null comment '白名单ID',
    user_name    varchar(64) null comment '用户昵称',
    start_time   datetime    null comment '开始时间',
    end_time     datetime    null comment '截止时间',
    del_flag     int         null comment '清除标识',
    create_by    varchar(64) null comment '创建者',
    update_bv    varchar(64) null comment '更新者',
    create_time  datetime    null comment '创建时间',
    update_time  datetime    null comment '更新时间',
    remark       varchar(64) null comment '备注'
)
    comment '白名单时限信息';

create index whitelist_deadline_info_id_index
    on whitelist_deadline_info (id desc);

create index whitelist_deadline_info_whitelist_id_index
    on whitelist_deadline_info (whitelist_id desc);


CREATE TABLE IF NOT EXISTS custom_email_templates
(
    id           int      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    create_time  datetime NOT NULL COMMENT '创建时间',
    create_by    varchar(64)  DEFAULT NULL COMMENT '创建者',
    server_id    int          DEFAULT NULL COMMENT '服务器ID',
    review_temp  text COMMENT '审核',
    pending_temp text COMMENT '待审核',
    pass_temp    text COMMENT '通过',
    refuse_temp  text COMMENT '拒绝',
    remove_temp  text COMMENT '移除',
    ban_temp     text COMMENT '封禁',
    pardon_temp  text COMMENT '解禁',
    verify_temp  text COMMENT '邮箱验证',
    warning_temp text COMMENT '系统告警',
    status       int      NOT NULL COMMENT '状态',
    update_time  datetime     DEFAULT NULL COMMENT '更新时间',
    update_by    varchar(64)  DEFAULT NULL COMMENT '更新者',
    remark       varchar(256) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY custom_email_templates_id_server_id_index (id DESC, server_id DESC)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='自定义邮件通知模板';

-- 白名单申请题库问题表
create table IF NOT EXISTS whitelist_quiz_question
(
    id            bigint auto_increment
        primary key,
    question_text varchar(500)                          not null comment '问题内容',
    question_type tinyint                               not null comment '问题类型：1-单选题，2-多选题，3-填空题',
    is_required   tinyint(1)  default 1                 null comment '是否必答：0-否，1-是',
    sort_order    int         default 0                 null comment '排序顺序',
    status        tinyint(1)  default 1                 null comment '状态：0-禁用，1-启用',
    remark        varchar(500)                          null comment '备注',
    create_by     varchar(64) default ''                null comment '创建者',
    create_time   datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_by     varchar(64) default ''                null comment '更新者',
    update_time   datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag      char        default '0'               null comment '删除标志（0代表存在 2代表删除）'
)
    comment '白名单申请题库问题表';

create index idx_question_type
    on whitelist_quiz_question (question_type);

create index idx_status
    on whitelist_quiz_question (status);

-- 白名单申请题目答案表
create table IF NOT EXISTS whitelist_quiz_answer
(
    id          bigint auto_increment
        primary key,
    question_id bigint                                not null comment '关联的问题ID',
    answer_text varchar(500)                          not null comment '答案内容',
    is_correct  tinyint(1)  default 0                 null comment '是否为正确答案：0-否，1-是',
    sort_order  int         default 0                 null comment '排序顺序（选择题选项排序）',
    score       double                                null comment '得分',
    remark      varchar(500)                          null comment '备注',
    create_by   varchar(64) default ''                null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_by   varchar(64) default ''                null comment '更新者',
    update_time datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag    char        default '0'               null comment '删除标志（0代表存在 2代表删除）',
    constraint whitelist_quiz_answer_ibfk_1
        foreign key (question_id) references whitelist_quiz_question (id)
            on delete cascade
)
    comment '白名单申请题目答案表';

create index idx_is_correct
    on whitelist_quiz_answer (is_correct);

create index idx_question_id
    on whitelist_quiz_answer (question_id);

-- 白名单申请答题记录表
create table IF NOT EXISTS whitelist_quiz_submission
(
    id             bigint auto_increment
        primary key,
    player_uuid    varchar(36)                           not null comment '玩家UUID',
    player_name    varchar(50)                           not null comment '玩家名称',
    total_score    int         default 0                 null comment '总得分',
    pass_status    tinyint(1)  default 0                 null comment '通过状态：0-未通过，1-已通过',
    submit_time    datetime    default CURRENT_TIMESTAMP null comment '提交时间',
    review_time    datetime                              null comment '审核时间',
    reviewer       varchar(50)                           null comment '审核人',
    review_comment varchar(500)                          null comment '审核备注',
    remark         varchar(500)                          null comment '备注',
    create_by      varchar(64) default ''                null comment '创建者',
    create_time    datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_by      varchar(64) default ''                null comment '更新者',
    update_time    datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag       char        default '0'               null comment '删除标志（0代表存在 2代表删除）'
)
    comment '白名单申请答题记录表';

create index idx_pass_status
    on whitelist_quiz_submission (pass_status);

create index idx_player_name
    on whitelist_quiz_submission (player_name);

create index idx_player_uuid
    on whitelist_quiz_submission (player_uuid);

create index idx_submit_time
    on whitelist_quiz_submission (submit_time);

-- 白名单申请答题详情表
create table IF NOT EXISTS whitelist_quiz_submission_detail
(
    id            bigint auto_increment
        primary key,
    submission_id bigint                                not null comment '关联的提交记录ID',
    question_id   bigint                                not null comment '问题ID',
    question_type tinyint                               not null comment '问题类型：1-单选题，2-多选题，3-填空题',
    player_answer text                                  null comment '玩家答案',
    is_correct    tinyint(1)  default 0                 null comment '是否正确：0-错误，1-正确',
    score         int         default 0                 null comment '得分',
    remark        varchar(500)                          null comment '备注',
    create_by     varchar(64) default ''                null comment '创建者',
    create_time   datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_by     varchar(64) default ''                null comment '更新者',
    update_time   datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag      char        default '0'               null comment '删除标志（0代表存在 2代表删除）',
    constraint whitelist_quiz_submission_detail_ibfk_1
        foreign key (submission_id) references whitelist_quiz_submission (id)
            on delete cascade,
    constraint whitelist_quiz_submission_detail_ibfk_2
        foreign key (question_id) references whitelist_quiz_question (id)
)
    comment '白名单申请答题详情表';

create index idx_is_correct
    on whitelist_quiz_submission_detail (is_correct);

create index idx_question_id
    on whitelist_quiz_submission_detail (question_id);

create index idx_submission_id
    on whitelist_quiz_submission_detail (submission_id);

-- 白名单申请题库配置表
create table IF NOT EXISTS whitelist_quiz_config
(
    id           bigint auto_increment
        primary key,
    config_key   varchar(50)                           not null comment '配置键',
    config_value varchar(500)                          not null comment '配置值',
    description  varchar(255)                          null comment '配置描述',
    remark       varchar(500)                          null comment '备注',
    create_by    varchar(64) default ''                null comment '创建者',
    create_time  datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_by    varchar(64) default ''                null comment '更新者',
    update_time  datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag     char        default '0'               null comment '删除标志（0代表存在 2代表删除）',
    constraint uk_config_key
        unique (config_key)
)
    comment '白名单申请题库配置表';


-- 初始化配置数据
INSERT INTO whitelist_quiz_config (id, config_key, config_value, description, remark, create_by, create_time,
                                   update_by, update_time, del_flag)
VALUES (1, 'pass_score', '60', '通过分数线', null, '', '2025-03-19 19:07:48', '', '2025-03-19 19:07:48', '0');
INSERT INTO whitelist_quiz_config (id, config_key, config_value, description, remark, create_by, create_time,
                                   update_by, update_time, del_flag)
VALUES (3, 'question_count', '0', '每次答题的题目数量', null, '', '2025-03-19 19:07:48', '', '2025-03-23 16:39:16',
        '0');
INSERT INTO whitelist_quiz_config (id, config_key, config_value, description, remark, create_by, create_time,
                                   update_by, update_time, del_flag)
VALUES (5, 'random', 'false', '随机答题', null, '', '2025-03-19 19:07:48', '', '2025-03-23 16:38:22', '0');
INSERT INTO whitelist_quiz_config (id, config_key, config_value, description, remark, create_by, create_time,
                                   update_by, update_time, del_flag)
VALUES (6, 'status', 'true', '答题功能', null, '', '2025-03-19 19:07:48', '', '2025-03-19 19:07:48', '0');


-- 机器人配置表
CREATE TABLE IF NOT EXISTS qq_bot_config
(
    id                  INT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    name                VARCHAR(128)  NOT NULL COMMENT '机器人名称',
    bot_qq              VARCHAR(64)   NULL COMMENT '机器人QQ',
    http_url            VARCHAR(128)  NOT NULL COMMENT 'HTTP通讯地址',
    ws_url              VARCHAR(128)  NOT NULL COMMENT 'websocket地址',
    token               VARCHAR(128)  NULL COMMENT '秘钥',
    group_ids           VARCHAR(1000) NOT NULL COMMENT '群组',
    command_prefix      VARCHAR(16)   NULL DEFAULT '/' COMMENT '命令前缀',
    description         VARCHAR(512)  NULL COMMENT '机器人描述',
    last_login_time     DATETIME      NULL COMMENT '最后登录时间',
    last_heartbeat_time DATETIME      NULL COMMENT '最后心跳时间',
    error_msg           VARCHAR(512)  NULL COMMENT '最后一次错误信息',
    status              INT           NOT NULL COMMENT '启用状态',
    create_time         DATETIME      NULL COMMENT '创建时间',
    create_by           VARCHAR(128)  NULL COMMENT '创建者',
    update_time         DATETIME      NULL COMMENT '更新时间',
    update_by           VARCHAR(128)  NULL COMMENT '更新者',
    remark              VARCHAR(128)  NULL COMMENT '备注'
) COMMENT 'QQ机器人配置表';


-- 管理员表
CREATE TABLE IF NOT EXISTS qq_bot_manager
(
    id               INT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    bot_id           INT          NOT NULL COMMENT '关联的机器人ID',
    manager_name     VARCHAR(128) NULL COMMENT '管理员名称',
    manager_qq       VARCHAR(64)  NOT NULL COMMENT 'QQ',
    permission_type  INT          NOT NULL DEFAULT 1 COMMENT '权限类型：0=超级管理员，1=普通管理员',
    last_active_time DATETIME     NULL COMMENT '最后活动时间',
    status           INT          NOT NULL COMMENT '状态',
    create_time      DATETIME     NULL COMMENT '创建时间',
    create_by        VARCHAR(128) NULL COMMENT '创建者',
    update_time      DATETIME     NULL COMMENT '更新时间',
    update_by        VARCHAR(128) NULL COMMENT '更新者',
    remark           VARCHAR(512) NULL COMMENT '备注',
    CONSTRAINT fk_bot_manager_bot FOREIGN KEY (bot_id) REFERENCES qq_bot_config (id)
) COMMENT 'QQ机器人管理员表';

-- 管理员-群组关联表
CREATE TABLE IF NOT EXISTS qq_bot_manager_group
(
    id          INT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    manager_id  INT          NOT NULL COMMENT '管理员ID',
    group_id    VARCHAR(64)  NOT NULL COMMENT '群号',
    status      INT          NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    create_time DATETIME     NULL COMMENT '创建时间',
    create_by   VARCHAR(128) NULL COMMENT '创建者',
    update_time DATETIME     NULL COMMENT '更新时间',
    update_by   VARCHAR(128) NULL COMMENT '更新者',
    remark      VARCHAR(512) NULL COMMENT '备注',
    CONSTRAINT fk_manager_group_manager FOREIGN KEY (manager_id) REFERENCES qq_bot_manager (id)
) COMMENT 'QQ机器人管理员群组关联表';

-- 机器人日志表
CREATE TABLE IF NOT EXISTS qq_bot_log
(
    id              INT AUTO_INCREMENT COMMENT '主键' PRIMARY KEY,
    bot_id          INT          NOT NULL COMMENT '关联的机器人ID',
    log_type        INT          NOT NULL COMMENT '日志类型：1=接收消息，2=发送消息，3=方法调用，4=系统事件',
    message_id      VARCHAR(64)  NULL COMMENT '消息ID',
    sender_id       VARCHAR(64)  NULL COMMENT '发送者ID',
    sender_type     VARCHAR(32)  NULL COMMENT '发送者类型：user=用户，group=群组',
    receiver_id     VARCHAR(64)  NULL COMMENT '接收者ID',
    receiver_type   VARCHAR(32)  NULL COMMENT '接收者类型：user=用户，group=群组',
    message_content TEXT         NULL COMMENT '消息内容',
    message_type    VARCHAR(32)  NULL COMMENT '消息类型：text=文本，image=图片，voice=语音，file=文件等',
    method_name     VARCHAR(128) NULL COMMENT '调用的方法名称',
    method_params   TEXT         NULL COMMENT '方法参数(JSON格式)',
    method_result   TEXT         NULL COMMENT '方法执行结果',
    execution_time  INT          NULL COMMENT '方法执行时间(毫秒)',
    error_message   TEXT         NULL COMMENT '错误信息',
    stack_trace     TEXT         NULL COMMENT '错误堆栈信息',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_bot_log_bot FOREIGN KEY (bot_id) REFERENCES qq_bot_config (id)
) COMMENT 'QQ机器人日志表';

-- 索引
CREATE INDEX qq_bot_config_id_index ON qq_bot_config (id DESC);
CREATE INDEX idx_bot_status ON qq_bot_config (status);

CREATE INDEX idx_bot_manager_id ON qq_bot_manager (id DESC);
CREATE INDEX idx_bot_manager_qq ON qq_bot_manager (manager_qq);
CREATE INDEX idx_bot_manager_bot_id ON qq_bot_manager (bot_id);
CREATE INDEX idx_bot_manager_status ON qq_bot_manager (status);

CREATE INDEX idx_manager_group_manager_id ON qq_bot_manager_group (manager_id);
CREATE INDEX idx_manager_group_group_id ON qq_bot_manager_group (group_id);
CREATE INDEX idx_manager_group_status ON qq_bot_manager_group (status);

-- 日志表索引
CREATE INDEX idx_bot_log_bot_id ON qq_bot_log (bot_id);
CREATE INDEX idx_bot_log_log_type ON qq_bot_log (log_type);
CREATE INDEX idx_bot_log_create_time ON qq_bot_log (create_time);
CREATE INDEX idx_bot_log_sender_id ON qq_bot_log (sender_id);
CREATE INDEX idx_bot_log_receiver_id ON qq_bot_log (receiver_id);
CREATE INDEX idx_bot_log_method_name ON qq_bot_log (method_name);

-- 节点服务器
create table IF NOT EXISTS node_server
(
    id             bigint auto_increment comment '节点ID'
        primary key,
    uuid           varchar(64)                 not null comment '节点UUID',
    name           varchar(255)                not null comment '节点名称',
    ip             varchar(64)                 not null comment '服务器IP',
    port           int                         not null comment 'API端口',
    protocol       varchar(10)  default 'http' null comment '通信协议(http/https)',
    token          varchar(255)                not null comment '秘钥',
    status         char         default '0'    null comment '状态（0正常 1离线 2故障）',
    last_heartbeat datetime                    null comment '最后心跳时间',
    version        varchar(64)                 null comment '节点版本',
    os_type        varchar(20)                 null comment '操作系统类型',
    description    varchar(500) default ''     null comment '节点描述',
    create_by      varchar(64)  default ''     null comment '创建者',
    create_time    datetime                    null comment '创建时间',
    update_by      varchar(64)  default ''     null comment '更新者',
    update_time    datetime                    null comment '更新时间',
    remark         varchar(255)                null comment '备注',
    del_flag       char         default '0'    null comment '删除标志（0代表存在 1代表删除）'
)
    comment '节点服务器' collate = utf8mb4_general_ci;

create index idx_heartbeat
    on node_server (last_heartbeat);

create index idx_status
    on node_server (status);

drop table if exists node_operation_log;
-- 节点操作日志表
create table IF NOT EXISTS node_operation_log
(
    id                 int auto_increment comment '日志ID'
        primary key,
    node_id            int                     null comment '节点ID',
    operation_type     varchar(64)             not null comment '操作类型（1新增节点 2修改节点 3删除节点 4下载日志 5启动游戏服务器 6停止游戏服务器 7重启游戏服务器 8强制终止游戏服务器 9新增游戏服务器 10修改游戏服务器 11删除游戏服务器）',
    operation_target   varchar(20)             not null comment '操作目标类型（1节点服务器 2游戏服务器）',
    node_obj_id        int                     null comment '节点对象ID',
    game_server_obj_id int                     null comment '游戏服务器对象ID',
    operation_name     varchar(255)            not null comment '操作名称',
    method_name        varchar(256)            null comment '方法名',
    operation_param    text                    null comment '操作参数',
    operation_result   text                    null comment '操作结果详情',
    execution_time     int                     null comment '执行耗时(ms)',
    operation_ip       varchar(128)            null comment '操作者IP地址',
    status             char        default '0' null comment '操作状态（0成功 1失败）',
    error_msg          varchar(2000)           null comment '错误消息',
    create_by          varchar(64) default ''  null comment '创建者',
    create_time        datetime                null comment '创建时间',
    update_by          varchar(64) default ''  null comment '更新者',
    update_time        datetime                null comment '更新时间',
    remark             varchar(255)            null comment '备注',
    del_flag           char        default '0' null comment '删除标志（0代表存在 1代表删除）'
)
    comment '节点操作日志' collate = utf8mb4_general_ci;

create index idx_create_time
    on node_operation_log (create_time);

create index idx_execution_time
    on node_operation_log (execution_time);

create index idx_game_server_obj_id
    on node_operation_log (game_server_obj_id);

create index idx_node_id
    on node_operation_log (node_id);

create index idx_node_obj_id
    on node_operation_log (node_obj_id);

create index idx_operation_target
    on node_operation_log (operation_target);

create index idx_operation_type
    on node_operation_log (operation_type);

create index idx_status
    on node_operation_log (status);

-- Minecraft游戏服务端表
create table IF NOT EXISTS node_minecraft_server
(
    id                bigint auto_increment comment '游戏服务器ID'
        primary key,
    node_id           bigint                   not null comment '所属节点ID',
    node_uuid         varchar(64)              not null comment '节点UUID',
    node_instances_id int                      null comment '节点实例ID',
    name              varchar(255)             not null comment '服务器名称',
    server_path       varchar(500)             not null comment '服务端所在目录',
    start_str         text                     not null comment '启动命令',
    java_path         text                     null comment 'Java路径',
    java_env_id       int                      null comment '环境ID',
    jvm_xmx           varchar(20)              not null comment '最大堆内存(XMX)',
    jvm_xms           varchar(20)              not null comment '最小堆内存(XMS)',
    jvm_args          varchar(1000)            null comment '其他JVM参数',
    core_type         varchar(64)              not null comment '核心类型(如：Paper、Spigot、Bukkit等)',
    version           varchar(64)              not null comment '核心版本',
    status            char         default '0' null comment '服务器状态（0未启动 1运行中 2已停止 3异常）',
    last_start_time   datetime                 null comment '最后启动时间',
    last_stop_time    datetime                 null comment '最后停止时间',
    description       varchar(500) default ''  null comment '服务器描述',
    create_by         varchar(64)  default ''  null comment '创建者',
    create_time       datetime                 null comment '创建时间',
    update_by         varchar(64)  default ''  null comment '更新者',
    update_time       datetime                 null comment '更新时间',
    remark            varchar(255)             null comment '备注',
    del_flag          char         default '0' null comment '删除标志（0代表存在 1代表删除）'
)
    comment 'Minecraft游戏服务端' collate = utf8mb4_general_ci;

create index idx_create_time
    on node_minecraft_server (create_time);

create index idx_node_id
    on node_minecraft_server (node_id);

create index idx_node_uuid
    on node_minecraft_server (node_uuid);

create index idx_status
    on node_minecraft_server (status);

drop table if exists node_env;

-- 节点Java多版本环境管理表
CREATE TABLE IF NOT EXISTS node_env
(
    id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',

    node_id     INT          NOT NULL COMMENT '节点ID',
    version     VARCHAR(64)  NOT NULL COMMENT 'Java版本号，如 1.8.0_361 / 17 / 21',
    env_name    VARCHAR(64)  NULL COMMENT '环境名称自定义，如 JAVA_17、JAVA_8',

    path        VARCHAR(512) NOT NULL COMMENT 'Java安装根路径（可能不是JAVA_HOME）',
    java_home   VARCHAR(512) NULL COMMENT 'JAVA_HOME路径',
    bin_path    VARCHAR(512) NULL COMMENT 'bin目录路径',

    type        VARCHAR(32)  NOT NULL DEFAULT 'JDK' COMMENT '安装类型：JDK、JRE、Runtime',
    arch        VARCHAR(32)  NULL COMMENT '架构：amd64、arm64、x86',

    is_default  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否为节点默认Java版本：0否|1是',
    valid       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '路径是否有效：1有效|0无效',
    source      VARCHAR(64)  NULL COMMENT '来源：manual、auto_detect、system、sdkman等',

    status      INT          NOT NULL DEFAULT 1 COMMENT '状态：1=正常|0=禁用|9=已删除',

    create_time DATETIME     NOT NULL COMMENT '创建时间',
    create_by   VARCHAR(64)  NULL COMMENT '创建者',
    update_time DATETIME     NULL COMMENT '更新时间',
    update_by   VARCHAR(64)  NULL COMMENT '更新者',

    remark      TEXT         NULL COMMENT '备注',

    -- 防止同一节点重复添加同一版本
    UNIQUE KEY uk_node_version (node_id, version),

    KEY idx_node_id (node_id),
    KEY idx_node_default (node_id, is_default),
    KEY idx_path_prefix (path(64)),
    KEY idx_java_home_prefix (java_home(64)),
    KEY idx_source (source),
    KEY idx_valid (valid),
    KEY idx_status (status)
) COMMENT ='节点Java多版本环境管理表';

-- 用户反馈记录表（本地存储，通过UUID关联远程反馈系统）
CREATE TABLE IF NOT EXISTS sys_feedback_record
(
    id            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    uuid          varchar(64)  NOT NULL COMMENT '反馈UUID（关联远程反馈系统）',
    user_id       bigint      DEFAULT NULL COMMENT '用户ID',
    user_name     varchar(64) DEFAULT NULL COMMENT '用户名',
    feedback_type int          NOT NULL COMMENT '反馈类型：1-Bug反馈 2-功能建议 3-使用问题 4-其他',
    title         varchar(200) NOT NULL COMMENT '反馈标题',
    status        int         DEFAULT 0 COMMENT '状态：0-待处理 1-处理中 2-已解决 3-已关闭',
    create_time   datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_uuid (uuid),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户反馈记录表';

-- ----------------------------
-- 群组指令功能配置表
-- 用于管理每个群组中每个功能指令的独立开关
-- ----------------------------
DROP TABLE IF EXISTS bot_group_command_config;
CREATE TABLE bot_group_command_config
(
    id               bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    group_id         varchar(50) NOT NULL COMMENT '群组ID',
    command_key      varchar(50) NOT NULL COMMENT '指令关键字（主命令名称）',
    command_name     varchar(100)         DEFAULT NULL COMMENT '指令显示名称',
    command_category varchar(50)          DEFAULT NULL COMMENT '指令分类（user/admin/super）',
    is_enabled       tinyint(1)  NOT NULL DEFAULT 1 COMMENT '是否启用（0=禁用，1=启用）',
    disabled_message varchar(500)         DEFAULT NULL COMMENT '禁用时的提示消息',
    create_time      datetime             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      datetime             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by        varchar(64)          DEFAULT NULL COMMENT '创建者',
    update_by        varchar(64)          DEFAULT NULL COMMENT '更新者',
    remark           varchar(500)         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_command (group_id, command_key) COMMENT '群组+指令唯一索引',
    KEY idx_group_id (group_id) COMMENT '群组ID索引',
    KEY idx_command_key (command_key) COMMENT '指令关键字索引',
    KEY idx_is_enabled (is_enabled) COMMENT '启用状态索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='群组指令功能配置表';

-- ----------------------------
-- 初始化默认指令配置数据
-- 注意：这里只是示例数据，实际使用时需要根据具体群组ID进行配置
-- ----------------------------

-- 普通用户指令
INSERT INTO bot_group_command_config (group_id, command_key, command_name, command_category, is_enabled,
                                      remark)
VALUES ('default', 'help', '帮助信息', 'user', 1, '显示帮助信息'),
       ('default', '白名单申请', '白名单申请', 'user', 1, '申请白名单'),
       ('default', '查询白名单', '查询白名单', 'user', 1, '查询自己的白名单状态'),
       ('default', '查询玩家', '查询玩家', 'user', 1, '查询指定玩家信息'),
       ('default', '查询在线', '查询在线', 'user', 1, '查询所有服务器在线玩家'),
       ('default', '查询服务器', '查询服务器', 'user', 1, '查询服务器列表'),
       ('default', 'test', '测试连通', 'user', 1, '测试服务器连通性');

-- 系统通知功能（非指令类功能）
INSERT INTO bot_group_command_config (group_id, command_key, command_name, command_category, is_enabled,
                                      remark)
VALUES ('default', '玩家上线通知', '玩家上线通知', 'system', 1, '玩家加入游戏时发送通知'),
       ('default', '玩家下线通知', '玩家下线通知', 'system', 1, '玩家离开游戏时发送通知');


-- 管理员指令
INSERT INTO bot_group_command_config (group_id, command_key, command_name, command_category, is_enabled,
                                      remark)
VALUES ('default', '过审', '白名单审核通过', 'admin', 1, '通过白名单申请'),
       ('default', '拒审', '白名单审核拒绝', 'admin', 1, '拒绝白名单申请'),
       ('default', '封禁', '封禁玩家', 'admin', 1, '封禁玩家'),
       ('default', '解封', '解封玩家', 'admin', 1, '解除玩家封禁'),
       ('default', '发送指令', 'RCON指令', 'admin', 1, '发送RCON指令'),
       ('default', '运行状态', '主机状态', 'admin', 1, '查看主机运行状态'),
       ('default', '刷新连接', '刷新连接', 'admin', 1, '刷新RCON连接'),
       ('default', '测试连接', '测试连接', 'admin', 1, '测试RCON连接'),
       ('default', '实例列表', '实例列表', 'admin', 1, '查看游戏服务器实例'),
       ('default', '启动实例', '启动实例', 'admin', 1, '启动实例'),
       ('default', '停止实例', '停止实例', 'admin', 1, '停止实例'),
       ('default', '重启实例', '重启实例', 'admin', 1, '重启实例'),
       ('default', '实例状态', '实例状态', 'admin', 1, '查看实例状态'),
       ('default', '实例日志', '实例日志', 'admin', 1, '查看实例日志'),
       ('default', '实例命令', '实例命令', 'admin', 1, '发送实例命令'),
       ('default', '节点状态', '节点状态', 'admin', 1, '查看节点服务器状态');

-- 超级管理员指令
INSERT INTO bot_group_command_config (group_id, command_key, command_name, command_category, is_enabled,
                                      remark)
VALUES ('default', '添加管理', '添加管理员', 'super', 1, '添加普通管理员'),
       ('default', '添加超管', '添加超级管理员', 'super', 1, '添加超级管理员');

-- 功能开关指令（这些指令不受开关限制）
INSERT INTO bot_group_command_config (group_id, command_key, command_name, command_category, is_enabled,
                                      remark)
VALUES ('default', '关闭', '关闭功能', 'admin', 1, '关闭指定功能（不可被关闭）'),
       ('default', '开启', '开启功能', 'admin', 1, '开启指定功能（不可被关闭）'),
       ('default', '功能列表', '功能列表', 'user', 1, '查看所有功能及状态（不可被关闭）');


-- 玩家活跃度统计相关表结构

-- 1. 玩家活跃度统计表
CREATE TABLE player_activity_stats
(
    id                   bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    stats_date           date        NOT NULL COMMENT '统计日期',
    stats_type           varchar(20) NOT NULL COMMENT '统计类型：daily-日报, weekly-周报, monthly-月报',
    active_player_count  int(11)      DEFAULT '0' COMMENT '活跃玩家数量',
    new_player_count     int(11)      DEFAULT '0' COMMENT '新增玩家数量',
    total_online_minutes bigint(20)   DEFAULT '0' COMMENT '总在线时长（分钟）',
    avg_online_minutes   bigint(20)   DEFAULT '0' COMMENT '平均在线时长（分钟）',
    peak_online_count    int(11)      DEFAULT '0' COMMENT '峰值在线人数',
    peak_online_time     datetime     DEFAULT NULL COMMENT '峰值在线时间',
    active_player_list   text COMMENT '活跃玩家列表（JSON格式）',
    new_player_list      text COMMENT '新增玩家列表（JSON格式）',
    period_start         datetime     DEFAULT NULL COMMENT '统计周期开始时间',
    period_end           datetime     DEFAULT NULL COMMENT '统计周期结束时间',
    create_time          datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time          datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark               varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_stats_date_type (stats_date, stats_type),
    KEY idx_stats_type (stats_type),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='玩家活跃度统计表';

-- 2. 每日玩家活跃度记录表
CREATE TABLE daily_player_activity
(
    id               bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    player_name      varchar(50) NOT NULL COMMENT '玩家名',
    player_id        bigint(20)     DEFAULT NULL COMMENT '玩家ID（关联白名单）',
    activity_date    date        NOT NULL COMMENT '记录日期',
    online_minutes   bigint(20)     DEFAULT '0' COMMENT '当日在线时长（分钟）',
    login_count      int(11)        DEFAULT '0' COMMENT '当日登录次数',
    first_login_time datetime       DEFAULT NULL COMMENT '首次登录时间',
    last_login_time  datetime       DEFAULT NULL COMMENT '最后登录时间',
    last_logout_time datetime       DEFAULT NULL COMMENT '最后离线时间',
    is_new_player    tinyint(1)     DEFAULT '0' COMMENT '是否为新玩家（当日首次加入）',
    activity_score   decimal(10, 2) DEFAULT '0.00' COMMENT '活跃度评分',
    create_time      datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_player_date (player_name, activity_date),
    KEY idx_activity_date (activity_date),
    KEY idx_player_name (player_name),
    KEY idx_activity_score (activity_score)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='每日玩家活跃度记录表';

-- 3. 玩家在线时长历史记录表（用于详细追踪）
CREATE TABLE player_online_history
(
    id             bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    player_name    varchar(50) NOT NULL COMMENT '玩家名',
    server_id      bigint(20) DEFAULT NULL COMMENT '服务器ID',
    login_time     datetime    NOT NULL COMMENT '登录时间',
    logout_time    datetime   DEFAULT NULL COMMENT '离线时间',
    online_minutes bigint(20) DEFAULT '0' COMMENT '本次在线时长（分钟）',
    session_date   date        NOT NULL COMMENT '会话日期',
    create_time    datetime   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_player_name (player_name),
    KEY idx_session_date (session_date),
    KEY idx_login_time (login_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='玩家在线时长历史记录表';

-- 4. 服务器每日统计表
CREATE TABLE server_daily_stats
(
    id                   bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    server_id            bigint(20) NOT NULL COMMENT '服务器ID',
    stats_date           date       NOT NULL COMMENT '统计日期',
    total_online_minutes bigint(20)     DEFAULT '0' COMMENT '服务器总在线时长',
    unique_player_count  int(11)        DEFAULT '0' COMMENT '独立玩家数',
    peak_online_count    int(11)        DEFAULT '0' COMMENT '峰值在线人数',
    peak_online_time     datetime       DEFAULT NULL COMMENT '峰值在线时间',
    avg_online_minutes   decimal(10, 2) DEFAULT '0.00' COMMENT '平均在线时长',
    create_time          datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time          datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_server_date (server_id, stats_date),
    KEY idx_stats_date (stats_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='服务器每日统计表';


DROP TABLE IF EXISTS rcon_node_instance_relation;
CREATE TABLE rcon_node_instance_relation
(
    id             bigint  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    rcon_server_id bigint  NOT NULL COMMENT 'RCON服务器ID(关联server_info表)',
    node_id        bigint  NOT NULL COMMENT '节点ID(关联node_server表)',
    instance_id    bigint  NOT NULL COMMENT '实例ID(关联node_minecraft_server表)',
    status         char(1) NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_by      varchar(64)      DEFAULT '' COMMENT '创建者',
    create_time    datetime         DEFAULT NULL COMMENT '创建时间',
    update_by      varchar(64)      DEFAULT '' COMMENT '更新者',
    update_time    datetime         DEFAULT NULL COMMENT '更新时间',
    remark         varchar(500)     DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_rcon_server (rcon_server_id) COMMENT '确保一个RCON服务器只能关联一个实例',
    UNIQUE KEY uk_instance (instance_id) COMMENT '确保一个实例只能被一个RCON服务器关联',
    KEY idx_node_id (node_id),
    KEY idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='RCON和节点实例关联表(一对一关系)';

