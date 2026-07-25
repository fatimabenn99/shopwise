package com.shopwise.app.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.shopwise.app.dto.request.CreateProductRequest;
import com.shopwise.app.dto.request.UpdateProductRequest;
import com.shopwise.app.dto.response.ProductResponse;
import com.shopwise.app.exception.ConflictException;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.service.ProductService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Test
    void shouldCreateAndPersistProduct() {
        CreateProductRequest request = createRequest(
                "Integration Phone",
                "Smartphone utilisé pour les tests",
                "SMARTPHONE",
                "899.99"
        );

        ProductResponse created = productService.create(request);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Integration Phone", created.getName());
        assertEquals("SMARTPHONE", created.getCategory());
        assertBigDecimalEquals("899.99", created.getPrice());

        ProductResponse persisted = productService.getById(created.getId());

        assertEquals(created.getId(), persisted.getId());
        assertEquals("Integration Phone", persisted.getName());
        assertBigDecimalEquals("899.99", persisted.getPrice());
    }

    @Test
    void shouldReturnAllPersistedProducts() {
        productService.create(createRequest(
                "Integration Product A",
                "Premier produit",
                "TEST",
                "10.00"
        ));

        productService.create(createRequest(
                "Integration Product B",
                "Deuxième produit",
                "TEST",
                "20.00"
        ));

        List<ProductResponse> products = productService.getAll();

        assertEquals(2, products.size());

        assertTrue(products.stream()
                .anyMatch(product -> "Integration Product A".equals(product.getName())));

        assertTrue(products.stream()
                .anyMatch(product -> "Integration Product B".equals(product.getName())));
    }

    @Test
    void shouldUpdatePersistedProduct() {
        ProductResponse created = productService.create(createRequest(
                "Original Product",
                "Description originale",
                "OLD_CATEGORY",
                "50.00"
        ));

        UpdateProductRequest updateRequest = new UpdateProductRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setDescription("Description mise à jour");
        updateRequest.setCategory("NEW_CATEGORY");
        updateRequest.setPrice(new BigDecimal("75.50"));

        ProductResponse updated =
                productService.update(created.getId(), updateRequest);

        assertEquals(created.getId(), updated.getId());
        assertEquals("Updated Product", updated.getName());
        assertEquals("Description mise à jour", updated.getDescription());
        assertEquals("NEW_CATEGORY", updated.getCategory());
        assertBigDecimalEquals("75.50", updated.getPrice());

        ProductResponse persisted = productService.getById(created.getId());

        assertEquals("Updated Product", persisted.getName());
        assertBigDecimalEquals("75.50", persisted.getPrice());
    }

    @Test
    void shouldDeletePersistedProduct() {
        ProductResponse created = productService.create(createRequest(
                "Product To Delete",
                "Produit destiné à être supprimé",
                "TEST",
                "15.00"
        ));

        productService.delete(created.getId());

        assertThrows(
                NotFoundException.class,
                () -> productService.getById(created.getId())
        );
    }

    @Test
    void shouldRejectDuplicateProductNameIgnoringCase() {
        productService.create(createRequest(
                "Unique Product",
                "Premier produit",
                "TEST",
                "10.00"
        ));

        CreateProductRequest duplicate = createRequest(
                "unique product",
                "Produit en doublon",
                "TEST",
                "12.00"
        );

        assertThrows(
                ConflictException.class,
                () -> productService.create(duplicate)
        );
    }

    @Test
    void shouldThrowNotFoundForUnknownProduct() {
        assertThrows(
                NotFoundException.class,
                () -> productService.getById(999999L)
        );
    }

    private CreateProductRequest createRequest(
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
        return request;
    }

    private void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertNotNull(actual);

        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual)
        );
    }
}