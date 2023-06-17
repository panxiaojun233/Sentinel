

package com.alibaba.csp.sentinel.cert.bootstrap;

import io.grpc.ChannelCredentials;

final class ServerInfoImpl extends Bootstrapper.ServerInfo {

    private final String target;

    private final ChannelCredentials channelCredentials;

    private final boolean useProtocolV3;

    private final boolean ignoreResourceDeletion;

    ServerInfoImpl(String target, ChannelCredentials channelCredentials,
                   boolean useProtocolV3, boolean ignoreResourceDeletion) {
        this.target = target;
        this.channelCredentials = channelCredentials;
        this.useProtocolV3 = useProtocolV3;
        this.ignoreResourceDeletion = ignoreResourceDeletion;
    }

    @Override
    public String target() {
        return target;
    }

    @Override
    ChannelCredentials channelCredentials() {
        return channelCredentials;
    }

    @Override
    boolean useProtocolV3() {
        return useProtocolV3;
    }

    @Override
    boolean ignoreResourceDeletion() {
        return ignoreResourceDeletion;
    }

    @Override
    public String toString() {
        return "ServerInfo{" + "target=" + target + ", " + "channelCredentials="
                + channelCredentials + ", " + "useProtocolV3=" + useProtocolV3 + ", "
                + "ignoreResourceDeletion=" + ignoreResourceDeletion + "}";
    }

}
