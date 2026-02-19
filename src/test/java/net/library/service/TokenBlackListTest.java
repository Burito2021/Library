package net.library.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class TokenBlackListTest {
    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private JwtService jwtService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void blacklistTokenSuccessfully() {
        final var accessToken = "access-token";

        when(jwtService.extractExpiration(accessToken)).thenReturn(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        tokenBlacklistService.blacklistToken(accessToken);

        verify(jwtService).extractExpiration(accessToken);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(
                eq("blacklist:" + accessToken),
                eq("revoked"),
                any(Duration.class)
        );
    }

    @Test
    void isBlacklistedReturnsTrue() {
        final var accessToken = "access-token";

        when(redisTemplate.hasKey("blacklist:" + accessToken)).thenReturn(true);

        boolean result = tokenBlacklistService.isBlacklisted(accessToken);

        assertTrue(result);
        verify(redisTemplate).hasKey("blacklist:" + accessToken);
    }

    @Test
    void isBlacklistedReturnsFalse() {
        final var accessToken = "access-token";

        when(redisTemplate.hasKey("blacklist:" + accessToken)).thenReturn(false);

        boolean result = tokenBlacklistService.isBlacklisted(accessToken);

        assertFalse(result);
        verify(redisTemplate).hasKey("blacklist:" + accessToken);
    }
}
