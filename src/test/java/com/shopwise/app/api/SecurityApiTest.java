package com.shopwise.app.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.shopwise.app.config.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldReturn401ForProtectedEndpointWithoutToken()
            throws Exception {

        mockMvc.perform(get("/api/sales"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn401ForInvalidToken() throws Exception {
        mockMvc.perform(get("/api/sales")
                        .header(
                                "Authorization",
                                "Bearer invalid.jwt.token"
                        ))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Invalid or expired token"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn401ForMalformedBearerToken()
            throws Exception {

        mockMvc.perform(get("/api/sales")
                        .header(
                                "Authorization",
                                "Bearer "
                        ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Invalid or expired token"));
    }

    @Test
    void shouldAllowUserToReadSales() throws Exception {
        String token = jwtService.generateToken(
                "security-user",
                List.of("ROLE_USER")
        );

        mockMvc.perform(get("/api/sales")
                        .header(
                                "Authorization",
                                bearer(token)
                        ))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAdminToReadSales() throws Exception {
        String token = jwtService.generateToken(
                "security-admin",
                List.of("ROLE_ADMIN")
        );

        mockMvc.perform(get("/api/sales")
                        .header(
                                "Authorization",
                                bearer(token)
                        ))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn403WhenUserCreatesProduct()
            throws Exception {

        String token = jwtService.generateToken(
                "security-user",
                List.of("ROLE_USER")
        );

        mockMvc.perform(post("/api/products")
                        .header(
                                "Authorization",
                                bearer(token)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Forbidden Security Product",
                                  "description": "Test 403",
                                  "category": "SECURITY",
                                  "price": 10.00
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message")
                        .value("Access denied"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldAllowAdminToCreateProduct()
            throws Exception {

        String token = jwtService.generateToken(
                "security-admin",
                List.of("ROLE_ADMIN")
        );

        mockMvc.perform(post("/api/products")
                        .header(
                                "Authorization",
                                bearer(token)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Authorized Security Product",
                                  "description": "Test autorisé",
                                  "category": "SECURITY",
                                  "price": 10.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name")
                        .value("Authorized Security Product"));
    }

    @Test
    void shouldAllowPublicProductReadingWithoutToken()
            throws Exception {

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}