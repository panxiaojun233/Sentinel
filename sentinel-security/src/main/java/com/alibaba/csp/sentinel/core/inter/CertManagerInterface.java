package com.alibaba.csp.sentinel.core.inter;

import com.alibaba.csp.sentinel.core.cert.CertPair;

public interface CertManagerInterface {

    public void registerCallback(CertUpdateCallback certUpdateCallback);

    public CertPair getCertPair();
}
