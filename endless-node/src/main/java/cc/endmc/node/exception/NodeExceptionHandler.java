package cc.endmc.node.exception;

import cc.endmc.common.core.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;

/**
 * 节点模块全局异常处理器
 * 统一处理各类异常，返回友好的错误信息
 *
 * @author Memory
 */
@Slf4j
@RestControllerAdvice(basePackages = "cc.endmc.node")
public class NodeExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(NodeBusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AjaxResult handleBusinessException(NodeBusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 处理验证异常
     */
    @ExceptionHandler(NodeValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AjaxResult handleValidationException(NodeValidationException e) {
        log.warn("验证异常: {}", e.getMessage());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 处理连接超时异常
     */
    @ExceptionHandler(SocketTimeoutException.class)
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    public AjaxResult handleSocketTimeoutException(SocketTimeoutException e) {
        log.error("连接超时", e);
        return AjaxResult.error("连接超时，请稍后重试");
    }

    /**
     * 处理连接异常
     */
    @ExceptionHandler(ConnectException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public AjaxResult handleConnectException(ConnectException e) {
        log.error("连接失败", e);
        return AjaxResult.error("无法连接到节点服务器");
    }

    /**
     * 处理数据库异常
     */
    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public AjaxResult handleSQLException(SQLException e) {
        log.error("数据库异常", e);
        return AjaxResult.error("数据库操作失败");
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public AjaxResult handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return AjaxResult.error("系统内部错误");
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AjaxResult handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public AjaxResult handleException(Exception e) {
        log.error("系统异常", e);
        return AjaxResult.error("系统内部错误，请联系管理员");
    }
}
