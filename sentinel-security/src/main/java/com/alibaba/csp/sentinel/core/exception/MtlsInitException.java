package com.alibaba.csp.sentinel.core.exception;

public class MtlsInitException extends RuntimeException {

    public MtlsInitException(String message) {
        super(message);
    }

    public MtlsInitException(String message, Throwable cause) {
        super(message, cause);
    }

}
