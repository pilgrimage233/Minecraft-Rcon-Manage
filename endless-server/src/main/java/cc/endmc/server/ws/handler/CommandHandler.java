package cc.endmc.server.ws.handler;

import cc.endmc.server.ws.QQMessage;

/**
 * 命令处理器接口
 */
@FunctionalInterface
public interface CommandHandler {
    /**
     * 处理命令
     *
     * @param message QQ消息对象
     */
    void handle(QQMessage message);
}
