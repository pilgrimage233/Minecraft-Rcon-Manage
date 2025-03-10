package com.ruoyi.server.common.rconclient;

/**
 * Generic exception thrown by {@link RconClientException} when any exception occurs.
 */
public class RconClientException extends RuntimeException {
    public RconClientException(String message) {
        super(message);
    }

    public RconClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
