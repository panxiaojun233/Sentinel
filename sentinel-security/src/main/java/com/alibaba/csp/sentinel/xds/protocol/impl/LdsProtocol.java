

package com.alibaba.csp.sentinel.xds.protocol.impl;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.envoyproxy.envoy.config.listener.v3.Filter;
import io.envoyproxy.envoy.config.listener.v3.FilterChain;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.Rds;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import org.springframework.util.CollectionUtils;
import com.alibaba.csp.sentinel.core.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.core.constant.IstioConstants;
import com.alibaba.csp.sentinel.core.util.common.lang.StringUtils;
import com.alibaba.csp.sentinel.xds.AggregateDiscoveryService;
import com.alibaba.csp.sentinel.xds.filiter.XdsResolveFilter;
import com.alibaba.csp.sentinel.xds.protocol.AbstractXdsProtocol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * LdsProtocol包含关于的鉴权配置和其他配置安全
 */
public class LdsProtocol extends AbstractXdsProtocol<Listener> {

    private final RdsProtocol rdsProtocol;

    public LdsProtocol(XdsConfigProperties xdsConfigProperties,
                       List<XdsResolveFilter<List<Listener>>> ldsFilters, RdsProtocol rdsProtocol,
                       AggregateDiscoveryService aggregateDiscoveryService) {
        super(xdsConfigProperties, aggregateDiscoveryService);
        // init filters
        for (XdsResolveFilter<List<Listener>> filter : ldsFilters) {
            if (IstioConstants.LDS_URL.equals(filter.getTypeUrl())) {
                filters.add(filter);
            }
        }
        this.rdsProtocol = rdsProtocol;
    }

    @Override
    public String getTypeUrl() {
        return IstioConstants.LDS_URL;
    }

    @Override
    public List<Listener> decodeXdsResponse(DiscoveryResponse response) {
        List<Listener> listeners = new ArrayList<>();
        for (com.google.protobuf.Any res : response.getResourcesList()) {
            try {
                Listener listener = res.unpack(Listener.class);
                if (listener != null) {
                    listeners.add(listener);
                }
            } catch (Exception e) {
                log.error("Unpack listeners failed", e);
            }
        }
        return listeners;
    }

    @Override
    public void onResponseDecoded(List<Listener> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            return;
        }
        fireXdsFilters(resources);
        Set<String> resourceName = getResourceNames();
        if (!CollectionUtils.isEmpty(resourceName)) {
            rdsProtocol.observeResource(resourceName);
        } else {
            initCdl.countDown();
        }
    }

    @Override
    protected Set<String> resolveResourceNames(List<Listener> resources) {
        Set<String> routeNames = new HashSet<>();
        for (Listener listener : resources) {
            for (FilterChain filterChain : listener.getFilterChainsList()) {
                for (Filter filter : filterChain.getFiltersList()) {
                    Any any = filter.getTypedConfig();
                    try {
                        if (!any.is(HttpConnectionManager.class)) {
                            continue;
                        }
                        HttpConnectionManager httpConnectionManager = any.unpack(HttpConnectionManager.class);
                        Rds rds = httpConnectionManager.getRds();
                        String routeConfigName = rds.getRouteConfigName();
                        if (StringUtils.isNotEmpty(routeConfigName)) {
                            routeNames.add(routeConfigName);
                        }
                    } catch (InvalidProtocolBufferException e) {
                        continue;
                    }
                }
            }
        }
        return routeNames;
    }

}
