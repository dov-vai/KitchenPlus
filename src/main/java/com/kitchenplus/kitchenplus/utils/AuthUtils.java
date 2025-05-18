package com.kitchenplus.kitchenplus.utils;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public class AuthUtils {
    /**
     * Check if the user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() &&
            !(auth instanceof AnonymousAuthenticationToken)) {
            return true;
        }
        return false;
    }

    /**
     * Gets email of currently logged-in user.
     *
     * @return email if user is authenticated, empty otherwise
     */
    public static Optional<String> getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            var principal = auth.getPrincipal();
            if (principal instanceof UserDetails) {
                return Optional.of(((UserDetails) principal).getUsername());
            }
        }
        return Optional.empty();
    }
}
