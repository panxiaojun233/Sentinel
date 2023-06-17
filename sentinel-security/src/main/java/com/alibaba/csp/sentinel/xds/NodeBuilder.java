
package com.alibaba.csp.sentinel.xds;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.envoyproxy.envoy.config.core.v3.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.csp.sentinel.core.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.core.constant.IstioConstants;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class NodeBuilder {

    private static final Logger log = LoggerFactory.getLogger(NodeBuilder.class);

    private static Node NODE;

    private NodeBuilder() {
    }

    public static Node getNode(XdsConfigProperties xdsConfigProperties) {
        try {
            if (NODE != null) {
                return NODE;
            }
            String podName = xdsConfigProperties.getPodName();
            String podNamespace = xdsConfigProperties.getNamespaceName();
            String svcName = xdsConfigProperties.getIstioMetaClusterId();
            String ip = "127.0.0.1";
            try {
                InetAddress local = InetAddress.getLocalHost();
                ip = local.getHostAddress();
            } catch (UnknownHostException e) {
                log.error("Can not get local ip", e);
            }
            Struct.Builder metaBuilder = Struct.newBuilder();
            // metadata is necessary for RequestAuthentication CRD
            metaBuilder.putFields("NAMESPACE",
                    Value.newBuilder().setStringValue(podNamespace).build());
            NODE = Node.newBuilder()
                    .setId(String.format(
                            "sidecar~%s~%s.%s~%s" + IstioConstants.SVC_CLUSTER_LOCAL, ip,
                            podName, podNamespace, podNamespace))
                    .setCluster(svcName).setMetadata(metaBuilder.build()).build();
            return NODE;
        } catch (Exception e) {
            log.error("Unable to create node for xds request", e);
        }
        return null;
    }

}
