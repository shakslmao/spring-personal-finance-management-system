package com.devshaks.personal_finance.security;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class CustomRoleHierarchy implements RoleHierarchy {

    @Override
    public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> reachableAuthorities = new HashSet<>(authorities);

        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_SUPER_ADMIN")) {
                reachableAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                reachableAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            } else if (authority.getAuthority().equals("ROLE_ADMIN")) {
                reachableAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
        }

        return reachableAuthorities;
    }
}
