package com.alibaba.csp.sentinel.core.exception;

public final class XdsInitializationException extends RuntimeException {

    public XdsInitializationException(String message) {
        super(message);
    }

    public XdsInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

}
