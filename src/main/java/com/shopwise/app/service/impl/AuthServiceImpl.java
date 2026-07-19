package com.shopwise.app.service.impl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shopwise.app.config.security.JwtService;
import com.shopwise.app.dto.request.LoginRequest;
import com.shopwise.app.dto.request.RegisterRequest;
import com.shopwise.app.dto.response.LoginResponse;
import com.shopwise.app.dto.response.RegisterResponse;
import com.shopwise.app.entity.User;
import com.shopwise.app.exception.ConflictException;
import com.shopwise.app.exception.UnauthorizedException;
import com.shopwise.app.repository.UserRepository;
import com.shopwise.app.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");

        User saved = userRepository.save(user);

        return new RegisterResponse(saved.getId(), saved.getUsername(), saved.getRole());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!passwordMatches) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtService.generateToken(
                user.getUsername(),
                List.of(user.getRole())
        );

        return new LoginResponse(token, user.getUsername(), user.getRole());
    }
}