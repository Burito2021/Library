package net.library.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RateLimiterServiceMockTest {
    @InjectMocks
    private RateLimitService rateLimitService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void isAllowedWhenCurrentAttemptsLessThanMaxAttempts() {
        final var maxAttempts = 5;
        final var enabled = true;
        final var windowSeconds = 900;
        final var clientId = "1232131";
        final var key = "rate_limit:login:" + clientId;
        ReflectionTestUtils.setField(rateLimitService, "enabled", enabled);
        ReflectionTestUtils.setField(rateLimitService, "maxAttempts", maxAttempts);
        ReflectionTestUtils.setField(rateLimitService, "windowSeconds", windowSeconds);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(key, 1)).thenReturn(2L);

        var isAllowed = rateLimitService.isAllowed(clientId);
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations).increment(key, 1);

        assertTrue(isAllowed);
    }

    @Test
    void isAllowedRateLimitServiceDisabled() {
        final var maxAttempts = 2;
        final var enabled = false;
        final var windowSeconds = 900;
        final var clientId = "1232131";
        final var key = "rate_limit:login:" + clientId;
        ReflectionTestUtils.setField(rateLimitService, "enabled", enabled);
        ReflectionTestUtils.setField(rateLimitService, "maxAttempts", maxAttempts);
        ReflectionTestUtils.setField(rateLimitService, "windowSeconds", windowSeconds);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.increment(key, 1)).thenReturn(4L);

        var isAllowed = rateLimitService.isAllowed(clientId);

        assertTrue(isAllowed);
    }

    @Test
    void isAllowedTrueWhenCurrentAttemptsEqualsMaxAttempts() {
        final var clientId = "1232131";
        final var key = "rate_limit:login:" + clientId;
        final var maxAttempts = 3;
        ReflectionTestUtils.setField(rateLimitService, "enabled", true);
        ReflectionTestUtils.setField(rateLimitService, "maxAttempts", maxAttempts);
        ReflectionTestUtils.setField(rateLimitService, "windowSeconds", 900);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.increment(key, 1)).thenReturn(3L);

        var isAllowed = rateLimitService.isAllowed(clientId);
        assertTrue(isAllowed);
    }

    @Test
    void isAllowedFalseWhenCurrentAttemptsExceedsMaxAttempts() {
        final var clientId = "1232131";
        final var key = "rate_limit:login:" + clientId;
        final var maxAttempts = 3;
        ReflectionTestUtils.setField(rateLimitService, "enabled", true);
        ReflectionTestUtils.setField(rateLimitService, "maxAttempts", maxAttempts);
        ReflectionTestUtils.setField(rateLimitService, "windowSeconds", 900);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.increment(key, 1)).thenReturn(4L);

        var isAllowed = rateLimitService.isAllowed(clientId);

        assertFalse(isAllowed);
    }
}
