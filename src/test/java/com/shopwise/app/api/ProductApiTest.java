package com.shopwise.app.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class ProductApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProductService productService;

    @Test
    void shouldReturnAllProductsWithoutAuthentication() throws Exception {
        createProduct(
                "Public Product A",
                "Produit public A",
                "TEST",
                "10.00"
        );

        createProduct(
                "Public Product B",
                "Produit public B",
                "TEST",
                "20.00"
        );

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnProductByIdWithoutAuthentication() throws Exception {
        ProductResponse product = createProduct(
                "Public Product Details",
                "Description publique",
                "PUBLIC",
                "49.99"
        );

        mockMvc.perform(get("/api/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name")
                        .value("Public Product Details"))
                .andExpect(jsonPath("$.description")
                        .value("Description publique"))
                .andExpect(jsonPath("$.category").value("PUBLIC"))
                .andExpect(jsonPath("$.price").value(49.99));
    }

    @Test
    void shouldRejectProductCreationWithoutToken() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson("Unauthorized Product")))
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
    void shouldRejectProductCreationForUserRole() throws Exception {
        String userToken = token("api-user", "ROLE_USER");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson("Forbidden Product")))
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
    void shouldCreateProductForAdminRole() throws Exception {
        String adminToken = token("api-admin", "ROLE_ADMIN");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson("Admin Product")))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name")
                        .value("Admin Product"))
                .andExpect(jsonPath("$.description")
                        .value("API product description"))
                .andExpect(jsonPath("$.category")
                        .value("API_TEST"))
                .andExpect(jsonPath("$.price").value(25.50));
    }

    @Test
    void shouldRejectInvalidProductForAdminRole() throws Exception {
        String adminToken = token("api-admin", "ROLE_ADMIN");

        String invalidBody = """
                {
                  "name": "",
                  "description": "Invalid product",
                  "category": "",
                  "price": -10
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ));
    }

    @Test
    void shouldUpdateProductForAdminRole() throws Exception {
        ProductResponse product = createProduct(
                "Product Before Update",
                "Ancienne description",
                "OLD_CATEGORY",
                "30.00"
        );

        String adminToken = token("api-admin", "ROLE_ADMIN");

        String updateBody = """
                {
                  "name": "Product After Update",
                  "description": "Nouvelle description",
                  "category": "NEW_CATEGORY",
                  "price": 55.75
                }
                """;

        mockMvc.perform(put(
                        "/api/products/{id}",
                        product.getId()
                )
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name")
                        .value("Product After Update"))
                .andExpect(jsonPath("$.description")
                        .value("Nouvelle description"))
                .andExpect(jsonPath("$.category")
                        .value("NEW_CATEGORY"))
                .andExpect(jsonPath("$.price").value(55.75));
    }

    @Test
    void shouldDeleteProductForAdminRole() throws Exception {
        ProductResponse product = createProduct(
                "Product To Delete Through API",
                "Produit à supprimer",
                "TEST",
                "15.00"
        );

        String adminToken = token("api-admin", "ROLE_ADMIN");

        mockMvc.perform(delete(
                        "/api/products/{id}",
                        product.getId()
                )
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(
                        "/api/products/{id}",
                        product.getId()
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void shouldReturnNotFoundForUnknownProduct() throws Exception {
        mockMvc.perform(get("/api/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
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

    private String validProductJson(String name) {
        return """
                {
                  "name": "%s",
                  "description": "API product description",
                  "category": "API_TEST",
                  "price": 25.50
                }
                """.formatted(name);
    }

    private String token(String username, String role) {
        return jwtService.generateToken(
                username,
                List.of(role)
        );
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}