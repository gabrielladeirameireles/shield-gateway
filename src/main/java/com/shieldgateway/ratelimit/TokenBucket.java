package com.shieldgateway.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class TokenBucket {

    private final StringRedisTemplate redisTemplate;

    public TokenBucket(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryConsume(String key, int capacity, int refillRatePerSecond) {
        String redisKey = "rate_limit:" + key;
        Long current = redisTemplate.opsForValue().increment(redisKey);
        if (current != null && current == 1) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(1));
        }
        return current != null && current <= capacity;
    }
}
