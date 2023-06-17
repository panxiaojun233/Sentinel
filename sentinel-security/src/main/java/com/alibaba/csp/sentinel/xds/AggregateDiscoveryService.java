package com.alibaba.csp.sentinel.xds;

import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.csp.sentinel.core.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.core.constant.IstioConstants;
import com.alibaba.csp.sentinel.xds.protocol.AbstractXdsProtocol;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Xds的聚合服务
 */
public class AggregateDiscoveryService {

    private static final Logger log = LoggerFactory
            .getLogger(AggregateDiscoveryService.class);

    private final Map<String, AbstractXdsProtocol> protocolMap = new HashMap<String, AbstractXdsProtocol>();

    private final Map<String, Set<String>> requestResource = new ConcurrentHashMap<>();
    private final XdsConfigProperties xdsConfigProperties;
    private final XdsChannel xdsChannel;
    private final ScheduledExecutorService retry;
    private StreamObserver<DiscoveryRequest> observer;

    public AggregateDiscoveryService(XdsChannel xdsChannel,
                                     XdsConfigProperties xdsConfigProperties) {
        this.xdsChannel = xdsChannel;
        this.xdsConfigProperties = xdsConfigProperties;
        this.observer = xdsChannel.createDiscoveryRequest(new XdsObserver());
        this.retry = Executors.newSingleThreadScheduledExecutor();
    }

    public void addProtocol(AbstractXdsProtocol abstractXdsProtocol) {
        protocolMap.put(abstractXdsProtocol.getTypeUrl(), abstractXdsProtocol);
    }

    public void sendXdsRequest(String typeUrl, Set<String> resourceNames) {
        requestResource.put(typeUrl, resourceNames);
        DiscoveryRequest request = DiscoveryRequest.newBuilder()
                .setNode(xdsChannel.getNode()).setTypeUrl(typeUrl)
                .addAllResourceNames(resourceNames).build();
        observer.onNext(request);
    }

    private void sendAckRequest(DiscoveryResponse response) {
        Set<String> ackResource = requestResource.get(response.getTypeUrl());
        if (ackResource == null) {
            ackResource = new HashSet<String>();
        }
        DiscoveryRequest request = DiscoveryRequest.newBuilder()
                .setVersionInfo(response.getVersionInfo()).setNode(xdsChannel.getNode())
                .addAllResourceNames(ackResource).setTypeUrl(response.getTypeUrl())
                .setResponseNonce(response.getNonce()).build();
        observer.onNext(request);
    }

    @PreDestroy
    public void close() {
        retry.shutdown();
    }

    private class XdsObserver implements StreamObserver<DiscoveryResponse> {

        @Override
        public void onNext(DiscoveryResponse discoveryResponse) {
            String typeUrl = discoveryResponse.getTypeUrl();
            if (xdsConfigProperties.isLogXds()) {
                log.info("Receive notification from xds server, type: {}, size: {}",
                        typeUrl, discoveryResponse.getResourcesCount());
            }
            AbstractXdsProtocol protocol = protocolMap.get(typeUrl);
            if (protocol == null) {
                throw new UnsupportedOperationException("No protocol of type " + typeUrl);
            }
            // 真实的业务逻辑
            List<?> responses = protocol.decodeXdsResponse(discoveryResponse);
            sendAckRequest(discoveryResponse);
            protocol.onResponseDecoded(responses);
        }

        @Override
        public void onError(Throwable throwable) {
            if (xdsConfigProperties.isLogXds()) {
                log.error("Connect to xds server failed, reconnect after 3 seconds",
                        throwable);
            }
            final XdsObserver finalXdsObserver = this;
            requestResource.clear();
            retry.schedule(new Runnable() {
                @Override
                public void run() {
                    xdsChannel.restart();
                    observer = xdsChannel.createDiscoveryRequest(finalXdsObserver);
                    sendXdsRequest(IstioConstants.CDS_URL, new HashSet<String>());
                    log.info("Reconnecting to istio control plane!");
                }
            }, 3, TimeUnit.SECONDS);
        }

        @Override
        public void onCompleted() {
            log.info("Xds connect completed");
        }

    }

}
