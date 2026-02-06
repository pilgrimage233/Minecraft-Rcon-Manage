-- 为白名单ID更改历史表添加白名单ID字段
ALTER TABLE `whitelist_id_change_history`
    ADD COLUMN `whitelist_id` BIGINT(20) NULL COMMENT '白名单ID' AFTER `id`;

-- 添加索引
ALTER TABLE `whitelist_id_change_history`
    ADD INDEX `idx_whitelist_id` (`whitelist_id`);

-- 更新现有数据（尝试关联白名单ID）
UPDATE `whitelist_id_change_history` h
    LEFT JOIN `whitelist_info` w ON (w.user_name = h.new_user_name or w.user_name = h.old_user_name) AND
                                    w.qq_num = h.qq_num
SET h.whitelist_id = w.id
WHERE h.whitelist_id IS NULL
  AND w.id IS NOT NULL;
