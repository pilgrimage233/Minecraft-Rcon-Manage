-- 白名单申请题库问题表
create table whitelist_quiz_question
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
create table whitelist_quiz_answer
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
create table whitelist_quiz_submission
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
create table whitelist_quiz_submission_detail
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
create table whitelist_quiz_config
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