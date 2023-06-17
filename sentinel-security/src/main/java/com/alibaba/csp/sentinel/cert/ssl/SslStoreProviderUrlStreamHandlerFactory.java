package com.alibaba.csp.sentinel.cert.ssl;

import org.springframework.boot.web.server.SslStoreProvider;
import com.alibaba.csp.sentinel.core.inter.MtlsSslStoreProviderInterface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.KeyStore;

public class SslStoreProviderUrlStreamHandlerFactory implements URLStreamHandlerFactory {



    // Must be a static variable, or we can not reload sslStoreProvider when we restart
    // the spring context.
    private static SslStoreProvider sslStoreProvider;

    public SslStoreProviderUrlStreamHandlerFactory(SslStoreProvider sslStoreProvider) {
        SslStoreProviderUrlStreamHandlerFactory.sslStoreProvider = sslStoreProvider;
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (MtlsSslStoreProviderInterface.PROTOCOL.equals(protocol)) {
            return new URLStreamHandler() {

                @Override
                protected URLConnection openConnection(URL url) throws IOException {
                    try {
                        if (MtlsSslStoreProviderInterface.KEY_STORE_PATH.equals(url.getPath())) {
                            return new KeyStoreUrlConnection(url,
                                    SslStoreProviderUrlStreamHandlerFactory.sslStoreProvider
                                            .getKeyStore());
                        }
                        if (MtlsSslStoreProviderInterface.TRUST_STORE_PATH.equals(url.getPath())) {
                            return new KeyStoreUrlConnection(url,
                                    SslStoreProviderUrlStreamHandlerFactory.sslStoreProvider
                                            .getTrustStore());
                        }
                    } catch (Exception ex) {
                        throw new IOException(ex);
                    }
                    throw new IOException("Invalid path: " + url.getPath());
                }
            };
        }
        return null;
    }

    private static final class KeyStoreUrlConnection extends URLConnection {

        private final KeyStore keyStore;

        private KeyStoreUrlConnection(URL url, KeyStore keyStore) {
            super(url);
            this.keyStore = keyStore;
        }

        @Override
        public void connect() throws IOException {

        }

        @Override
        public InputStream getInputStream() throws IOException {

            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                this.keyStore.store(stream, new char[0]);
                return new ByteArrayInputStream(stream.toByteArray());
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }

    }

}
