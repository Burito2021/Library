package net.library.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static net.library.util.HttpUtil.GLOBAL_BASE_URI;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, GLOBAL_BASE_URI + "users/details").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, GLOBAL_BASE_URI + "users/details").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, GLOBAL_BASE_URI + "**").hasAnyRole("ADMIN")
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers(GLOBAL_BASE_URI + "**").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .httpBasic(basic -> {
                });
        return http.build();
    }
}