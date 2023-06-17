package com.alibaba.csp.sentinel.xds.routing;


public class UnifiedRoutingDataStructure {

    private RoutingRule routingRule;

    private String targetService;

    public RoutingRule getLabelRouteRule() {
        return routingRule;
    }

    public void setLabelRouteRule(RoutingRule labelRouteRule) {
        this.routingRule = labelRouteRule;
    }

    public String getTargetService() {
        return targetService;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    @Override
    public String toString() {
        return "UntiedRoutingDataStructure{" + "RoutingData=" + routingRule
                + ", targetService='" + targetService + '\'' + '}';
    }

}
