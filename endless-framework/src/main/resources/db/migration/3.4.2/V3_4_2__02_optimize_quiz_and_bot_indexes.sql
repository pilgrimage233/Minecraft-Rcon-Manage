-- =============================================
-- 数据库升级脚本
-- 版本: 3.4.2
-- 序号: 02
-- 描述: 优化题库和机器人相关表索引（第二阶段）
-- 作者: Memory
-- 日期: 2026-01-15
-- =============================================

-- ========================================
-- 1. whitelist_quiz_question 表索引优化
-- ========================================
-- 添加排序和必答字段索引
CREATE INDEX idx_quiz_question_sort ON whitelist_quiz_question (sort_order);
CREATE INDEX idx_quiz_question_required ON whitelist_quiz_question (is_required);
CREATE INDEX idx_quiz_question_del_flag ON whitelist_quiz_question (del_flag);
-- 复合索引：状态+排序（常用组合）
CREATE INDEX idx_quiz_question_status_sort ON whitelist_quiz_question (status, sort_order);

-- ========================================
-- 2. whitelist_quiz_answer 表索引优化
-- ========================================
-- 添加排序和分数索引
CREATE INDEX idx_quiz_answer_sort ON whitelist_quiz_answer (sort_order);
CREATE INDEX idx_quiz_answer_score ON whitelist_quiz_answer (score DESC);
CREATE INDEX idx_quiz_answer_del_flag ON whitelist_quiz_answer (del_flag);
-- 复合索引：问题ID+正确性（常用组合）
CREATE INDEX idx_quiz_answer_question_correct ON whitelist_quiz_answer (question_id, is_correct);

-- ========================================
-- 3. whitelist_quiz_submission 表索引优化
-- ========================================
-- 添加审核人和审核时间索引
CREATE INDEX idx_quiz_submission_reviewer ON whitelist_quiz_submission (reviewer);
CREATE INDEX idx_quiz_submission_review_time ON whitelist_quiz_submission (review_time DESC);
CREATE INDEX idx_quiz_submission_del_flag ON whitelist_quiz_submission (del_flag);
-- 复合索引：通过状态+提交时间（常用组合）
CREATE INDEX idx_quiz_submission_pass_time ON whitelist_quiz_submission (pass_status, submit_time DESC);

-- ========================================
-- 4. whitelist_quiz_submission_detail 表索引优化
-- ========================================
-- 添加问题类型和分数索引
CREATE INDEX idx_quiz_detail_type ON whitelist_quiz_submission_detail (question_type);
CREATE INDEX idx_quiz_detail_score ON whitelist_quiz_submission_detail (score DESC);
CREATE INDEX idx_quiz_detail_del_flag ON whitelist_quiz_submission_detail (del_flag);
-- 复合索引：提交ID+正确性（常用组合）
CREATE INDEX idx_quiz_detail_submission_correct ON whitelist_quiz_submission_detail (submission_id, is_correct);

-- ========================================
-- 5. whitelist_quiz_config 表索引优化
-- ========================================
CREATE INDEX idx_quiz_config_del_flag ON whitelist_quiz_config (del_flag);
CREATE INDEX idx_quiz_config_create_time ON whitelist_quiz_config (create_time DESC);

-- ========================================
-- 6. qq_bot_config 表索引优化
-- ========================================
-- 添加机器人QQ和时间索引
CREATE INDEX idx_bot_config_bot_qq ON qq_bot_config (bot_qq);
CREATE INDEX idx_bot_config_create_time ON qq_bot_config (create_time DESC);
CREATE INDEX idx_bot_config_last_login ON qq_bot_config (last_login_time DESC);
CREATE INDEX idx_bot_config_last_heartbeat ON qq_bot_config (last_heartbeat_time DESC);

-- ========================================
-- 7. qq_bot_manager 表索引优化
-- ========================================
-- 添加权限类型和最后活动时间索引
CREATE INDEX idx_bot_manager_permission ON qq_bot_manager (permission_type);
CREATE INDEX idx_bot_manager_last_active ON qq_bot_manager (last_active_time DESC);
CREATE INDEX idx_bot_manager_create_time ON qq_bot_manager (create_time DESC);
-- 复合索引：机器人ID+状态（常用组合）
CREATE INDEX idx_bot_manager_bot_status ON qq_bot_manager (bot_id, status);

-- ========================================
-- 8. qq_bot_manager_group 表索引优化
-- ========================================
-- 添加创建时间索引
CREATE INDEX idx_bot_manager_group_create_time ON qq_bot_manager_group (create_time DESC);
-- 复合索引：管理员ID+状态（常用组合）
CREATE INDEX idx_bot_manager_group_manager_status ON qq_bot_manager_group (manager_id, status);

-- ========================================
-- 9. qq_bot_log 表索引优化
-- ========================================
-- 添加消息类型和执行时间索引
CREATE INDEX idx_bot_log_message_type ON qq_bot_log (message_type);
CREATE INDEX idx_bot_log_execution_time ON qq_bot_log (execution_time DESC);
CREATE INDEX idx_bot_log_sender_type ON qq_bot_log (sender_type);
CREATE INDEX idx_bot_log_receiver_type ON qq_bot_log (receiver_type);
-- 复合索引：机器人ID+日志类型+创建时间（常用组合）
CREATE INDEX idx_bot_log_bot_type_time ON qq_bot_log (bot_id, log_type, create_time DESC);
