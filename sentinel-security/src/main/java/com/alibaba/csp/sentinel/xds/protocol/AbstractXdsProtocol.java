
package com.alibaba.csp.sentinel.xds.protocol;

import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.csp.sentinel.core.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.xds.AggregateDiscoveryService;
import com.alibaba.csp.sentinel.xds.filiter.XdsResolveFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public abstract class AbstractXdsProtocol<T>
        implements XdsProtocol<T>, XdsDecoder<T> {

    protected static final Logger log = LoggerFactory
            .getLogger(AbstractXdsProtocol.class);
    protected static CountDownLatch initCdl;
    private final AggregateDiscoveryService aggregateDiscoveryService;
    protected XdsConfigProperties xdsConfigProperties;
    protected List<XdsResolveFilter<List<T>>> filters = new ArrayList<XdsResolveFilter<List<T>>>();


    private Set<String> resourceNames = new HashSet<>();

    public AbstractXdsProtocol(XdsConfigProperties xdsConfigProperties,
                               AggregateDiscoveryService aggregateDiscoveryService) {
        this.xdsConfigProperties = xdsConfigProperties;
        this.aggregateDiscoveryService = aggregateDiscoveryService;
        initCdl = new CountDownLatch(1);
    }

    public synchronized void observeResource() {
        observeResource(null);
    }

    @Override
    public synchronized void observeResource(Set<String> resourceNames) {
        String typeUrl = getTypeUrl();
        if (resourceNames == null) {
            resourceNames = new HashSet<>();
        }
        aggregateDiscoveryService.sendXdsRequest(typeUrl, resourceNames);
    }

    public Set<String> getResourceNames() {
        return resourceNames;
    }

    public abstract List<T> decodeXdsResponse(DiscoveryResponse response);

    protected Set<String> resolveResourceNames(List<T> resources) {
        return new HashSet<>();
    }

    protected void fireXdsFilters(List<T> resources) {
        try {
            this.resourceNames = resolveResourceNames(resources);
        } catch (Exception e) {
            log.error("Error on resolving resource names from {}", resources);
        }
        for (XdsResolveFilter<List<T>> filter : filters) {
            try {
                if (!filter.resolve(resources)) {
                    return;
                }
            } catch (Exception e) {
                log.error("Error on executing Xds filter {}", filter.getClass().getName(),
                        e);
            }
        }

    }

}
