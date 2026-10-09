package com.shieldgateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gateway")
public class GatewayConfig {

    private String targetUrl = "https://httpbin.org";
    private RateLimit rateLimit = new RateLimit();
    private Jwt jwt = new Jwt();

    public static class RateLimit {
        private int capacity = 10;
        private int refillRatePerSecond = 2;

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }
        public int getRefillRatePerSecond() { return refillRatePerSecond; }
        public void setRefillRatePerSecond(int refillRatePerSecond) { this.refillRatePerSecond = refillRatePerSecond; }
    }

    public static class Jwt {
        private String secret;
        private boolean enabled = false;

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    public RateLimit getRateLimit() { return rateLimit; }
    public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }
    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }
}
