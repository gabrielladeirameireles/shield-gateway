package com.shieldgateway.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayController {

    private final RouteResolver routeResolver;
    private final HttpReverseProxy httpReverseProxy;

    public GatewayController(RouteResolver routeResolver, HttpReverseProxy httpReverseProxy) {
        this.routeResolver = routeResolver;
        this.httpReverseProxy = httpReverseProxy;
    }

    @RequestMapping("/**")
    public ResponseEntity<byte[]> handleRequest(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        RouteResolver.ResolvedRoute resolvedRoute = routeResolver.resolve(request);
        if (resolvedRoute == null) {
            return ResponseEntity.status(404)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body("{\"status\": 404, \"error\": \"Not Found\", \"message\": \"Rota não encontrada no Gateway.\"}".getBytes());
        }

        return httpReverseProxy.forward(request, resolvedRoute.getTargetUrl(), body);
    }
}
