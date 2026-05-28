package cc.endmc.node.exception;

/**
 * 节点验证异常
 * 用于处理输入验证相关的异常
 *
 * @author Memory
 */
public class NodeValidationException extends RuntimeException {

    public NodeValidationException(String message) {
        super(message);
    }

    public NodeValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
