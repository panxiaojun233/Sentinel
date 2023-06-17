package com.alibaba.csp.sentinel.core.inter;

public class TlsCenter {
    private static CertManagerInterface certManagerInterface = null;
    private static MtlsSslStoreProviderInterface mtlsSslStoreProviderInterface = null;


    public static CertManagerInterface getCertManagerInterface() {
        return certManagerInterface;
    }

    public static void setCertManagerInterface(CertManagerInterface certManagerInterface) {
        TlsCenter.certManagerInterface = certManagerInterface;
    }

    public static MtlsSslStoreProviderInterface getMtlsSslStoreProviderInterface() {
        return mtlsSslStoreProviderInterface;
    }

    public static void setMtlsSslStoreProviderInterface(MtlsSslStoreProviderInterface mtlsSslStoreProviderInterface) {
        TlsCenter.mtlsSslStoreProviderInterface = mtlsSslStoreProviderInterface;
    }
}
