-- =============================================
-- 数据库升级脚本
-- 版本: 3.4.2
-- 序号: 03
-- 描述: 优化节点服务器和玩家活跃度相关表索引 (第三阶段)
-- 作者: Memory
-- 日期: 2026-01-15
-- =============================================

-- ========================================
-- 1. node_server 表索引优化
-- ========================================
-- 添加协议、版本和操作系统类型索引
CREATE INDEX idx_node_protocol ON node_server (protocol);
CREATE INDEX idx_node_version ON node_server (version);
CREATE INDEX idx_node_os_type ON node_server (os_type);
CREATE INDEX idx_node_create_time ON node_server (create_time DESC);
CREATE INDEX idx_node_del_flag ON node_server (del_flag);
-- 复合索引：状态+删除标识（常用组合）
CREATE INDEX idx_node_status_del ON node_server (status, del_flag);

-- ========================================
-- 2. node_operation_log 表索引优化
-- ========================================
-- 添加操作名称和IP地址索引
CREATE INDEX idx_node_log_operation_name ON node_operation_log (operation_name);
CREATE INDEX idx_node_log_operation_ip ON node_operation_log (operation_ip);
CREATE INDEX idx_node_log_method_name ON node_operation_log (method_name);
CREATE INDEX idx_node_log_update_time ON node_operation_log (update_time DESC);
CREATE INDEX idx_node_log_del_flag ON node_operation_log (del_flag);
-- 复合索引：节点ID+操作类型+创建时间（常用组合）
CREATE INDEX idx_node_log_node_type_time ON node_operation_log (node_id, operation_type, create_time DESC);
-- 复合索引：操作目标+对象ID（常用组合）
CREATE INDEX idx_node_log_target_obj ON node_operation_log (operation_target, node_obj_id);

-- ========================================
-- 3. node_minecraft_server 表索引优化
-- ========================================
-- 添加核心类型、版本和实例ID索引
CREATE INDEX idx_node_mc_core_type ON node_minecraft_server (core_type);
CREATE INDEX idx_node_mc_version ON node_minecraft_server (version);
CREATE INDEX idx_node_mc_instances_id ON node_minecraft_server (node_instances_id);
CREATE INDEX idx_node_mc_last_start ON node_minecraft_server (last_start_time DESC);
CREATE INDEX idx_node_mc_last_stop ON node_minecraft_server (last_stop_time DESC);
CREATE INDEX idx_node_mc_update_time ON node_minecraft_server (update_time DESC);
CREATE INDEX idx_node_mc_del_flag ON node_minecraft_server (del_flag);
-- 复合索引：节点ID+状态（常用组合）
CREATE INDEX idx_node_mc_node_status ON node_minecraft_server (node_id, status);
-- 复合索引：节点UUID+删除标识（常用组合）
CREATE INDEX idx_node_mc_uuid_del ON node_minecraft_server (node_uuid, del_flag);

-- ========================================
-- 4. node_env 表索引优化
-- ========================================
-- 添加环境名称、类型和架构索引
CREATE INDEX idx_node_env_name ON node_env (env_name);
CREATE INDEX idx_node_env_type ON node_env (type);
CREATE INDEX idx_node_env_arch ON node_env (arch);
CREATE INDEX idx_node_env_version ON node_env (version);
CREATE INDEX idx_node_env_create_time ON node_env (create_time DESC);
-- 复合索引：节点ID+是否默认（常用组合）
CREATE INDEX idx_node_env_node_default ON node_env (node_id, is_default);
-- 复合索引：节点ID+状态+有效性（常用组合）
CREATE INDEX idx_node_env_node_status_valid ON node_env (node_id, status, valid);

-- ========================================
-- 5. player_activity_stats 表索引优化
-- ========================================
-- 添加周期时间索引
CREATE INDEX idx_activity_stats_period_start ON player_activity_stats (period_start DESC);
CREATE INDEX idx_activity_stats_period_end ON player_activity_stats (period_end DESC);
CREATE INDEX idx_activity_stats_update_time ON player_activity_stats (update_time DESC);
-- 复合索引：统计类型+统计日期（常用组合）
CREATE INDEX idx_activity_stats_type_date ON player_activity_stats (stats_type, stats_date DESC);

-- ========================================
-- 6. daily_player_activity 表索引优化
-- ========================================
-- 添加玩家ID和登录次数索引
CREATE INDEX idx_daily_activity_player_id ON daily_player_activity (player_id);
CREATE INDEX idx_daily_activity_login_count ON daily_player_activity (login_count DESC);
CREATE INDEX idx_daily_activity_online_minutes ON daily_player_activity (online_minutes DESC);
CREATE INDEX idx_daily_activity_is_new ON daily_player_activity (is_new_player);
CREATE INDEX idx_daily_activity_update_time ON daily_player_activity (update_time DESC);
-- 复合索引：活动日期+活跃度评分（常用组合）
CREATE INDEX idx_daily_activity_date_score ON daily_player_activity (activity_date DESC, activity_score DESC);

-- ========================================
-- 7. player_online_history 表索引优化
-- ========================================
-- 添加服务器ID和离线时间索引
CREATE INDEX idx_online_history_server ON player_online_history (server_id);
CREATE INDEX idx_online_history_logout_time ON player_online_history (logout_time DESC);
CREATE INDEX idx_online_history_online_minutes ON player_online_history (online_minutes DESC);
-- 复合索引：玩家名+会话日期（常用组合）
CREATE INDEX idx_online_history_player_date ON player_online_history (player_name, session_date DESC);

-- ========================================
-- 8. server_daily_stats 表索引优化
-- ========================================
-- 添加独立玩家数和峰值在线索引
CREATE INDEX idx_server_stats_unique_players ON server_daily_stats (unique_player_count DESC);
CREATE INDEX idx_server_stats_peak_online ON server_daily_stats (peak_online_count DESC);
CREATE INDEX idx_server_stats_peak_time ON server_daily_stats (peak_online_time DESC);
CREATE INDEX idx_server_stats_update_time ON server_daily_stats (update_time DESC);
-- 复合索引：服务器ID+统计日期（常用组合）
CREATE INDEX idx_server_stats_server_date ON server_daily_stats (server_id, stats_date DESC);

-- ========================================
-- 9. sys_feedback_record 表索引优化
-- ========================================
-- 添加反馈类型和状态索引
CREATE INDEX idx_feedback_type ON sys_feedback_record (feedback_type);
CREATE INDEX idx_feedback_status ON sys_feedback_record (status);
CREATE INDEX idx_feedback_user_name ON sys_feedback_record (user_name);
CREATE INDEX idx_feedback_update_time ON sys_feedback_record (update_time DESC);
-- 复合索引：用户ID+创建时间（常用组合）
CREATE INDEX idx_feedback_user_time ON sys_feedback_record (user_id, create_time DESC);

-- ========================================
-- 10. bot_group_command_config 表索引优化
-- ========================================
-- 添加指令分类和创建时间索引
CREATE INDEX idx_bot_cmd_category ON bot_group_command_config (command_category);
CREATE INDEX idx_bot_cmd_create_time ON bot_group_command_config (create_time DESC);
CREATE INDEX idx_bot_cmd_update_time ON bot_group_command_config (update_time DESC);
-- 复合索引：群组ID+启用状态（常用组合）
CREATE INDEX idx_bot_cmd_group_enabled ON bot_group_command_config (group_id, is_enabled);

-- ========================================
-- 11. rcon_node_instance_relation 表索引优化
-- ========================================
-- 添加创建时间和更新时间索引
CREATE INDEX idx_rcon_relation_create_time ON rcon_node_instance_relation (create_time DESC);
CREATE INDEX idx_rcon_relation_update_time ON rcon_node_instance_relation (update_time DESC);
-- 复合索引：节点ID+状态（常用组合）
CREATE INDEX idx_rcon_relation_node_status ON rcon_node_instance_relation (node_id, status);
