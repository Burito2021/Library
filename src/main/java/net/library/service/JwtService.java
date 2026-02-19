package net.library.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import net.library.config.security.CustomUserDetails;
import net.library.exception.InvalidToken;
import net.library.exception.TokenExpired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret:mySecretKey}")
    private String secret;

    @Value("${jwt.access-token.expiration:900000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration:1800000}")
    private long refreshTokenExpiration;

    @PostConstruct
    public void validateJwtConfiguration() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 64) {
            throw new IllegalStateException(
                    String.format(
                            "JWT secret must be at least 64 bytes for HMAC-SHA512 security. " +
                                    "Current length: %d bytes. " +
                                    "Please update your jwt.secret configuration to be at least 64 characters long.",
                            keyBytes.length
                    )
            );
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractUserId(String token) {
        var claims = extractAllClaims(token);
        var userIdStr = claims.get("userId", String.class);
        if (userIdStr == null) {
            throw new InvalidToken("User id not found in token");
        }
        return UUID.fromString(userIdStr);
    }

    public List<String> extractRoles(String token) {
        var claims = extractAllClaims(token);
        var roles = claims.get("roles", List.class);
        if (roles == null) {
            throw new InvalidToken("Roles not found in token");
        }
        return roles;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final var claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(UserDetails userDetails) {
        var claims = addUserIdAndRolesToClaims(userDetails);
        claims.put("tokenType", "ACCESS");
        return buildToken(claims, userDetails, accessTokenExpiration * 1000);
    }

    public LocalDateTime getRefreshTokenExpirationDate() {
        return LocalDateTime.now().plusSeconds(refreshTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        var claims = addUserIdAndRolesToClaims(userDetails);
        claims.put("tokenType", "REFRESH");
        return buildToken(claims, userDetails, refreshTokenExpiration * 1000);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final var username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        var expiration = extractClaim(token, Claims::getExpiration);
        if (expiration == null) {
            throw new InvalidToken("Expiration not found in token");
        }
        return expiration;
    }

    private Map<String, Object> addUserIdAndRolesToClaims(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());

        if (userDetails instanceof CustomUserDetails customUser) {
            claims.put("userId", customUser.getUserId().toString());
        }

        return claims;
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new TokenExpired("Token is expired");
        } catch (JwtException e) {
            throw new InvalidToken("Invalid token");
        }
    }

    public String extractTokenType(String token) {
        var claims = extractAllClaims(token);
        return claims.get("tokenType", String.class);
    }

    public boolean isAccessToken(String token) {
        return "ACCESS".equals(extractTokenType(token));
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}