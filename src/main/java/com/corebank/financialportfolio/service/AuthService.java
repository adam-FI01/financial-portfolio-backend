package com.corebank.financialportfolio.service;

import com.corebank.financialportfolio.dto.AuthResponse;
import com.corebank.financialportfolio.dto.LoginRequest;
import com.corebank.financialportfolio.dto.RegisterRequest;
import com.corebank.financialportfolio.entity.User;
import com.corebank.financialportfolio.exception.EmailAlreadyExistsException;
import com.corebank.financialportfolio.exception.InvalidCredentialsException;
import com.corebank.financialportfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.email());
        }

        User user = new User(request.email(), passwordEncoder.encode(request.password()));

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.email());
        }

        return new AuthResponse(jwtService.generateToken(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return new AuthResponse(jwtService.generateToken(user));
    }

}
