-- 更新现有的测验提交记录，关联对应的白名单ID
-- 通过玩家UUID匹配白名单记录
UPDATE whitelist_quiz_submission wqs
    INNER JOIN whitelist_info wi ON wqs.player_uuid = wi.user_uuid
SET wqs.whitelist_id = wi.id
WHERE wqs.whitelist_id IS NULL
  AND wi.user_uuid IS NOT NULL
  AND wi.user_uuid != '';

-- 对于无法通过UUID匹配的记录，尝试通过玩家名称匹配
UPDATE whitelist_quiz_submission wqs
    INNER JOIN whitelist_info wi ON wqs.player_name = wi.user_name
SET wqs.whitelist_id = wi.id
WHERE wqs.whitelist_id IS NULL
  AND wi.user_name IS NOT NULL
  AND wi.user_name != '';