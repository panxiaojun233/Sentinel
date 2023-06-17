

package com.alibaba.csp.sentinel.cert.bootstrap;

import java.util.Map;

final class CertificateProviderInfoImpl extends Bootstrapper.CertificateProviderInfo {

    private final String pluginName;

    private final Map<String, ?> config;

    CertificateProviderInfoImpl(String pluginName, Map<String, ?> config) {
        this.pluginName = pluginName;
        this.config = config;
    }

    @Override
    public String pluginName() {
        return pluginName;
    }

    @Override
    public Map<String, ?> config() {
        return config;
    }

    @Override
    public String toString() {
        return "CertificateProviderInfo{" + "pluginName=" + pluginName + ", " + "config="
                + config + "}";
    }

}
