package net.library.service;


import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Setter
@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${rate-limit.max-attempts:5}")
    private int maxAttempts;

    @Value("${rate-limit.window-seconds:900}")
    private int windowSeconds;

    public boolean isAllowed(String clientId) {
        if (!enabled) {
            return true;
        }
        var key = "rate_limit:login:" + clientId;

        var currentAttempts = redisTemplate.opsForValue().increment(key, 1);

        if (currentAttempts == null) {
            return false;
        }

        if (currentAttempts == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        return currentAttempts <= maxAttempts;
    }
}
