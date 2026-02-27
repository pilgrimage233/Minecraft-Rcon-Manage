-- =====================================================
-- 版本: 3.8.0
-- 描述: 白名单用户新增等级/头衔/投票发起权限
-- 作者: Memory
-- 日期: 2026-02-16
-- =====================================================

ALTER TABLE `whitelist_user`
    ADD COLUMN `role_level`        int         NOT NULL DEFAULT 1 COMMENT '用户等级(1成员, 50代表, 80管理员, 100Owner)' AFTER `status`,
    ADD COLUMN `role_title`        varchar(64) NOT NULL DEFAULT '成员' COMMENT '用户头衔' AFTER `role_level`,
    ADD COLUMN `can_initiate_vote` tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否可发起投票(0否 1是)' AFTER `role_title`;

ALTER TABLE `whitelist_user`
    ADD KEY `idx_whitelist_user_role_level` (`role_level`),
    ADD KEY `idx_whitelist_user_can_initiate` (`can_initiate_vote`);

-- 确保历史数据都有默认头衔
UPDATE `whitelist_user`
SET `role_title` = '成员'
WHERE `role_title` IS NULL
   OR `role_title` = '';

-- 保证至少有一位可以发起投票的代表用户
-- 若当前无人具备发起权限，则将最早注册的一位提升为代表成员
UPDATE `whitelist_user` wu
    JOIN (SELECT id
          FROM whitelist_user
          ORDER BY id asc
          LIMIT 1) seed ON wu.id = seed.id
SET wu.can_initiate_vote = 1,
    wu.role_level        = IF(wu.role_level < 50, 50, wu.role_level),
    wu.role_title        = IF(wu.role_title IS NULL OR wu.role_title = '' OR wu.role_title = '成员', '代表成员',
                              wu.role_title)
WHERE NOT EXISTS (SELECT 1 FROM whitelist_user WHERE can_initiate_vote = 1);
