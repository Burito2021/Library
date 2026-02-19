// Update src/main/java/net/library/service/AuthService.java
package net.library.service;

import lombok.RequiredArgsConstructor;
import net.library.config.security.CustomUserDetails;
import net.library.exception.InvalidFingerprint;
import net.library.exception.InvalidToken;
import net.library.model.entity.RefreshToken;
import net.library.model.request.LoginRequest;
import net.library.model.request.RefreshTokenRequest;
import net.library.model.response.AuthResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;

    public UUID getCurrentUserIdOrThrow(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomUserDetails customUser) {
            return customUser.getUserId();
        }
        throw new SecurityException("Invalid authentication");
    }

    @Transactional
    public AuthResponse authenticate(LoginRequest request) {

        final var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        var fingerPrint = request.getFingerprint();
        if (fingerPrint.isBlank()) {
            throw new InvalidFingerprint("Fingerprint is required");
        }

        var userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        var accessToken = jwtService.generateAccessToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        refreshTokenService.save(getCurrentUserIdOrThrow(authentication),
                new RefreshToken()
                        .setF(fingerPrint)
                        .setT(refreshToken)
                        .setC(LocalDateTime.now()));

        return new AuthResponse()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken);
    }

    @Transactional
    public void revokeTokenAndLogout(String accessToken) {
        tokenBlacklistService.blacklistToken(accessToken);

        var username = jwtService.extractUserId(accessToken);

        refreshTokenService.deleteByUserId(username);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        var refreshToken = request.getRefreshToken();
        var fingerprint = request.getFingerprint();
        var userId = jwtService.extractUserId(refreshToken);

        if (refreshToken.isBlank()) {
            throw new InvalidToken("Invalid refresh token");
        }

        var storedToken = refreshTokenService.findRefreshTokenByUserId(userId);

        if (!refreshToken.equals(storedToken.getT())) {
            throw new InvalidToken("Invalid refresh token");
        }

        var username = jwtService.extractUsername(refreshToken);
        var userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new InvalidToken("Invalid refresh token");
        }

        var storedTokenFingerPrint = storedToken.getF();

        if (fingerprint.isBlank() || !storedTokenFingerPrint.equals(fingerprint)) {
            throw new InvalidFingerprint("Invalid fingerprint");
        }

        var newRefreshToken = jwtService.generateRefreshToken(userDetails);
        var newAccessToken = jwtService.generateAccessToken(userDetails);

        refreshTokenService.save(
                userId,
                new RefreshToken()
                        .setT(newRefreshToken)
                        .setC(LocalDateTime.now())
                        .setF(fingerprint));

        return new AuthResponse()
                .setAccessToken(newAccessToken)
                .setRefreshToken(newRefreshToken);
    }
}
