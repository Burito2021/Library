package net.library.service;

import net.library.config.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    public UUID getCurrentUserIdOrThrow(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomUserDetails customUser) {
            return customUser.getUserId();
        }
        throw new SecurityException("Invalid authentication");
    }

//    public boolean hasAdminRole(Authentication authentication) {
//        return authentication.getAuthorities().stream()
//                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
//    }
//
//    public boolean canAccessResource(Authentication authentication, UUID resourceUserId) {
//        var currentUserId = getCurrentUserIdOrThrow(authentication);
//        return currentUserId.equals(resourceUserId) || hasAdminRole(authentication);
//    }
}