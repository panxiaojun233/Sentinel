package com.alibaba.csp.sentinel.cert;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import com.alibaba.csp.sentinel.core.cert.CertPair;
import com.alibaba.csp.sentinel.core.exception.CertificateException;
import com.alibaba.csp.sentinel.util.CertificateUtil;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;

public class CertPairResolver {
    public static void setPrivateKey(CertPair certPair, PrivateKey privateKey) {
        certPair.setPrivateKey(privateKey);
        try {
            PemObject pemObject = new PemObject("RSA PRIVATE KEY",
                    privateKey.getEncoded());
            StringWriter str = new StringWriter();
            JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(str);
            jcaPEMWriter.writeObject(pemObject);
            jcaPEMWriter.close();
            str.close();
            byte[] rawPrivateKey = str.toString().getBytes(StandardCharsets.UTF_8);
            certPair.setRawPrivateKey(rawPrivateKey);
        } catch (Exception e) {
            throw new CertificateException("Unable to parse raw private key");
        }
    }


    public static void setCertificateChain(CertPair certPair, List<String> certificateChain) {
        final int n = certificateChain.size();
        Certificate[] certificates = new Certificate[n];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            sb.append(certificateChain.get(i));
            certificates[i] = CertificateUtil.loadCertificate(certificateChain.get(i));
            if (certificates[i] == null) {
                throw new RuntimeException(
                        "Failed to load certificate, pem is " + certificateChain.get(i));
            }
        }
        certPair.setRawCertificateChain(sb.toString().getBytes(StandardCharsets.UTF_8));
        certPair.setCertificateChain(certificates);
    }
}
