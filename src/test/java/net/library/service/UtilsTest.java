package net.library.service;

import net.library.util.Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {

    @Test
    void extractTokenFromHeaderValidBearerToken() {
        var token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        final var extractedToken = Utils.extractTokenFromHeader(token);

        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9", extractedToken);
    }
}
