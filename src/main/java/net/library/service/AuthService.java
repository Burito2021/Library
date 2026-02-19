// Update src/main/java/net/library/service/AuthService.java
package net.library.service;

import lombok.RequiredArgsConstructor;
import net.library.config.security.CustomUserDetails;
import net.library.exception.InvalidToken;
import net.library.exception.RefreshTokenNotFound;
import net.library.exception.TokenExpired;
import net.library.model.entity.RefreshToken;
import net.library.model.entity.User;
import net.library.model.request.LoginRequest;
import net.library.model.response.AuthResponse;
import net.library.repository.RefreshTokenRepository;
import net.library.repository.UserRepository;
import net.library.util.Utils;
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

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    public UUID getCurrentUserIdOrThrow(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomUserDetails customUser) {
            return customUser.getUserId();
        }
        throw new SecurityException("Invalid authentication");
    }

    @Transactional
    public AuthResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        //question how to remake to one request to DB
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        //----------------------------------------
        var accessToken = jwtService.generateAccessToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        saveRefreshToken(user, refreshToken);

        return new AuthResponse()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken);
    }

    public void revokeToken(String token) {
        tokenBlacklistService.blacklistToken(token);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken.isBlank()) {
            throw new InvalidToken("Invalid refresh token");
        }

        var username = jwtService.extractUsername(refreshToken);
        var userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new InvalidToken("Invalid refresh token");
        }

        var storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RefreshTokenNotFound("Refresh token not found"));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new TokenExpired("Refresh token expired");
        }

        storedToken.setExpiresAt(jwtService.getRefreshTokenExpirationDate());
        refreshTokenRepository.save(storedToken);
        var newAccessToken = jwtService.generateAccessToken(userDetails);

        return new AuthResponse()
                .setAccessToken(newAccessToken)
                .setRefreshToken(refreshToken);
    }

    @Transactional
    public void logout(String token) {
        var username = jwtService.extractUsername(token);
        var user = userRepository.findByUsername(username).orElseThrow();

        refreshTokenRepository.deleteByUser(user);
    }

    private void saveRefreshToken(User user, String token) {
        refreshTokenRepository.deleteByUser(user);

        var refreshToken = new RefreshToken()
                .setToken(token)
                .setUser(user)
                .setExpiresAt(jwtService.getRefreshTokenExpirationDate());

        refreshTokenRepository.save(refreshToken);
    }
}
