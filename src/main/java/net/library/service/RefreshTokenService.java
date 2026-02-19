package net.library.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.library.exception.RefreshTokenNotFound;
import net.library.model.entity.RefreshToken;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final JwtService jwtService;
    private final RedisTemplate<UUID, RefreshToken> redisTemplate;

    public void save(UUID userId, RefreshToken token) {
        deleteByUserId(userId);

        var expiresAt = jwtService.getRefreshTokenExpirationDate();
        long ttl = Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
        redisTemplate.opsForValue().set(userId, token, ttl, TimeUnit.SECONDS);

        log.info("Saved refresh token for userId: {} with TTL: {} seconds", userId, ttl);
    }

    public RefreshToken findRefreshTokenByUserId(UUID userId) {
        var token = redisTemplate.opsForValue().get(userId);
        if (token == null) {
            throw new RefreshTokenNotFound("Refresh token not found");
        }
        return token;
    }

    public void deleteByUserId(UUID userId) {

        if (userId != null) {
            redisTemplate.delete(userId);
        }
    }
}
