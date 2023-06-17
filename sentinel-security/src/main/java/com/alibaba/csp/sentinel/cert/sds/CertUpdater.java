

package com.alibaba.csp.sentinel.cert.sds;

import com.alibaba.csp.sentinel.core.inter.CertUpdateCallback;

public interface CertUpdater {

    void registerCallback(CertUpdateCallback certUpdateCallback);

}
