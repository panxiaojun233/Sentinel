
package com.alibaba.csp.sentinel.cert.sds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.csp.sentinel.core.cert.CertPair;
import com.alibaba.csp.sentinel.core.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.core.inter.CertManagerInterface;
import com.alibaba.csp.sentinel.core.inter.CertUpdateCallback;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractCertManager implements CertUpdater, CertPairProvider, CertManagerInterface {

    protected static final Logger log = LoggerFactory
            .getLogger(AbstractCertManager.class);
    protected static final String CSR_REQUEST_BEGIN = "-----BEGIN CERTIFICATE REQUEST-----";
    protected static final String CSR_REQUEST_END = "-----END CERTIFICATE REQUEST-----";
    protected final ScheduledExecutorService schedule;
    protected List<CertUpdateCallback> callbacks = new ArrayList<>();
    protected XdsConfigProperties xdsConfigProperties;
    protected CertPair certPair = new CertPair();

    public AbstractCertManager(XdsConfigProperties xdsConfigProperties) {
        this.xdsConfigProperties = xdsConfigProperties;
        schedule = Executors.newScheduledThreadPool(1);
    }

    public synchronized CertPair getCertPair0(boolean flag) {
        if (!flag && System.currentTimeMillis() < certPair.getExpireTime()) {
            return certPair;
        }
        CertPair p = doGetCertPair();
        certPair = p;
        if (p != null && p.getExpireTime() != 0) {
            for (CertUpdateCallback callback : callbacks) {
                callback.onUpdateCert(p);
            }
        }
        log.info("refresh Cert,cert={}", certPair.toString());
        log.info("new time={}", (certPair.getExpireTime() - System.currentTimeMillis()) / 3 * 2);
        //每次更新后,在*2/3之后获得证书
        schedule.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    getCertPair0(true);
                } catch (Exception e) {
                    log.error("Generate Cert from Istio failed.", e);
                }
            }
        }, (certPair.getExpireTime() - System.currentTimeMillis()) * 2 / 3, TimeUnit.MILLISECONDS);
        return certPair;
    }


    @Override
    public CertPair getCertPair() {
        if (System.currentTimeMillis() < certPair.getExpireTime()) {
            return certPair;
        }
        return getCertPair0(false);
    }

    protected abstract CertPair doGetCertPair();

    @Override
    public void registerCallback(CertUpdateCallback certUpdateCallback) {
        callbacks.add(certUpdateCallback);
    }

    @PreDestroy
    public void close() {
        schedule.shutdown();
    }


}
