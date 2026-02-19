package net.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtService;

    public void blacklistToken(String token) {
        var expiresAt = jwtService.extractExpiration(token);
        var ttl = Duration.between(LocalDateTime.now(),
                expiresAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        redisTemplate.opsForValue().set("blacklist:" + token, "revoked", ttl);
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
}