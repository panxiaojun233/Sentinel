package com.alibaba.csp.sentinel.core.util;

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CertUtil {

    private static final Logger log = LoggerFactory.getLogger(CertUtil.class);

    private CertUtil() {

    }

    public static String getIstioIdentity(X509Certificate x509Certificate) {
        try {
            Collection<List<?>> san = x509Certificate.getSubjectAlternativeNames();
            return (String) san.iterator().next().get(1);
        } catch (Exception e) {
            log.error("Failed to get istio SAN from X509Certificate", e);
        }
        return "";
    }

}
