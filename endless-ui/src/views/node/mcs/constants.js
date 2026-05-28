/**
 * MCS Terminal 组件常量定义
 * 集中管理所有常量，便于维护和复用
 */

// ==================== 文件相关常量 ====================

/** MC 配置文件列表 */
export const MC_CONFIG_FILES = [
    'server.properties',
    'bukkit.yml',
    'spigot.yml',
    'paper.yml',
    'paper-global.yml',
    'paper-world-defaults.yml',
    'velocity.toml',
    'bungeecord.yml',
    'config.yml'
]

/** 可预览的文本文件扩展名 */
export const TEXT_EXTENSIONS = [
    '.txt', '.json', '.xml', '.html', '.css', '.js', '.md', '.log',
    '.yml', '.yaml', '.properties', '.conf', '.ini', '.toml', '.cfg',
    '.sh', '.bat', '.cmd', '.ps1', '.py', '.java', '.c', '.cpp', '.h',
    '.cs', '.php', '.sql', '.vue', '.ts', '.tsx', '.jsx', '.go', '.rs',
    '.rb', '.pl', '.lua', '.r', '.scala', '.kt', '.swift', '.dart',
    '.dockerfile', '.gitignore', '.gitattributes', '.editorconfig',
    '.env', '.example', '.sample', '.template', '.backup', '.bak',
    '.config', '.settings', '.prefs', '.options', '.rc', '.profile'
]

/** 无扩展名的特殊文件名 */
export const SPECIAL_FILE_NAMES = [
    'dockerfile', 'makefile', 'readme', 'license', 'changelog', 'authors', 'contributors'
]

/** 图片文件扩展名 */
export const IMAGE_EXTENSIONS = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg', '.ico']

/** 编辑器语言映射 */
export const EDITOR_LANGUAGE_MAP = {
    // Web 相关
    '.html': 'html',
    '.htm': 'html',
    '.css': 'css',
    '.js': 'javascript',
    '.jsx': 'javascript',
    '.ts': 'typescript',
    '.tsx': 'typescript',
    '.vue': 'html',
    // 配置文件
    '.json': 'json',
    '.xml': 'xml',
    '.yml': 'yaml',
    '.yaml': 'yaml',
    '.toml': 'toml',
    '.ini': 'ini',
    '.conf': 'ini',
    '.cfg': 'ini',
    '.properties': 'properties',
    // 脚本
    '.sh': 'shell',
    '.bash': 'shell',
    '.bat': 'bat',
    '.cmd': 'bat',
    '.ps1': 'powershell',
    // 标记语言
    '.md': 'markdown',
    '.markdown': 'markdown',
    // 其他
    '.log': 'plaintext',
    '.txt': 'plaintext'
}

/** 特殊文件名语言映射 */
export const SPECIAL_FILE_LANGUAGE_MAP = {
    'dockerfile': 'dockerfile',
    'makefile': 'makefile',
    'readme': 'markdown',
    'license': 'plaintext',
    'changelog': 'markdown'
}

/** 图片 MIME 类型映射 */
export const IMAGE_MIME_TYPES = {
    'jpg': 'image/jpeg',
    'jpeg': 'image/jpeg',
    'png': 'image/png',
    'gif': 'image/gif',
    'bmp': 'image/bmp',
    'webp': 'image/webp',
    'svg': 'image/svg+xml'
}

// ==================== 终端相关常量 ====================

/** 控制台最大行数 */
export const CONSOLE_MAX_LINES = 2000

/** 状态轮询间隔（毫秒） */
export const STATUS_POLL_INTERVAL = 10000

/** 玩家列表刷新间隔（毫秒） */
export const PLAYERS_REFRESH_INTERVAL = 30000

/** WebSocket 连接超时（毫秒） */
export const WS_CONNECT_TIMEOUT = 10000

/** WebSocket 重连延迟（毫秒） */
export const WS_RECONNECT_DELAY = 5000

/** 滚动到底部阈值（像素） */
export const SCROLL_BOTTOM_THRESHOLD = 80

/** 防抖延迟（毫秒） */
export const DEBOUNCE_DELAY = 100

// ==================== 终端主题相关 ====================

/** 可用终端主题列表 */
export const TERMINAL_THEMES = [
    'github-dark',
    'dracula',
    'monokai',
    'solarized-dark',
    'one-dark',
    'terminal-green',
    'warm-light'
]

/** 终端主题显示名称 */
export const TERMINAL_THEME_NAMES = {
    'github-dark': 'GitHub Dark',
    'dracula': 'Dracula',
    'monokai': 'Monokai',
    'solarized-dark': 'Solarized Dark',
    'one-dark': 'One Dark',
    'terminal-green': 'Terminal Green',
    'warm-light': 'Warm-Light'
}

// ==================== WebSocket 连接模式 ====================

/** WebSocket 连接模式 */
export const WS_CONNECTION_MODES = {
    AUTO: 'auto',
    DIRECT: 'direct',
    PROXY: 'proxy'
}

// ==================== 日志级别颜色 ====================

/** 日志级别颜色映射 */
export const LOG_LEVEL_COLORS = {
    'INFO': '#67c23a',    // 绿色
    'WARN': '#e6a23c',    // 黄色
    'WARNING': '#e6a23c', // 黄色
    'ERROR': '#f56c6c',   // 红色
    'SEVERE': '#f56c6c',  // 红色
    'DEBUG': '#909399',   // 灰色
    'TRACE': '#c0c4cc'    // 浅灰色
}

/** 日志级别正则表达式 */
export const LOG_LEVEL_PATTERN = /^(\[[\d:]+\s+(INFO|WARN|WARNING|ERROR|SEVERE|DEBUG|TRACE)\]:\s*)(.*)/i

// ==================== 命令相关 ====================

/** Minecraft 命令建议列表 */
export const MC_COMMAND_SUGGESTIONS = [
    // 基础命令
    { value: 'help', description: '显示帮助信息' },
    { value: 'stop', description: '停止服务器' },
    { value: 'save-all', description: '保存所有世界数据' },
    { value: 'save-on', description: '启用自动保存' },
    { value: 'save-off', description: '禁用自动保存' },
    // 玩家管理
    { value: 'kick <玩家> [原因]', description: '踢出玩家' },
    { value: 'ban <玩家> [原因]', description: '封禁玩家' },
    { value: 'ban-ip <IP> [原因]', description: '封禁IP地址' },
    { value: 'pardon <玩家>', description: '解除玩家封禁' },
    { value: 'pardon-ip <IP>', description: '解除IP封禁' },
    { value: 'banlist [players|ips]', description: '查看封禁列表' },
    { value: 'whitelist add <玩家>', description: '添加白名单' },
    { value: 'whitelist remove <玩家>', description: '移除白名单' },
    { value: 'whitelist list', description: '查看白名单' },
    { value: 'whitelist on', description: '启用白名单' },
    { value: 'whitelist off', description: '关闭白名单' },
    { value: 'whitelist reload', description: '重载白名单' },
    { value: 'op <玩家>', description: '给予玩家管理员权限' },
    { value: 'deop <玩家>', description: '移除玩家管理员权限' },
    // 游戏模式
    { value: 'gamemode survival [玩家]', description: '设置生存模式' },
    { value: 'gamemode creative [玩家]', description: '设置创造模式' },
    { value: 'gamemode adventure [玩家]', description: '设置冒险模式' },
    { value: 'gamemode spectator [玩家]', description: '设置旁观模式' },
    // 时间和天气
    { value: 'time set day', description: '设置为白天' },
    { value: 'time set night', description: '设置为夜晚' },
    { value: 'time set <时间>', description: '设置时间' },
    { value: 'time add <时间>', description: '增加时间' },
    { value: 'time query daytime', description: '查询游戏时间' },
    { value: 'weather clear [持续时间]', description: '设置晴天' },
    { value: 'weather rain [持续时间]', description: '设置雨天' },
    { value: 'weather thunder [持续时间]', description: '设置雷雨' },
    // 传送
    { value: 'tp <玩家> <目标玩家>', description: '传送玩家到另一玩家' },
    { value: 'tp <玩家> <x> <y> <z>', description: '传送玩家到坐标' },
    { value: 'teleport <玩家> <x> <y> <z>', description: '传送玩家到坐标' },
    // 给予物品
    { value: 'give <玩家> <物品> [数量]', description: '给予玩家物品' },
    { value: 'clear <玩家> [物品]', description: '清空玩家物品' },
    // 效果
    { value: 'effect give <玩家> <效果> [持续时间] [等级]', description: '给予玩家效果' },
    { value: 'effect clear <玩家> [效果]', description: '清除玩家效果' },
    // 经验
    { value: 'xp add <玩家> <数量> [points|levels]', description: '给予玩家经验' },
    { value: 'xp set <玩家> <数量> [points|levels]', description: '设置玩家经验' },
    { value: 'xp query <玩家> [points|levels]', description: '查询玩家经验' },
    // 难度
    { value: 'difficulty peaceful', description: '设置和平难度' },
    { value: 'difficulty easy', description: '设置简单难度' },
    { value: 'difficulty normal', description: '设置普通难度' },
    { value: 'difficulty hard', description: '设置困难难度' },
    // 世界管理
    { value: 'seed', description: '显示世界种子' },
    { value: 'setworldspawn [x] [y] [z]', description: '设置世界出生点' },
    { value: 'spawnpoint <玩家> [x] [y] [z]', description: '设置玩家出生点' },
    { value: 'gamerule <规则> [值]', description: '设置游戏规则' },
    { value: 'gamerule keepInventory true', description: '死亡不掉落' },
    { value: 'gamerule doDaylightCycle false', description: '停止时间流逝' },
    { value: 'gamerule doMobSpawning false', description: '禁止生物生成' },
    { value: 'gamerule doFireTick false', description: '禁止火焰蔓延' },
    { value: 'gamerule mobGriefing false', description: '禁止生物破坏方块' },
    // 聊天和消息
    { value: 'say <消息>', description: '向所有玩家发送消息' },
    { value: 'tell <玩家> <消息>', description: '向玩家发送私聊消息' },
    { value: 'msg <玩家> <消息>', description: '向玩家发送私聊消息' },
    { value: 'w <玩家> <消息>', description: '向玩家发送私聊消息' },
    { value: 'me <动作>', description: '发送动作消息' },
    { value: 'title <玩家> title <文本>', description: '显示标题' },
    { value: 'title <玩家> subtitle <文本>', description: '显示副标题' },
    { value: 'title <玩家> actionbar <文本>', description: '显示快捷栏文本' },
    // 服务器信息
    { value: 'list', description: '列出在线玩家' },
    { value: 'list uuids', description: '列出在线玩家及UUID' },
    { value: 'tps', description: '查看服务器TPS' },
    { value: 'perf', description: '查看性能信息' },
    { value: 'timings', description: '性能分析工具' },
    // 插件管理
    { value: 'plugins', description: '列出所有插件' },
    { value: 'pl', description: '列出所有插件' },
    { value: 'reload', description: '重载服务器配置' },
    { value: 'reload confirm', description: '确认重载服务器' },
    { value: 'version', description: '查看服务器版本' },
    { value: 'ver', description: '查看服务器版本' },
    // 权限管理 (LuckPerms)
    { value: 'lp user <玩家> permission set <权限> true', description: 'LP: 给予玩家权限' },
    { value: 'lp user <玩家> permission unset <权限>', description: 'LP: 移除玩家权限' },
    { value: 'lp user <玩家> parent add <组>', description: 'LP: 添加玩家到组' },
    { value: 'lp user <玩家> parent remove <组>', description: 'LP: 从组移除玩家' },
    { value: 'lp group <组> permission set <权限> true', description: 'LP: 给予组权限' },
    { value: 'lp group list', description: 'LP: 列出所有组' },
    // 世界编辑 (WorldEdit)
    { value: '//wand', description: 'WE: 获取选区工具' },
    { value: '//pos1', description: 'WE: 设置第一个点' },
    { value: '//pos2', description: 'WE: 设置第二个点' },
    { value: '//set <方块>', description: 'WE: 填充选区' },
    { value: '//replace <旧方块> <新方块>', description: 'WE: 替换方块' },
    { value: '//copy', description: 'WE: 复制选区' },
    { value: '//paste', description: 'WE: 粘贴选区' },
    { value: '//undo', description: 'WE: 撤销操作' },
    { value: '//redo', description: 'WE: 重做操作' },
    // 领地管理 (Residence)
    { value: 'res create <领地名>', description: 'Res: 创建领地' },
    { value: 'res remove <领地名>', description: 'Res: 删除领地' },
    { value: 'res tp <领地名>', description: 'Res: 传送到领地' },
    { value: 'res pset <领地名> <玩家> <权限> true', description: 'Res: 设置玩家权限' },
    // 经济管理 (Vault/EssentialsX)
    { value: 'eco give <玩家> <金额>', description: '给予玩家金钱' },
    { value: 'eco take <玩家> <金额>', description: '扣除玩家金钱' },
    { value: 'eco set <玩家> <金额>', description: '设置玩家金钱' },
    { value: 'balance <玩家>', description: '查看玩家余额' },
    { value: 'bal <玩家>', description: '查看玩家余额' },
    { value: 'pay <玩家> <金额>', description: '支付给玩家' },
    // EssentialsX 常用命令
    { value: 'spawn', description: 'Ess: 传送到出生点' },
    { value: 'home [名称]', description: 'Ess: 传送到家' },
    { value: 'sethome [名称]', description: 'Ess: 设置家' },
    { value: 'delhome [名称]', description: 'Ess: 删除家' },
    { value: 'warp <传送点>', description: 'Ess: 传送到传送点' },
    { value: 'setwarp <传送点>', description: 'Ess: 设置传送点' },
    { value: 'delwarp <传送点>', description: 'Ess: 删除传送点' },
    { value: 'tpa <玩家>', description: 'Ess: 请求传送到玩家' },
    { value: 'tpahere <玩家>', description: 'Ess: 请求玩家传送到你' },
    { value: 'tpaccept', description: 'Ess: 接受传送请求' },
    { value: 'tpdeny', description: 'Ess: 拒绝传送请求' },
    { value: 'back', description: 'Ess: 返回上一个位置' },
    // 调试命令
    { value: 'debug start', description: '开始调试' },
    { value: 'debug stop', description: '停止调试' },
    { value: 'debug report', description: '生成调试报告' }
]

// ==================== 玩家操作相关 ====================

/** 玩家操作描述映射 */
export const PLAYER_ACTION_DESCRIPTIONS = {
    'kick': '踢出',
    'ban': '封禁',
    'ban-ip': 'IP封禁',
    'pardon': '解封',
    'pardon-ip': 'IP解封',
    'op': '设为管理员',
    'deop': '取消管理员',
    'whitelist-add': '加入白名单',
    'whitelist-remove': '移出白名单',
    'gamemode-creative': '设为创造模式',
    'gamemode-survival': '设为生存模式',
    'gamemode-adventure': '设为冒险模式',
    'gamemode-spectator': '设为观察者模式',
    'tp-to-spawn': '传送到出生点'
}

/** 需要输入原因的操作 */
export const ACTIONS_REQUIRING_REASON = ['kick', 'ban', 'ban-ip']

// ==================== 默认头像 ====================

/** 默认玩家头像 SVG */
export const DEFAULT_AVATAR_SVG = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMiAzMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjMyIiBoZWlnaHQ9IjMyIiBmaWxsPSIjNDA5RUZGIi8+CjxwYXRoIGQ9Ik0xNiA4QzEzLjc5IDggMTIgOS43OSAxMiAxMkMxMiAxNC4yMSAxMy43OSAxNiAxNiAxNkMxOC4yMSAxNiAyMCAxNC4yMSAyMCAxMkMyMCA5Ljc5IDE4LjIxIDggMTYgOFpNMTYgMjJDMTIuNjcgMjIgNiAyMy4zNCA2IDI2LjY3VjI4SDE2SDI2VjI2LjY3QzI2IDIzLjM0IDE5LjMzIDIyIDE6IDIyWiIgZmlsbD0id2hpdGUiLz4KPC9zdmc+'
