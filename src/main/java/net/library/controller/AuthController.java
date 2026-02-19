package net.library.controller;


import lombok.RequiredArgsConstructor;
import net.library.model.request.LoginRequest;
import net.library.model.request.RefreshTokenRequest;
import net.library.model.response.AuthResponse;
import net.library.service.AuthService;
import net.library.util.Utils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static net.library.util.Utils.*;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/revoke")
    public ResponseEntity<Void> revoke(@RequestHeader("Authorization") String token) {
        var jwtToken = extractTokenFromHeader(token);
        authService.revokeToken(jwtToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        var jwtToken = extractTokenFromHeader(token);
        authService.revokeToken(jwtToken);
        authService.logout(jwtToken);
        return ResponseEntity.ok().build();
    }
}