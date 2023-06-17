package com.alibaba.csp.sentinel.core.inter;

import java.net.URLStreamHandlerFactory;
import java.security.KeyStore;

import com.alibaba.csp.sentinel.core.cert.CertPair;

public interface MtlsSslStoreProviderInterface {

    public static final String PROTOCOL = "springbootssl";

    public static final String KEY_STORE_PATH = "keyStore";

    public static final String KEY_STORE_URL = PROTOCOL + ":" + KEY_STORE_PATH;

    public static final String TRUST_STORE_PATH = "trustStore";

    public static final String TRUST_STORE_URL = PROTOCOL + ":" + TRUST_STORE_PATH;

    URLStreamHandlerFactory getURLStreamHandlerFactory();


    public KeyStore getKeyStore(CertPair certPair);

    public KeyStore getKeyStore();

    public KeyStore getTrustStore(CertPair certPair);

    public KeyStore getTrustStore();


}
