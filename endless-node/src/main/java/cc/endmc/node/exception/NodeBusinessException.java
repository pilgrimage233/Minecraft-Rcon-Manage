package cc.endmc.node.exception;

/**
 * 节点业务异常
 * 用于处理业务逻辑相关的异常
 *
 * @author Memory
 */
public class NodeBusinessException extends RuntimeException {

    public NodeBusinessException(String message) {
        super(message);
    }

    public NodeBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
