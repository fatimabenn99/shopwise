package com.shopwise.app.config.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;

class JwtServiceTest {

    private static final String SECRET =
            "my-test-secret-key-for-jwt-tests-123456789";

    @Test
    void shouldGenerateAndParseToken() {
        JwtService jwtService = new JwtService(SECRET, 3600000);

        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN", "ROLE_USER"));

        assertNotNull(token);

        Claims claims = jwtService.parseClaims(token);

        assertEquals("admin", claims.getSubject());
        assertTrue(claims.get("roles", List.class).contains("ROLE_ADMIN"));
        assertTrue(claims.get("roles", List.class).contains("ROLE_USER"));
    }

    @Test
    void shouldThrowExceptionWhenTokenIsInvalid() {
        JwtService jwtService = new JwtService(SECRET, 3600000);

        assertThrows(Exception.class, () -> jwtService.parseClaims("invalid-token"));
    }

    @Test
    void shouldThrowExceptionWhenTokenIsExpired() throws InterruptedException {
        JwtService jwtService = new JwtService(SECRET, 1);

        String token = jwtService.generateToken("user", List.of("ROLE_USER"));

        Thread.sleep(10);

        assertThrows(Exception.class, () -> jwtService.parseClaims(token));
    }
}