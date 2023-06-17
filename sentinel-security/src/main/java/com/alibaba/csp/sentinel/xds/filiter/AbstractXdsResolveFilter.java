package com.alibaba.csp.sentinel.xds.filiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * filter是具体对某个请求处理的抽象
 */
public abstract class AbstractXdsResolveFilter<T>
        implements XdsResolveFilter<T> {

    protected static final Logger log = LoggerFactory
            .getLogger(AbstractXdsResolveFilter.class);

    protected static final String ALLOW_ANY = "allow_any";

    protected static final String VIRTUAL_INBOUND = "virtualInbound";

    protected static final String CONNECTION_MANAGER = "envoy.filters.network.http_connection_manager";

    protected static final String RBAC_FILTER = "envoy.filters.http.rbac";

    protected static final String JWT_FILTER = "envoy.filters.http.jwt_authn";

    protected static final String ISTIO_AUTHN = "istio_authn";

    protected static final String REQUEST_AUTH_PRINCIPAL = "request.auth.principal";

    protected static final String REQUEST_AUTH_AUDIENCE = "request.auth.audiences";

    protected static final String REQUEST_AUTH_PRESENTER = "request.auth.presenter";

    protected static final String REQUEST_AUTH_CLAIMS = "request.auth.claims";

    protected static final String HEADER_NAME_AUTHORITY = ":authority";

    protected static final String HEADER_NAME_METHOD = ":method";

    protected static final String RAW_BUFFER = "raw_buffer";

    protected static final String TLS = "tls";

    protected static final int MIN_PORT = 0;

    protected static final int MAX_PORT = 65535;


}
