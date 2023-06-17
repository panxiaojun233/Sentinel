
package com.alibaba.csp.sentinel.cert.bootstrap;

import io.envoyproxy.envoy.config.core.v3.Node;
import io.grpc.ChannelCredentials;
import com.alibaba.csp.sentinel.core.exception.XdsInitializationException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public abstract class Bootstrapper {

    public abstract BootstrapInfo bootstrap() throws XdsInitializationException;

    BootstrapInfo bootstrap(Map<String, ?> rawData) throws XdsInitializationException {
        throw new UnsupportedOperationException();
    }

    public abstract static class ServerInfo {

        public abstract String target();

        abstract ChannelCredentials channelCredentials();

        abstract boolean useProtocolV3();

        abstract boolean ignoreResourceDeletion();

    }

    public abstract static class CertificateProviderInfo {

        public abstract String pluginName();

        public abstract Map<String, ?> config();

    }

    public abstract static class BootstrapInfo {

        public abstract List<ServerInfo> servers();

        public abstract Map<String, CertificateProviderInfo> certProviders();

        public abstract Node node();

        public abstract String serverListenerResourceNameTemplate();

        abstract static class Builder {

            abstract Builder servers(List<ServerInfo> servers);

            abstract Builder node(Node node);

            abstract Builder certProviders(
                    @Nullable Map<String, CertificateProviderInfo> certProviders);

            abstract Builder serverListenerResourceNameTemplate(
                    @Nullable String serverListenerResourceNameTemplate);

            abstract BootstrapInfo build();

        }

    }

}