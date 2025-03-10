package com.ruoyi.server.common.rconclient;


public class AuthFailureException extends RconClientException {
    public AuthFailureException() {
        super("Authentication failure");
    }
}
