package com.alibaba.csp.sentinel.util;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class CertificateUtil {

	private static final Logger log = LoggerFactory.getLogger(CertificateUtil.class);

	private static final String ALG_RSA = "RSA";

	private static final String PEM_CERTIFICATE_START = "-----BEGIN CERTIFICATE-----";

	private static final String PEM_CERTIFICATE_END = "-----END CERTIFICATE-----";

	private static final String PEM_PRIVATE_START = "-----BEGIN PRIVATE KEY-----";

	private static final String PEM_PRIVATE_END = "-----END PRIVATE KEY-----";

	private CertificateUtil() {

	}

	public static Certificate loadCertificate(String certificatePem) {
		try {
			CertificateFactory certificateFactory = CertificateFactory
					.getInstance("X509");
			certificatePem = certificatePem.replaceAll(PEM_CERTIFICATE_START, "");
			certificatePem = certificatePem.replaceAll(PEM_CERTIFICATE_END, "");
			certificatePem = certificatePem.replaceAll("\\s*", "");
			return certificateFactory.generateCertificate(
					new ByteArrayInputStream(Base64.getDecoder().decode(certificatePem)));
		} catch (Exception e) {
			log.error("Load certificate failed from pem string", e);
		}
		return null;
	}

	public static Certificate[] loadCertificateFromPath(String path) {
		List<Certificate> certificates = new ArrayList<>();
		try {
			PEMParser pemParser = new PEMParser(new FileReader(path));
			Object object;
			while ((object = pemParser.readObject()) != null) {
				if (object instanceof X509CertificateHolder) {
					X509CertificateHolder certificateHolder = (X509CertificateHolder) object;
					X509Certificate certificate = new JcaX509CertificateConverter()
							.getCertificate(certificateHolder);
					certificates.add(certificate);
				}
			}
		} catch (Exception e) {
			log.error("Load certificate failed from path {}", path, e);
		}
		return certificates.toArray(new Certificate[0]);
	}

	public static PrivateKey loadPrivateKeyFromPath(String path) {
		try {
			PEMParser pemParser = new PEMParser(new FileReader(path));
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
			Object o = pemParser.readObject();
			if (o instanceof PEMKeyPair) {
				return converter.getPrivateKey(((PEMKeyPair) o).getPrivateKeyInfo());
			}
		} catch (Exception e) {
			log.error("Load private key failed from path {}", path, e);
		}
		return null;
	}

}
