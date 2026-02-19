package net.library;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

public class TestContainers {
    private static final GenericContainer<?> redis;

    static {
        redis = new GenericContainer<>("redis:7-alpine")
                .withExposedPorts(6379)
                .withNetworkAliases("redis");
        redis.start();
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }
}
