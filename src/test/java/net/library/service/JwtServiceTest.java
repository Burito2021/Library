package net.library.service;

import net.library.config.security.CustomUserDetails;
import net.library.exception.InvalidToken;
import net.library.exception.TokenExpired;
import net.library.model.entity.User;
import net.library.repository.enums.ModerationState;
import net.library.repository.enums.UserState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static net.library.tools.HttpUtil.EXPIRED_REFRESH_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class JwtServiceTest {
    @Autowired
    JwtService jwtService;

    @Test
    void extractUsernameReturnsValidUsername() {
        var user = new User()
                .setUsername("admin")
                .setPassword("password")
                .setUserState(UserState.ACTIVE)
                .setId(UUID.randomUUID())
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now())
                .setAddress("address")
                .setModerationState(ModerationState.APPROVED)
                .setEmail("email")
                .setPhoneNumber("phoneNumber");

        var admin = new CustomUserDetails(
                user,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        var token = jwtService.generateAccessToken(admin);
        var username = jwtService.extractUsername(token);

        assertThat(username).contains("admin");
    }

    @Test
    void extractRolesReturnsValidRoles() {
        var user = new User()
                .setUsername("admin")
                .setPassword("password")
                .setUserState(UserState.ACTIVE)
                .setId(UUID.randomUUID())
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now())
                .setAddress("address")
                .setModerationState(ModerationState.APPROVED)
                .setEmail("email")
                .setPhoneNumber("phoneNumber");

        var admin = new CustomUserDetails(
                user,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        var token = jwtService.generateAccessToken(admin);
        List<String> roles = jwtService.extractRoles(token);

        assertThat(roles).contains("ROLE_ADMIN");
    }

    @Test
    void refreshTokenReturnsRefreshToken() {
        var user = new User()
                .setUsername("admin")
                .setPassword("password")
                .setUserState(UserState.ACTIVE)
                .setId(UUID.randomUUID())
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now())
                .setAddress("address")
                .setModerationState(ModerationState.APPROVED)
                .setEmail("email")
                .setPhoneNumber("phoneNumber");

        var admin = new CustomUserDetails(
                user,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        var refreshToken = jwtService.generateRefreshToken(admin);

        assertNotNull(refreshToken);
    }

    @Test
    void extractRolesThrowsInvalidTokenWhenAccessTokenWithoutRoles() {
        final var accessTokenWithoutRoles = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOiIyMTA3MjYxMC05NWNmLTQwNjctOTE3NS04ZmY0NjFlZTczMjAiLCJzdWIiOiJ1c2VyMSIsImlhdCI6MTc3MzY3MDM5OSwiZXhwIjoyMDg5MjQ2Mzk5fQ.ytiVt_UdvelrBovCcezKUH_0JsDy7PzO3Sg6iksIZ0nb5tioAlHUc9hhk8mlBe_7MbpNOsBOChCfUeLc1Xl5Jw";

        assertThrows(InvalidToken.class, () -> jwtService.extractRoles(accessTokenWithoutRoles));
    }

    @Test
    void extractUserIdReturnsValidUserId() {
        var user = new User()
                .setUsername("admin")
                .setPassword("password")
                .setUserState(UserState.ACTIVE)
                .setId(UUID.randomUUID())
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now())
                .setAddress("address")
                .setModerationState(ModerationState.APPROVED)
                .setEmail("email")
                .setPhoneNumber("phoneNumber");

        var admin = new CustomUserDetails(
                user,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        var token = jwtService.generateAccessToken(admin);
        var userIdFromToken = jwtService.extractUserId(token);

        assertThat(userIdFromToken).isEqualTo(user.getId());
    }

    @Test
    void extractUserIdThrowsInvalidTokenWhenAccessTokenWithoutUserId() {
        final var accessTokenWithoutUserId = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6InVzZXIxIiwiaWF0IjoxNzczNjcyMjUzLCJleHAiOjIwODkyNDgyNTN9.pFeKzDYgjnKRvEqb1hAKOWLh_eD18kYsw1LzHQd2MYf7QLQf7h1MsQtlbu0TneqF-BzLzPcbkrUQZiyMs4OKMw";

        assertThrows(InvalidToken.class, () -> jwtService.extractUserId(accessTokenWithoutUserId));
    }

    @Test
    void extractExpirationReturnsValidExpirationDate() {
        long beforeCreation = System.currentTimeMillis();
        var user = new User()
                .setUsername("admin")
                .setPassword("password")
                .setUserState(UserState.ACTIVE)
                .setId(UUID.randomUUID())
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now())
                .setAddress("address")
                .setModerationState(ModerationState.APPROVED)
                .setEmail("email")
                .setPhoneNumber("phoneNumber");

        var admin = new CustomUserDetails(
                user,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        var token = jwtService.generateAccessToken(admin);
        var expirationDateFromToken = jwtService.extractExpiration(token);

        long expectedExpirationTime = beforeCreation + (15 * 60 * 1000);
        long actualExpirationTime = expirationDateFromToken.getTime();

        assertThat(actualExpirationTime)
                .isCloseTo(expectedExpirationTime, within(1000L));
    }

    @Test
    void extractExpirationThrowsTokenExpiredWhenExpirationDateIsExpired() {

        assertThrows(TokenExpired.class, () -> jwtService.extractExpiration(EXPIRED_REFRESH_TOKEN));
    }

    @Test
    void extractExpirationThrowsInvalidTokenWhenExpirationClaimIsMissing() {
        final var accessTokenWithoutExpirationDate = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInVzZXJJZCI6IjRjZjBhMWIwLWFkYTEtNDIzOS05OWUxLTMxZGFlOWVlZmIyNyIsInN1YiI6InVzZXIxIiwiaWF0IjoxNzczNjc0Mjg5fQ.miKm2gRj_B3Bg8PdCPQMcIZvoTEF39G1gwuGFI6oPWvEf2qWTw90SNU-wRfUFah_7peTggD3HW3hHC_izS_Fsw";

        assertThrows(InvalidToken.class, () -> jwtService.extractExpiration(accessTokenWithoutExpirationDate));
    }

    @Test
    void isTokenValidReturnsTrueWhenTokenIsValid() {
        var user = new User()
                .setUsername("admin")
                .setPassword("password")
                .setUserState(UserState.ACTIVE)
                .setId(UUID.randomUUID())
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now())
                .setAddress("address")
                .setModerationState(ModerationState.APPROVED)
                .setEmail("email")
                .setPhoneNumber("phoneNumber");

        var admin = new CustomUserDetails(
                user,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        var token = jwtService.generateAccessToken(admin);
        var isTokenValid = jwtService.isTokenValid(token, admin);

        assertTrue(isTokenValid);
    }

    @Test
    void isTokenValidReturnsFalseWhenUsernameDoesNotMatch() {
        var user = new User()
                .setUsername("admin")
                .setPassword("password")
                .setUserState(UserState.ACTIVE)
                .setId(UUID.randomUUID())
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now())
                .setAddress("address")
                .setModerationState(ModerationState.APPROVED)
                .setEmail("email")
                .setPhoneNumber("phoneNumber");

        var admin = new CustomUserDetails(
                user,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        var user2 = new User()
                .setUsername("admin3")
                .setPassword("password");

        var invalidUser = new CustomUserDetails(
                user2,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        var token = jwtService.generateAccessToken(admin);
        var isTokenValid = jwtService.isTokenValid(token, invalidUser);

        assertFalse(isTokenValid);
    }

    @Test
    void isTokenValidReturnsFalseWhenTokenExpired() {
        var user = new User()
                .setUsername("admin")
                .setPassword("password")
                .setUserState(UserState.ACTIVE)
                .setId(UUID.randomUUID())
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now())
                .setAddress("address")
                .setModerationState(ModerationState.APPROVED)
                .setEmail("email")
                .setPhoneNumber("phoneNumber");

        UserDetails admin = new CustomUserDetails(
                user,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        assertThrows(TokenExpired.class, () -> jwtService.isTokenValid(EXPIRED_REFRESH_TOKEN, admin));
    }
}
