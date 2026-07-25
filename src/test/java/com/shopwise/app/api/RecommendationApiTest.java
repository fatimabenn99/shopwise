package com.shopwise.app.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import com.shopwise.app.dto.request.CreateProductRequest;
import com.shopwise.app.dto.response.ProductResponse;
import com.shopwise.app.service.ProductService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RecommendationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProductService productService;

    @Test
    void shouldRejectRecommendationsWithoutToken() throws Exception {
        mockMvc.perform(get(
                        "/api/recommendations/products/{productId}",
                        1L
                ))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Authentication required"));
    }

    @Test
    void shouldReturnRecommendationsForUserRole() throws Exception {
        ProductResponse target = createProduct(
                "Recommendation Target Phone",
                "Smartphone Android avec caméra OLED",
                "SMARTPHONE",
                "800.00"
        );

        ProductResponse similar = createProduct(
                "Recommendation Similar Phone",
                "Smartphone Android premium avec caméra OLED",
                "SMARTPHONE",
                "950.00"
        );

        createProduct(
                "Recommendation Laptop",
                "Ordinateur portable professionnel",
                "LAPTOP",
                "1500.00"
        );

        mockMvc.perform(get(
                        "/api/recommendations/products/{productId}",
                        target.getId()
                )
                        .header(
                                "Authorization",
                                bearer(userToken())
                        ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productId")
                        .value(similar.getId()))
                .andExpect(jsonPath("$[0].similarityScore")
                        .isNumber())
                .andExpect(jsonPath("$[0].reason")
                        .isNotEmpty());
    }

    @Test
    void shouldReturnRecommendationsForAdminRole() throws Exception {
        ProductResponse target = createProduct(
                "Admin Recommendation Target",
                "Produit cible",
                "TEST",
                "100.00"
        );

        createProduct(
                "Admin Recommendation Candidate",
                "Produit candidat",
                "TEST",
                "110.00"
        );

        mockMvc.perform(get(
                        "/api/recommendations/products/{productId}",
                        target.getId()
                )
                        .header(
                                "Authorization",
                                bearer(adminToken())
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldReturnNotFoundForUnknownTargetProduct() throws Exception {
        mockMvc.perform(get(
                        "/api/recommendations/products/999999"
                )
                        .header(
                                "Authorization",
                                bearer(userToken())
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    private ProductResponse createProduct(
            String name,
            String description,
            String category,
            String price
    ) {
        CreateProductRequest request = new CreateProductRequest();

        request.setName(name);
        request.setDescription(description);
        request.setCategory(category);
        request.setPrice(new BigDecimal(price));

        return productService.create(request);
    }

    private String userToken() {
        return jwtService.generateToken(
                "recommendation-api-user",
                List.of("ROLE_USER")
        );
    }

    private String adminToken() {
        return jwtService.generateToken(
                "recommendation-api-admin",
                List.of("ROLE_ADMIN")
        );
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}