package com.lms.review.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final UUID userId;
    private final String username;
    private final List<String> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        if (roles != null) {
            for (String role : roles) {
                String auth = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                authorities.add(new SimpleGrantedAuthority(auth));
                if (role.toUpperCase().contains("ADMIN")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_MAIN_ADMIN"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_SUB_ADMIN"));
                }
            }
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
