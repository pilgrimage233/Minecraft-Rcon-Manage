-- 机器人配置表
CREATE TABLE qq_bot_config
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
CREATE TABLE qq_bot_manager
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
CREATE TABLE qq_bot_manager_group
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
