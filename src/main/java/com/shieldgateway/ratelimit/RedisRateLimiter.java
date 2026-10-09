package com.shieldgateway.ratelimit;

import com.shieldgateway.config.GatewayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimiter.class);

    private final StringRedisTemplate redisTemplate;
    private final InMemoryRateLimiter localFallback;
    private final GatewayConfig gatewayConfig;

    public RedisRateLimiter(StringRedisTemplate redisTemplate,
                            InMemoryRateLimiter localFallback,
                            GatewayConfig gatewayConfig) {
        this.redisTemplate = redisTemplate;
        this.localFallback = localFallback;
        this.gatewayConfig = gatewayConfig;
    }

    public boolean isAllowed(String key) {
        String redisKey = "rate_limit:" + key;
        try {
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);
            if (currentCount != null && currentCount == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(1));
            }
            
            int maxAllowedPerSecond = gatewayConfig.getRateLimit().getCapacity();
            if (currentCount != null && currentCount > maxAllowedPerSecond) {
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("Falha ao comunicar com Redis ({}), acionando Fallback em memória para chave: {}", e.getMessage(), key);
            return localFallback.isAllowed(key);
        }
    }
}
