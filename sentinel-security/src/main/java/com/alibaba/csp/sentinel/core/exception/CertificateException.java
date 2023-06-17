package com.alibaba.csp.sentinel.core.exception;

public class CertificateException extends RuntimeException {

    public CertificateException(String message) {
        super(message);
    }

    public CertificateException(String message, Throwable cause) {
        super(message, cause);
    }

}
