package com.dahaiwuliang.tongue;

/**
 * 统一业务异常,携带对外返回的 HTTP 状态码。
 */
public class TongueAnalysisException extends RuntimeException {

    private final int status;

    public TongueAnalysisException(int status, String message) {
        super(message);
        this.status = status;
    }

    public TongueAnalysisException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() { return status; }
}
