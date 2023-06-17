
package com.alibaba.csp.sentinel.xds.protocol;

import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;

import java.util.List;

public interface XdsDecoder<T> {

    List<T> decodeXdsResponse(DiscoveryResponse discoveryResponse);

}
