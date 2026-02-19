package net.library.config.security;

import lombok.RequiredArgsConstructor;
import net.library.model.entity.User;
import net.library.repository.enums.UserState;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final User user;
//    private final String username;
//    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    public User getUser() {
        return user;
    }

    public UUID getUserId() {
        return user.getId();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {

        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getUserState() != UserState.BANNED &&
                user.getUserState() != UserState.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getUserState() == UserState.ACTIVE;
    }
}

