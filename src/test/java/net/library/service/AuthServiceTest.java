package net.library.service;

import net.library.config.security.CustomUserDetails;
import net.library.exception.InvalidFingerprint;
import net.library.exception.InvalidToken;
import net.library.model.entity.RefreshToken;
import net.library.model.request.LoginRequest;
import net.library.model.request.RefreshTokenRequest;
import net.library.util.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class AuthServiceTest {
    @InjectMocks
    private AuthService authService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private Authentication authentication;
    @Mock
    private CustomUserDetails customUserDetails;

    @Test
    void authenticateSuccessfulLogin() {
        final var userName = "admin";
        final var password = "asdfadsf";

        final var loginRequest = LoginRequest.builder()
                .username(userName)
                .password(password)
                .fingerprint(Utils.getUUID())
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(userDetailsService.loadUserByUsername(userName)).thenReturn(customUserDetails);
        when(jwtService.generateAccessToken(customUserDetails)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(customUserDetails)).thenReturn("refresh-token");

        var result = authService.authenticate(loginRequest);

        verify(authenticationManager).authenticate(any());
        verify(userDetailsService).loadUserByUsername(userName);
        verify(jwtService).generateAccessToken(customUserDetails);
        verify(jwtService).generateRefreshToken(customUserDetails);

        Assertions.assertNotNull(result.getRefreshToken());
        Assertions.assertNotNull(result.getRefreshToken());
    }

    @Test
    void authenticateInvalidFingerprint() {
        final var userName = "admin";
        final var password = "asdfadsf";
        final var fingerprint = "";

        final var loginRequest = LoginRequest.builder()
                .username(userName)
                .password(password)
                .fingerprint(fingerprint)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        assertThrows(InvalidFingerprint.class, () -> authService.authenticate(loginRequest));

        verify(authenticationManager).authenticate(any());
    }

    @Test
    void authenticateInvalidCredentials() {
        final var userName = "admin";
        var mockAuth = mock(Authentication.class);
        final var password = "asdfadsf";

        final var loginRequest = LoginRequest.builder()
                .username(userName)
                .password(password)
                .fingerprint(Utils.getUUID())
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(userDetailsService.loadUserByUsername(userName)).thenReturn(customUserDetails);
        when(mockAuth.getPrincipal()).thenReturn(null);
        when(jwtService.generateAccessToken(customUserDetails)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(customUserDetails)).thenReturn("refresh-token");

        assertThrows(SecurityException.class, () -> authService.authenticate(loginRequest));

        verify(authenticationManager).authenticate(any());
        verify(userDetailsService).loadUserByUsername(userName);
        verify(mockAuth).getPrincipal();
        verify(jwtService).generateAccessToken(customUserDetails);
        verify(jwtService).generateRefreshToken(customUserDetails);
    }

    @Test
    void revokeTokenAndLogoutSuccessfulLogout() {
        var accessToken = "fake-token";
        var userId = UUID.randomUUID();

        when(jwtService.extractUserId(accessToken)).thenReturn(userId);

        authService.revokeTokenAndLogout(accessToken);

        verify(tokenBlacklistService).blacklistToken(accessToken);
        verify(jwtService).extractUserId(accessToken);
        verify(refreshTokenService).deleteByUserId(userId);
    }

    @Test
    void refreshTokenSuccessful() {
        final var refreshToken = "refresh-token";
        final var userId = UUID.randomUUID();
        final var userName = "admin";
        final var fingerprint = Utils.getUUID();

        final var refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken("refresh-token")
                .fingerprint(fingerprint)
                .build();

        var storedToken = new RefreshToken()
                .setT(refreshToken)
                .setF(fingerprint)
                .setC(LocalDateTime.now());

        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(jwtService.extractUsername(refreshToken)).thenReturn(userName);
        when(userDetailsService.loadUserByUsername(userName)).thenReturn(customUserDetails);
        when(jwtService.isTokenValid(refreshToken, customUserDetails)).thenReturn(true);
        when(refreshTokenService.findRefreshTokenByUserId(userId)).thenReturn(storedToken);
        when(jwtService.generateAccessToken(customUserDetails)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(customUserDetails)).thenReturn("new-refresh-token");

        var result = authService.refreshToken(refreshTokenRequest);

        verify(jwtService).extractUserId(refreshToken);
        verify(jwtService).extractUsername(refreshToken);
        verify(userDetailsService).loadUserByUsername(userName);
        verify(jwtService).isTokenValid(refreshToken, customUserDetails);
        verify(refreshTokenService, times(1)).findRefreshTokenByUserId(userId);
        verify(jwtService).generateAccessToken(customUserDetails);
        verify(jwtService).generateRefreshToken(customUserDetails);

        Assertions.assertNotNull(result.getRefreshToken());
        Assertions.assertNotNull(result.getRefreshToken());
    }

    @Test
    void refreshTokenIsBlank() {
        final var fingerprint = Utils.getUUID();

        final var refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken("")
                .fingerprint(fingerprint)
                .build();

        assertThrows(InvalidToken.class, () -> authService.refreshToken(refreshTokenRequest));
    }

    @Test
    void refreshTokenIsTokenValidFalse() {
        final var refreshToken = "refresh-token";
        final var userId = UUID.randomUUID();
        final var userName = "admin";
        final var fingerprint = Utils.getUUID();

        final var refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .fingerprint(fingerprint)
                .build();

        final var storedRefreshToken = new RefreshToken()
                .setT(refreshToken)
                .setF(fingerprint);

        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(refreshTokenService.findRefreshTokenByUserId(userId)).thenReturn(storedRefreshToken);
        when(jwtService.extractUsername(refreshToken)).thenReturn(userName);
        when(userDetailsService.loadUserByUsername(userName)).thenReturn(customUserDetails);
        when(jwtService.isTokenValid(refreshToken, customUserDetails)).thenReturn(false);

        assertThrows(InvalidToken.class, () -> authService.refreshToken(refreshTokenRequest));

        verify(jwtService).extractUserId(refreshToken);
        verify(jwtService).extractUsername(refreshToken);
        verify(userDetailsService).loadUserByUsername(userName);
        verify(jwtService).isTokenValid(refreshToken, customUserDetails);
    }

    @Test
    void refreshTokenDoesNotMatchTheOneInCache() {
        final var refreshToken = "refresh-token";
        final var userId = UUID.randomUUID();
//        final var userName = "admin";
        final var fingerprint = "agfdag";

        final var refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken("refresh-token")
                .fingerprint(fingerprint)
                .build();

        final var returnedRefreshToken = new RefreshToken()
                .setC(LocalDateTime.now()).setF(fingerprint).setT(Utils.getUUID());

        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(refreshTokenService.findRefreshTokenByUserId(userId)).thenReturn(returnedRefreshToken);

        assertThrows(InvalidToken.class, () -> authService.refreshToken(refreshTokenRequest));

        verify(jwtService).extractUserId(refreshToken);
        verify(refreshTokenService, times(1)).findRefreshTokenByUserId(userId);
    }

    @Test
    void refreshTokenFingerPrintIsBlank() {
        final var refreshToken = "refresh-token";
        final var userId = UUID.randomUUID();
        final var userName = "admin";
        final var fingerprint = "";

        final var refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken("refresh-token")
                .fingerprint(fingerprint)
                .build();

        var storedToken = new RefreshToken()
                .setT(refreshToken)
                .setF(fingerprint)
                .setC(LocalDateTime.now());

        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(jwtService.extractUsername(refreshToken)).thenReturn(userName);
        when(userDetailsService.loadUserByUsername(userName)).thenReturn(customUserDetails);
        when(jwtService.isTokenValid(refreshToken, customUserDetails)).thenReturn(true);
        when(refreshTokenService.findRefreshTokenByUserId(userId)).thenReturn(storedToken);

        assertThrows(InvalidFingerprint.class, () -> authService.refreshToken(refreshTokenRequest));

        verify(jwtService).extractUserId(refreshToken);
        verify(jwtService).extractUsername(refreshToken);
        verify(userDetailsService).loadUserByUsername(userName);
        verify(jwtService).isTokenValid(refreshToken, customUserDetails);
        verify(refreshTokenService, times(1)).findRefreshTokenByUserId(userId);
    }

    @Test
    void refreshTokenFingerPrintDoesNotMatchTheOneInCache() {
        final var refreshToken = "refresh-token";
        final var userId = UUID.randomUUID();
        final var userName = "admin";
        final var fingerprint = "agfdag";

        final var refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken("refresh-token")
                .fingerprint(fingerprint)
                .build();

        final var differentToken = new RefreshToken()
                .setC(LocalDateTime.now()).setF("different-fingerprint").setT(refreshToken);

        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(jwtService.extractUsername(refreshToken)).thenReturn(userName);
        when(userDetailsService.loadUserByUsername(userName)).thenReturn(customUserDetails);
        when(jwtService.isTokenValid(refreshToken, customUserDetails)).thenReturn(true);
        when(refreshTokenService.findRefreshTokenByUserId(userId)).thenReturn(differentToken);

        assertThrows(InvalidFingerprint.class, () -> authService.refreshToken(refreshTokenRequest));

        verify(jwtService).extractUserId(refreshToken);
        verify(jwtService).extractUsername(refreshToken);
        verify(userDetailsService).loadUserByUsername(userName);
        verify(jwtService).isTokenValid(refreshToken, customUserDetails);
        verify(refreshTokenService, times(1)).findRefreshTokenByUserId(userId);
    }
}
