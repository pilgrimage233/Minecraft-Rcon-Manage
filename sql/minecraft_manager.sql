/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : minecraft_manager

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 03/03/2025 16:01:08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for banlist_info
-- ----------------------------
DROP TABLE IF EXISTS `banlist_info`;
CREATE TABLE `banlist_info`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `white_id`    bigint                                                        NULL DEFAULT NULL COMMENT '关联白名单ID',
    `user_name`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名称',
    `state`       bigint                                                        NULL DEFAULT NULL COMMENT '封禁状态',
    `reason`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封禁原因',
    `remark`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    `create_time` datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `create_by`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
    `update_by`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '封禁管理表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of banlist_info
-- ----------------------------

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
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
  AUTO_INCREMENT = 11
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
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
  AUTO_INCREMENT = 129
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表字段'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------
INSERT INTO `gen_table_column`
VALUES (14, 2, 'id', '主键ID', 'int(11)', 'Long', 'id', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1,
        'admin', '2023-12-26 21:56:56', '', '2023-12-26 22:00:11');
INSERT INTO `gen_table_column`
VALUES (15, 2, 'time', '申请时间', 'datetime', 'Date', 'time', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'datetime', '',
        2, 'admin', '2023-12-26 21:56:56', '', '2023-12-26 22:00:11');
INSERT INTO `gen_table_column`
VALUES (16, 2, 'user_name', '游戏名称', 'varchar(256)', 'String', 'userName', '0', '0', '0', '1', '1', '1', '1', 'LIKE',
        'input', '', 3, 'admin', '2023-12-26 21:56:56', '', '2023-12-26 22:00:11');
INSERT INTO `gen_table_column`
VALUES (17, 2, 'user_uuid', NULL, 'varchar(256)', 'String', 'userUuid', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'input', '', 4, 'admin', '2023-12-26 21:56:57', '', '2023-12-26 22:00:11');
INSERT INTO `gen_table_column`
VALUES (18, 2, 'online_flag', '正版标识', 'int(11)', 'Long', 'onlineFlag', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'input', '', 5, 'admin', '2023-12-26 21:56:57', '', '2023-12-26 22:00:11');
INSERT INTO `gen_table_column`
VALUES (19, 2, 'qq_num', '用户QQ号', 'varchar(64)', 'String', 'qqNum', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input',
        '', 6, 'admin', '2023-12-26 21:56:57', '', '2023-12-26 22:00:11');
INSERT INTO `gen_table_column`
VALUES (20, 2, 'remark', '描述', 'text', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'textarea', '',
        7, 'admin', '2023-12-26 21:56:57', '', '2023-12-26 22:00:11');
INSERT INTO `gen_table_column`
VALUES (21, 2, 'review_users', '审核用户', 'varchar(256)', 'String', 'reviewUsers', '0', '0', '0', '1', '1', '1', '1',
        'EQ', 'input', '', 8, 'admin', '2023-12-26 21:56:57', '', '2023-12-26 22:00:11');
INSERT INTO `gen_table_column`
VALUES (22, 2, 'status', '审核状态', 'varchar(32)', 'String', 'status', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'radio', '', 9, 'admin', '2023-12-26 21:56:57', '', '2023-12-26 22:00:11');
INSERT INTO `gen_table_column`
VALUES (23, 2, 'add_state', '添加状态', 'varchar(64)', 'String', 'addState', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'input', '', 10, 'admin', '2023-12-26 21:56:57', '', '2023-12-26 22:00:11');
INSERT INTO `gen_table_column`
VALUES (24, 2, 'add_time', '添加时间', 'datetime', 'Date', 'addTime', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'datetime', '', 11, 'admin', '2023-12-26 21:56:57', '', '2023-12-26 22:00:11');
INSERT INTO `gen_table_column`
VALUES (25, 2, 'remove_reason', '移除原因', 'text', 'String', 'removeReason', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'textarea', '', 12, 'admin', '2023-12-26 21:56:57', '', '2023-12-26 22:00:11');
INSERT INTO `gen_table_column`
VALUES (26, 2, 'remove_time', '移除时间', 'datetime', 'Date', 'removeTime', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'datetime', '', 13, 'admin', '2023-12-26 21:56:57', '', '2023-12-26 22:00:11');
INSERT INTO `gen_table_column`
VALUES (27, 3, 'id', '主键ID', 'int', 'Long', 'id', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin',
        '2024-03-10 15:46:36', '', '2024-03-10 15:57:12');
INSERT INTO `gen_table_column`
VALUES (28, 3, 'uuid', '随机UUID', 'varchar(64)', 'String', 'uuid', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input',
        '', 2, 'admin', '2024-03-10 15:46:36', '', '2024-03-10 15:57:12');
INSERT INTO `gen_table_column`
VALUES (29, 3, 'name_tag', '服务器名称标签', 'varchar(128)', 'String', 'nameTag', '0', '0', '1', '1', '1', '1', '1',
        'EQ', 'input', '', 3, 'admin', '2024-03-10 15:46:36', '', '2024-03-10 15:57:12');
INSERT INTO `gen_table_column`
VALUES (30, 3, 'ip', '服务器IP', 'varchar(128)', 'String', 'ip', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '',
        4, 'admin', '2024-03-10 15:46:36', '', '2024-03-10 15:57:12');
INSERT INTO `gen_table_column`
VALUES (31, 3, 'rcon_port', 'RCON远程端口号', 'int', 'Long', 'rconPort', '0', '0', '1', '1', '1', '1', '1', 'EQ',
        'input', '', 5, 'admin', '2024-03-10 15:46:36', '', '2024-03-10 15:57:12');
INSERT INTO `gen_table_column`
VALUES (32, 3, 'rcon_password', '远程密码/MD5加密', 'varchar(256)', 'String', 'rconPassword', '0', '0', '1', '1', '1',
        '1', '1', 'EQ', 'input', '', 6, 'admin', '2024-03-10 15:46:36', '', '2024-03-10 15:57:12');
INSERT INTO `gen_table_column`
VALUES (33, 3, 'creat_time', '创建时间', 'datetime', 'Date', 'creatTime', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'datetime', '', 7, 'admin', '2024-03-10 15:46:36', '', '2024-03-10 15:57:12');
INSERT INTO `gen_table_column`
VALUES (34, 3, 'creat_by', '创建者', 'varchar(128)', 'String', 'creatBy', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'input', '', 8, 'admin', '2024-03-10 15:46:36', '', '2024-03-10 15:57:12');
INSERT INTO `gen_table_column`
VALUES (35, 3, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ',
        'datetime', '', 9, 'admin', '2024-03-10 15:46:36', '', '2024-03-10 15:57:12');
INSERT INTO `gen_table_column`
VALUES (36, 3, 'update_by', '更新者', 'varchar(128)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ',
        'input', '', 10, 'admin', '2024-03-10 15:46:36', '', '2024-03-10 15:57:12');
INSERT INTO `gen_table_column`
VALUES (37, 3, 'status', '启用状态', 'int', 'Long', 'status', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'radio', '', 11,
        'admin', '2024-03-10 15:46:36', '', '2024-03-10 15:57:12');
INSERT INTO `gen_table_column`
VALUES (38, 3, 'remark', '描述', 'text', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'textarea', '',
        12, 'admin', '2024-03-10 15:46:36', '', '2024-03-10 15:57:12');
INSERT INTO `gen_table_column`
VALUES (39, 4, 'id', '主键ID', 'bigint(20)', 'Long', 'id', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1,
        'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (40, 4, 'uuid', '随机UUID', 'varchar(255)', 'String', 'uuid', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input',
        '', 2, 'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (41, 4, 'ip', 'IP地址', 'varchar(255)', 'String', 'ip', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3,
        'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (42, 4, 'user_agent', 'UA标识', 'varchar(255)', 'String', 'userAgent', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'input', '', 4, 'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (43, 4, 'count', '请求次数', 'bigint(20)', 'Long', 'count', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '',
        5, 'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (44, 4, 'province', '省', 'varchar(255)', 'String', 'province', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input',
        '', 6, 'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (45, 4, 'city', '地市', 'varchar(255)', 'String', 'city', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '',
        7, 'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (46, 4, 'county', '区县', 'varchar(255)', 'String', 'county', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input',
        '', 8, 'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (47, 4, 'longitude', '经度', 'varchar(255)', 'String', 'longitude', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'input', '', 9, 'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (48, 4, 'latitude', '纬度', 'varchar(255)', 'String', 'latitude', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'input', '', 10, 'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (49, 4, 'body_params', '请求参数', 'text', 'String', 'bodyParams', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'textarea', '', 11, 'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (50, 4, 'remark', '备注', 'varchar(255)', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'input',
        '', 12, 'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (51, 4, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ',
        'datetime', '', 13, 'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (52, 4, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ',
        'datetime', '', 14, 'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (53, 4, 'create_by', '创建者', 'varchar(255)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ',
        'input', '', 15, 'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (54, 4, 'update_by', '更新者', 'varchar(255)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ',
        'input', '', 16, 'admin', '2024-12-20 19:11:37', '', '2024-12-20 19:24:18');
INSERT INTO `gen_table_column`
VALUES (55, 5, 'id', '主键ID', 'bigint(20)', 'Long', 'id', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1,
        'admin', '2024-12-20 19:15:07', '', '2024-12-20 19:26:41');
INSERT INTO `gen_table_column`
VALUES (56, 5, 'white_id', '关联白名单ID', 'bigint(20)', 'Long', 'whiteId', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'input', '', 2, 'admin', '2024-12-20 19:15:07', '', '2024-12-20 19:26:41');
INSERT INTO `gen_table_column`
VALUES (57, 5, 'user_name', '用户名称', 'varchar(255)', 'String', 'userName', '0', '0', '0', '1', '1', '1', '1', 'LIKE',
        'input', '', 3, 'admin', '2024-12-20 19:15:07', '', '2024-12-20 19:26:41');
INSERT INTO `gen_table_column`
VALUES (58, 5, 'state', '封禁状态', 'bigint(20)', 'Long', 'state', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '',
        4, 'admin', '2024-12-20 19:15:07', '', '2024-12-20 19:26:41');
INSERT INTO `gen_table_column`
VALUES (59, 5, 'reason', '封禁原因', 'varchar(255)', 'String', 'reason', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'input', '', 5, 'admin', '2024-12-20 19:15:07', '', '2024-12-20 19:26:41');
INSERT INTO `gen_table_column`
VALUES (60, 5, 'remark', '备注', 'varchar(255)', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'input',
        '', 6, 'admin', '2024-12-20 19:15:07', '', '2024-12-20 19:26:41');
INSERT INTO `gen_table_column`
VALUES (61, 5, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ',
        'datetime', '', 7, 'admin', '2024-12-20 19:15:07', '', '2024-12-20 19:26:41');
INSERT INTO `gen_table_column`
VALUES (62, 5, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ',
        'datetime', '', 8, 'admin', '2024-12-20 19:15:07', '', '2024-12-20 19:26:41');
INSERT INTO `gen_table_column`
VALUES (63, 5, 'create_by', '创建者', 'varchar(255)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ',
        'input', '', 9, 'admin', '2024-12-20 19:15:07', '', '2024-12-20 19:26:41');
INSERT INTO `gen_table_column`
VALUES (64, 5, 'update_by', '更新者', 'varchar(255)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ',
        'input', '', 10, 'admin', '2024-12-20 19:15:07', '', '2024-12-20 19:26:41');
INSERT INTO `gen_table_column`
VALUES (65, 6, 'id', '主键ID', 'bigint(20)', 'Long', 'id', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1,
        'admin', '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (66, 6, 'server_id', '服务器ID', 'varchar(255)', 'String', 'serverId', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'input', '', 2, 'admin', '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (67, 6, 'online_add_whitelist_command', '在线模式白名单添加指令', 'varchar(255)', 'String',
        'onlineAddWhitelistCommand', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin',
        '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (68, 6, 'offline_add_whitelist_command', '离线模式白名单添加指令', 'varchar(255)', 'String',
        'offlineAddWhitelistCommand', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin',
        '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (69, 6, 'online_rm_whitelist_command', '在线模式白名单移除指令', 'varchar(255)', 'String',
        'onlineRmWhitelistCommand', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin',
        '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (70, 6, 'offline_rm_whitelist_command', '离线模式白名单移除指令', 'varchar(255)', 'String',
        'offlineRmWhitelistCommand', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin',
        '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (71, 6, 'online_add_ban_command', '在线模式添加封禁指令', 'varchar(255)', 'String', 'onlineAddBanCommand', '0',
        '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 7, 'admin', '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (72, 6, 'offline_add_ban_command', '离线模式添加封禁指令', 'varchar(255)', 'String', 'offlineAddBanCommand', '0',
        '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (73, 6, 'online_rm_ban_command', '在线模式移除封禁指令', 'varchar(255)', 'String', 'onlineRmBanCommand', '0',
        '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 9, 'admin', '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (74, 6, 'offline_rm_ban_command', '离线模式移除封禁指令', 'varchar(255)', 'String', 'offlineRmBanCommand', '0',
        '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 10, 'admin', '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (75, 6, 'easyauth', '是否启用EasyAuthMod', 'varchar(255)', 'String', 'easyauth', '0', '0', '0', '1', '1', '1',
        '1', 'EQ', 'input', '', 11, 'admin', '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (76, 6, 'remark', '备注', 'varchar(255)', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'input',
        '', 12, 'admin', '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (77, 6, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ',
        'datetime', '', 13, 'admin', '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (78, 6, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ',
        'datetime', '', 14, 'admin', '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (79, 6, 'create_by', '创建者', 'varchar(255)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ',
        'input', '', 15, 'admin', '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (80, 6, 'update_by', '更新者', 'varchar(255)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ',
        'input', '', 16, 'admin', '2024-12-20 19:28:58', '', '2024-12-20 19:29:31');
INSERT INTO `gen_table_column`
VALUES (81, 7, 'id', 'ID', 'int(11)', 'Long', 'id', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin',
        '2024-12-31 03:09:59', '', '2025-01-11 12:09:32');
INSERT INTO `gen_table_column`
VALUES (82, 7, 'user_name', '玩家昵称', 'varchar(128)', 'String', 'userName', '0', '0', '1', '1', '1', '1', '1', 'LIKE',
        'input', '', 2, 'admin', '2024-12-31 03:09:59', '', '2025-01-11 12:09:32');
INSERT INTO `gen_table_column`
VALUES (83, 7, 'uuid', 'UUID', 'varchar(64)', 'String', 'uuid', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 3,
        'admin', '2024-12-31 03:09:59', '', '2025-01-11 12:09:32');
INSERT INTO `gen_table_column`
VALUES (84, 7, 'status', '状态', 'int(11)', 'Long', 'status', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'radio', '', 4,
        'admin', '2024-12-31 03:09:59', '', '2025-01-11 12:09:32');
INSERT INTO `gen_table_column`
VALUES (86, 7, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ',
        'datetime', '', 6, 'admin', '2024-12-31 03:09:59', '', '2025-01-11 12:09:32');
INSERT INTO `gen_table_column`
VALUES (87, 7, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ',
        'datetime', '', 7, 'admin', '2024-12-31 03:09:59', '', '2025-01-11 12:09:32');
INSERT INTO `gen_table_column`
VALUES (88, 7, 'create_by', '创建者', 'varchar(128)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ',
        'input', '', 8, 'admin', '2024-12-31 03:09:59', '', '2025-01-11 12:09:32');
INSERT INTO `gen_table_column`
VALUES (89, 7, 'update_by', '更新者', 'varchar(128)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ',
        'input', '', 9, 'admin', '2024-12-31 03:09:59', '', '2025-01-11 12:09:32');
INSERT INTO `gen_table_column`
VALUES (90, 7, 'remark', '备注', 'varchar(128)', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'input',
        '', 10, 'admin', '2024-12-31 03:09:59', '', '2025-01-11 12:09:32');
INSERT INTO `gen_table_column`
VALUES (91, 8, 'id', '', 'int(11)', 'Long', 'id', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin',
        '2024-12-31 03:09:59', '', '2024-12-31 03:33:14');
INSERT INTO `gen_table_column`
VALUES (92, 8, 'user_name', '玩家昵称', 'varchar(128)', 'String', 'userName', '0', '0', '1', '1', '1', '1', '1', 'LIKE',
        'input', '', 2, 'admin', '2024-12-31 03:09:59', '', '2024-12-31 03:33:14');
INSERT INTO `gen_table_column`
VALUES (93, 8, 'qq', 'QQ号', 'varchar(64)', 'String', 'qq', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3,
        'admin', '2024-12-31 03:09:59', '', '2024-12-31 03:33:14');
INSERT INTO `gen_table_column`
VALUES (94, 8, 'identity', '身份', 'varchar(64)', 'String', 'identity', '0', '0', '1', '1', '1', '1', '1', 'EQ',
        'input', '', 4, 'admin', '2024-12-31 03:09:59', '', '2024-12-31 03:33:14');
INSERT INTO `gen_table_column`
VALUES (95, 8, 'last_time', '最后游玩时间', 'datetime', 'Date', 'lastTime', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'datetime', '', 5, 'admin', '2024-12-31 03:09:59', '', '2024-12-31 03:33:14');
INSERT INTO `gen_table_column`
VALUES (96, 8, 'province', '省份', 'varchar(128)', 'String', 'province', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'input', '', 6, 'admin', '2024-12-31 03:09:59', '', '2024-12-31 03:33:14');
INSERT INTO `gen_table_column`
VALUES (97, 8, 'city', '地市', 'varchar(128)', 'String', 'city', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '',
        7, 'admin', '2024-12-31 03:09:59', '', '2024-12-31 03:33:14');
INSERT INTO `gen_table_column`
VALUES (98, 8, 'whitelist_id', '白名单ID', 'int(11)', 'Long', 'whitelistId', '0', '0', '0', '1', '0', '0', '0', 'EQ',
        'input', '', 8, 'admin', '2024-12-31 03:09:59', '', '2024-12-31 03:33:14');
INSERT INTO `gen_table_column`
VALUES (99, 8, 'banlist_id', '封禁ID', 'int(11)', 'Long', 'banlistId', '0', '0', '0', '1', '0', '0', '0', 'EQ', 'input',
        '', 9, 'admin', '2024-12-31 03:09:59', '', '2024-12-31 03:33:14');
INSERT INTO `gen_table_column`
VALUES (101, 8, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '1', '1', NULL, NULL, NULL, 'EQ',
        'datetime', '', 11, 'admin', '2024-12-31 03:09:59', '', '2024-12-31 03:33:14');
INSERT INTO `gen_table_column`
VALUES (102, 8, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ',
        'datetime', '', 12, 'admin', '2024-12-31 03:09:59', '', '2024-12-31 03:33:14');
INSERT INTO `gen_table_column`
VALUES (103, 8, 'create_by', '创建者', 'varchar(64)', 'String', 'createBy', '0', '0', '1', '1', NULL, NULL, NULL, 'EQ',
        'input', '', 13, 'admin', '2024-12-31 03:09:59', '', '2024-12-31 03:33:14');
INSERT INTO `gen_table_column`
VALUES (104, 8, 'update_by', '更新者', 'varchar(64)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ',
        'input', '', 14, 'admin', '2024-12-31 03:09:59', '', '2024-12-31 03:33:14');
INSERT INTO `gen_table_column`
VALUES (105, 8, 'remark', '备注', 'varchar(256)', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'input',
        '', 15, 'admin', '2024-12-31 03:09:59', '', '2024-12-31 03:33:14');
INSERT INTO `gen_table_column`
VALUES (106, 8, 'parameters', '其他参数', 'varchar(256)', 'String', 'parameters', '0', '0', '0', '1', '0', '0', '0',
        'EQ', 'input', '', 10, '', '2024-12-31 03:16:31', '', '2024-12-31 03:33:14');
INSERT INTO `gen_table_column`
VALUES (107, 7, 'parameter', '其他参数', 'varchar(256)', 'String', 'parameter', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'input', '', 5, '', '2025-01-11 12:09:19', '', '2025-01-11 12:09:32');
INSERT INTO `gen_table_column`
VALUES (108, 9, 'id', '主键ID', 'int(11)', 'Long', 'id', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1,
        'admin', '2025-02-11 20:08:30', '', '2025-02-11 20:10:34');
INSERT INTO `gen_table_column`
VALUES (109, 9, 'server_id', '服务器ID', 'int(11)', 'Long', 'serverId', '0', '0', '1', '1', '1', '1', '1', 'EQ',
        'input', '', 2, 'admin', '2025-02-11 20:08:30', '', '2025-02-11 20:10:34');
INSERT INTO `gen_table_column`
VALUES (110, 9, 'user', '执行用户', 'varchar(64)', 'String', 'user', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input',
        '', 3, 'admin', '2025-02-11 20:08:30', '', '2025-02-11 20:10:34');
INSERT INTO `gen_table_column`
VALUES (111, 9, 'command', '执行指令', 'varchar(1000)', 'String', 'command', '0', '0', '1', '1', '1', '1', '1', 'EQ',
        'textarea', '', 4, 'admin', '2025-02-11 20:08:30', '', '2025-02-11 20:10:34');
INSERT INTO `gen_table_column`
VALUES (112, 9, 'execute_time', '执行时间', 'datetime', 'Date', 'executeTime', '0', '0', '1', '1', '1', '1', '1', 'EQ',
        'datetime', '', 5, 'admin', '2025-02-11 20:08:30', '', '2025-02-11 20:10:34');
INSERT INTO `gen_table_column`
VALUES (113, 9, 'response', '执行结果', 'text', 'String', 'response', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'textarea', '', 6, 'admin', '2025-02-11 20:08:30', '', '2025-02-11 20:10:34');
INSERT INTO `gen_table_column`
VALUES (114, 9, 'status', '执行状态', 'varchar(8)', 'String', 'status', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'radio', '', 7, 'admin', '2025-02-11 20:08:30', '', '2025-02-11 20:10:34');
INSERT INTO `gen_table_column`
VALUES (115, 9, 'run_time', '运行时间(毫秒值)', 'varchar(64)', 'String', 'runTime', '0', '0', '0', '1', '1', '1', '1',
        'EQ', 'input', '', 8, 'admin', '2025-02-11 20:08:30', '', '2025-02-11 20:10:34');
INSERT INTO `gen_table_column`
VALUES (116, 10, 'id', '主键', 'int(11)', 'Long', 'id', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1,
        'admin', '2025-02-14 23:30:54', '', '2025-02-14 23:39:47');
INSERT INTO `gen_table_column`
VALUES (117, 10, 'cmd', '指令', 'varchar(256)', 'String', 'cmd', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '',
        2, 'admin', '2025-02-14 23:30:54', '', '2025-02-14 23:39:47');
INSERT INTO `gen_table_column`
VALUES (118, 10, 'result', '执行结果', 'varchar(256)', 'String', 'result', '0', '0', '0', '1', '1', '1', '1', 'EQ',
        'input', '', 3, 'admin', '2025-02-14 23:30:54', '', '2025-02-14 23:39:47');
INSERT INTO `gen_table_column`
VALUES (119, 10, 'history_count', '执行历史保留次数', 'int(11)', 'Long', 'historyCount', '0', '0', '0', '1', '1', '1',
        '1', 'EQ', 'input', '', 4, 'admin', '2025-02-14 23:30:54', '', '2025-02-14 23:39:47');
INSERT INTO `gen_table_column`
VALUES (120, 10, 'history_result', '历史结果', 'text', 'String', 'historyResult', '0', '0', '0', '1', '1', '1', '1',
        'EQ', 'textarea', '', 5, 'admin', '2025-02-14 23:30:54', '', '2025-02-14 23:39:47');
INSERT INTO `gen_table_column`
VALUES (121, 10, 'cron', 'Cron表达式', 'varchar(64)', 'String', 'cron', '0', '0', '1', '1', '1', '1', '1', 'EQ',
        'input', '', 6, 'admin', '2025-02-14 23:30:54', '', '2025-02-14 23:39:47');
INSERT INTO `gen_table_column`
VALUES (122, 10, 'status', '状态', 'int(11)', 'Long', 'status', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'radio', '', 7,
        'admin', '2025-02-14 23:30:54', '', '2025-02-14 23:39:47');
INSERT INTO `gen_table_column`
VALUES (123, 10, 'execute_count', '执行次数', 'int(11)', 'Long', 'executeCount', '0', '0', '0', '1', '1', '1', '1',
        'EQ', 'input', '', 8, 'admin', '2025-02-14 23:30:54', '', '2025-02-14 23:39:47');
INSERT INTO `gen_table_column`
VALUES (124, 10, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL,
        'EQ', 'datetime', '', 9, 'admin', '2025-02-14 23:30:54', '', '2025-02-14 23:39:47');
INSERT INTO `gen_table_column`
VALUES (125, 10, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ',
        'datetime', '', 10, 'admin', '2025-02-14 23:30:54', '', '2025-02-14 23:39:47');
INSERT INTO `gen_table_column`
VALUES (126, 10, 'create_by', '创建者', 'varchar(128)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL,
        'EQ', 'input', '', 11, 'admin', '2025-02-14 23:30:54', '', '2025-02-14 23:39:47');
INSERT INTO `gen_table_column`
VALUES (127, 10, 'update_by', '更新者', 'varchar(128)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ',
        'input', '', 12, 'admin', '2025-02-14 23:30:54', '', '2025-02-14 23:39:47');
INSERT INTO `gen_table_column`
VALUES (128, 10, 'remark', '备注', 'varchar(256)', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ',
        'input', '', 13, 'admin', '2025-02-14 23:30:54', '', '2025-02-14 23:39:47');

-- ----------------------------
-- Table structure for history_command
-- ----------------------------
DROP TABLE IF EXISTS `history_command`;
CREATE TABLE `history_command`
(
    `id`           int                                                            NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `server_id`    int                                                            NOT NULL COMMENT '服务器ID',
    `user`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NOT NULL COMMENT '执行用户',
    `command`      varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行指令',
    `execute_time` datetime                                                       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    `response`     text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          NULL COMMENT '执行结果',
    `status`       varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci    NULL     DEFAULT NULL COMMENT '执行状态',
    `run_time`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NULL     DEFAULT NULL COMMENT '运行时间(毫秒值)',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `history_command_server_id_index` (`server_id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 49
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '历史命令'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of history_command
-- ----------------------------

-- ----------------------------
-- Table structure for ip_limit_info
-- ----------------------------
DROP TABLE IF EXISTS `ip_limit_info`;
CREATE TABLE `ip_limit_info`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `uuid`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '随机UUID',
    `ip`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP地址',
    `user_agent`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'UA标识',
    `count`       bigint                                                        NULL DEFAULT NULL COMMENT '请求次数',
    `province`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '省',
    `city`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地市',
    `county`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '区县',
    `longitude`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '经度',
    `latitude`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '纬度',
    `body_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         NULL COMMENT '请求参数',
    `remark`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    `create_time` datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `create_by`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
    `update_by`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 13
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = 'IP限流表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ip_limit_info
-- ----------------------------

-- ----------------------------
-- Table structure for operator_list
-- ----------------------------
DROP TABLE IF EXISTS `operator_list`;
CREATE TABLE `operator_list`
(
    `id`          int                                                           NOT NULL AUTO_INCREMENT,
    `user_name`   varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_german2_ci NOT NULL COMMENT '玩家昵称',
    `status`      int                                                           NULL DEFAULT NULL COMMENT '状态',
    `parameter`   varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_german2_ci NULL DEFAULT NULL COMMENT '其他参数',
    `create_time` datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `create_by`   varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_german2_ci NULL DEFAULT NULL COMMENT '创建者',
    `update_by`   varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_german2_ci NULL DEFAULT NULL COMMENT '更新者',
    `remark`      varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_german2_ci NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `operator_list_id_index` (`id` ASC) USING BTREE,
    INDEX `operator_list_user_name_index` (`user_name` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 5
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_german2_ci COMMENT = '管理员列表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of operator_list
-- ----------------------------

-- ----------------------------
-- Table structure for player_details
-- ----------------------------
DROP TABLE IF EXISTS `player_details`;
CREATE TABLE `player_details`
(
    `id`                int                                                           NOT NULL AUTO_INCREMENT,
    `user_name`         varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '玩家昵称',
    `qq`                varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL DEFAULT NULL COMMENT 'QQ号',
    `identity`          varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NOT NULL COMMENT '身份',
    `last_online_time`  datetime                                                      NULL DEFAULT NULL COMMENT '最后上线时间',
    `last_offline_time` datetime                                                      NULL DEFAULT NULL COMMENT '最后离线时间',
    `game_time`         int                                                           NULL DEFAULT NULL COMMENT '游戏时间',
    `province`          varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '省份',
    `city`              varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '地市',
    `whitelist_id`      int                                                           NULL DEFAULT NULL COMMENT '白名单ID',
    `banlist_id`        int                                                           NULL DEFAULT NULL COMMENT '封禁ID',
    `parameters`        varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '其他参数',
    `create_time`       datetime                                                      NOT NULL COMMENT '创建时间',
    `update_time`       datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `create_by`         varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NOT NULL COMMENT '创建者',
    `update_by`         varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL DEFAULT NULL COMMENT '更新者',
    `remark`            varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `player_details_id_whitelist_id_index` (`id` ASC, `whitelist_id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 38
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_general_ci COMMENT = '玩家信息'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of player_details
-- ----------------------------

-- ----------------------------
-- Table structure for public_server_command
-- ----------------------------
DROP TABLE IF EXISTS `public_server_command`;
CREATE TABLE `public_server_command`
(
    `id`             bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `server_id`      bigint                                                        NULL DEFAULT NULL COMMENT '服务器ID',
    `command`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '指令',
    `status`         bigint                                                        NULL DEFAULT NULL COMMENT '启用状态',
    `vague_matching` bigint                                                        NULL DEFAULT NULL COMMENT '模糊匹配',
    `remark`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    `create_time`    datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`    datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `create_by`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
    `update_by`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '公开命令表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of public_server_command
-- ----------------------------

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
-- Table structure for regular_cmd
-- ----------------------------
DROP TABLE IF EXISTS `regular_cmd`;
CREATE TABLE `regular_cmd`
(
    `id`             int                                                           NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_name`      varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
    `task_id`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '任务id',
    `cmd`            varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '指令',
    `execute_server` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '执行服务器',
    `result`         varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行结果',
    `history_count`  int                                                           NULL DEFAULT NULL COMMENT '执行历史保留次数',
    `history_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         NULL COMMENT '历史结果',
    `cron`           varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT 'Cron表达式',
    `status`         int                                                           NOT NULL COMMENT '状态',
    `execute_count`  int                                                           NULL DEFAULT NULL COMMENT '执行次数',
    `create_time`    datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`    datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `create_by`      varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
    `update_by`      varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
    `remark`         varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '定时命令'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of regular_cmd
-- ----------------------------

-- ----------------------------
-- Table structure for server_command_info
-- ----------------------------
DROP TABLE IF EXISTS `server_command_info`;
CREATE TABLE `server_command_info`
(
    `id`                            bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `server_id`                     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务器ID',
    `online_add_whitelist_command`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '在线模式白名单添加指令',
    `offline_add_whitelist_command` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '离线模式白名单添加指令',
    `online_rm_whitelist_command`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '在线模式白名单移除指令',
    `offline_rm_whitelist_command`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '离线模式白名单移除指令',
    `online_add_ban_command`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '在线模式添加封禁指令',
    `offline_add_ban_command`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '离线模式添加封禁指令',
    `online_rm_ban_command`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '在线模式移除封禁指令',
    `offline_rm_ban_command`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '离线模式移除封禁指令',
    `easyauth`                      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否启用EasyAuthMod',
    `remark`                        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    `create_time`                   datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`                   datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `create_by`                     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
    `update_by`                     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '指令管理表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of server_command_info
-- ----------------------------

-- ----------------------------
-- Table structure for server_info
-- ----------------------------
DROP TABLE IF EXISTS `server_info`;
create table server_info
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

create index server_info_name_tag_index
    on server_info (name_tag);



-- ----------------------------
-- Records of server_info
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
  AUTO_INCREMENT = 7
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
  AUTO_INCREMENT = 110
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
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`
(
    `dict_code`   bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '字典编码',
    `dict_sort`   int                                                           NULL DEFAULT 0 COMMENT '字典排序',
    `dict_label`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典标签',
    `dict_value`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典键值',
    `dict_type`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
    `css_class`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
    `list_class`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '表格回显样式',
    `is_default`  char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
    `status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 118
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '字典数据表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data`
VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', NULL, '性别男');
INSERT INTO `sys_dict_data`
VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL, '性别女');
INSERT INTO `sys_dict_data`
VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL, '性别未知');
INSERT INTO `sys_dict_data`
VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '显示菜单');
INSERT INTO `sys_dict_data`
VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '隐藏菜单');
INSERT INTO `sys_dict_data`
VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '正常状态');
INSERT INTO `sys_dict_data`
VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '停用状态');
INSERT INTO `sys_dict_data`
VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '正常状态');
INSERT INTO `sys_dict_data`
VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '停用状态');
INSERT INTO `sys_dict_data`
VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '默认分组');
INSERT INTO `sys_dict_data`
VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '系统分组');
INSERT INTO `sys_dict_data`
VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '系统默认是');
INSERT INTO `sys_dict_data`
VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL, '系统默认否');
INSERT INTO `sys_dict_data`
VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '通知');
INSERT INTO `sys_dict_data`
VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '公告');
INSERT INTO `sys_dict_data`
VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '正常状态');
INSERT INTO `sys_dict_data`
VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '关闭状态');
INSERT INTO `sys_dict_data`
VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '其他操作');
INSERT INTO `sys_dict_data`
VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '新增操作');
INSERT INTO `sys_dict_data`
VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '修改操作');
INSERT INTO `sys_dict_data`
VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '删除操作');
INSERT INTO `sys_dict_data`
VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '授权操作');
INSERT INTO `sys_dict_data`
VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '导出操作');
INSERT INTO `sys_dict_data`
VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '导入操作');
INSERT INTO `sys_dict_data`
VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '强退操作');
INSERT INTO `sys_dict_data`
VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '生成操作');
INSERT INTO `sys_dict_data`
VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '清空操作');
INSERT INTO `sys_dict_data`
VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '正常状态');
INSERT INTO `sys_dict_data`
VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2023-12-26 16:54:02', '', NULL,
        '停用状态');
INSERT INTO `sys_dict_data`
VALUES (100, 1, '待审核', '0', 'white_examine_status', NULL, 'warning', 'N', '0', 'admin', '2023-12-27 18:07:29',
        'admin', '2023-12-27 18:27:23', NULL);
INSERT INTO `sys_dict_data`
VALUES (101, 2, '已过审', '1', 'white_examine_status', NULL, 'success', 'N', '0', 'admin', '2023-12-27 18:07:47',
        'admin', '2023-12-27 18:27:26', NULL);
INSERT INTO `sys_dict_data`
VALUES (102, 3, '已拒审', '2', 'white_examine_status', NULL, 'danger', 'N', '0', 'admin', '2023-12-27 18:08:15',
        'admin', '2023-12-27 18:27:30', NULL);
INSERT INTO `sys_dict_data`
VALUES (103, 1, '未添加', '0', 'white_add_status', NULL, 'info', 'N', '0', 'admin', '2023-12-27 18:40:35', 'admin',
        '2023-12-27 18:40:43', NULL);
INSERT INTO `sys_dict_data`
VALUES (104, 2, '已添加', '1', 'white_add_status', NULL, 'success', 'N', '0', 'admin', '2023-12-27 18:41:05', 'admin',
        '2024-01-14 13:38:09', NULL);
INSERT INTO `sys_dict_data`
VALUES (105, 3, '已删除', '2', 'white_add_status', NULL, 'warning', 'N', '0', 'admin', '2023-12-27 18:41:20', 'admin',
        '2023-12-27 18:41:44', NULL);
INSERT INTO `sys_dict_data`
VALUES (106, 4, '已封禁', '9', 'white_add_status', NULL, 'danger', 'N', '0', 'admin', '2023-12-27 18:42:07', 'admin',
        '2023-12-27 19:32:11', NULL);
INSERT INTO `sys_dict_data`
VALUES (107, 1, '正版', '1', 'online_status', NULL, 'primary', 'N', '0', 'admin', '2023-12-27 15:28:43', 'admin',
        '2023-12-27 15:29:00', NULL);
INSERT INTO `sys_dict_data`
VALUES (108, 2, '离线', '0', 'online_status', NULL, 'info', 'N', '0', 'admin', '2023-12-27 15:29:13', '', NULL, NULL);
INSERT INTO `sys_dict_data`
VALUES (109, 0, '停用', '0', 'server_status', NULL, 'danger', 'N', '0', 'admin', '2024-03-10 17:13:17', '', NULL, NULL);
INSERT INTO `sys_dict_data`
VALUES (110, 0, '启用', '1', 'server_status', NULL, 'success', 'N', '0', 'admin', '2024-03-10 17:13:36', '', NULL,
        NULL);
INSERT INTO `sys_dict_data`
VALUES (111, 0, '已封禁', '1', 'ban_status', NULL, 'danger', 'N', '0', 'admin', '2024-12-23 17:43:23', '', NULL, NULL);
INSERT INTO `sys_dict_data`
VALUES (112, 0, '已解封', '0', 'ban_status', NULL, 'warning', 'N', '0', 'admin', '2024-12-23 17:47:28', '', NULL, NULL);
INSERT INTO `sys_dict_data`
VALUES (113, 0, '玩家', 'player', 'player_identity', NULL, 'primary', 'N', '0', 'admin', '2024-12-31 04:06:48', 'admin',
        '2024-12-31 04:09:36', NULL);
INSERT INTO `sys_dict_data`
VALUES (114, 0, '管理', 'operator', 'player_identity', NULL, 'success', 'N', '0', 'admin', '2024-12-31 04:07:52',
        'admin', '2024-12-31 04:08:16', NULL);
INSERT INTO `sys_dict_data`
VALUES (115, 0, '封禁', 'banned', 'player_identity', NULL, 'danger', 'N', '0', 'admin', '2024-12-31 04:09:02', 'admin',
        '2024-12-31 04:09:15', NULL);
INSERT INTO `sys_dict_data`
VALUES (116, 0, '启用', '1', 'op_status', NULL, 'success', 'N', '0', 'admin', '2025-01-12 10:25:26', '', NULL, NULL);
INSERT INTO `sys_dict_data`
VALUES (117, 0, '停用', '0', 'op_status', NULL, 'warning', 'N', '0', 'admin', '2025-01-12 10:25:38', '', NULL, NULL);

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`
(
    `dict_id`     bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '字典主键',
    `dict_name`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典名称',
    `dict_type`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
    `status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `create_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`dict_id`) USING BTREE,
    UNIQUE INDEX `dict_type` (`dict_type` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 107
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '字典类型表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type`
VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2023-12-26 16:54:02', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type`
VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2023-12-26 16:54:02', '', NULL, '菜单状态列表');
INSERT INTO `sys_dict_type`
VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2023-12-26 16:54:02', '', NULL, '系统开关列表');
INSERT INTO `sys_dict_type`
VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2023-12-26 16:54:02', '', NULL, '任务状态列表');
INSERT INTO `sys_dict_type`
VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2023-12-26 16:54:02', '', NULL, '任务分组列表');
INSERT INTO `sys_dict_type`
VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2023-12-26 16:54:02', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type`
VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2023-12-26 16:54:02', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type`
VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2023-12-26 16:54:02', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type`
VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2023-12-26 16:54:02', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type`
VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2023-12-26 16:54:02', '', NULL, '登录状态列表');
INSERT INTO `sys_dict_type`
VALUES (100, '审核状态', 'white_examine_status', '0', 'admin', '2023-12-27 18:07:01', '', NULL, NULL);
INSERT INTO `sys_dict_type`
VALUES (101, '添加状态', 'white_add_status', '0', 'admin', '2023-12-27 18:38:34', 'admin', '2023-12-27 18:43:28', NULL);
INSERT INTO `sys_dict_type`
VALUES (102, '正版标识', 'online_status', '0', 'admin', '2023-12-27 15:28:19', '', NULL, NULL);
INSERT INTO `sys_dict_type`
VALUES (103, '服务器开关状态', 'server_status', '0', 'admin', '2024-03-10 17:12:54', '', NULL, NULL);
INSERT INTO `sys_dict_type`
VALUES (104, '封禁状态', 'ban_status', '0', 'admin', '2024-12-23 17:41:42', '', NULL, NULL);
INSERT INTO `sys_dict_type`
VALUES (105, '玩家身份组', 'player_identity', '0', 'admin', '2024-12-31 04:06:12', '', NULL, NULL);
INSERT INTO `sys_dict_type`
VALUES (106, '管理员状态', 'op_status', '0', 'admin', '2025-01-12 10:25:05', '', NULL, NULL);

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`
(
    `job_id`          bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `job_name`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL DEFAULT '' COMMENT '任务名称',
    `job_group`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
    `invoke_target`   varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
    `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT '' COMMENT 'cron执行表达式',
    `misfire_policy`  varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL     DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
    `concurrent`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL     DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
    `status`          char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      NULL     DEFAULT '0' COMMENT '状态（0正常 1暂停）',
    `create_by`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL     DEFAULT '' COMMENT '创建者',
    `create_time`     datetime                                                      NULL     DEFAULT NULL COMMENT '创建时间',
    `update_by`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL     DEFAULT '' COMMENT '更新者',
    `update_time`     datetime                                                      NULL     DEFAULT NULL COMMENT '更新时间',
    `remark`          varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT '' COMMENT '备注信息',
    PRIMARY KEY (`job_id`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 10
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '定时任务调度表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_job
-- ----------------------------
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES (4, '同步白名单', 'DEFAULT', 'whiteListTask.syncWhitelistByServerId(\'3\')', '30 1 0/1 * * ?', '1', '1', '0',
        'admin', '2024-12-21 18:56:29', 'admin', '2025-03-08 19:42:48', '');
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
VALUES (8, '心跳检测', 'DEFAULT', 'rconTask.heartBeat', '0 0/3 * * * ?', '1', '1', '1', 'admin', '2024-12-25 23:41:20',
        'admin', '2025-01-03 02:26:19', '');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES (9, '玩家监控', 'DEFAULT', 'onlineTask.monitor', '0 0/1 * * * ?', '1', '1', '0', 'admin', '2025-01-02 05:14:41',
        '', '2025-01-02 22:56:40', '');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES (10, '指令重试', 'DEFAULT', 'onlineTask.commandRetry', '0 0/1 * * * ?', '1', '1', '0', 'admin',
        '2025-03-07 18:25:35', 'admin', '2025-03-07 18:27:34', '');
INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent,
                           status, create_by, create_time, update_by, update_time, remark)
VALUES (11, '退群监控', 'DEFAULT', 'botTask.monitorWhiteList', '0 0/5 * * * ?', '1', '1', '1', 'admin',
        '2025-03-09 19:05:55', '', '2025-03-26 22:00:30', '');


-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log`
(
    `job_log_id`     bigint                                                         NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
    `job_name`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NOT NULL COMMENT '任务名称',
    `job_group`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NOT NULL COMMENT '任务组名',
    `invoke_target`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '调用目标字符串',
    `job_message`    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL DEFAULT NULL COMMENT '日志信息',
    `status`         char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci       NULL DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
    `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '异常信息',
    `create_time`    datetime                                                       NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 74107
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '定时任务调度日志表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_job_log
-- ----------------------------
INSERT INTO `sys_job_log`
VALUES (74101, '玩家监控', 'DEFAULT', 'onlineTask.monitor', '玩家监控 总共耗时：0毫秒', '0', '', '2025-03-03 15:55:00');
INSERT INTO `sys_job_log`
VALUES (74102, '玩家监控', 'DEFAULT', 'onlineTask.monitor', '玩家监控 总共耗时：0毫秒', '0', '', '2025-03-03 15:56:00');
INSERT INTO `sys_job_log`
VALUES (74103, '玩家监控', 'DEFAULT', 'onlineTask.monitor', '玩家监控 总共耗时：0毫秒', '0', '', '2025-03-03 15:57:00');
INSERT INTO `sys_job_log`
VALUES (74104, '玩家监控', 'DEFAULT', 'onlineTask.monitor', '玩家监控 总共耗时：1毫秒', '0', '', '2025-03-03 15:58:00');
INSERT INTO `sys_job_log`
VALUES (74105, '玩家监控', 'DEFAULT', 'onlineTask.monitor', '玩家监控 总共耗时：3毫秒', '0', '', '2025-03-03 16:00:00');
INSERT INTO `sys_job_log`
VALUES (74106, '玩家监控', 'DEFAULT', 'onlineTask.monitor', '玩家监控 总共耗时：0毫秒', '0', '', '2025-03-03 16:01:00');

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
  AUTO_INCREMENT = 547
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
  AUTO_INCREMENT = 2075
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '菜单权限表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1, '系统管理', 0, 5, 'system', null, '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2023-12-26 16:54:02',
        'admin', '2025-03-04 23:09:22', '系统管理目录');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2, '系统监控', 0, 4, 'monitor', null, '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', '2023-12-26 16:54:02',
        'admin', '2025-03-12 15:55:10', '系统监控目录');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (3, '系统工具', 0, 6, 'tool', null, '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2023-12-26 16:54:02', 'admin',
        '2025-03-12 15:55:25', '系统工具目录');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user',
        'admin', '2023-12-26 16:54:02', '', null, '用户管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples',
        'admin', '2023-12-26 16:54:02', '', null, '角色管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table',
        'admin', '2023-12-26 16:54:02', '', null, '菜单管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', 1, 0, 'C', '1', '0', 'system:dept:list', 'tree',
        'admin', '2023-12-26 16:54:02', 'admin', '2023-12-26 21:37:32', '部门管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', 1, 0, 'C', '1', '0', 'system:post:list', 'post',
        'admin', '2023-12-26 16:54:02', 'admin', '2023-12-26 21:37:37', '岗位管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', 1, 0, 'C', '0', '0', 'system:dict:list', 'dict',
        'admin', '2023-12-26 16:54:02', '', null, '字典管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit',
        'admin', '2023-12-26 16:54:02', '', null, '参数设置菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', 1, 0, 'C', '0', '0', 'system:notice:list',
        'message', 'admin', '2023-12-26 16:54:02', '', null, '通知公告菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (108, '日志管理', 1, 9, 'log', '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', '2023-12-26 16:54:02', '', null,
        '日志管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', 1, 0, 'C', '0', '0', 'monitor:online:list',
        'online', 'admin', '2023-12-26 16:54:02', '', null, '在线用户菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (110, '定时任务', 2, 2, 'job', 'monitor/job/index', '', 1, 0, 'C', '0', '0', 'monitor:job:list', 'job', 'admin',
        '2023-12-26 16:54:02', '', null, '定时任务菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (111, '数据监控', 2, 3, 'druid', 'monitor/druid/index', '', 1, 0, 'C', '0', '0', 'monitor:druid:list', 'druid',
        'admin', '2023-12-26 16:54:02', '', null, '数据监控菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (112, '服务监控', 2, 4, 'server', 'monitor/server/index', '', 1, 0, 'C', '0', '0', 'monitor:server:list',
        'server', 'admin', '2023-12-26 16:54:02', '', null, '服务监控菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (113, '缓存监控', 2, 5, 'cache', 'monitor/cache/index', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis',
        'admin', '2023-12-26 16:54:02', '', null, '缓存监控菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (114, '缓存列表', 2, 6, 'cacheList', 'monitor/cache/list', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',
        'redis-list', 'admin', '2023-12-26 16:54:02', '', null, '缓存列表菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (115, '表单构建', 3, 1, 'build', 'tool/build/index', '', 1, 0, 'C', '0', '0', 'tool:build:list', 'build',
        'admin', '2023-12-26 16:54:02', '', null, '表单构建菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (116, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin',
        '2023-12-26 16:54:02', '', null, '代码生成菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (117, '系统接口', 3, 3, 'swagger', 'tool/swagger/index', '', 1, 0, 'C', '0', '0', 'tool:swagger:list', 'swagger',
        'admin', '2023-12-26 16:54:02', '', null, '系统接口菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list',
        'form', 'admin', '2023-12-26 16:54:02', '', null, '操作日志菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', 1, 0, 'C', '0', '0',
        'monitor:logininfor:list', 'logininfor', 'admin', '2023-12-26 16:54:02', '', null, '登录日志菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1000, '用户查询', 100, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1001, '用户新增', 100, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1002, '用户修改', 100, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1003, '用户删除', 100, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1004, '用户导出', 100, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1005, '用户导入', 100, 6, '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1006, '重置密码', 100, 7, '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1007, '角色查询', 101, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1008, '角色新增', 101, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1009, '角色修改', 101, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1010, '角色删除', 101, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1011, '角色导出', 101, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1012, '菜单查询', 102, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1013, '菜单新增', 102, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1014, '菜单修改', 102, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1015, '菜单删除', 102, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1016, '部门查询', 103, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1017, '部门新增', 103, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1018, '部门修改', 103, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1019, '部门删除', 103, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1020, '岗位查询', 104, 1, '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1021, '岗位新增', 104, 2, '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1022, '岗位修改', 104, 3, '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1023, '岗位删除', 104, 4, '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1024, '岗位导出', 104, 5, '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1025, '字典查询', 105, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1026, '字典新增', 105, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1027, '字典修改', 105, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1028, '字典删除', 105, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1029, '字典导出', 105, 5, '#', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1030, '参数查询', 106, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1031, '参数新增', 106, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1032, '参数修改', 106, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1033, '参数删除', 106, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1034, '参数导出', 106, 5, '#', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1035, '公告查询', 107, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1036, '公告新增', 107, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1037, '公告修改', 107, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1038, '公告删除', 107, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1039, '操作查询', 500, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1040, '操作删除', 500, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1041, '日志导出', 500, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1042, '登录查询', 501, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1043, '登录删除', 501, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1044, '日志导出', 501, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1045, '账户解锁', 501, 4, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1046, '在线查询', 109, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1047, '批量强退', 109, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1048, '单条强退', 109, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1049, '任务查询', 110, 1, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1050, '任务新增', 110, 2, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1051, '任务修改', 110, 3, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1052, '任务删除', 110, 4, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1053, '状态修改', 110, 5, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1054, '任务导出', 110, 6, '#', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1055, '生成查询', 116, 1, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1056, '生成修改', 116, 2, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1057, '生成删除', 116, 3, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1058, '导入代码', 116, 4, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1059, '预览代码', 116, 5, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (1060, '生成代码', 116, 6, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin',
        '2023-12-26 16:54:02', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2000, '服务器管理', 0, 1, 'mc', null, null, 1, 0, 'M', '0', '0', '', 'server', 'admin', '2023-12-26 21:28:37',
        'admin', '2023-12-26 21:56:23', '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2009, '白名单管理', 2000, 1, 'whitelist', 'mc/whitelist/index', null, 1, 0, 'C', '0', '0', 'mc:whitelist:list',
        'peoples', 'admin', '2023-12-26 22:02:01', 'admin', '2023-12-26 22:56:52', '白名单菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2010, '白名单查询', 2009, 1, '#', '', null, 1, 0, 'F', '0', '0', 'mc:whitelist:query', '#', 'admin',
        '2023-12-26 22:02:01', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2011, '白名单新增', 2009, 2, '#', '', null, 1, 0, 'F', '0', '0', 'mc:whitelist:add', '#', 'admin',
        '2023-12-26 22:02:01', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2012, '白名单修改', 2009, 3, '#', '', null, 1, 0, 'F', '0', '0', 'mc:whitelist:edit', '#', 'admin',
        '2023-12-26 22:02:01', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2013, '白名单删除', 2009, 4, '#', '', null, 1, 0, 'F', '0', '0', 'mc:whitelist:remove', '#', 'admin',
        '2023-12-26 22:02:01', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2014, '白名单导出', 2009, 5, '#', '', null, 1, 0, 'F', '0', '0', 'mc:whitelist:export', '#', 'admin',
        '2023-12-26 22:02:01', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2021, '服务器信息', 2000, 1, 'serverlist', 'server/serverlist/index', null, 1, 0, 'C', '0', '0',
        'server:serverlist:list', 'server', 'admin', '2024-03-10 16:00:54', 'admin', '2024-03-10 16:01:38',
        '服务器信息菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2022, '服务器信息查询', 2021, 1, '#', '', null, 1, 0, 'F', '0', '0', 'server:serverlist:query', '#', 'admin',
        '2024-03-10 16:00:54', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2023, '服务器信息新增', 2021, 2, '#', '', null, 1, 0, 'F', '0', '0', 'server:serverlist:add', '#', 'admin',
        '2024-03-10 16:00:54', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2024, '服务器信息修改', 2021, 3, '#', '', null, 1, 0, 'F', '0', '0', 'server:serverlist:edit', '#', 'admin',
        '2024-03-10 16:00:54', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2025, '服务器信息删除', 2021, 4, '#', '', null, 1, 0, 'F', '0', '0', 'server:serverlist:remove', '#', 'admin',
        '2024-03-10 16:00:54', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2026, '服务器信息导出', 2021, 5, '#', '', null, 1, 0, 'F', '0', '0', 'server:serverlist:export', '#', 'admin',
        '2024-03-10 16:00:54', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2033, 'IP限流', 2000, 1, 'limit', 'ipinfo/limit/index', null, 1, 0, 'C', '0', '0', 'ipinfo:limit:list',
        'number', 'admin', '2024-12-20 19:25:20', 'admin', '2024-12-20 19:25:56', 'IP限流菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2034, 'IP限流查询', 2033, 1, '#', '', null, 1, 0, 'F', '0', '0', 'ipinfo:limit:query', '#', 'admin',
        '2024-12-20 19:25:20', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2035, 'IP限流新增', 2033, 2, '#', '', null, 1, 0, 'F', '0', '0', 'ipinfo:limit:add', '#', 'admin',
        '2024-12-20 19:25:20', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2036, 'IP限流修改', 2033, 3, '#', '', null, 1, 0, 'F', '0', '0', 'ipinfo:limit:edit', '#', 'admin',
        '2024-12-20 19:25:20', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2037, 'IP限流删除', 2033, 4, '#', '', null, 1, 0, 'F', '0', '0', 'ipinfo:limit:remove', '#', 'admin',
        '2024-12-20 19:25:20', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2038, 'IP限流导出', 2033, 5, '#', '', null, 1, 0, 'F', '0', '0', 'ipinfo:limit:export', '#', 'admin',
        '2024-12-20 19:25:20', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2039, '封禁管理', 2000, 1, 'banlist', 'mc/banlist/index', null, 1, 0, 'C', '0', '0', 'mc:banlist:list', 'lock',
        'admin', '2024-12-20 19:26:57', 'admin', '2024-12-20 19:28:40', '封禁管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2040, '封禁管理查询', 2039, 1, '#', '', null, 1, 0, 'F', '0', '0', 'mc:banlist:query', '#', 'admin',
        '2024-12-20 19:26:57', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2041, '封禁管理新增', 2039, 2, '#', '', null, 1, 0, 'F', '0', '0', 'mc:banlist:add', '#', 'admin',
        '2024-12-20 19:26:57', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2042, '封禁管理修改', 2039, 3, '#', '', null, 1, 0, 'F', '0', '0', 'mc:banlist:edit', '#', 'admin',
        '2024-12-20 19:26:57', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2043, '封禁管理删除', 2039, 4, '#', '', null, 1, 0, 'F', '0', '0', 'mc:banlist:remove', '#', 'admin',
        '2024-12-20 19:26:57', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2044, '封禁管理导出', 2039, 5, '#', '', null, 1, 0, 'F', '0', '0', 'mc:banlist:export', '#', 'admin',
        '2024-12-20 19:26:57', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2045, '指令管理', 2000, 1, 'command', 'mc/command/index', null, 1, 0, 'C', '0', '0', 'mc:command:list', 'code',
        'admin', '2024-12-20 19:29:44', 'admin', '2024-12-20 19:30:35', '指令管理菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2046, '指令管理查询', 2045, 1, '#', '', null, 1, 0, 'F', '0', '0', 'mc:command:query', '#', 'admin',
        '2024-12-20 19:29:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2047, '指令管理新增', 2045, 2, '#', '', null, 1, 0, 'F', '0', '0', 'mc:command:add', '#', 'admin',
        '2024-12-20 19:29:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2048, '指令管理修改', 2045, 3, '#', '', null, 1, 0, 'F', '0', '0', 'mc:command:edit', '#', 'admin',
        '2024-12-20 19:29:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2049, '指令管理删除', 2045, 4, '#', '', null, 1, 0, 'F', '0', '0', 'mc:command:remove', '#', 'admin',
        '2024-12-20 19:29:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2050, '指令管理导出', 2045, 5, '#', '', null, 1, 0, 'F', '0', '0', 'mc:command:export', '#', 'admin',
        '2024-12-20 19:29:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2057, '玩家详情', 2000, 1, 'details', 'player/details/index', null, 1, 0, 'C', '0', '0', 'player:details:list',
        'list', 'admin', '2024-12-31 03:34:39', 'admin', '2024-12-31 03:46:23', '玩家详情菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2058, '玩家详情查询', 2057, 1, '#', '', null, 1, 0, 'F', '0', '0', 'player:details:query', '#', 'admin',
        '2024-12-31 03:34:39', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2059, '玩家详情新增', 2057, 2, '#', '', null, 1, 0, 'F', '0', '0', 'player:details:add', '#', 'admin',
        '2024-12-31 03:34:39', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2060, '玩家详情修改', 2057, 3, '#', '', null, 1, 0, 'F', '0', '0', 'player:details:edit', '#', 'admin',
        '2024-12-31 03:34:39', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2061, '玩家详情删除', 2057, 4, '#', '', null, 1, 0, 'F', '0', '0', 'player:details:remove', '#', 'admin',
        '2024-12-31 03:34:39', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2062, '玩家详情导出', 2057, 5, '#', '', null, 1, 0, 'F', '0', '0', 'player:details:export', '#', 'admin',
        '2024-12-31 03:34:39', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2063, '管理员名单', 2000, 1, 'operator', 'player/operator/index', null, 1, 0, 'C', '0', '0',
        'player:operator:list', 'user', 'admin', '2025-01-11 12:20:35', 'admin', '2025-01-11 12:21:19',
        '管理员名单菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2064, '管理员名单查询', 2063, 1, '#', '', null, 1, 0, 'F', '0', '0', 'player:operator:query', '#', 'admin',
        '2025-01-11 12:20:35', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2065, '管理员名单新增', 2063, 2, '#', '', null, 1, 0, 'F', '0', '0', 'player:operator:add', '#', 'admin',
        '2025-01-11 12:20:35', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2066, '管理员名单修改', 2063, 3, '#', '', null, 1, 0, 'F', '0', '0', 'player:operator:edit', '#', 'admin',
        '2025-01-11 12:20:35', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2067, '管理员名单删除', 2063, 4, '#', '', null, 1, 0, 'F', '0', '0', 'player:operator:remove', '#', 'admin',
        '2025-01-11 12:20:35', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2068, '管理员名单导出', 2063, 5, '#', '', null, 1, 0, 'F', '0', '0', 'player:operator:export', '#', 'admin',
        '2025-01-11 12:20:35', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2069, '定时命令', 2000, 1, 'regular', 'regular/command/index', null, 1, 0, 'C', '0', '0',
        'regular:command:list', 'time', 'admin', '2025-02-14 23:47:25', 'admin', '2025-02-14 23:54:39', '定时命令菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2070, '定时命令查询', 2069, 1, '#', '', null, 1, 0, 'F', '0', '0', 'regular:command:query', '#', 'admin',
        '2025-02-14 23:47:26', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2071, '定时命令新增', 2069, 2, '#', '', null, 1, 0, 'F', '0', '0', 'regular:command:add', '#', 'admin',
        '2025-02-14 23:47:26', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2072, '定时命令修改', 2069, 3, '#', '', null, 1, 0, 'F', '0', '0', 'regular:command:edit', '#', 'admin',
        '2025-02-14 23:47:26', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2073, '定时命令删除', 2069, 4, '#', '', null, 1, 0, 'F', '0', '0', 'regular:command:remove', '#', 'admin',
        '2025-02-14 23:47:26', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2074, '定时命令导出', 2069, 5, '#', '', null, 1, 0, 'F', '0', '0', 'regular:command:export', '#', 'admin',
        '2025-02-14 23:47:26', '', null, '');


/*如果存在menu_name包含机器人的菜单，全部删除并覆盖*/
DELETE FROM sys_menu WHERE menu_name LIKE '%机器人%';


INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2082, '机器人管理', 0, 3, '/bot', null, null, 1, 0, 'M', '0', '0', '', 'qq', 'admin', '2025-03-12 15:54:38',
        'admin', '2025-03-12 15:55:01', '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2083, '机器人配置', 2082, 1, 'config', 'bot/config/index', null, 1, 0, 'C', '0', '0', 'bot:config:list', 'dict',
        'admin', '2025-03-12 16:16:35', 'admin', '2025-03-13 18:04:35', 'QQ机器人配置菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2084, 'QQ机器人配置查询', 2083, 1, '#', '', null, 1, 0, 'F', '0', '0', 'bot:config:query', '#', 'admin',
        '2025-03-12 16:16:36', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2085, 'QQ机器人配置新增', 2083, 2, '#', '', null, 1, 0, 'F', '0', '0', 'bot:config:add', '#', 'admin',
        '2025-03-12 16:16:36', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2086, 'QQ机器人配置修改', 2083, 3, '#', '', null, 1, 0, 'F', '0', '0', 'bot:config:edit', '#', 'admin',
        '2025-03-12 16:16:36', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2087, 'QQ机器人配置删除', 2083, 4, '#', '', null, 1, 0, 'F', '0', '0', 'bot:config:remove', '#', 'admin',
        '2025-03-12 16:16:36', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2088, 'QQ机器人配置导出', 2083, 5, '#', '', null, 1, 0, 'F', '0', '0', 'bot:config:export', '#', 'admin',
        '2025-03-12 16:16:36', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2089, '机器人管理员', 2082, 1, 'manager', 'bot/manager/index', null, 1, 0, 'C', '1', '0', 'bot:manager:list',
        'peoples', 'admin', '2025-03-13 18:03:44', 'admin', '2025-03-15 14:10:11', 'QQ机器人管理员菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2090, 'QQ机器人管理员查询', 2089, 1, '#', '', null, 1, 0, 'F', '0', '0', 'bot:manager:query', '#', 'admin',
        '2025-03-13 18:03:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2091, 'QQ机器人管理员新增', 2089, 2, '#', '', null, 1, 0, 'F', '0', '0', 'bot:manager:add', '#', 'admin',
        '2025-03-13 18:03:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2092, 'QQ机器人管理员修改', 2089, 3, '#', '', null, 1, 0, 'F', '0', '0', 'bot:manager:edit', '#', 'admin',
        '2025-03-13 18:03:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2093, 'QQ机器人管理员删除', 2089, 4, '#', '', null, 1, 0, 'F', '0', '0', 'bot:manager:remove', '#', 'admin',
        '2025-03-13 18:03:44', '', null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache,
                            menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time,
                            remark)
VALUES (2094, 'QQ机器人管理员导出', 2089, 5, '#', '', null, 1, 0, 'F', '0', '0', 'bot:manager:export', '#', 'admin',
        '2025-03-13 18:03:44', '', null, '');



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
  AUTO_INCREMENT = 3
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '通知公告表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice`
VALUES (1, '温馨提醒：2018-07-01 若依新版本发布啦', '2', 0xE696B0E78988E69CACE58685E5AEB9, '0', 'admin',
        '2023-12-26 16:54:02', '', NULL, '管理员');
INSERT INTO `sys_notice`
VALUES (2, '维护通知：2018-07-01 若依系统凌晨维护', '1', 0xE7BBB4E68AA4E58685E5AEB9, '0', 'admin', '2023-12-26 16:54:02',
        '', NULL, '管理员');

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
  AUTO_INCREMENT = 821
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
  AUTO_INCREMENT = 5
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
  AUTO_INCREMENT = 3
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
  AUTO_INCREMENT = 2
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

-- ----------------------------
-- Table structure for whitelist_info
-- ----------------------------
DROP TABLE IF EXISTS `whitelist_info`;
CREATE TABLE `whitelist_info`
(
    `id`            int                                                    NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `time`          datetime                                               NULL DEFAULT NULL COMMENT '申请时间',
    `user_name`     varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '游戏名称',
    `user_uuid`     varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
    `online_flag`   int                                                    NULL DEFAULT NULL COMMENT '正版标识',
    `qq_num`        varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL COMMENT '用户QQ号',
    `remark`        varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '描述',
    `review_users`  varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '审核用户',
    `status`        varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL COMMENT '审核状态',
    `add_state`     varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL COMMENT '添加状态',
    `add_time`      datetime                                               NULL DEFAULT NULL COMMENT '添加时间',
    `remove_reason` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '移除原因',
    `remove_time`   datetime                                               NULL DEFAULT NULL COMMENT '移除时间',
    `create_by`     varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '创建人',
    `update_by`     varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '更新者',
    `create_time`   datetime                                               NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`   datetime                                               NULL DEFAULT NULL COMMENT '更新时间',
    `servers`       varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `whitelist_info_qq_num_index` (`qq_num` ASC) USING BTREE,
    INDEX `whitelist_info_user_name_index` (`user_name` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 69
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_bin
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of whitelist_info
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
