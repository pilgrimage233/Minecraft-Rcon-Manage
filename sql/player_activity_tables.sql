-- 玩家活跃度统计相关表结构

-- 1. 玩家活跃度统计表
CREATE TABLE `player_activity_stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `stats_date` date NOT NULL COMMENT '统计日期',
  `stats_type` varchar(20) NOT NULL COMMENT '统计类型：daily-日报, weekly-周报, monthly-月报',
  `active_player_count` int(11) DEFAULT '0' COMMENT '活跃玩家数量',
  `new_player_count` int(11) DEFAULT '0' COMMENT '新增玩家数量',
  `total_online_minutes` bigint(20) DEFAULT '0' COMMENT '总在线时长（分钟）',
  `avg_online_minutes` bigint(20) DEFAULT '0' COMMENT '平均在线时长（分钟）',
  `peak_online_count` int(11) DEFAULT '0' COMMENT '峰值在线人数',
  `peak_online_time` datetime DEFAULT NULL COMMENT '峰值在线时间',
  `active_player_list` text COMMENT '活跃玩家列表（JSON格式）',
  `new_player_list` text COMMENT '新增玩家列表（JSON格式）',
  `period_start` datetime DEFAULT NULL COMMENT '统计周期开始时间',
  `period_end` datetime DEFAULT NULL COMMENT '统计周期结束时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stats_date_type` (`stats_date`, `stats_type`),
  KEY `idx_stats_type` (`stats_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家活跃度统计表';

-- 2. 每日玩家活跃度记录表
CREATE TABLE `daily_player_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `player_name` varchar(50) NOT NULL COMMENT '玩家名',
  `player_id` bigint(20) DEFAULT NULL COMMENT '玩家ID（关联白名单）',
  `activity_date` date NOT NULL COMMENT '记录日期',
  `online_minutes` bigint(20) DEFAULT '0' COMMENT '当日在线时长（分钟）',
  `login_count` int(11) DEFAULT '0' COMMENT '当日登录次数',
  `first_login_time` datetime DEFAULT NULL COMMENT '首次登录时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_logout_time` datetime DEFAULT NULL COMMENT '最后离线时间',
  `is_new_player` tinyint(1) DEFAULT '0' COMMENT '是否为新玩家（当日首次加入）',
  `activity_score` decimal(10,2) DEFAULT '0.00' COMMENT '活跃度评分',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_player_date` (`player_name`, `activity_date`),
  KEY `idx_activity_date` (`activity_date`),
  KEY `idx_player_name` (`player_name`),
  KEY `idx_activity_score` (`activity_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日玩家活跃度记录表';

-- 3. 玩家在线时长历史记录表（用于详细追踪）
CREATE TABLE `player_online_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `player_name` varchar(50) NOT NULL COMMENT '玩家名',
  `server_id` bigint(20) DEFAULT NULL COMMENT '服务器ID',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `logout_time` datetime DEFAULT NULL COMMENT '离线时间',
  `online_minutes` bigint(20) DEFAULT '0' COMMENT '本次在线时长（分钟）',
  `session_date` date NOT NULL COMMENT '会话日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_player_name` (`player_name`),
  KEY `idx_session_date` (`session_date`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家在线时长历史记录表';

-- 4. 服务器每日统计表
CREATE TABLE `server_daily_stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `server_id` bigint(20) NOT NULL COMMENT '服务器ID',
  `stats_date` date NOT NULL COMMENT '统计日期',
  `total_online_minutes` bigint(20) DEFAULT '0' COMMENT '服务器总在线时长',
  `unique_player_count` int(11) DEFAULT '0' COMMENT '独立玩家数',
  `peak_online_count` int(11) DEFAULT '0' COMMENT '峰值在线人数',
  `peak_online_time` datetime DEFAULT NULL COMMENT '峰值在线时间',
  `avg_online_minutes` decimal(10,2) DEFAULT '0.00' COMMENT '平均在线时长',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_server_date` (`server_id`, `stats_date`),
  KEY `idx_stats_date` (`stats_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务器每日统计表';
