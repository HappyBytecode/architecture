package com.lirui.lib_common.net.netError;

/**
 * 服务器返回的错误异常
 */
public class ServerException extends RuntimeException {
    public int code;
    public String message;

    public ServerException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

}
