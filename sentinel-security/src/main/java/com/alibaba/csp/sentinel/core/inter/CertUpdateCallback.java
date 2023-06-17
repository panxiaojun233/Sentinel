

package com.alibaba.csp.sentinel.core.inter;

import com.alibaba.csp.sentinel.core.cert.CertPair;

public interface CertUpdateCallback {

    void onUpdateCert(CertPair certPair);

}
