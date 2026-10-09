package com.shieldgateway.ratelimit;

import com.shieldgateway.config.GatewayConfig;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRateLimiter {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final GatewayConfig gatewayConfig;

    public InMemoryRateLimiter(GatewayConfig gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
    }

    public boolean isAllowed(String key) {
        GatewayConfig.RateLimit limit = gatewayConfig.getRateLimit();
        TokenBucket bucket = buckets.computeIfAbsent(key,
            k -> new TokenBucket(limit.getCapacity(), limit.getRefillRatePerSecond()));
        return bucket.tryConsume();
    }
}
