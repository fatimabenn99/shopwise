package com.shopwise.app.regression;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

/**
 * Tests de non-régression des fonctionnalités historiques du catalogue.
 *
 * Ces tests vérifient que l'ajout des modules Ventes, Sécurité et
 * Recommandations n'a pas cassé les opérations existantes sur les produits
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductNonRegressionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldPreserveCompleteProductLifecycle() throws Exception {
        String adminToken = adminToken();

        /*
         * Création d'un produit
         */
        String creationResponse = mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Regression Product",
                                  "description": "Produit historique de test",
                                  "category": "REGRESSION",
                                  "price": 49.99
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name")
                        .value("Regression Product"))
                .andExpect(jsonPath("$.description")
                        .value("Produit historique de test"))
                .andExpect(jsonPath("$.category")
                        .value("REGRESSION"))
                .andExpect(jsonPath("$.price")
                        .value(49.99))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long productId = extractId(creationResponse);

        /*
         * Consultation du produit sans authentification
         */
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name")
                        .value("Regression Product"))
                .andExpect(jsonPath("$.category")
                        .value("REGRESSION"))
                .andExpect(jsonPath("$.price")
                        .value(49.99));

        /*
         * Présence du produit dans la liste
         */
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath(
                        "$[?(@.id == %d)]".formatted(productId)
                ).exists());

        /*
         * Mise à jour du produit
         */
        mockMvc.perform(put("/api/products/{id}", productId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Regression Product Updated",
                                  "description": "Description mise à jour",
                                  "category": "REGRESSION_UPDATED",
                                  "price": 59.99
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name")
                        .value("Regression Product Updated"))
                .andExpect(jsonPath("$.description")
                        .value("Description mise à jour"))
                .andExpect(jsonPath("$.category")
                        .value("REGRESSION_UPDATED"))
                .andExpect(jsonPath("$.price")
                        .value(59.99));

        /*
         * Vérification de la persistance de la mise à jour
         */
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Regression Product Updated"))
                .andExpect(jsonPath("$.category")
                        .value("REGRESSION_UPDATED"))
                .andExpect(jsonPath("$.price")
                        .value(59.99));

        /*
         * Suppression du produit
         */
        mockMvc.perform(delete("/api/products/{id}", productId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());

        /*
         * Vérification de la suppression
         */
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void shouldPreservePublicProductReading() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldPreserveProductValidationRules() throws Exception {
        mockMvc.perform(post("/api/products")
                        .header(
                                "Authorization",
                                bearer(adminToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "description": "Produit invalide",
                                  "category": "",
                                  "price": -5
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ));
    }

    @Test
    void shouldPreserveNotFoundBehaviour() throws Exception {
        mockMvc.perform(get("/api/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void shouldPreserveProtectionOfProductWritingOperations()
            throws Exception {

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Unauthenticated Product",
                                  "description": "Produit non autorisé",
                                  "category": "SECURITY",
                                  "price": 10.00
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        String userToken = jwtService.generateToken(
                "regression-user",
                List.of("ROLE_USER")
        );

        mockMvc.perform(post("/api/products")
                        .header(
                                "Authorization",
                                bearer(userToken)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Forbidden Product",
                                  "description": "Produit interdit",
                                  "category": "SECURITY",
                                  "price": 10.00
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    private String adminToken() {
        return jwtService.generateToken(
                "regression-admin",
                List.of("ROLE_ADMIN")
        );
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    /*
     * Extraction simple de l'identifiant retourné sous la forme : {"id":1,...}
     */
    private Long extractId(String json) {
        String idMarker = "\"id\":";
        int idStart = json.indexOf(idMarker);

        if (idStart < 0) {
            throw new IllegalArgumentException(
                    "Le JSON ne contient pas d'identifiant : " + json
            );
        }

        idStart += idMarker.length();

        int idEnd = idStart;

        while (idEnd < json.length()
                && Character.isDigit(json.charAt(idEnd))) {
            idEnd++;
        }

        return Long.valueOf(json.substring(idStart, idEnd));
    }
}