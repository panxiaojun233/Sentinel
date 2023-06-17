package com.alibaba.csp.sentinel.cert.ssl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.SslStoreProvider;
import com.alibaba.csp.sentinel.core.cert.CertPair;
import com.alibaba.csp.sentinel.core.constant.MtlsConstants;
import com.alibaba.csp.sentinel.core.inter.MtlsSslStoreProviderInterface;
import com.alibaba.csp.sentinel.cert.sds.AbstractCertManager;

import java.net.URLStreamHandlerFactory;
import java.security.KeyStore;

public class MtlsSslStoreProvider implements SslStoreProvider, MtlsSslStoreProviderInterface {

    private static final Logger log = LoggerFactory.getLogger(MtlsSslStoreProvider.class);

    private final AbstractCertManager certManager;

    public MtlsSslStoreProvider(AbstractCertManager certManager) {
        this.certManager = certManager;
    }

    @Override
    public URLStreamHandlerFactory getURLStreamHandlerFactory() {
        return new SslStoreProviderUrlStreamHandlerFactory(this);
    }

    public KeyStore getKeyStore(CertPair certPair) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setKeyEntry(MtlsConstants.MTLS_DEFAULT_KEY_STORE_ALIAS,
                    certPair.getPrivateKey(), "".toCharArray(),
                    certPair.getCertificateChain());
            return keyStore;
        } catch (Exception e) {
            log.error("Unable to get key store", e);
        }
        return null;
    }

    @Override
    public KeyStore getKeyStore() {
        CertPair certPair = certManager.getCertPair();
        return getKeyStore(certPair);
    }

    public KeyStore getTrustStore(CertPair certPair) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry(MtlsConstants.MTLS_DEFAULT_TRUST_STORE_ALIAS,
                    certPair.getRootCA());
            return keyStore;
        } catch (Exception e) {
            log.error("Unable to get trust store", e);
        }
        return null;
    }

    @Override
    public KeyStore getTrustStore() {
        CertPair certPair = certManager.getCertPair();
        return getTrustStore(certPair);
    }

}
