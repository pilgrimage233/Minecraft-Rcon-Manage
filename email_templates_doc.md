# 📧 邮件模板自定义文档

本文档整理了系统内置的邮件模板，用户可以根据需求自定义邮件内容。
所有模板均为 **HTML** 格式，并支持以下两类扩展： -
**替换标签**：在发送时会替换为实际数据（如 `{username}` → 玩家昵称）

- **样式 class**：定义了整体样式，用户可自定义 CSS 或修改结构

------------------------------------------------------------------------

## 1. 邮箱验证模板 (EMAIL_VERIFY_TEMPLATE)

**替换标签** - `{verifyLink}` → 验证链接

------------------------------------------------------------------------

## 2. 白名单审核通知 (WHITELIST_NOTIFICATION_TEMPLATE)

**替换标签** - `{username}` → 用户昵称

- `{gameId}` → 游戏ID
- `{applyTime}` → 申请时间
- `{reviewTime}` → 审核时间
- `{info}` → 服务器信息块

**动态插入区块**

- `<!-- 审核通过模板 -->`
  `<div class="status approved"><span>🎉 恭喜，您的白名单申请已通过！</span></div>`
- `<!-- 审核拒绝模板 -->`
  `<div class="status rejected"><span>😢 很抱歉，您的白名单申请未通过</span></div>`

**服务器信息块内标签** - `{name}` → 服务器名称

- `{serverAddress}` → 服务器地址
- `{port}` → 端口号
- `{core}` → 核心类型
- `{version}` → 版本号

**可用 class** -

- `status` (`approved` / `rejected`)

------------------------------------------------------------------------

## 3. 白名单移除/封禁通知 (WHITELIST_NOTIFICATION_TEMPLATE_BAN)

**替换标签** - `{username}`

- `{gameId}`
- `{applyTime}`
- `{timeTittle}` → 时间标题 (封禁时间 / 移除时间 / 拒审时间)
- `{time}` → 对应时间
- `{removeReason}` → 移除/封禁原因

**动态插入区块**

- `<!-- 移除模板 -->`  `<div class="status rejected"><span>😢 很抱歉，您的白名单已被移除</span></div>`
- `<!-- 解禁模板 -->` `<div class="status approved"><span>🎉 恭喜，您的封禁已解除！</span></div>`
- `<!-- 封禁模板 -->` `<div class="status rejected"><span>🚫 你已被封禁！🚫</span></div>`

**可用 class** - 与审核通知相同

- `status` (通常为 `rejected`)

------------------------------------------------------------------------

## 4. 白名单解禁通知 (WHITELIST_NOTIFICATION_TEMPLATE_UNBAN)

**替换标签** - `{username}`

- `{gameId}`
- `{banTime}` → 封禁时间
- `{unBanTime}` → 解禁时间

**动态插入区块** - `<!-- 解禁模板 -->`

**可用 class** - 与审核通知相同

------------------------------------------------------------------------

## 5. 白名单待审核通知 (WHITELIST_NOTIFICATION_TEMPLATE_PENDING)

**替换标签** - `{username}`

- `{gameId}`
- `{applyTime}`

**动态插入区块** - `<!-- 提交模板 -->`

**可用 class** - 与审核通知相同

------------------------------------------------------------------------

## 6. 系统异常告警 (ALERT_TEMPLATE)

**替换标签** - `{time}` → 异常时间

- `{count}` → 异常次数
- `{type}` → 异常类型
- `{serverName}` → 服务器名称
- `{serverAddress}` → 服务器地址

**可用 class** - `container`

- `header`
- `content`
- `alert-info`
- `info-box`
- `info-item`
- `info-label`
- `info-value`
- `footer`

------------------------------------------------------------------------

## 7. 模板扩展说明

- **动态插入区块** 使用 HTML 注释标记
  (`<!-- -->`)，在不同状态下会被系统替换为提示信息。
- **class 样式** 可统一修改，影响所有邮件模板的显示效果。
- **替换标签** 建议保留大括号格式 `{xxx}`，避免与 HTML 冲突。
