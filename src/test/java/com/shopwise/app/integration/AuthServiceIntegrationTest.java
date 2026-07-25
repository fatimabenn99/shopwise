package com.shopwise.app.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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

import io.jsonwebtoken.Claims;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldRegisterAndPersistUserWithEncodedPassword() {
        RegisterRequest request = registerRequest(
                "integration-user",
                "secure-password-123"
        );

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("integration-user", response.getUsername());
        assertEquals("ROLE_USER", response.getRole());

        User persisted = userRepository.findByUsername("integration-user")
                .orElseThrow();

        assertNotEquals("secure-password-123", persisted.getPassword());

        assertTrue(passwordEncoder.matches(
                "secure-password-123",
                persisted.getPassword()
        ));

        assertEquals("ROLE_USER", persisted.getRole());
    }

    @Test
    void shouldRejectDuplicateUsername() {
        authService.register(registerRequest(
                "duplicate-user",
                "password-123"
        ));

        RegisterRequest duplicate = registerRequest(
                "duplicate-user",
                "another-password"
        );

        assertThrows(
                ConflictException.class,
                () -> authService.register(duplicate)
        );
    }

    @Test
    void shouldLoginAndGenerateValidJwt() {
        authService.register(registerRequest(
                "login-user",
                "correct-password"
        ));

        LoginRequest loginRequest = loginRequest(
                "login-user",
                "correct-password"
        );

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isBlank());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("login-user", response.getUsername());
        assertEquals("ROLE_USER", response.getRole());

        Claims claims = jwtService.parseClaims(response.getToken());

        assertEquals("login-user", claims.getSubject());
        assertTrue(
                claims.get("roles", java.util.List.class)
                        .contains("ROLE_USER")
        );
    }

    @Test
    void shouldRejectLoginWithUnknownUsername() {
        LoginRequest request = loginRequest(
                "unknown-user",
                "password"
        );

        assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request)
        );
    }

    @Test
    void shouldRejectLoginWithInvalidPassword() {
        authService.register(registerRequest(
                "password-user",
                "correct-password"
        ));

        LoginRequest request = loginRequest(
                "password-user",
                "wrong-password"
        );

        assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request)
        );
    }

    private RegisterRequest registerRequest(
            String username,
            String password
    ) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private LoginRequest loginRequest(
            String username,
            String password
    ) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}