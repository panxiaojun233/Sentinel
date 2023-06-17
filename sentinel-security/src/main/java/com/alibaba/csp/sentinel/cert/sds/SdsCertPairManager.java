

package com.alibaba.csp.sentinel.cert.sds;

import com.google.protobuf.Duration;
import com.google.protobuf.util.Durations;
import com.alibaba.csp.sentinel.core.cert.CertPair;
import com.alibaba.csp.sentinel.core.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.cert.CertPairResolver;
import com.alibaba.csp.sentinel.cert.bootstrap.Bootstrapper;
import com.alibaba.csp.sentinel.util.CertificateUtil;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.text.ParseException;
import java.util.Map;

/**
 * sds拉取证书
 */
public class SdsCertPairManager extends AbstractCertManager {

    private static final String CERTIFICATE_FILE_KEY = "certificate_file";
    private static final String PRIVATE_KEY_FILE_KEY = "private_key_file";
    private static final String CA_CERTIFICATE_FILE_KEY = "ca_certificate_file";
    private static final String REFRESH_INTERVAL_KEY = "refresh_interval";
    private final Bootstrapper.BootstrapInfo bootstrapInfo;

    public SdsCertPairManager(Bootstrapper.BootstrapInfo bootstrapInfo,
                              XdsConfigProperties xdsConfigProperties) {
        super(xdsConfigProperties);
        this.bootstrapInfo = bootstrapInfo;
    }

    public synchronized CertPair doGetCertPair() {
        CertPair certPair = new CertPair();
        try {
            Bootstrapper.CertificateProviderInfo certificateProviderInfo = bootstrapInfo
                    .certProviders().get("default");
            if (certificateProviderInfo == null) {
                return new CertPair();
            }
            Map<String, ?> config = certificateProviderInfo.config();
            if (config == null) {
                return new CertPair();
            }
            certPair = getCertPairFromConfig(config);
            return certPair;
        } catch (Exception e) {
            log.error("Failed to request cert pair", e);
        }
        return certPair;
    }

    private CertPair getCertPairFromConfig(Map<String, ?> config) throws ParseException {
        CertPair certPair = new CertPair();
        String certificateFilePath = (String) config.get(CERTIFICATE_FILE_KEY);
        Certificate[] certificates = CertificateUtil
                .loadCertificateFromPath(certificateFilePath);
        String privateKeyPath = (String) config.get(PRIVATE_KEY_FILE_KEY);
        PrivateKey privateKey = CertificateUtil.loadPrivateKeyFromPath(privateKeyPath);
        String caCertFilePath = (String) config.get(CA_CERTIFICATE_FILE_KEY);
        Certificate caCertificate = CertificateUtil
                .loadCertificateFromPath(caCertFilePath)[0];
        String refreshInterval = (String) config.get(REFRESH_INTERVAL_KEY);
        Duration duration = Durations.parse(refreshInterval);
        certPair.setCertificateChain(certificates);
        CertPairResolver.setPrivateKey(certPair, privateKey);
        certPair.setRootCA(caCertificate);
        certPair.setExpireTime(
                duration.getSeconds() * 1000L + System.currentTimeMillis());
        log.info("Received {} certificates, from pilot-agent", certificates.length);
        return certPair;
    }

}
