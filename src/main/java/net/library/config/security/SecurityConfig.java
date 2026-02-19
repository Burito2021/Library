// Update src/main/java/net/library/config/security/SecurityConfig.java
package net.library.config.security;

import lombok.RequiredArgsConstructor;
import net.library.filter.FilterExceptionHandler;
import net.library.service.JwtService;
import net.library.service.TokenBlacklistService;
import net.library.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static net.library.util.HttpUtil.GLOBAL_BASE_URI;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, tokenBlacklistService, userDetailsService);
    }

    @Bean
    public FilterExceptionHandler filterExceptionHandler() {
        return new FilterExceptionHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(GLOBAL_BASE_URI + "auth/login").permitAll()
                        .requestMatchers(GLOBAL_BASE_URI + "auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, GLOBAL_BASE_URI + "users").permitAll()
                        .requestMatchers(HttpMethod.POST, GLOBAL_BASE_URI + "auth/revoke").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, GLOBAL_BASE_URI + "auth/logout").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, GLOBAL_BASE_URI + "books").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, GLOBAL_BASE_URI + "books/all").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, GLOBAL_BASE_URI + "users/details").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, GLOBAL_BASE_URI + "users/details").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, GLOBAL_BASE_URI + "**").hasAnyRole("ADMIN")
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers(GLOBAL_BASE_URI + "**").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(filterExceptionHandler(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
