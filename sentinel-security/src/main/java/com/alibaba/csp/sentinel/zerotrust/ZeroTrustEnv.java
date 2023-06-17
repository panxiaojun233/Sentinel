package com.alibaba.csp.sentinel.zerotrust;

import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.csp.sentinel.core.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.cert.bootstrap.Bootstrapper;
import com.alibaba.csp.sentinel.cert.bootstrap.BootstrapperImpl;
import com.alibaba.csp.sentinel.cert.sds.AbstractCertManager;
import com.alibaba.csp.sentinel.cert.sds.IstioCertPairManager;
import com.alibaba.csp.sentinel.cert.sds.SdsCertPairManager;
import com.alibaba.csp.sentinel.cert.ssl.MtlsSslStoreProvider;
import com.alibaba.csp.sentinel.xds.AggregateDiscoveryService;
import com.alibaba.csp.sentinel.xds.XdsAutoProducer;
import com.alibaba.csp.sentinel.xds.XdsChannel;
import com.alibaba.csp.sentinel.xds.filiter.XdsResolveFilter;
import com.alibaba.csp.sentinel.xds.protocol.impl.CdsProtocol;
import com.alibaba.csp.sentinel.xds.protocol.impl.EdsProtocol;
import com.alibaba.csp.sentinel.xds.protocol.impl.LdsProtocol;
import com.alibaba.csp.sentinel.xds.protocol.impl.RdsProtocol;

import java.util.Arrays;
import java.util.List;

public class ZeroTrustEnv {

    protected static final Logger log = LoggerFactory.getLogger(ZeroTrustEnv.class);


    public XdsConfigProperties xdsConfigProperties;

    public AbstractCertManager certManager;

    public MtlsSslStoreProvider sslStoreProvider;


    public IstioCertPairManager istioCertPairManage;

    public Bootstrapper.BootstrapInfo bootstrapInfo;

    public XdsChannel xdsChannel;

    public AggregateDiscoveryService aggregateDiscoveryService;


    public ZeroTrustEnv(XdsConfigProperties config) {
        this.xdsConfigProperties = config;
        xdsConfigProperties.init();

        if (Boolean.FALSE.equals(xdsConfigProperties.getUseAgent())) {
            certManager = new IstioCertPairManager(xdsConfigProperties);
            istioCertPairManage = (IstioCertPairManager) certManager;
            bootstrapInfo = null;
        } else {
            istioCertPairManage = null;
            bootstrapInfo = new BootstrapperImpl().bootstrap();
            certManager = new SdsCertPairManager(bootstrapInfo, xdsConfigProperties);
        }


        try {
            certManager.getCertPair0(true);
        } catch (Exception e) {
            log.error("Generate Cert from Istio failed.", e);
        }

        sslStoreProvider = new MtlsSslStoreProvider(certManager);


        xdsChannel = new XdsChannel(xdsConfigProperties, istioCertPairManage,
                bootstrapInfo);

        aggregateDiscoveryService = new AggregateDiscoveryService(
                xdsChannel, xdsConfigProperties);

        XdsAutoProducer xdsAutoProducer = new XdsAutoProducer(xdsConfigProperties);


        XdsResolveFilter<List<RouteConfiguration>> routingFilter = xdsAutoProducer
                .routingXdsResolveFilter();

        //挂载 rds --routing
        RdsProtocol rdsProtocol = xdsAutoProducer
                .rdsProtocol(Arrays.asList(routingFilter), aggregateDiscoveryService);


        XdsResolveFilter<List<Listener>> ldsfilter = xdsAutoProducer
                .authXdsResolveFilter();

        XdsResolveFilter<List<Listener>> xdsResolveFilter = xdsAutoProducer
                .serverTlsXdsResolveFilter();
        //挂载lds --listening
        LdsProtocol ldsProtocol = xdsAutoProducer.ldsProtocol(
                Arrays.asList(ldsfilter, xdsResolveFilter), rdsProtocol,
                aggregateDiscoveryService);


        //挂载eds
        EdsProtocol edsProtocol = xdsAutoProducer.edsProtocol(ldsProtocol,
                aggregateDiscoveryService);

        //挂载cds
        CdsProtocol cdsProtocol = xdsAutoProducer.cdsProtocol(edsProtocol,
                ldsProtocol, aggregateDiscoveryService);
    }

}
