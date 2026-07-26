package com.corebank.financialportfolio.service;

import com.corebank.financialportfolio.entity.User;
import com.corebank.financialportfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Resolves the {@link User} behind the current request's {@link Authentication}.
 * The security principal is the JWT subject (the user's email) — see
 * JwtAuthenticationFilter — so this is a single lookup by email.
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }

}
