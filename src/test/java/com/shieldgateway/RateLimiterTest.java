package com.shieldgateway;

import com.shieldgateway.ratelimit.TokenBucket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    @Test
    @DisplayName("Deve permitir requisições até atingir a capacidade máxima do TokenBucket")
    void testTokenBucketCapacity() {
        TokenBucket bucket = new TokenBucket(5, 1);

        for (int i = 0; i < 5; i++) {
            assertTrue(bucket.tryConsume(), "A requisição " + (i + 1) + " deveria ser autorizada");
        }

        assertFalse(bucket.tryConsume(), "A 6ª requisição deveria ser bloqueada (Rate Limit)");
    }

    @Test
    @DisplayName("Deve recarregar tokens após o intervalo de tempo decorrido")
    void testTokenBucketRefill() throws InterruptedException {
        TokenBucket bucket = new TokenBucket(1, 1);

        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());

        Thread.sleep(1100);

        assertTrue(bucket.tryConsume(), "Após o tempo de refill, o token deveria estar disponível novamente");
    }
}
