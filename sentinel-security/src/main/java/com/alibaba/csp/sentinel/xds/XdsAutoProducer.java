package com.alibaba.csp.sentinel.xds;

import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import com.alibaba.csp.sentinel.core.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.xds.filiter.XdsResolveFilter;
import com.alibaba.csp.sentinel.xds.filiter.impl.AuthXdsResolveFilter;
import com.alibaba.csp.sentinel.xds.filiter.impl.RoutingXdsResolveFilter;
import com.alibaba.csp.sentinel.xds.filiter.impl.ServerTlsXdsResolveFilter;
import com.alibaba.csp.sentinel.xds.protocol.impl.CdsProtocol;
import com.alibaba.csp.sentinel.xds.protocol.impl.EdsProtocol;
import com.alibaba.csp.sentinel.xds.protocol.impl.LdsProtocol;
import com.alibaba.csp.sentinel.xds.protocol.impl.RdsProtocol;

import java.util.List;


/**
 * 工具生产类
 */
public class XdsAutoProducer {


    private XdsConfigProperties xdsConfigProperties;

    public XdsAutoProducer(XdsConfigProperties xdsConfigProperties) {
        this.xdsConfigProperties = xdsConfigProperties;
    }


    public XdsResolveFilter<List<Listener>> authXdsResolveFilter() {
        return new AuthXdsResolveFilter();
    }


    public XdsResolveFilter<List<RouteConfiguration>> routingXdsResolveFilter() {
        return new RoutingXdsResolveFilter();
    }


    public XdsResolveFilter<List<Listener>> serverTlsXdsResolveFilter() {
        return new ServerTlsXdsResolveFilter();
    }


    public LdsProtocol ldsProtocol(List<XdsResolveFilter<List<Listener>>> filters,
                                   RdsProtocol rdsProtocol,
                                   AggregateDiscoveryService aggregateDiscoveryService) {
        LdsProtocol ldsProtocol = new LdsProtocol(xdsConfigProperties, filters,
                rdsProtocol, aggregateDiscoveryService);
        aggregateDiscoveryService.addProtocol(ldsProtocol);
        return ldsProtocol;
    }

    public CdsProtocol cdsProtocol(EdsProtocol edsProtocol, LdsProtocol ldsProtocol,
                                   AggregateDiscoveryService aggregateDiscoveryService) {
        CdsProtocol cdsProtocol = new CdsProtocol(xdsConfigProperties, edsProtocol,
                ldsProtocol, aggregateDiscoveryService);
        aggregateDiscoveryService.addProtocol(cdsProtocol);
        cdsProtocol.initAndObserve();
        return cdsProtocol;
    }

    public EdsProtocol edsProtocol(LdsProtocol ldsProtocol,
                                   AggregateDiscoveryService aggregateDiscoveryService) {
        EdsProtocol edsProtocol = new EdsProtocol(xdsConfigProperties, ldsProtocol,
                aggregateDiscoveryService);
        aggregateDiscoveryService.addProtocol(edsProtocol);
        return edsProtocol;
    }

    public RdsProtocol rdsProtocol(
            List<XdsResolveFilter<List<RouteConfiguration>>> filters,
            AggregateDiscoveryService aggregateDiscoveryService) {
        RdsProtocol rdsProtocol = new RdsProtocol(xdsConfigProperties, filters,
                aggregateDiscoveryService);
        aggregateDiscoveryService.addProtocol(rdsProtocol);
        return rdsProtocol;
    }


}
