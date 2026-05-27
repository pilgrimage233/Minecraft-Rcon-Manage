-- =====================================================
-- 版本: 3.8.2
-- 描述: 公告功能增强（类型颜色、生效时间、前台展示、置顶）
-- 作者: Memory
-- 日期: 2026-02-28
-- =====================================================

ALTER TABLE `sys_notice`
    ADD COLUMN `type_color` varchar(20) NULL DEFAULT NULL COMMENT '类型颜色（如 #3b82f6）' AFTER `status`,
    ADD COLUMN `effective_start_time` datetime NULL DEFAULT NULL COMMENT '生效开始时间' AFTER `type_color`,
    ADD COLUMN `effective_end_time` datetime NULL DEFAULT NULL COMMENT '生效结束时间' AFTER `effective_start_time`,
    ADD COLUMN `show_in_frontend` char(1) NULL DEFAULT '1' COMMENT '是否在前台展示（0否 1是）' AFTER `effective_end_time`,
    ADD COLUMN `is_pinned` char(1) NULL DEFAULT '0' COMMENT '是否置顶（0否 1是）' AFTER `show_in_frontend`;

ALTER TABLE `sys_notice`
    MODIFY COLUMN `type_color` varchar(64) NULL DEFAULT NULL COMMENT '类型颜色（如 #3b82f6）';
