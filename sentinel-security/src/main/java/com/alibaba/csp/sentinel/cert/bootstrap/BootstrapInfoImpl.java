package com.alibaba.csp.sentinel.cert.bootstrap;

import io.envoyproxy.envoy.config.core.v3.Node;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class BootstrapInfoImpl extends Bootstrapper.BootstrapInfo {

    private final List<Bootstrapper.ServerInfo> servers;

    private final String serverListenerResourceNameTemplate;

    private final Map<String, Bootstrapper.CertificateProviderInfo> certProviders;

    private final Node node;

    BootstrapInfoImpl(List<Bootstrapper.ServerInfo> servers,
                      String serverListenerResourceNameTemplate,
                      Map<String, Bootstrapper.CertificateProviderInfo> certProviders, Node node) {
        this.servers = servers;
        this.serverListenerResourceNameTemplate = serverListenerResourceNameTemplate;
        this.certProviders = certProviders;
        this.node = node;
    }

    @Override
    public List<Bootstrapper.ServerInfo> servers() {
        return servers;
    }

    public Map<String, Bootstrapper.CertificateProviderInfo> certProviders() {
        return certProviders;
    }

    @Override
    public Node node() {
        return node;
    }

    @Override
    public String serverListenerResourceNameTemplate() {
        return serverListenerResourceNameTemplate;
    }

    @Override
    public String toString() {
        return "BootstrapInfo{" + "servers=" + servers + ", "
                + "serverListenerResourceNameTemplate="
                + serverListenerResourceNameTemplate + ", " + "node=" + node + ", " + "}";
    }

    public static final class Builder extends Bootstrapper.BootstrapInfo.Builder {

        private List<Bootstrapper.ServerInfo> servers;

        private Node node;

        private Map<String, Bootstrapper.CertificateProviderInfo> certProviders;

        private String serverListenerResourceNameTemplate;

        Builder() {
        }

        @Override
        Bootstrapper.BootstrapInfo.Builder servers(
                List<Bootstrapper.ServerInfo> servers) {
            this.servers = new LinkedList<>(servers);
            return this;
        }

        @Override
        Bootstrapper.BootstrapInfo.Builder node(Node node) {
            if (node == null) {
                throw new NullPointerException("Null node");
            }
            this.node = node;
            return this;
        }

        @Override
        Bootstrapper.BootstrapInfo.Builder certProviders(
                @Nullable Map<String, Bootstrapper.CertificateProviderInfo> certProviders) {
            this.certProviders = certProviders;
            return this;
        }

        @Override
        Bootstrapper.BootstrapInfo.Builder serverListenerResourceNameTemplate(
                @Nullable String serverListenerResourceNameTemplate) {
            this.serverListenerResourceNameTemplate = serverListenerResourceNameTemplate;
            return this;
        }

        @Override
        Bootstrapper.BootstrapInfo build() {
            if (this.servers == null || this.node == null) {
                StringBuilder missing = new StringBuilder();
                if (this.servers == null) {
                    missing.append(" servers");
                }
                if (this.node == null) {
                    missing.append(" node");
                }
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            return new BootstrapInfoImpl(this.servers,
                    this.serverListenerResourceNameTemplate, this.certProviders,
                    this.node);
        }

    }

}
