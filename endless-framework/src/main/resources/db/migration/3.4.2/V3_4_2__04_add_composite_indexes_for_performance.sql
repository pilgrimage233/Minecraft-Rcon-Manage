-- =============================================
-- 数据库升级脚本
-- 版本: 3.4.2
-- 序号: 04
-- 描述: 添加复合索引以优化常见查询场景性能（第四阶段）
-- 作者: Memory
-- 日期: 2026-01-15
-- =============================================

-- ========================================
-- 1. 白名单相关复合索引优化
-- ========================================
-- 白名单信息：状态+添加状态+时间（审核列表查询）
CREATE INDEX idx_whitelist_status_add_time ON whitelist_info (status, add_state, time DESC);

-- 白名单信息：QQ号+状态（用户查询自己的白名单）
CREATE INDEX idx_whitelist_qq_status ON whitelist_info (qq_num, status);

-- 白名单时限：白名单ID+截止时间+删除标识（时限检查）
CREATE INDEX idx_deadline_whitelist_end_del ON whitelist_deadline_info (whitelist_id, end_time, del_flag);

-- ========================================
-- 2. 玩家相关复合索引优化
-- ========================================
-- 玩家详情：白名单ID+用户名（关联查询）
CREATE INDEX idx_player_whitelist_name ON player_details (whitelist_id, user_name);

-- 玩家详情：QQ+身份（用户身份验证）
CREATE INDEX idx_player_qq_identity ON player_details (qq, identity);

-- 玩家活跃度：活动日期+是否新玩家+活跃度评分（统计查询）
CREATE INDEX idx_daily_activity_date_new_score ON daily_player_activity (activity_date DESC, is_new_player, activity_score DESC);

-- ========================================
-- 3. 服务器相关复合索引优化
-- ========================================
-- 服务器信息：状态+名称标签（服务器列表查询）
CREATE INDEX idx_server_status_name ON server_info (status, name_tag);

-- 历史命令：服务器ID+执行时间+状态（命令历史查询）
CREATE INDEX idx_history_server_time_status ON history_command (server_id, execute_time DESC, status);

-- 公开命令：服务器ID+状态（命令权限检查）
CREATE INDEX idx_public_cmd_server_status ON public_server_command (server_id, status);

-- ========================================
-- 4. 机器人相关复合索引优化
-- ========================================
-- 机器人配置：状态+最后心跳时间（健康检查）
CREATE INDEX idx_bot_config_status_heartbeat ON qq_bot_config (status, last_heartbeat_time DESC);

-- 机器人管理员：机器人ID+管理员QQ+状态（权限验证）
CREATE INDEX idx_bot_manager_bot_qq_status ON qq_bot_manager (bot_id, manager_qq, status);

-- 机器人日志：机器人ID+日志类型+发送者ID+创建时间（日志查询）
CREATE INDEX idx_bot_log_bot_type_sender_time ON qq_bot_log (bot_id, log_type, sender_id, create_time DESC);

-- 群组命令配置：群组ID+指令分类+启用状态（命令权限检查）
CREATE INDEX idx_bot_cmd_group_category_enabled ON bot_group_command_config (group_id, command_category, is_enabled);

-- ========================================
-- 5. 节点服务器相关复合索引优化
-- ========================================
-- 节点服务器：状态+最后心跳时间+删除标识（节点监控）
CREATE INDEX idx_node_status_heartbeat_del ON node_server (status, last_heartbeat DESC, del_flag);

-- Minecraft服务器：节点ID+核心类型+版本（服务器筛选）
CREATE INDEX idx_node_mc_node_core_version ON node_minecraft_server (node_id, core_type, version);

-- 节点操作日志：节点ID+操作目标+状态+创建时间（操作审计）
CREATE INDEX idx_node_log_node_target_status_time ON node_operation_log (node_id, operation_target, status, create_time DESC);

-- 节点环境：节点ID+来源+有效性+状态（环境管理）
CREATE INDEX idx_node_env_node_source_valid_status ON node_env (node_id, source, valid, status);

-- ========================================
-- 6. 题库相关复合索引优化
-- ========================================
-- 题库问题：状态+问题类型+排序+删除标识（题目列表）
CREATE INDEX idx_quiz_question_status_type_sort_del ON whitelist_quiz_question (status, question_type, sort_order, del_flag);

-- 题库答案：问题ID+是否正确+排序+删除标识（答案列表）
CREATE INDEX idx_quiz_answer_question_correct_sort_del ON whitelist_quiz_answer (question_id, is_correct, sort_order, del_flag);

-- 答题记录：玩家UUID+通过状态+提交时间（玩家答题历史）
CREATE INDEX idx_quiz_submission_player_pass_time ON whitelist_quiz_submission (player_uuid, pass_status, submit_time DESC);

-- 答题详情：提交ID+问题类型+是否正确（答题分析）
CREATE INDEX idx_quiz_detail_submission_type_correct ON whitelist_quiz_submission_detail (submission_id, question_type, is_correct);

-- ========================================
-- 7. 封禁相关复合索引优化
-- ========================================
-- 封禁信息：白名单ID+状态+创建时间（封禁查询）
CREATE INDEX idx_banlist_white_state_time ON banlist_info (white_id, state, create_time DESC);

-- 封禁信息：用户名+状态（快速封禁检查）
CREATE INDEX idx_banlist_name_state ON banlist_info (user_name, state);

-- ========================================
-- 8. 定时任务相关复合索引优化
-- ========================================
-- 定时命令：状态+执行服务器+创建时间（任务调度）
CREATE INDEX idx_regular_status_server_time ON regular_cmd (status, execute_server, create_time DESC);

-- 定时命令：任务ID+状态（任务管理）
CREATE INDEX idx_regular_task_status ON regular_cmd (task_id, status);

-- ========================================
-- 9. 邮件模板相关复合索引优化
-- ========================================
-- 邮件模板：服务器ID+状态（模板查询）
CREATE INDEX idx_email_template_server_status ON custom_email_templates (server_id, status);

-- ========================================
-- 10. RCON关联相关复合索引优化
-- ========================================
-- RCON关联：节点ID+实例ID+状态（关联查询）
CREATE INDEX idx_rcon_relation_node_instance_status ON rcon_node_instance_relation (node_id, instance_id, status);
