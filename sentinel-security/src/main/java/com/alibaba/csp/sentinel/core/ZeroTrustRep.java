package com.alibaba.csp.sentinel.core;


import com.alibaba.csp.sentinel.core.auth.TlsContext;
import com.alibaba.csp.sentinel.core.auth.repository.AuthRepository;

public class ZeroTrustRep {

    //是否开启tls
    public final static TlsContext tlsContext = new TlsContext();

    public final static AuthRepository authRepository = new AuthRepository();
}
