package cc.endmc.server.utils;

public class CommandUtil {

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

        // 将命令转为小写并去除前导空格，以便更准确地匹配
        String cmdLower = command.trim().toLowerCase();

        // 高危命令列表
        String[] highRiskCommands = {
                // 服务器核心命令
                "stop",                // 关闭服务器
                "reload",              // 重载服务器
                "restart",             // 重启服务器
                "shutdown",            // 关闭服务器
                "whitelist off",       // 关闭白名单
                "op ",                 // 给予OP权限
                "deop ",               // 移除OP权限
                "ban-ip ",             // IP封禁
                "pardon-ip ",          // 解除IP封禁
                "ban ",                // 封禁玩家
                "pardon ",             // 解除玩家封禁
                "save-off",            // 关闭自动保存
                "kill @e",             // 杀死所有实体
                "difficulty ",         // 修改游戏难度
                "gamerule ",           // 修改游戏规则
                "defaultgamemode ",    // 修改默认游戏模式

                // 世界编辑命令
                "fill",                // 填充大量方块
                "setblock",            // 设置方块
                "worldedit",           // WorldEdit命令
                "we",                  // WorldEdit简写
                "/expand",             // WorldEdit扩展选区
                "/set",                // WorldEdit设置方块
                "/replace",            // WorldEdit替换方块

                // 多世界管理插件
                "mv delete",           // Multiverse删除世界
                "mv remove",           // Multiverse移除世界
                "mv unload",           // Multiverse卸载世界
                "mv modify",           // Multiverse修改世界设置
                "mvtp",                // Multiverse传送

                // 权限插件
                "pex group default set",  // PermissionsEx更改默认组权限
                "pex user * set",         // PermissionsEx设置所有用户权限
                "lp group default set",   // LuckPerms更改默认组权限
                "lp user * set",          // LuckPerms设置所有用户权限
                "lp group",               // LuckPerms组操作
                "permissions",            // 权限操作

                // Essentials插件危险命令
                "essentials.eco",         // 经济系统修改
                "eco give",               // 给予金钱
                "eco reset",              // 重置经济
                "eco set",                // 设置金钱
                "eco take",               // 移除金钱
                "ess reload",             // 重载Essentials
                "essentials reload",      // 重载Essentials
                "god",                    // 上帝模式
                "ext",                    // 灭火
                "ext all",                // 灭所有火
                "ext -a",                 // 灭所有火
                "kickall",                // 踢出所有玩家
                "killall",                // 杀死所有实体
                "spawnmob",               // 生成怪物
                "sudo",                   // 以他人身份执行命令
                "unlimited",              // 无限物品
                "nuke",                   // 核爆
                "essentials.gamemode",    // 修改游戏模式
                "tpall",                  // 传送所有人
                "antioch",                // 圣手雷
                "essentials.give",        // 给予物品
                "give",                   // 给予物品
                "item",                   // 给予物品
                "i",                      // 给予物品简写
                "more",                   // 更多物品
                "backup",                 // 备份服务器
                "fireball",               // 火球
                "lightning",              // 闪电
                "thunder",                // 雷暴
                "tempban",                // 临时封禁
                "banip",                  // IP封禁
                "unbanip",                // 解除IP封禁
                "mute",                   // 禁言
                "broadcast",              // 广播
                "essentials.clearinventory", // 清空背包
                "clear",                  // 清空背包
                "ci",                     // 清空背包
                "clearinventory",         // 清空背包
                "socialspy",              // 窥探私聊
                "tp",                     // 传送
                "tphere",                 // 传送到这里
                "tppos",                  // 传送到坐标
                "top",                    // 传送到顶部
                "tptoggle",               // 切换传送
                "vanish",                 // 隐身
                "v",                      // 隐身简写

                // 其他常见插件的危险命令
                "upc ",                   // UltraPermissions
                "lpc ",                   // LuckPerms
                "pex ",                   // PermissionsEx
                "nuker ",                 // 核爆插件
                "essentialsreload",       // Essentials重载
                "plugman",                // 插件管理
                "pl ",                    // 插件操作
                "plugin ",                // 插件操作
                "worldborder set",        // 设置世界边界
                "timings",                // 服务器性能分析
                "lag",                    // 卡顿分析
                "pstop",                  // 停止服务器
                "coreprotect ",           // CoreProtect核心保护
                "co rollback",            // CoreProtect回滚
                "co restore",             // CoreProtect恢复
                "co purge"                // CoreProtect清除数据
        };

        // 检查命令是否匹配任何高危命令
        for (String highRiskCmd : highRiskCommands) {
            if (cmdLower.startsWith(highRiskCmd.trim().toLowerCase())) {
                return true;
            }
        }

        return false;
    }

}
