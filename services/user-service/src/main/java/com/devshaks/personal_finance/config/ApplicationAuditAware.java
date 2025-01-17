package com.devshaks.personal_finance.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import com.devshaks.personal_finance.users.User;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

public class ApplicationAuditAware implements AuditorAware<Long> {

    @Override
    @SuppressWarnings("null")
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            User user = (User) principal;
            return Optional.of(user.getId());
        }

        return Optional.empty();

    }
}
