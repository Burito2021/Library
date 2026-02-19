// src/main/java/net/library/config/security/JwtAuthenticationFilter.java
package net.library.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.library.exception.*;
import net.library.model.entity.User;
import net.library.service.JwtService;
import net.library.service.TokenBlacklistService;
import net.library.util.Utils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final var authHeader = request.getHeader("Authorization");
        final String jwt;
        final User user;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = Utils.extractTokenFromHeader(authHeader);

        if (jwt.isBlank() || !Utils.isValidJwtFormat(jwt)) {
            throw new InvalidToken("Invalid token format");
        }

        if (jwtService.isTokenExpired(jwt)) {
            throw new TokenExpired("Token is expired");
        }

        if (tokenBlacklistService.isBlacklisted(jwt)) {
            throw new TokenBlackListed("Token is blacklisted");
        }

        try {
            username = jwtService.extractUsername(jwt);
        } catch (RuntimeException e) {
            throw new InvalidToken(e.getMessage());
        }

        try {
            var dbUserDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
            user = dbUserDetails.getUser();
            if (!dbUserDetails.isEnabled() || !dbUserDetails.isAccountNonLocked()) {
                throw new UserDisabledException("User account is disabled or locked");
            }
        } catch (UsernameNotFoundException e) {
            throw new UserDisabledException("User not found");
        }

        if (!jwtService.isAccessToken(jwt)) {
            throw new InvalidToken("Not an access token");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRoleType().name()));

            CustomUserDetails userDetails = new CustomUserDetails(
                    user,
                    authorities
            );

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
