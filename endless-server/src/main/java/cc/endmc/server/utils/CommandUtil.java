package cc.endmc.server.utils;

import java.util.*;

/**
 * 命令工具类
 * 用于判断命令是否为高危命令
 *
 * @author Memory
 */
public class CommandUtil {

    /**
     * 精确匹配的高危命令集合（不含空格的命令）
     */
    private static final Set<String> EXACT_MATCH_COMMANDS = new HashSet<>(Arrays.asList(
            "stop",                // 关闭服务器
            "reload",              // 重载服务器
            "restart",             // 重启服务器
            "shutdown",            // 关闭服务器
            "save-off",            // 关闭自动保存
            "fill",                // 填充大量方块
            "setblock",            // 设置方块
            "worldedit",           // WorldEdit命令
            "mvtp",                // Multiverse传送
            "permissions",         // 权限操作
            "god",                 // 上帝模式
            "kickall",             // 踢出所有玩家
            "killall",             // 杀死所有实体
            "spawnmob",            // 生成怪物
            "sudo",                // 以他人身份执行命令
            "unlimited",           // 无限物品
            "nuke",                // 核爆
            "tpall",               // 传送所有人
            "antioch",             // 圣手雷
            "give",                // 给予物品
            "item",                // 给予物品
            "more",                // 更多物品
            "backup",              // 备份服务器
            "fireball",            // 火球
            "lightning",           // 闪电
            "thunder",             // 雷暴
            "tempban",             // 临时封禁
            "banip",               // IP封禁
            "unbanip",             // 解除IP封禁
            "mute",                // 禁言
            "broadcast",           // 广播
            "clear",               // 清空背包
            "clearinventory",      // 清空背包
            "socialspy",           // 窥探私聊
            "tphere",              // 传送到这里
            "tppos",               // 传送到坐标
            "top",                 // 传送到顶部
            "tptoggle",            // 切换传送
            "vanish",              // 隐身
            "plugman",             // 插件管理
            "timings",             // 服务器性能分析
            "lag",                 // 卡顿分析
            "pstop",               // 停止服务器
            "essentialsreload"     // Essentials重载
    ));

    /**
     * 前缀匹配的高危命令列表（包含空格或需要前缀匹配的命令）
     * 按长度降序排列，确保最长匹配优先
     */
    private static final List<String> PREFIX_MATCH_COMMANDS = new ArrayList<>();

    static {
        // 初始化前缀匹配命令
        String[] prefixes = {
                "whitelist off",       // 关闭白名单
                "kill @e",             // 杀死所有实体
                "defaultgamemode ",    // 修改默认游戏模式
                "difficulty ",         // 修改游戏难度
                "gamerule ",           // 修改游戏规则
                "pex group default set", // PermissionsEx更改默认组权限
                "pex user * set",      // PermissionsEx设置所有用户权限
                "lp group default set", // LuckPerms更改默认组权限
                "lp user * set",       // LuckPerms设置所有用户权限
                "essentials.eco",      // 经济系统修改
                "essentials.gamemode", // 修改游戏模式
                "essentials.give",     // 给予物品
                "essentials.clearinventory", // 清空背包
                "essentials reload",   // 重载Essentials
                "worldborder set",     // 设置世界边界
                "coreprotect ",        // CoreProtect核心保护
                "co rollback",         // CoreProtect回滚
                "co restore",          // CoreProtect恢复
                "co purge",            // CoreProtect清除数据
                "mv delete",           // Multiverse删除世界
                "mv remove",           // Multiverse移除世界
                "mv unload",           // Multiverse卸载世界
                "mv modify",           // Multiverse修改世界设置
                "eco give",            // 给予金钱
                "eco reset",           // 重置经济
                "eco set",             // 设置金钱
                "eco take",            // 移除金钱
                "ess reload",          // 重载Essentials
                "ext all",             // 灭所有火
                "ext -a",              // 灭所有火
                "op ",                 // 给予OP权限
                "deop ",               // 移除OP权限
                "ban-ip ",             // IP封禁
                "pardon-ip ",          // 解除IP封禁
                "ban ",                // 封禁玩家
                "pardon ",             // 解除玩家封禁
                "upc ",                // UltraPermissions
                "lpc ",                // LuckPerms
                "pex ",                // PermissionsEx
                "nuker ",              // 核爆插件
                "pl ",                 // 插件操作
                "plugin ",             // 插件操作
                "we",                  // WorldEdit简写
                "ext",                 // 灭火
                "ci",                  // 清空背包简写
                "tp"                   // 传送（注意：这个会有一定误报风险，但比 "i" 和 "v" 更合理）
        };

        // 按长度降序排列，确保最长匹配优先
        Arrays.sort(prefixes, (a, b) -> Integer.compare(b.length(), a.length()));
        PREFIX_MATCH_COMMANDS.addAll(Arrays.asList(prefixes));
    }

    /**
     * 判断是否为高危命令
     *
     * @param command 要执行的命令
     * @return 是否为高危命令
     */
    public static boolean isHighRiskCommand(String command) {
        if (command == null || command.isEmpty()) {
            return false;
        }

        String cmdLower = command.trim().toLowerCase();

        // 1. 先检查精确匹配（O(1) 复杂度）
        if (EXACT_MATCH_COMMANDS.contains(cmdLower)) {
            return true;
        }

        // 2. 再检查前缀匹配
        for (String prefix : PREFIX_MATCH_COMMANDS) {
            if (cmdLower.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取高危命令列表（用于调试和展示）
     *
     * @return 高危命令列表
     */
    public static List<String> getHighRiskCommands() {
        List<String> allCommands = new ArrayList<>(EXACT_MATCH_COMMANDS);
        allCommands.addAll(PREFIX_MATCH_COMMANDS);
        Collections.sort(allCommands);
        return allCommands;
    }
}
