package cc.endmc.server.ws.handler;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 命令注册器
 * 管理所有命令及其处理器的映射关系
 */
@Slf4j
public class CommandRegistry {

    /**
     * 命令处理器映射表
     * Key: 命令关键字（包括别名）
     * Value: 命令处理器
     */
    private final Map<String, CommandHandler> handlers = new ConcurrentHashMap<>();

    /**
     * 命令别名映射表
     * Key: 别名
     * Value: 主命令
     */
    private final Map<String, String> aliases = new ConcurrentHashMap<>();

    /**
     * 注册命令处理器
     *
     * @param command 主命令名称
     * @param handler 命令处理器
     * @param aliases 命令别名列表
     */
    public void register(String command, CommandHandler handler, String... aliases) {
        // 注册主命令
        handlers.put(command.toLowerCase(), handler);
        log.debug("注册命令: {}", command);

        // 注册别名
        for (String alias : aliases) {
            String lowerAlias = alias.toLowerCase();
            this.aliases.put(lowerAlias, command.toLowerCase());
            handlers.put(lowerAlias, handler);
            log.debug("注册命令别名: {} -> {}", alias, command);
        }
    }

    /**
     * 获取命令处理器
     *
     * @param command 命令名称或别名
     * @return 命令处理器，如果未找到则返回null
     */
    public CommandHandler getHandler(String command) {
        if (command == null) {
            return null;
        }
        return handlers.get(command.toLowerCase());
    }

    /**
     * 检查命令是否已注册
     *
     * @param command 命令名称或别名
     * @return 如果已注册返回true，否则返回false
     */
    public boolean hasCommand(String command) {
        if (command == null) {
            return false;
        }
        return handlers.containsKey(command.toLowerCase());
    }

    /**
     * 获取主命令名称
     *
     * @param commandOrAlias 命令名称或别名
     * @return 主命令名称，如果是别名则返回对应的主命令，否则返回原命令
     */
    public String getMainCommand(String commandOrAlias) {
        if (commandOrAlias == null) {
            return null;
        }
        String lower = commandOrAlias.toLowerCase();
        return aliases.getOrDefault(lower, lower);
    }

    /**
     * 获取所有已注册的命令
     *
     * @return 命令集合
     */
    public Set<String> getAllCommands() {
        return new HashSet<>(handlers.keySet());
    }

    /**
     * 清空所有注册的命令
     */
    public void clear() {
        handlers.clear();
        aliases.clear();
        log.info("已清空所有命令注册");
    }
}
