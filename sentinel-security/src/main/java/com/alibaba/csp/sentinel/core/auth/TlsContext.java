package com.alibaba.csp.sentinel.core.auth;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 存储是否启用tls链接
 */
public final class TlsContext {

    private AtomicBoolean tls = new AtomicBoolean(false);

    public boolean isTls() {
        return tls.get();
    }

    public void setTls(boolean tlsc) {
        tls.set(tlsc);
    }

}
