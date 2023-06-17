

package com.alibaba.csp.sentinel.xds.protocol.impl;

import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import com.alibaba.csp.sentinel.core.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.core.constant.IstioConstants;
import com.alibaba.csp.sentinel.xds.AggregateDiscoveryService;
import com.alibaba.csp.sentinel.xds.protocol.AbstractXdsProtocol;

import java.util.ArrayList;
import java.util.List;


public class EdsProtocol extends AbstractXdsProtocol<ClusterLoadAssignment> {

    private final LdsProtocol ldsProtocol;

    public EdsProtocol(XdsConfigProperties xdsConfigProperties, LdsProtocol ldsProtocol,
                       AggregateDiscoveryService aggregateDiscoveryService) {
        super(xdsConfigProperties, aggregateDiscoveryService);
        this.ldsProtocol = ldsProtocol;
    }

    @Override
    public List<ClusterLoadAssignment> decodeXdsResponse(DiscoveryResponse response) {
        List<ClusterLoadAssignment> endpoints = new ArrayList<>();
        for (com.google.protobuf.Any res : response.getResourcesList()) {
            try {
                ClusterLoadAssignment endpoint = res.unpack(ClusterLoadAssignment.class);
                endpoints.add(endpoint);
            } catch (Exception e) {
                log.error("Unpack cluster failed", e);
            }
        }
        return endpoints;
    }

    @Override
    public String getTypeUrl() {
        return IstioConstants.EDS_URL;
    }

    @Override
    public void onResponseDecoded(List<ClusterLoadAssignment> resources) {
        fireXdsFilters(resources);
        ldsProtocol.observeResource();
    }

}
