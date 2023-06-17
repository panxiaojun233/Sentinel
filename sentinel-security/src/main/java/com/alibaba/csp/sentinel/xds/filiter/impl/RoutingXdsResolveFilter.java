package com.alibaba.csp.sentinel.xds.filiter.impl;

import io.envoyproxy.envoy.config.route.v3.*;
import com.alibaba.csp.sentinel.core.constant.IstioConstants;
import com.alibaba.csp.sentinel.core.util.common.lang.StringUtils;
import com.alibaba.csp.sentinel.core.util.common.matcher.StringMatcherType;
import com.alibaba.csp.sentinel.util.ConvUtil;
import com.alibaba.csp.sentinel.xds.filiter.AbstractXdsResolveFilter;
import com.alibaba.csp.sentinel.xds.routing.MatchService;
import com.alibaba.csp.sentinel.xds.routing.RoutingRule;
import com.alibaba.csp.sentinel.xds.routing.rule.HeaderRoutingRule;
import com.alibaba.csp.sentinel.xds.routing.rule.Rule;
import com.alibaba.csp.sentinel.xds.routing.rule.UrlRoutingRule;

import java.util.ArrayList;
import java.util.List;

/**
 * 路由信息处理，此处暂不处理
 */
public class RoutingXdsResolveFilter
        extends AbstractXdsResolveFilter<List<RouteConfiguration>> {

    @Override
    public boolean resolve(List<RouteConfiguration> routeConfigurations) {
        //暂时无逻辑处理
/*		if (routeConfigurations == null) {
            return false;
        }
        Map<String, UnifiedRoutingDataStructure> untiedRouteDataStructures = new HashMap<>();
        for (RouteConfiguration routeConfiguration : routeConfigurations) {
            List<VirtualHost> virtualHosts = routeConfiguration.getVirtualHostsList();
            for (VirtualHost virtualHost : virtualHosts) {
                UnifiedRoutingDataStructure unifiedRouteDataStructure = new UnifiedRoutingDataStructure();
                String targetService = "";
                String[] serviceAndPort = virtualHost.getName().split(":");
                if (serviceAndPort.length > 0) {
                    targetService = serviceAndPort[0].split("\\.")[0];
                }
                if (ALLOW_ANY.equals(targetService)) {
                    continue;
                }
                unifiedRouteDataStructure.setTargetService(targetService);
                List<Route> routes = virtualHost.getRoutesList();
                RoutingRule labelRouteRule = getRouteData(routes);
                unifiedRouteDataStructure.setLabelRouteRule(labelRouteRule);
                untiedRouteDataStructures.put(
                        unifiedRouteDataStructure.getTargetService(),
                        unifiedRouteDataStructure);
            }
        }*/
        return true;
    }

    private RoutingRule getRouteData(List<Route> routes) {
        List<MatchService> matchServices = new ArrayList<>();
        RoutingRule labelRouteRule = new RoutingRule();
        for (Route route : routes) {
            String cluster = route.getRoute().getCluster();
            if (StringUtils.isNotEmpty(cluster)) {
                MatchService matchService = getMatchService(route, cluster, 100);
                matchServices.add(matchService);
            }
            WeightedCluster weightedCluster = route.getRoute().getWeightedClusters();
            for (WeightedCluster.ClusterWeight clusterWeight : weightedCluster
                    .getClustersList()) {
                MatchService matchService = getMatchService(route,
                        clusterWeight.getName(), clusterWeight.getWeight().getValue());
                matchServices.add(matchService);
            }
        }
        labelRouteRule.setMatchRouteList(matchServices);
        if (!matchServices.isEmpty()) {
            labelRouteRule.setDefaultRouteVersion(
                    matchServices.get(matchServices.size() - 1).getVersion());
        }
        return labelRouteRule;
    }

    private MatchService getMatchService(Route route, String cluster, int weight) {
        String version = "";
        try {
            String[] info = cluster.split("\\|");
            version = info[2];
        } catch (Exception e) {
            log.error("Invalid cluster info for route {}", route.getName());
        }
        MatchService matchService = new MatchService();
        matchService.setVersion(version);
        matchService.setRuleList(match2RouteRules(route.getMatch()));
        matchService.setWeight(weight);
        return matchService;
    }

    private List<Rule> match2RouteRules(RouteMatch routeMatch) {
        List<Rule> routeRules = new ArrayList<>();
        for (HeaderMatcher headerMatcher : routeMatch.getHeadersList()) {
            HeaderRoutingRule headerRule = ConvUtil
                    .headerMatcher2HeaderRule(headerMatcher);
            if (headerRule != null) {
                routeRules.add(headerRule);
            }
        }

        for (QueryParameterMatcher parameterMatcher : routeMatch
                .getQueryParametersList()) {
            UrlRoutingRule.ParameterRoutingRule parameterRoutingRule = ConvUtil
                    .parameterMatcher2ParameterRule(parameterMatcher);
            if (parameterRoutingRule != null) {
                routeRules.add(parameterRoutingRule);
            }
        }

        UrlRoutingRule.PathRoutingRule path = new UrlRoutingRule.PathRoutingRule();
        switch (routeMatch.getPathSpecifierCase()) {
            case PREFIX:
                path.setCondition(StringMatcherType.PREFIX.toString());
                path.setValue(routeMatch.getPrefix());
                break;

            case PATH:
                path.setCondition(StringMatcherType.EXACT.toString());
                path.setValue(routeMatch.getPath());
                break;

            case SAFE_REGEX:
                path.setCondition(StringMatcherType.REGEX.toString());
                path.setValue(routeMatch.getSafeRegex().getRegex());
                break;

            default:
                // unknown type
                path = null;

        }
        if (path != null) {
            routeRules.add(path);
        }
        return routeRules;
    }

    @Override
    public String getTypeUrl() {
        return IstioConstants.RDS_URL;
    }

}
