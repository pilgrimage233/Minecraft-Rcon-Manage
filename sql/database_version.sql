-- =====================================================
-- 数据库版本管理表
-- 用于记录数据库升级历史和当前版本
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for database_version
-- ----------------------------
DROP TABLE IF EXISTS `database_version`;
CREATE TABLE `database_version`
(
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `app_version`    varchar(50)  NOT NULL COMMENT '应用程序版本号(如: 3.9.1, 3.9.2)',
    `script_type`    varchar(20)  NOT NULL COMMENT '脚本类型(schema/data)',
    `script_name`    varchar(200) NOT NULL COMMENT '脚本名称',
    `file_name`      varchar(200) NOT NULL COMMENT '脚本文件名',
    `description`    varchar(500)          DEFAULT NULL COMMENT '版本描述',
    `checksum`       varchar(64)           DEFAULT NULL COMMENT '脚本内容校验和(MD5)',
    `execution_time` int                   DEFAULT NULL COMMENT '执行耗时(毫秒)',
    `success`        tinyint(1)   NOT NULL DEFAULT 1 COMMENT '执行是否成功(0失败 1成功)',
    `error_message`  text                  DEFAULT NULL COMMENT '错误信息(如果执行失败)',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_script` (`app_version`, `script_type`, `script_name`) COMMENT '确保同一版本的脚本不重复执行',
    KEY `idx_app_version` (`app_version`),
    KEY `idx_script_type` (`script_type`),
    KEY `idx_success` (`success`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='数据库版本管理表';

-- 插入初始版本记录
INSERT INTO `database_version` (`app_version`, `script_type`, `script_name`, `file_name`, `description`, `checksum`,
                                `execution_time`, `success`, `create_time`)
VALUES ('1.0.0', 'schema', 'init', 'init.sql', '初始化数据库版本管理', 'init', 0, 1, NOW());

SET FOREIGN_KEY_CHECKS = 1;