package com.vlad.eventhub.service;

import com.vlad.eventhub.dto.request.LoginRequest;
import com.vlad.eventhub.dto.request.RegisterRequest;
import com.vlad.eventhub.dto.response.AuthResponse;
import com.vlad.eventhub.entity.User;
import com.vlad.eventhub.exception.EmailAlreadyExistsException;
import com.vlad.eventhub.repository.UserRepository;
import com.vlad.eventhub.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .role(request.role())
                .build();
        userRepository.save(user);

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        return AuthResponse.of(jwtTokenProvider.generateToken(userDetails));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        var userDetails = userDetailsService.loadUserByUsername(request.email());
        return AuthResponse.of(jwtTokenProvider.generateToken(userDetails));
    }
}
