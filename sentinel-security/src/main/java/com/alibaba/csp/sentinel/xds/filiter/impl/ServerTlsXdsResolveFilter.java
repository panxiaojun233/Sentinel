package com.alibaba.csp.sentinel.xds.filiter.impl;

import io.envoyproxy.envoy.config.listener.v3.FilterChain;
import io.envoyproxy.envoy.config.listener.v3.FilterChainMatch;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import org.springframework.util.CollectionUtils;
import com.alibaba.csp.sentinel.core.ZeroTrustRep;
import com.alibaba.csp.sentinel.core.constant.IstioConstants;
import com.alibaba.csp.sentinel.xds.filiter.AbstractXdsResolveFilter;

import java.util.List;

/**
 * 判定使用的协议
 */
public class ServerTlsXdsResolveFilter extends AbstractXdsResolveFilter<List<Listener>> {

    @Override
    public boolean resolve(List<Listener> listeners) {
        if (listeners == null || listeners.isEmpty()) {
            return false;
        }
        boolean tls = false;
        for (Listener listener : listeners) {
            List<FilterChain> filterChains = listener.getFilterChainsList();
            if (!VIRTUAL_INBOUND.equals(listener.getName())) {
                continue;
            }
            if (CollectionUtils.isEmpty(filterChains)) {
                continue;
            }
            for (FilterChain filterChain : filterChains) {
                if (!VIRTUAL_INBOUND.equals(filterChain.getName())) {
                    continue;
                }
                FilterChainMatch match = filterChain.getFilterChainMatch();
                if (TLS.equals(match.getTransportProtocol())) {
                    tls = true;
                }
                break;
            }
        }
        // 修改为加入tls
        ZeroTrustRep.tlsContext.setTls(tls);
        return true;
    }

    @Override
    public String getTypeUrl() {
        return IstioConstants.LDS_URL;
    }

}
