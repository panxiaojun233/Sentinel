

package com.alibaba.csp.sentinel.xds.protocol;

import java.util.List;
import java.util.Set;

public interface XdsProtocol<T> {

    String getTypeUrl();

    void observeResource(Set<String> resourceNames);

    void onResponseDecoded(List<T> resources);

}
