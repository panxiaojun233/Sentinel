package com.alibaba.csp.sentinel.xds.protocol.impl;


import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import com.alibaba.csp.sentinel.core.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.core.constant.IstioConstants;
import com.alibaba.csp.sentinel.xds.AggregateDiscoveryService;
import com.alibaba.csp.sentinel.xds.filiter.XdsResolveFilter;
import com.alibaba.csp.sentinel.xds.protocol.AbstractXdsProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * RdsProtocol包含路由信息。
 */
public class RdsProtocol extends AbstractXdsProtocol<RouteConfiguration> {

    public RdsProtocol(XdsConfigProperties xdsConfigProperties,
                       List<XdsResolveFilter<List<RouteConfiguration>>> rdsFilters,
                       AggregateDiscoveryService aggregateDiscoveryService) {
        super(xdsConfigProperties, aggregateDiscoveryService);
        for (XdsResolveFilter<List<RouteConfiguration>> filter : rdsFilters) {
            if (IstioConstants.RDS_URL.equals(filter.getTypeUrl())) {
                filters.add(filter);
            }
        }
    }

    @Override
    public List<RouteConfiguration> decodeXdsResponse(DiscoveryResponse response) {
        List<RouteConfiguration> routes = new ArrayList<>();
        for (com.google.protobuf.Any res : response.getResourcesList()) {
            try {
                RouteConfiguration route = res.unpack(RouteConfiguration.class);
                routes.add(route);
            } catch (Exception e) {
                log.error("Unpack cluster failed", e);
            }
        }
        return routes;
    }

    @Override
    public String getTypeUrl() {
        return IstioConstants.RDS_URL;
    }

    @Override
    public void onResponseDecoded(List<RouteConfiguration> resources) {
        if (log.isDebugEnabled()) {
            log.debug("A Xds configuration update is finished");
        }
        fireXdsFilters(resources);
        initCdl.countDown();
    }

}
