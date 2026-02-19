package net.library.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.library.model.request.LoginRequest;
import net.library.model.request.RefreshTokenRequest;
import net.library.model.response.AuthResponse;
import net.library.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static net.library.util.Utils.extractTokenFromHeader;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(
            summary = "Revoke token and logout",
            description = "Revokes the user's authentication token and logs them out of the system"
    )
    @ApiResponse(responseCode = "200", description = "Successfully logged out")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestHeader("Authorization") String token) {
        var jwtToken = extractTokenFromHeader(token);
        authService.revokeTokenAndLogout(jwtToken);
        return ResponseEntity.ok().build();
    }
}