package com.shieldgateway.filter;

import com.shieldgateway.ratelimit.TokenBucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    private final TokenBucket tokenBucket;

    public RateLimiterFilter(TokenBucket tokenBucket) {
        this.tokenBucket = tokenBucket;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();

        if (!tokenBucket.tryConsume(clientIp, 10, 1)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\": 429, \"error\": \"Too Many Requests\", \"message\": \"Limite de requisições excedido. Tente novamente em instantes.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
