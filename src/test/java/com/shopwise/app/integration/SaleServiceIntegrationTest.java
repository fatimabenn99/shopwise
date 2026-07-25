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
import com.shopwise.app.dto.request.CreateSaleItemRequest;
import com.shopwise.app.dto.request.CreateSaleRequest;
import com.shopwise.app.dto.response.ProductResponse;
import com.shopwise.app.dto.response.SaleResponse;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.repository.SaleRepository;
import com.shopwise.app.service.ProductService;
import com.shopwise.app.service.SaleService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SaleServiceIntegrationTest {

    @Autowired
    private SaleService saleService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SaleRepository saleRepository;

    @Test
    void shouldCreatePersistAndRetrieveSale() {
        ProductResponse phone = createProduct(
                "Integration Phone Sale",
                "Smartphone",
                "SMARTPHONE",
                "999.99"
        );

        ProductResponse headphones = createProduct(
                "Integration Headphones Sale",
                "Casque audio",
                "AUDIO",
                "149.99"
        );

        CreateSaleRequest request = saleRequest(
                saleItem(phone.getId(), 2),
                saleItem(headphones.getId(), 1)
        );

        SaleResponse created = saleService.create(request);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertBigDecimalEquals("2149.97", created.getTotal());
        assertNotNull(created.getItems());
        assertEquals(2, created.getItems().size());

        assertTrue(saleRepository.existsById(created.getId()));

        SaleResponse persisted = saleService.getById(created.getId());

        assertEquals(created.getId(), persisted.getId());
        assertBigDecimalEquals("2149.97", persisted.getTotal());
        assertEquals(2, persisted.getItems().size());

        assertTrue(persisted.getItems().stream()
                .anyMatch(item ->
                        item.getProductId().equals(phone.getId())
                                && item.getQuantity() == 2
                ));

        assertTrue(persisted.getItems().stream()
                .anyMatch(item ->
                        item.getProductId().equals(headphones.getId())
                                && item.getQuantity() == 1
                ));
    }

    @Test
    void shouldUsePersistedProductPriceInsteadOfClientPrice() {
        ProductResponse product = createProduct(
                "Server Price Product",
                "Produit dont le prix vient du serveur",
                "TEST",
                "25.00"
        );

        SaleResponse created = saleService.create(
                saleRequest(saleItem(product.getId(), 3))
        );

        assertBigDecimalEquals("75.00", created.getTotal());
        assertEquals(1, created.getItems().size());
        assertBigDecimalEquals(
                "25.00",
                created.getItems().get(0).getUnitPrice()
        );
        assertBigDecimalEquals(
                "75.00",
                created.getItems().get(0).getLineTotal()
        );
    }

    @Test
    void shouldReturnSalesOrderedByNewestFirst() throws InterruptedException {
        ProductResponse product = createProduct(
                "Ordered Sale Product",
                "Produit utilisé pour tester le tri",
                "TEST",
                "10.00"
        );

        SaleResponse first = saleService.create(
                saleRequest(saleItem(product.getId(), 1))
        );

        /*
         * Petit délai pour garantir des dates différentes
         */
        Thread.sleep(10);

        SaleResponse second = saleService.create(
                saleRequest(saleItem(product.getId(), 2))
        );

        List<SaleResponse> sales = saleService.getAll();

        assertEquals(2, sales.size());
        assertEquals(second.getId(), sales.get(0).getId());
        assertEquals(first.getId(), sales.get(1).getId());
    }

    @Test
    void shouldRejectSaleWhenProductDoesNotExist() {
        CreateSaleRequest request = saleRequest(
                saleItem(999999L, 1)
        );

        assertThrows(
                NotFoundException.class,
                () -> saleService.create(request)
        );

        assertEquals(0, saleRepository.count());
    }

    @Test
    void shouldThrowNotFoundForUnknownSale() {
        assertThrows(
                NotFoundException.class,
                () -> saleService.getById(999999L)
        );
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

    private CreateSaleItemRequest saleItem(Long productId, int quantity) {
        CreateSaleItemRequest item = new CreateSaleItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private CreateSaleRequest saleRequest(
            CreateSaleItemRequest... items
    ) {
        CreateSaleRequest request = new CreateSaleRequest();
        request.setItems(List.of(items));
        return request;
    }

    private void assertBigDecimalEquals(
            String expected,
            BigDecimal actual
    ) {
        assertNotNull(actual);

        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual)
        );
    }
}