package com.shopwise.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.shopwise.app.config.security.JwtService;
import com.shopwise.app.dto.request.LoginRequest;
import com.shopwise.app.dto.request.RegisterRequest;
import com.shopwise.app.dto.response.LoginResponse;
import com.shopwise.app.dto.response.RegisterResponse;
import com.shopwise.app.entity.User;
import com.shopwise.app.exception.ConflictException;
import com.shopwise.app.exception.UnauthorizedException;
import com.shopwise.app.repository.UserRepository;
import com.shopwise.app.service.impl.AuthServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void shouldRegisterUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setPassword("password123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("john");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole("ROLE_USER");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponse result = authService.register(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john", result.getUsername());
        assertEquals("ROLE_USER", result.getRole());

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowConflictWhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setPassword("password123");

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginWithValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encodedPassword");
        user.setRole("ROLE_ADMIN");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(eq("admin"), anyList())).thenReturn("fake-jwt-token");

        LoginResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals("fake-jwt-token", result.getToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals("admin", result.getUsername());
        assertEquals("ROLE_ADMIN", result.getRole());
    }

    @Test
    void shouldThrowUnauthorizedWhenUsernameDoesNotExist() {
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("password123");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));

        verify(jwtService, never()).generateToken(anyString(), anyList());
    }

    @Test
    void shouldThrowUnauthorizedWhenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong-password");

        User user = new User();
        user.setUsername("admin");
        user.setPassword("encodedPassword");
        user.setRole("ROLE_ADMIN");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encodedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));

        verify(jwtService, never()).generateToken(anyString(), anyList());
    }
}