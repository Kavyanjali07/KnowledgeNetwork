package com.knowledgenetwork.common.util;

import com.knowledgenetwork.common.exception.BusinessException;
import com.knowledgenetwork.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Utility class to interact with the Spring Security context and retrieve current authenticated user details.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Retrieves the optional UserPrincipal of the currently authenticated user.
     *
     * @return Optional containing UserPrincipal if authenticated, empty otherwise.
     */
    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    /**
     * Retrieves the UUID of the currently authenticated user.
     *
     * @return UUID of the authenticated user.
     * @throws BusinessException if user is not authenticated.
     */
    public static UUID getCurrentUserId() {
        return getCurrentUserPrincipal()
                .map(UserPrincipal::getId)
                .orElseThrow(() -> new BusinessException("No authenticated user found in security context"));
    }

    /**
     * Retrieves the email address of the currently authenticated user.
     *
     * @return User email address.
     * @throws BusinessException if user is not authenticated.
     */
    public static String getCurrentUserEmail() {
        return getCurrentUserPrincipal()
                .map(UserPrincipal::getEmail)
                .orElseThrow(() -> new BusinessException("No authenticated user found in security context"));
    }
}
