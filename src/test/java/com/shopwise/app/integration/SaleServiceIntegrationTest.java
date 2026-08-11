package com.shopwise.app.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.shopwise.app.dto.request.CreateProductRequest;
import com.shopwise.app.dto.request.CreateSaleItemRequest;
import com.shopwise.app.dto.request.CreateSaleRequest;
import com.shopwise.app.dto.request.UpdateSaleRequest;
import com.shopwise.app.dto.response.ProductResponse;
import com.shopwise.app.dto.response.SaleResponse;
import com.shopwise.app.entity.Sale;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.repository.SaleItemRepository;
import com.shopwise.app.repository.SaleRepository;
import com.shopwise.app.service.ProductService;
import com.shopwise.app.service.SaleService;

import jakarta.persistence.EntityManager;

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

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private EntityManager entityManager;

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

    @Test
    void shouldUpdateExistingSaleAndReplaceItsItems() {
        ProductResponse oldProduct = createProduct(
                "Old Sale Product",
                "Produit présent avant la modification",
                "OLD",
                "50.00"
        );

        ProductResponse phone = createProduct(
                "Updated Sale Phone",
                "Smartphone utilisé après la modification",
                "SMARTPHONE",
                "999.99"
        );

        ProductResponse headphones = createProduct(
                "Updated Sale Headphones",
                "Casque utilisé après la modification",
                "AUDIO",
                "149.99"
        );

        SaleResponse created = saleService.create(
                saleRequest(
                        saleItem(oldProduct.getId(), 2)
                )
        );

        Long saleId = created.getId();
        LocalDateTime originalSaleDate = created.getSaleDate();

        assertNotNull(saleId);
        assertNotNull(originalSaleDate);
        assertEquals(1, created.getItems().size());
        assertBigDecimalEquals("100.00", created.getTotal());

        /*
         * On force l'écriture de la vente initiale dans la base
         */
        saleRepository.flush();

        Sale persistedBeforeUpdate = saleRepository.findById(saleId)
                .orElseThrow();

        assertEquals(1, persistedBeforeUpdate.getItems().size());

        Long oldSaleItemId = persistedBeforeUpdate
                .getItems()
                .get(0)
                .getId();

        assertNotNull(oldSaleItemId);
        assertTrue(saleItemRepository.existsById(oldSaleItemId));

        UpdateSaleRequest updateRequest = updateSaleRequest(
                saleItem(phone.getId(), 2),
                saleItem(headphones.getId(), 1)
        );

        SaleResponse updated = saleService.update(
                saleId,
                updateRequest
        );

        assertNotNull(updated);

        /*
         * La vente doit conserver son identifiant et sa date
         */
        assertEquals(saleId, updated.getId());
        assertEquals(originalSaleDate, updated.getSaleDate());

        /*
         * Nouveau calcul :
         * 999,99 x 2 = 1999,98
         * 149,99 x 1 = 149,99
         * Total = 2149,97
         */
        assertBigDecimalEquals("2149.97", updated.getTotal());

        assertNotNull(updated.getItems());
        assertEquals(2, updated.getItems().size());

        assertTrue(updated.getItems().stream()
                .anyMatch(item ->
                        item.getProductId().equals(phone.getId())
                                && item.getQuantity() == 2
                                && decimalEquals(
                                        "999.99",
                                        item.getUnitPrice()
                                )
                                && decimalEquals(
                                        "1999.98",
                                        item.getLineTotal()
                                )
                ));

        assertTrue(updated.getItems().stream()
                .anyMatch(item ->
                        item.getProductId().equals(headphones.getId())
                                && item.getQuantity() == 1
                                && decimalEquals(
                                        "149.99",
                                        item.getUnitPrice()
                                )
                                && decimalEquals(
                                        "149.99",
                                        item.getLineTotal()
                                )
                ));

        assertFalse(updated.getItems().stream()
                .anyMatch(item ->
                        item.getProductId().equals(oldProduct.getId())
                ));

        /*
         * On force Hibernate à exécuter les INSERT et DELETE,
         * puis on vide le contexte de persistance pour relire les
         * données réellement enregistrées dans H2
         */
        saleRepository.flush();
        entityManager.clear();

        Sale persistedAfterUpdate = saleRepository.findById(saleId)
                .orElseThrow();

        assertEquals(saleId, persistedAfterUpdate.getId());
        assertEquals(
                originalSaleDate,
                persistedAfterUpdate.getSaleDate()
        );

        assertBigDecimalEquals(
                "2149.97",
                persistedAfterUpdate.getTotal()
        );

        assertEquals(
                2,
                persistedAfterUpdate.getItems().size()
        );

        
        assertFalse(
                saleItemRepository.existsById(oldSaleItemId)
        );

        assertTrue(persistedAfterUpdate.getItems().stream()
                .anyMatch(item ->
                        item.getProduct()
                                .getId()
                                .equals(phone.getId())
                                && item.getQuantity() == 2
                                && decimalEquals(
                                        "999.99",
                                        item.getUnitPrice()
                                )
                                && decimalEquals(
                                        "1999.98",
                                        item.getLineTotal()
                                )
                ));

        assertTrue(persistedAfterUpdate.getItems().stream()
                .anyMatch(item ->
                        item.getProduct()
                                .getId()
                                .equals(headphones.getId())
                                && item.getQuantity() == 1
                                && decimalEquals(
                                        "149.99",
                                        item.getUnitPrice()
                                )
                                && decimalEquals(
                                        "149.99",
                                        item.getLineTotal()
                                )
                ));
    }

    @Test
    void shouldUseCurrentProductPriceDuringSaleUpdate() {
        ProductResponse oldProduct = createProduct(
                "Initial Product For Price Update",
                "Produit de la vente initiale",
                "TEST",
                "10.00"
        );

        ProductResponse replacementProduct = createProduct(
                "Replacement Product With Server Price",
                "Le prix doit être récupéré depuis la base",
                "TEST",
                "35.50"
        );

        SaleResponse created = saleService.create(
                saleRequest(
                        saleItem(oldProduct.getId(), 1)
                )
        );

        UpdateSaleRequest updateRequest = updateSaleRequest(
                saleItem(replacementProduct.getId(), 3)
        );

        SaleResponse updated = saleService.update(
                created.getId(),
                updateRequest
        );

        assertEquals(created.getId(), updated.getId());
        assertEquals(1, updated.getItems().size());

        assertBigDecimalEquals(
                "35.50",
                updated.getItems().get(0).getUnitPrice()
        );

        assertBigDecimalEquals(
                "106.50",
                updated.getItems().get(0).getLineTotal()
        );

        assertBigDecimalEquals(
                "106.50",
                updated.getTotal()
        );
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingUnknownSale() {
        ProductResponse product = createProduct(
                "Product For Unknown Sale Update",
                "Produit valide",
                "TEST",
                "20.00"
        );

        UpdateSaleRequest request = updateSaleRequest(
                saleItem(product.getId(), 1)
        );

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> saleService.update(999999L, request)
        );

        assertEquals(
                "Sale not found",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectSaleUpdateWhenProductDoesNotExist() {
        ProductResponse initialProduct = createProduct(
                "Initial Product Before Invalid Update",
                "Produit initial",
                "TEST",
                "25.00"
        );

        SaleResponse created = saleService.create(
                saleRequest(
                        saleItem(initialProduct.getId(), 2)
                )
        );

        UpdateSaleRequest request = updateSaleRequest(
                saleItem(999999L, 1)
        );

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> saleService.update(created.getId(), request)
        );

        assertEquals(
                "Product not found",
                exception.getMessage()
        );

        /*
         * Comme le test est transactionnel, l'exception doit provoquer
         * l'échec de la modification. On vérifie que la vente initiale
         * reste disponible avec son ancien contenu
         */
        entityManager.clear();

        SaleResponse persisted = saleService.getById(
                created.getId()
        );

        assertBigDecimalEquals("50.00", persisted.getTotal());
        assertEquals(1, persisted.getItems().size());

        assertEquals(
                initialProduct.getId(),
                persisted.getItems().get(0).getProductId()
        );

        assertEquals(
                2,
                persisted.getItems().get(0).getQuantity()
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

    private UpdateSaleRequest updateSaleRequest(
            CreateSaleItemRequest... items
    ) {
        UpdateSaleRequest request = new UpdateSaleRequest();
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

    private boolean decimalEquals(
            String expected,
            BigDecimal actual
    ) {
        return actual != null
                && new BigDecimal(expected).compareTo(actual) == 0;
    }
}