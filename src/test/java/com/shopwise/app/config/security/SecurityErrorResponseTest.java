package com.shopwise.app.config.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

class SecurityErrorResponseTest {

    @Test
    void shouldWriteUnauthorizedResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        SecurityErrorResponse.writeUnauthorized(response, "Authentication required");

        String body = response.getContentAsString();

        assertEquals(401, response.getStatus());
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertTrue(response.getContentType().contains("application/json"));
        assertTrue(body.contains("Authentication required"));
    }

    @Test
    void shouldWriteForbiddenResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        SecurityErrorResponse.writeForbidden(response, "Access denied");

        String body = response.getContentAsString();

        assertEquals(403, response.getStatus());
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertTrue(response.getContentType().contains("application/json"));
        assertTrue(body.contains("Access denied"));
    }
}