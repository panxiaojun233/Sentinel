package com.alibaba.csp.sentinel.xds;

import io.envoyproxy.envoy.config.core.v3.Node;
import io.envoyproxy.envoy.service.discovery.v3.AggregatedDiscoveryServiceGrpc;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollDomainSocketChannel;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.unix.DomainSocketAddress;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.csp.sentinel.core.cert.CertPair;
import com.alibaba.csp.sentinel.core.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.core.constant.IstioConstants;
import com.alibaba.csp.sentinel.core.exception.XdsInitializationException;
import com.alibaba.csp.sentinel.cert.bootstrap.Bootstrapper;
import com.alibaba.csp.sentinel.cert.sds.IstioCertPairManager;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLException;
import java.io.ByteArrayInputStream;
import java.net.URI;

import static com.alibaba.csp.sentinel.core.constant.IstioConstants.ISTIOD_SECURE_PORT;


/**
 * Xds的链接通道
 */
public class XdsChannel implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(XdsChannel.class);
    private final XdsConfigProperties xdsConfigProperties;
    private final Node node;
    private final IstioCertPairManager istioCertPairManager;
    private final Bootstrapper.BootstrapInfo bootstrapInfo;
    private ManagedChannel channel;
    private String istiodToken;

    public XdsChannel(XdsConfigProperties xdsConfigProperties,
                      IstioCertPairManager istioCertPairManager,
                      Bootstrapper.BootstrapInfo bootstrapInfo) {
        this.xdsConfigProperties = xdsConfigProperties;
        this.istioCertPairManager = istioCertPairManager;
        this.bootstrapInfo = bootstrapInfo;
        try {
            if (Boolean.FALSE.equals(xdsConfigProperties.getUseAgent())) {
                this.channel = createIstioManagedChannel();
                this.node = NodeBuilder.getNode(xdsConfigProperties);
            } else {
                if (bootstrapInfo == null) {
                    throw new XdsInitializationException(
                            "No bootstrap info while using pilot agent");
                }
                this.channel = createPilotAgentManagedChannel();
                this.node = bootstrapInfo.node();
            }
            if (this.channel == null) {
                throw new XdsInitializationException(
                        "Failed to create ManagedChannel while initializing");
            }
        } catch (Exception e) {
            throw new XdsInitializationException("Init xds channel failed", e);
        }
    }

    private ManagedChannel createIstioManagedChannel() throws SSLException {
        if (xdsConfigProperties.getPort() == ISTIOD_SECURE_PORT) {
            this.refreshIstiodToken();
            CertPair certPair = istioCertPairManager.getCertPair();
            if (certPair == null) {
                throw new XdsInitializationException(
                        "Unable to init XdsChannel, failed to fetch certificate from CA");
            }
            SslContext sslcontext = GrpcSslContexts.forClient()
                    // if server's cert doesn't chain to a standard root
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .keyManager(
                            new ByteArrayInputStream(certPair.getRawCertificateChain()),
                            new ByteArrayInputStream(certPair.getRawPrivateKey()))
                    .build();
            return NettyChannelBuilder
                    .forTarget(xdsConfigProperties.getHost() + ":"
                            + xdsConfigProperties.getPort())
                    .negotiationType(NegotiationType.TLS).sslContext(sslcontext).build();
        } else {
            this.channel = NettyChannelBuilder
                    .forTarget(xdsConfigProperties.getHost() + ":"
                            + xdsConfigProperties.getPort())
                    .negotiationType(NegotiationType.PLAINTEXT).build();
        }
        return null;
    }

    private ManagedChannel createPilotAgentManagedChannel() {
        if (bootstrapInfo == null) {
            return null;
        }
        EpollEventLoopGroup elg = new EpollEventLoopGroup();
        return NettyChannelBuilder
                .forAddress(new DomainSocketAddress(
                        URI.create(bootstrapInfo.servers().get(0).target()).getPath()))
                .eventLoopGroup(elg).channelType(EpollDomainSocketChannel.class)
                .usePlaintext().build();
    }

    private void refreshIstiodToken() {
        this.istiodToken = xdsConfigProperties.getIstiodToken();
        if (this.istiodToken == null) {
            throw new UnsupportedOperationException(
                    "Unable to found kubernetes service account token file. "
                            + "Please check if work in Kubernetes and mount service account token file correctly.");
        }
    }

    @PreDestroy
    @Override
    public void close() {
        if (channel != null) {
            log.warn("Xds channel closing!");
            channel.shutdown();
        }
    }

    public void restart() {
        try {
            close();
            // refresh token again
            if (!xdsConfigProperties.getUseAgent() && xdsConfigProperties
                    .getPort() == IstioConstants.ISTIOD_SECURE_PORT) {
                refreshIstiodToken();
            }
            if (istioCertPairManager != null) {
                this.channel = createIstioManagedChannel();
            } else if (bootstrapInfo != null) {
                this.channel = createPilotAgentManagedChannel();
            }
        } catch (Exception e) {
            log.error("Failed to restart xds channel", e);
        }
    }

    public StreamObserver<DiscoveryRequest> createDiscoveryRequest(
            StreamObserver<DiscoveryResponse> observer) {
        if (channel == null) {
            return null;
        }
        AggregatedDiscoveryServiceGrpc.AggregatedDiscoveryServiceStub stub = AggregatedDiscoveryServiceGrpc
                .newStub(channel);
        Metadata header = new Metadata();
        Metadata.Key<String> key = Metadata.Key.of("authorization",
                Metadata.ASCII_STRING_MARSHALLER);
        header.put(key, "Bearer " + this.istiodToken);
        stub = MetadataUtils.attachHeaders(stub, header);
        return stub.streamAggregatedResources(observer);
    }

    public Node getNode() {
        return node;
    }

}
