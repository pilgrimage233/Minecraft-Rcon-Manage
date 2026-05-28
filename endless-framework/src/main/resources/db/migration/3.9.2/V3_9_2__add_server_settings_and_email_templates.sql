-- =====================================================
-- Version: 3.9.2
-- Description: 实例服务器运维策略配置 + 邮件模板管理
-- Author: Memory
-- Date: 2026-05-29
-- =====================================================

-- 实例服务器运维策略配置表
CREATE TABLE IF NOT EXISTS node_server_settings (
    id                           BIGINT AUTO_INCREMENT PRIMARY KEY,
    node_id                      BIGINT NOT NULL COMMENT '节点ID',
    node_server_id               BIGINT NOT NULL COMMENT '实例ID (node_minecraft_server.id)',
    node_uuid                    VARCHAR(64) COMMENT '节点UUID',

    -- 崩溃重启
    crash_restart_enabled        TINYINT DEFAULT 0 COMMENT '是否启用崩溃重启: 0否 1是',
    crash_restart_delay_sec      INT DEFAULT 10 COMMENT '崩溃后延迟重启秒数',
    crash_restart_max_retry      INT DEFAULT 3 COMMENT '最大连续重启次数(防无限重启)',

    -- 持久在线 (保活)
    keep_alive_enabled           TINYINT DEFAULT 0 COMMENT '是否启用持久在线: 0否 1是',
    keep_alive_check_interval_sec INT DEFAULT 60 COMMENT '保活检查间隔秒数',

    -- 定时开关机
    scheduled_start_enabled      TINYINT DEFAULT 0 COMMENT '是否启用定时开机: 0否 1是',
    scheduled_start_cron         VARCHAR(64) COMMENT '定时开机 cron 表达式',
    scheduled_stop_enabled       TINYINT DEFAULT 0 COMMENT '是否启用定时关机: 0否 1是',
    scheduled_stop_cron          VARCHAR(64) COMMENT '定时关机 cron 表达式',

    -- 定时重启
    scheduled_restart_enabled    TINYINT DEFAULT 0 COMMENT '是否启用定时重启: 0否 1是',
    scheduled_restart_cron       VARCHAR(64) COMMENT '定时重启 cron 表达式',

    -- 优雅关闭
    graceful_shutdown_timeout_sec INT DEFAULT 30 COMMENT '优雅关闭超时秒数',

    -- 通知
    notify_email                 VARCHAR(255) COMMENT '统一通知邮箱',
    crash_notify_enabled         TINYINT DEFAULT 0 COMMENT '崩溃通知开关',
    backup_notify_enabled        TINYINT DEFAULT 0 COMMENT '备份完成通知开关',

    -- 定时备份
    backup_enabled               TINYINT DEFAULT 0 COMMENT '定时备份开关',
    backup_cron                  VARCHAR(64) COMMENT '定时备份 cron 表达式',
    backup_retain_count          INT DEFAULT 5 COMMENT '备份保留份数',

    -- 磁盘告警
    disk_alert_enabled           TINYINT DEFAULT 0 COMMENT '磁盘告警开关',
    disk_alert_threshold_gb      INT DEFAULT 5 COMMENT '磁盘剩余低于此值告警(GB)',

    -- TPS 告警
    tps_mode                     VARCHAR(16) DEFAULT 'AUTO' COMMENT 'TPS采集方式: AUTO/TPS_COMMAND/SPARK_API/DISABLED',
    tps_alert_enabled            TINYINT DEFAULT 0 COMMENT 'TPS告警开关',
    tps_alert_threshold          DOUBLE DEFAULT 15.0 COMMENT 'TPS低于此值告警',
    spark_api_port               INT COMMENT 'Spark HTTP API 端口(可选)',

    -- 通用
    del_flag                     VARCHAR(1) DEFAULT '0',
    create_by                    VARCHAR(64),
    create_time                  DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by                    VARCHAR(64),
    update_time                  DATETIME,
    remark                       VARCHAR(500),

    UNIQUE KEY uk_node_server (node_server_id)
) COMMENT '实例服务器运维策略配置';

CREATE INDEX idx_settings_node ON node_server_settings (node_id, del_flag);

-- 邮件模板表
CREATE TABLE IF NOT EXISTS node_email_template (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_key  VARCHAR(64) NOT NULL COMMENT '模板标识',
    template_name VARCHAR(128) NOT NULL COMMENT '模板名称',
    subject       VARCHAR(255) NOT NULL COMMENT '邮件主题模板',
    content       TEXT NOT NULL COMMENT '邮件内容模板 (HTML)',
    description   VARCHAR(500) COMMENT '模板说明',
    del_flag      VARCHAR(1) DEFAULT '0',
    create_by     VARCHAR(64),
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by     VARCHAR(64),
    update_time   DATETIME,

    UNIQUE KEY uk_template_key (template_key)
) COMMENT '邮件模板';

-- 默认邮件模板
INSERT INTO node_email_template (template_key, template_name, subject, content, description) VALUES
('crash_notify', '崩溃通知',
 '⚠️ 服务器崩溃通知 - {serverName}',
 '<div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;">
  <div style="background:#ff4d4f;color:#fff;padding:16px 24px;border-radius:8px 8px 0 0;">
    <h2 style="margin:0;">⚠️ 服务器崩溃通知</h2>
  </div>
  <div style="background:#fafafa;padding:24px;border:1px solid #eee;border-top:none;border-radius:0 0 8px 8px;">
    <p><strong>服务器名称：</strong>{serverName}</p>
    <p><strong>节点：</strong>{nodeName} ({nodeIp})</p>
    <p><strong>退出码：</strong>{exitCode}</p>
    <p><strong>崩溃时间：</strong>{timestamp}</p>
    <p><strong>连续崩溃次数：</strong>{crashCount}</p>
    <hr style="border:none;border-top:1px solid #eee;margin:16px 0;">
    <p style="color:#666;font-size:12px;">此邮件由 Endless 管理系统自动发送</p>
  </div>
</div>',
 '服务器崩溃时发送的邮件通知'),

('backup_notify', '备份完成通知',
 '✅ 备份完成 - {serverName}',
 '<div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;">
  <div style="background:#52c41a;color:#fff;padding:16px 24px;border-radius:8px 8px 0 0;">
    <h2 style="margin:0;">✅ 备份完成通知</h2>
  </div>
  <div style="background:#fafafa;padding:24px;border:1px solid #eee;border-top:none;border-radius:0 0 8px 8px;">
    <p><strong>服务器名称：</strong>{serverName}</p>
    <p><strong>节点：</strong>{nodeName} ({nodeIp})</p>
    <p><strong>备份文件：</strong>{backupFile}</p>
    <p><strong>备份大小：</strong>{backupSize}</p>
    <p><strong>备份时间：</strong>{timestamp}</p>
    <hr style="border:none;border-top:1px solid #eee;margin:16px 0;">
    <p style="color:#666;font-size:12px;">此邮件由 Endless 管理系统自动发送</p>
  </div>
</div>',
 '定时备份完成后的通知邮件'),

('disk_alert', '磁盘空间告警',
 '⚠️ 磁盘空间不足告警 - {serverName}',
 '<div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;">
  <div style="background:#faad14;color:#fff;padding:16px 24px;border-radius:8px 8px 0 0;">
    <h2 style="margin:0;">⚠️ 磁盘空间告警</h2>
  </div>
  <div style="background:#fafafa;padding:24px;border:1px solid #eee;border-top:none;border-radius:0 0 8px 8px;">
    <p><strong>服务器名称：</strong>{serverName}</p>
    <p><strong>节点：</strong>{nodeName} ({nodeIp})</p>
    <p><strong>剩余空间：</strong>{freeSpace} GB</p>
    <p><strong>告警阈值：</strong>{threshold} GB</p>
    <p><strong>检测时间：</strong>{timestamp}</p>
    <hr style="border:none;border-top:1px solid #eee;margin:16px 0;">
    <p style="color:#666;font-size:12px;">此邮件由 Endless 管理系统自动发送</p>
  </div>
</div>',
 '磁盘空间不足时的告警邮件'),

('restart_notify', '定时重启通知',
 '🔄 服务器定时重启 - {serverName}',
 '<div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;">
  <div style="background:#1890ff;color:#fff;padding:16px 24px;border-radius:8px 8px 0 0;">
    <h2 style="margin:0;">🔄 定时重启通知</h2>
  </div>
  <div style="background:#fafafa;padding:24px;border:1px solid #eee;border-top:none;border-radius:0 0 8px 8px;">
    <p><strong>服务器名称：</strong>{serverName}</p>
    <p><strong>节点：</strong>{nodeName} ({nodeIp})</p>
    <p><strong>重启时间：</strong>{timestamp}</p>
    <p><strong>重启结果：</strong>{result}</p>
    <hr style="border:none;border-top:1px solid #eee;margin:16px 0;">
    <p style="color:#666;font-size:12px;">此邮件由 Endless 管理系统自动发送</p>
  </div>
</div>',
 '定时重启完成后的通知邮件');
