-- =============================================
-- 数据库升级脚本
-- 版本: 3.4.2
-- 序号: 01
-- 描述: 优化数据库索引，提升查询性能（第一阶段）
-- 作者: Memory
-- 日期: 2026-01-15
-- =============================================

-- ========================================
-- 1. banlist_info 表索引优化
-- ========================================
-- 添加常用查询字段索引
CREATE INDEX idx_banlist_white_id ON banlist_info (white_id);
CREATE INDEX idx_banlist_user_name ON banlist_info (user_name);
CREATE INDEX idx_banlist_state ON banlist_info (state);
CREATE INDEX idx_banlist_create_time ON banlist_info (create_time DESC);

-- ========================================
-- 2. history_command 表索引优化
-- ========================================
-- 添加执行时间和用户索引
CREATE INDEX idx_history_execute_time ON history_command (execute_time DESC);
CREATE INDEX idx_history_user ON history_command (user);
CREATE INDEX idx_history_status ON history_command (status);
-- 复合索引：服务器+时间（常用组合查询）
CREATE INDEX idx_history_server_time ON history_command (server_id, execute_time DESC);

-- ========================================
-- 3. ip_limit_info 表索引优化
-- ========================================
-- 添加IP和时间索引
CREATE INDEX idx_ip_limit_ip ON ip_limit_info (ip);
CREATE INDEX idx_ip_limit_uuid ON ip_limit_info (uuid);
CREATE INDEX idx_ip_limit_create_time ON ip_limit_info (create_time DESC);
CREATE INDEX idx_ip_limit_count ON ip_limit_info (count DESC);

-- ========================================
-- 4. operator_list 表索引优化
-- ========================================
-- 添加状态索引
CREATE INDEX idx_operator_status ON operator_list (status);
CREATE INDEX idx_operator_create_time ON operator_list (create_time DESC);

-- ========================================
-- 5. player_details 表索引优化
-- ========================================
-- 添加常用查询字段索引
CREATE INDEX idx_player_qq ON player_details (qq);
CREATE INDEX idx_player_identity ON player_details (identity);
CREATE INDEX idx_player_banlist_id ON player_details (banlist_id);
CREATE INDEX idx_player_create_time ON player_details (create_time DESC);
CREATE INDEX idx_player_last_online ON player_details (last_online_time DESC);

-- ========================================
-- 6. public_server_command 表索引优化
-- ========================================
CREATE INDEX idx_public_cmd_server_id ON public_server_command (server_id);
CREATE INDEX idx_public_cmd_status ON public_server_command (status);
CREATE INDEX idx_public_cmd_command ON public_server_command (command);

-- ========================================
-- 7. regular_cmd 表索引优化
-- ========================================
CREATE INDEX idx_regular_task_id ON regular_cmd (task_id);
CREATE INDEX idx_regular_status ON regular_cmd (status);
CREATE INDEX idx_regular_execute_server ON regular_cmd (execute_server);
CREATE INDEX idx_regular_create_time ON regular_cmd (create_time DESC);

-- ========================================
-- 8. server_command_info 表索引优化
-- ========================================
CREATE INDEX idx_server_cmd_server_id ON server_command_info (server_id);
CREATE INDEX idx_server_cmd_create_time ON server_command_info (create_time DESC);

-- ========================================
-- 9. server_info 表索引优化
-- ========================================
CREATE INDEX idx_server_uuid ON server_info (uuid);
CREATE INDEX idx_server_status ON server_info (status);
CREATE INDEX idx_server_name_tag ON server_info (name_tag);
CREATE INDEX idx_server_create_time ON server_info (create_time DESC);

-- ========================================
-- 10. whitelist_info 表索引优化
-- ========================================
CREATE INDEX idx_whitelist_status ON whitelist_info (status);
CREATE INDEX idx_whitelist_add_state ON whitelist_info (add_state);
CREATE INDEX idx_whitelist_online_flag ON whitelist_info (online_flag);
CREATE INDEX idx_whitelist_time ON whitelist_info (time DESC);
CREATE INDEX idx_whitelist_add_time ON whitelist_info (add_time DESC);
CREATE INDEX idx_whitelist_user_uuid ON whitelist_info (user_uuid);

-- ========================================
-- 11. whitelist_deadline_info 表索引优化
-- ========================================
CREATE INDEX idx_deadline_user_name ON whitelist_deadline_info (user_name);
CREATE INDEX idx_deadline_end_time ON whitelist_deadline_info (end_time);
CREATE INDEX idx_deadline_del_flag ON whitelist_deadline_info (del_flag);
-- 复合索引：白名单ID+删除标识（常用组合）
CREATE INDEX idx_deadline_whitelist_del ON whitelist_deadline_info (whitelist_id, del_flag);

-- ========================================
-- 12. custom_email_templates 表索引优化
-- ========================================
CREATE INDEX idx_email_template_status ON custom_email_templates (status);
CREATE INDEX idx_email_template_create_time ON custom_email_templates (create_time DESC);
