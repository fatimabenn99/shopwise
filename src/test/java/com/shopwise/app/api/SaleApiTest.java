package com.shopwise.app.api;

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
class SaleApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProductService productService;

    @Test
    void shouldRejectSaleCreationWithoutToken() throws Exception {
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {
                                      "productId": 1,
                                      "quantity": 1
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Authentication required"));
    }

    @Test
    void shouldCreateSaleForUserRole() throws Exception {
        ProductResponse product = createProduct(
                "Sale API Product",
                "Produit utilisé pour une vente API",
                "SALE_TEST",
                "25.00"
        );

        String userToken = userToken();

        String requestBody = """
                {
                  "items": [
                    {
                      "productId": %d,
                      "quantity": 3
                    }
                  ]
                }
                """.formatted(product.getId());

        mockMvc.perform(post("/api/sales")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.total").value(75.00))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId")
                        .value(product.getId()))
                .andExpect(jsonPath("$.items[0].quantity")
                        .value(3))
                .andExpect(jsonPath("$.items[0].unitPrice")
                        .value(25.00))
                .andExpect(jsonPath("$.items[0].lineTotal")
                        .value(75.00));
    }

    @Test
    void shouldCreateSaleWithSeveralProducts() throws Exception {
        ProductResponse firstProduct = createProduct(
                "Sale Multiple Product A",
                "Premier produit",
                "SALE_TEST",
                "10.00"
        );

        ProductResponse secondProduct = createProduct(
                "Sale Multiple Product B",
                "Deuxième produit",
                "SALE_TEST",
                "15.00"
        );

        String requestBody = """
                {
                  "items": [
                    {
                      "productId": %d,
                      "quantity": 2
                    },
                    {
                      "productId": %d,
                      "quantity": 3
                    }
                  ]
                }
                """.formatted(
                firstProduct.getId(),
                secondProduct.getId()
        );

        mockMvc.perform(post("/api/sales")
                        .header("Authorization", bearer(userToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(65.00))
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void shouldReturnSalesForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/sales")
                        .header(
                                "Authorization",
                                bearer(userToken())
                        ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldRejectSalesListWithoutToken() throws Exception {
        mockMvc.perform(get("/api/sales"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Authentication required"));
    }

    @Test
    void shouldRejectSaleWithEmptyItems() throws Exception {
        mockMvc.perform(post("/api/sales")
                        .header(
                                "Authorization",
                                bearer(userToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ));
    }

    @Test
    void shouldRejectSaleWithInvalidQuantity() throws Exception {
        ProductResponse product = createProduct(
                "Invalid Quantity Product",
                "Produit pour quantité invalide",
                "SALE_TEST",
                "10.00"
        );

        String requestBody = """
                {
                  "items": [
                    {
                      "productId": %d,
                      "quantity": 0
                    }
                  ]
                }
                """.formatted(product.getId());

        mockMvc.perform(post("/api/sales")
                        .header(
                                "Authorization",
                                bearer(userToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/sales")
                        .header(
                                "Authorization",
                                bearer(userToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {
                                      "productId": 999999,
                                      "quantity": 1
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void shouldReturnNotFoundForUnknownSale() throws Exception {
        mockMvc.perform(get("/api/sales/999999")
                        .header(
                                "Authorization",
                                bearer(userToken())
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void shouldUpdateSaleForUserRole() throws Exception {
        ProductResponse initialProduct = createProduct(
                "Sale Update Initial Product",
                "Produit initial",
                "SALE_TEST",
                "20.00"
        );

        ProductResponse replacementProduct = createProduct(
                "Sale Update Replacement Product",
                "Produit de remplacement",
                "SALE_TEST",
                "35.00"
        );

        String createBody = """
                {
                  "items": [
                    {
                      "productId": %d,
                      "quantity": 1
                    }
                  ]
                }
                """.formatted(initialProduct.getId());

        String createResponse = mockMvc.perform(post("/api/sales")
                        .header(
                                "Authorization",
                                bearer(userToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        com.fasterxml.jackson.databind.ObjectMapper objectMapper =
                new com.fasterxml.jackson.databind.ObjectMapper();

        Long saleId = objectMapper
                .readTree(createResponse)
                .get("id")
                .asLong();

        String updateBody = """
                {
                  "items": [
                    {
                      "productId": %d,
                      "quantity": 2
                    }
                  ]
                }
                """.formatted(replacementProduct.getId());

        mockMvc.perform(put(
                        "/api/sales/{id}",
                        saleId
                )
                        .header(
                                "Authorization",
                                bearer(userToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").value(saleId))
                .andExpect(jsonPath("$.total").value(70.00))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId")
                        .value(replacementProduct.getId()))
                .andExpect(jsonPath("$.items[0].quantity")
                        .value(2))
                .andExpect(jsonPath("$.items[0].unitPrice")
                        .value(35.00))
                .andExpect(jsonPath("$.items[0].lineTotal")
                        .value(70.00));
    }

    @Test
    void shouldRejectSaleUpdateWithoutToken() throws Exception {
        mockMvc.perform(put(
                        "/api/sales/{id}",
                        1L
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {
                                      "productId": 1,
                                      "quantity": 1
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Authentication required"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownSale() throws Exception {
        ProductResponse product = createProduct(
                "Unknown Sale Update Product",
                "Produit valide",
                "SALE_TEST",
                "15.00"
        );

        String updateBody = """
                {
                  "items": [
                    {
                      "productId": %d,
                      "quantity": 1
                    }
                  ]
                }
                """.formatted(product.getId());

        mockMvc.perform(put(
                        "/api/sales/{id}",
                        999999L
                )
                        .header(
                                "Authorization",
                                bearer(userToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Sale not found"));
    }

    @Test
    void shouldRejectSaleUpdateWithInvalidQuantity() throws Exception {
        ProductResponse product = createProduct(
                "Invalid Sale Update Quantity Product",
                "Produit pour modification invalide",
                "SALE_TEST",
                "10.00"
        );

        String createBody = """
                {
                  "items": [
                    {
                      "productId": %d,
                      "quantity": 1
                    }
                  ]
                }
                """.formatted(product.getId());

        String createResponse = mockMvc.perform(post("/api/sales")
                        .header(
                                "Authorization",
                                bearer(userToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        com.fasterxml.jackson.databind.ObjectMapper objectMapper =
                new com.fasterxml.jackson.databind.ObjectMapper();

        Long saleId = objectMapper
                .readTree(createResponse)
                .get("id")
                .asLong();

        String invalidBody = """
                {
                  "items": [
                    {
                      "productId": %d,
                      "quantity": 0
                    }
                  ]
                }
                """.formatted(product.getId());

        mockMvc.perform(put(
                        "/api/sales/{id}",
                        saleId
                )
                        .header(
                                "Authorization",
                                bearer(userToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ));
    }
    
    @Test
    void shouldReturnNotFoundWhenUpdatingSaleWithUnknownProduct()
            throws Exception {

        ProductResponse product = createProduct(
                "Sale Unknown Product Initial",
                "Produit initial",
                "SALE_TEST",
                "20.00"
        );

        String createBody = """
                {
                  "items": [
                    {
                      "productId": %d,
                      "quantity": 1
                    }
                  ]
                }
                """.formatted(product.getId());

        String createResponse = mockMvc.perform(post("/api/sales")
                        .header(
                                "Authorization",
                                bearer(userToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        com.fasterxml.jackson.databind.ObjectMapper objectMapper =
                new com.fasterxml.jackson.databind.ObjectMapper();

        Long saleId = objectMapper
                .readTree(createResponse)
                .get("id")
                .asLong();

        mockMvc.perform(put(
                        "/api/sales/{id}",
                        saleId
                )
                        .header(
                                "Authorization",
                                bearer(userToken())
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {
                                      "productId": 999999,
                                      "quantity": 1
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Product not found"));
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
                "sale-api-user",
                List.of("ROLE_USER")
        );
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}