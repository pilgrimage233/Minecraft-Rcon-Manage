-- 节点服务器
create table node_server
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
create table node_operation_log
(
    id                 int auto_increment comment '日志ID'
        primary key,
    node_id            int          null comment '节点ID',
    operation_type     varchar(64)             not null comment '操作类型（1新增节点 2修改节点 3删除节点 4下载日志 5启动游戏服务器 6停止游戏服务器 7重启游戏服务器 8强制终止游戏服务器 9新增游戏服务器 10修改游戏服务器 11删除游戏服务器）',
    operation_target   varchar(20)             not null comment '操作目标类型（1节点服务器 2游戏服务器）',
    node_obj_id        int          null comment '节点对象ID',
    game_server_obj_id int          null comment '游戏服务器对象ID',
    operation_name     varchar(255)            not null comment '操作名称',
    method_name        varchar(256) null comment '方法名',
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


drop table if exists node_minecraft_server;
-- Minecraft游戏服务端表
create table node_minecraft_server
(
    id                bigint auto_increment comment '游戏服务器ID'
        primary key,
    node_id           bigint                   not null comment '所属节点ID',
    node_uuid         varchar(64)              not null comment '节点UUID',
    node_instances_id int                      null comment '节点实例ID',
    name              varchar(255)             not null comment '服务器名称',
    server_path       varchar(500)             not null comment '服务端所在目录',
    start_str         text                     not null comment '启动命令',
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

