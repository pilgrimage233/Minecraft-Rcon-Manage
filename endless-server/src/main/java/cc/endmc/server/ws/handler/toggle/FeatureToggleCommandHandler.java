package cc.endmc.server.ws.handler.toggle;

import cc.endmc.common.core.redis.RedisCache;
import cc.endmc.common.utils.StringUtils;
import cc.endmc.server.domain.bot.BotGroupCommandConfig;
import cc.endmc.server.domain.bot.QqBotManager;
import cc.endmc.server.service.bot.IBotGroupCommandConfigService;
import cc.endmc.server.ws.BotClient;
import cc.endmc.server.ws.QQMessage;
import cc.endmc.server.ws.handler.BaseCommandHandler;
import cc.endmc.server.ws.handler.CommandRegistry;
import cc.endmc.server.ws.helper.BotMessageHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 功能开关命令处理器
 * 处理功能启用/禁用相关的命令
 */
@Slf4j
public class FeatureToggleCommandHandler extends BaseCommandHandler {

    private final CommandRegistry commandRegistry;
    private final IBotGroupCommandConfigService commandConfigService;

    public FeatureToggleCommandHandler(BotClient botClient, RedisCache redisCache,
                                       CommandRegistry commandRegistry,
                                       IBotGroupCommandConfigService commandConfigService) {
        super(botClient, redisCache);
        this.commandRegistry = commandRegistry;
        this.commandConfigService = commandConfigService;
    }

    /**
     * 注册功能开关命令到命令注册器
     */
    public void registerCommands(CommandRegistry registry) {
        registry.register("关闭", this::handleDisableCommand, "disable", "off");
        registry.register("开启", this::handleEnableCommand, "enable", "on");
        registry.register("功能列表", this::handleCommandList, "cmdlist", "cl");
    }

    /**
     * 判断是否是功能控制命令（这些命令不受开关限制）
     */
    public boolean isCommandControlCommand(String command) {
        return "关闭".equals(command) || "开启".equals(command) || "功能列表".equals(command) || "help".equals(command);
    }

    /**
     * 处理关闭功能命令
     */
    public void handleDisableCommand(QQMessage message) {
        handleToggleCommand(message, false);
    }

    /**
     * 处理开启功能命令
     */
    public void handleEnableCommand(QQMessage message) {
        handleToggleCommand(message, true);
    }

    /**
     * 处理功能开关切换
     */
    private void handleToggleCommand(QQMessage message, boolean enable) {
        executeWithPermissionCheck(message, false, () -> {
            String base = getAtPrefix(message);

            // 检查管理员权限
            List<QqBotManager> managers = getManagers(message);
            if (managers.isEmpty()) {
                sendMessage(message, base + " 您没有权限执行此操作，需要管理员权限。");
                return;
            }

            String[] parts = message.getMessage().split("\\s+");
            if (parts.length < 2) {
                String action = enable ? "开启" : "关闭";
                sendMessage(message, base + " 格式错误，正确格式：" + action + " <功能名称>\n使用 /功能列表 查看所有可用功能。");
                return;
            }

            String commandKey = parts[1];

            // 不允许关闭功能控制命令本身
            if (isCommandControlCommand(commandKey)) {
                sendMessage(message, base + " 该功能不允许被关闭。");
                return;
            }

            // 获取主命令名称（如果是注册的命令别名，则转换为主命令）
            String mainCommand = commandKey;
            if (commandRegistry.hasCommand(commandKey)) {
                mainCommand = commandRegistry.getMainCommand(commandKey);
            } else {
                // 检查是否是系统功能（非指令类功能，如玩家上下线通知）
                BotGroupCommandConfig systemConfig = commandConfigService.checkCommandEnabled("default", commandKey);
                if (systemConfig == null) {
                    sendMessage(message, base + " 未找到功能：" + commandKey + "\n使用 /功能列表 查看所有可用功能。");
                    return;
                }
            }

            // 执行切换
            int result = commandConfigService.toggleCommandStatus(
                    message.getGroupId().toString(),
                    mainCommand,
                    enable,
                    message.getSender().getUserId().toString()
            );

            if (result > 0) {
                String action = enable ? "开启" : "关闭";
                sendMessage(message, base + " 已成功" + action + "功能：" + mainCommand);
            } else if (result == -1) {
                sendMessage(message, base + " 功能配置不存在：" + mainCommand);
            } else {
                sendMessage(message, base + " 操作失败，请稍后重试。");
            }
        });
    }

    /**
     * 处理功能列表命令
     */
    public void handleCommandList(QQMessage message) {
        String base = getAtPrefix(message);
        String groupId = message.getGroupId().toString();

        StringBuilder sb = new StringBuilder();
        sb.append(base).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("📋 功能列表\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━\n\n");

        // 获取所有默认配置的命令
        BotGroupCommandConfig query = new BotGroupCommandConfig();
        query.setGroupId("default");
        List<BotGroupCommandConfig> defaultConfigs = commandConfigService.selectBotGroupCommandConfigList(query);

        // 按分类分组
        Map<String, List<BotGroupCommandConfig>> categoryMap = defaultConfigs.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCommandCategory() != null ? c.getCommandCategory() : "other"
                ));

        String[] categories = {"user", "admin", "super", "system"};
        String[] categoryNames = {"👥 普通用户功能", "👮 管理员功能", "⭐ 超级管理员功能", "🔔 系统通知功能"};

        for (int i = 0; i < categories.length; i++) {
            List<BotGroupCommandConfig> configs = categoryMap.get(categories[i]);
            if (configs == null || configs.isEmpty()) continue;

            sb.append(categoryNames[i]).append("\n");
            sb.append("────────────────────\n");

            for (BotGroupCommandConfig cfg : configs) {
                // 检查该群组的实际状态
                BotGroupCommandConfig actualConfig = commandConfigService.checkCommandEnabled(groupId, cfg.getCommandKey());
                boolean enabled = actualConfig == null || actualConfig.getIsEnabled() == null || actualConfig.getIsEnabled() == 1;
                String status = enabled ? "✅" : "❌";
                sb.append(status).append(" ").append(cfg.getCommandKey());
                if (StringUtils.isNotEmpty(cfg.getCommandName())) {
                    sb.append(" (").append(cfg.getCommandName()).append(")");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("💡 管理员可使用 /关闭 <功能> 或 /开启 <功能> 来控制");

        sendMessage(message, sb.toString());
    }
}
