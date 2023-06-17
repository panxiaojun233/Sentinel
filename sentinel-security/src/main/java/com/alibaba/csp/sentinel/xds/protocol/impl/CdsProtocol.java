package com.alibaba.csp.sentinel.xds.protocol.impl;

import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import org.springframework.util.CollectionUtils;
import com.alibaba.csp.sentinel.core.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.core.constant.IstioConstants;
import com.alibaba.csp.sentinel.core.exception.XdsInitializationException;
import com.alibaba.csp.sentinel.xds.AggregateDiscoveryService;
import com.alibaba.csp.sentinel.xds.protocol.AbstractXdsProtocol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class CdsProtocol extends AbstractXdsProtocol<Cluster> {

    private final EdsProtocol edsProtocol;

    private final LdsProtocol ldsProtocol;

    public CdsProtocol(XdsConfigProperties xdsConfigProperties, EdsProtocol edsProtocol,
                       LdsProtocol ldsProtocol,
                       AggregateDiscoveryService aggregateDiscoveryService) {
        super(xdsConfigProperties, aggregateDiscoveryService);
        this.edsProtocol = edsProtocol;
        this.ldsProtocol = ldsProtocol;
    }

    @Override
    public List<Cluster> decodeXdsResponse(DiscoveryResponse response) {
        List<Cluster> clusters = new ArrayList<>();
        for (com.google.protobuf.Any res : response.getResourcesList()) {
            try {
                Cluster cluster = res.unpack(Cluster.class);
                clusters.add(cluster);
            } catch (Exception e) {
                log.error("Unpack cluster failed", e);
            }
        }
        return clusters;
    }

    @Override
    protected Set<String> resolveResourceNames(List<Cluster> resources) {
        Set<String> endpoints = new HashSet<>();
        if (resources == null) {
            return endpoints;
        }
        for (Cluster cluster : resources) {
            cluster.getEdsClusterConfig().getServiceName();
            endpoints.add(cluster.getEdsClusterConfig().getServiceName());
        }
        return endpoints;
    }

    @Override
    public String getTypeUrl() {
        return IstioConstants.CDS_URL;
    }

    @Override
    public void onResponseDecoded(List<Cluster> resources) {
        fireXdsFilters(resources);
        Set<String> resourceName = getResourceNames();
        if (!CollectionUtils.isEmpty(resourceName)) {
            // eds
            edsProtocol.observeResource(resourceName);
        } else {
            // lds
            ldsProtocol.observeResource();
        }
    }

    public synchronized void initAndObserve() {
        try {
            observeResource();
            boolean flag = initCdl.await(30, TimeUnit.SECONDS);
            if (!flag) {
                throw new XdsInitializationException(
                        "Timeout when init config from xds server");
            }
        } catch (Exception e) {
            throw new XdsInitializationException("Error on fetch xds config", e);
        }
    }

}
