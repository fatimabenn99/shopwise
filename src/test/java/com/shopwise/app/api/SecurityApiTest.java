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
import com.shopwise.app.dto.request.CreateSaleItemRequest;
import com.shopwise.app.dto.request.CreateSaleRequest;
import com.shopwise.app.dto.response.ProductResponse;
import com.shopwise.app.dto.response.SaleResponse;
import com.shopwise.app.service.ProductService;
import com.shopwise.app.service.SaleService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SaleService saleService;

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

    @Test
    void shouldRejectSaleUpdateWithoutToken()
            throws Exception {

        mockMvc.perform(put("/api/sales/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {
                                      "productId": 1,
                                      "quantity": 2
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
                        .value("Authentication required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldAllowUserToUpdateSale()
            throws Exception {

        ProductResponse initialProduct = createProduct(
                "Security User Initial Product",
                "Produit initial de la vente",
                "SECURITY",
                "20.00"
        );

        ProductResponse replacementProduct = createProduct(
                "Security User Replacement Product",
                "Produit utilisé pendant la modification",
                "SECURITY",
                "35.00"
        );

        SaleResponse sale = createSale(
                initialProduct.getId(),
                1
        );

        String token = jwtService.generateToken(
                "security-sale-user",
                List.of("ROLE_USER")
        );

        mockMvc.perform(put(
                        "/api/sales/{id}",
                        sale.getId()
                )
                        .header(
                                "Authorization",
                                bearer(token)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {
                                      "productId": %d,
                                      "quantity": 2
                                    }
                                  ]
                                }
                                """.formatted(
                                replacementProduct.getId()
                        )))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id")
                        .value(sale.getId()))
                .andExpect(jsonPath("$.total")
                        .value(70.00))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()")
                        .value(1))
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
    void shouldAllowAdminToUpdateSale()
            throws Exception {

        ProductResponse initialProduct = createProduct(
                "Security Admin Initial Product",
                "Produit initial de la vente",
                "SECURITY",
                "15.00"
        );

        ProductResponse replacementProduct = createProduct(
                "Security Admin Replacement Product",
                "Produit utilisé pendant la modification",
                "SECURITY",
                "50.00"
        );

        SaleResponse sale = createSale(
                initialProduct.getId(),
                2
        );

        String token = jwtService.generateToken(
                "security-sale-admin",
                List.of("ROLE_ADMIN")
        );

        mockMvc.perform(put(
                        "/api/sales/{id}",
                        sale.getId()
                )
                        .header(
                                "Authorization",
                                bearer(token)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {
                                      "productId": %d,
                                      "quantity": 3
                                    }
                                  ]
                                }
                                """.formatted(
                                replacementProduct.getId()
                        )))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id")
                        .value(sale.getId()))
                .andExpect(jsonPath("$.total")
                        .value(150.00))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()")
                        .value(1))
                .andExpect(jsonPath("$.items[0].productId")
                        .value(replacementProduct.getId()))
                .andExpect(jsonPath("$.items[0].quantity")
                        .value(3))
                .andExpect(jsonPath("$.items[0].unitPrice")
                        .value(50.00))
                .andExpect(jsonPath("$.items[0].lineTotal")
                        .value(150.00));
    }

    private ProductResponse createProduct(
            String name,
            String description,
            String category,
            String price
    ) {
        CreateProductRequest request =
                new CreateProductRequest();

        request.setName(name);
        request.setDescription(description);
        request.setCategory(category);
        request.setPrice(new BigDecimal(price));

        return productService.create(request);
    }

    private SaleResponse createSale(
            Long productId,
            int quantity
    ) {
        CreateSaleItemRequest item =
                new CreateSaleItemRequest();

        item.setProductId(productId);
        item.setQuantity(quantity);

        CreateSaleRequest request =
                new CreateSaleRequest();

        request.setItems(List.of(item));

        return saleService.create(request);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}