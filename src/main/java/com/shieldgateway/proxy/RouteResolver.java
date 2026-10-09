package com.shieldgateway.proxy;

import com.shieldgateway.config.GatewayProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteResolver {

    private final GatewayProperties gatewayProperties;

    public RouteResolver(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    public ResolvedRoute resolve(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        List<GatewayProperties.RouteDefinition> routes = gatewayProperties.getRoutes();

        for (GatewayProperties.RouteDefinition route : routes) {
            if (requestPath.startsWith(route.getPath())) {
                String targetPath = route.isStripPrefix() 
                        ? requestPath.substring(route.getPath().length()) 
                        : requestPath;
                
                String targetUrl = route.getTargetUrl() + targetPath;
                return new ResolvedRoute(targetUrl, route);
            }
        }
        return null;
    }

    public static class ResolvedRoute {
        private final String targetUrl;
        private final GatewayProperties.RouteDefinition routeDefinition;

        public ResolvedRoute(String targetUrl, GatewayProperties.RouteDefinition routeDefinition) {
            this.targetUrl = targetUrl;
            this.routeDefinition = routeDefinition;
        }

        public String getTargetUrl() {
            return targetUrl;
        }

        public GatewayProperties.RouteDefinition getRouteDefinition() {
            return routeDefinition;
        }
    }
}
