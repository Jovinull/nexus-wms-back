package br.com.nexus.nexus_wms.application.service;

import br.com.nexus.nexus_wms.application.dto.wms.StockMovementDTO;
import br.com.nexus.nexus_wms.domain.entity.wms.Product;
import br.com.nexus.nexus_wms.domain.entity.wms.Stock;
import br.com.nexus.nexus_wms.domain.entity.wms.WarehouseLocation;
import br.com.nexus.nexus_wms.domain.repository.ProductRepository;
import br.com.nexus.nexus_wms.domain.repository.StockRepository;
import br.com.nexus.nexus_wms.domain.repository.WarehouseLocationRepository;
import br.com.nexus.nexus_wms.infrastructure.exception.BusinessException;
import br.com.nexus.nexus_wms.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseLocationRepository locationRepository;

    @InjectMocks
    private StockService stockService;

    private StockMovementDTO validDTO;
    private Product product;
    private WarehouseLocation location;

    @BeforeEach
    void setup() {
        validDTO = new StockMovementDTO();
        validDTO.setProductId(1L);
        validDTO.setLocationId(2L);
        validDTO.setQuantity(50);
        validDTO.setBatchNumber("LOTE-123");

        product = new Product();
        product.setId(1L);

        location = new WarehouseLocation();
        location.setId(2L);
    }

    @Test
    void receiveStock_WhenNewStock_ShouldCreateNewRegistro() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(location));
        when(stockRepository.findByProductId(1L)).thenReturn(java.util.Collections.emptyList());

        // Act
        stockService.receiveStock(validDTO);

        // Assert
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    void receiveStock_WhenStockExists_ShouldAddQuantity() {
        // Arrange
        Stock existingStock = new Stock();
        existingStock.setId(10L);
        existingStock.setProduct(product);
        existingStock.setLocation(location);
        existingStock.setQuantity(20);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(location));
        when(stockRepository.findByProductId(1L)).thenReturn(java.util.List.of(existingStock));

        // Act
        stockService.receiveStock(validDTO); // Add 50

        // Assert
        assertEquals(70, existingStock.getQuantity());
        verify(stockRepository).save(existingStock);
    }

    @Test
    void receiveStock_ProductNotFound_ShouldThrowError() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> stockService.receiveStock(validDTO));

        verify(stockRepository, never()).save(any());
    }

    @Test
    void removeStock_WhenEnoughQuantity_ShouldDeduct() {
        // Arrange
        Stock existingStock = new Stock();
        existingStock.setId(10L);
        existingStock.setProduct(product);
        existingStock.setLocation(location);
        existingStock.setQuantity(60); // We want to remove 50

        when(stockRepository.findByProductId(1L)).thenReturn(java.util.List.of(existingStock));

        // Act
        stockService.removeStock(validDTO);

        // Assert
        assertEquals(10, existingStock.getQuantity());
        verify(stockRepository).save(existingStock);
        verify(stockRepository, never()).delete(any());
    }

    @Test
    void removeStock_WhenExactQuantity_ShouldDeleteRecord() {
        // Arrange
        Stock existingStock = new Stock();
        existingStock.setId(10L);
        existingStock.setProduct(product);
        existingStock.setLocation(location);
        existingStock.setQuantity(50); // Exact match

        when(stockRepository.findByProductId(1L)).thenReturn(java.util.List.of(existingStock));

        // Act
        stockService.removeStock(validDTO); // Remove 50

        // Assert
        assertEquals(0, existingStock.getQuantity());
        verify(stockRepository).delete(existingStock); // Should be dropped from the shelf DB entirely
        verify(stockRepository, never()).save(any());
    }

    @Test
    void removeStock_WhenInsufficientQuantity_ShouldThrowBusinessException() {
        // Arrange
        Stock existingStock = new Stock();
        existingStock.setProduct(product);
        existingStock.setLocation(location);
        // Only 10 available, but DTO asks for 50
        existingStock.setQuantity(10);

        when(stockRepository.findByProductId(1L)).thenReturn(java.util.List.of(existingStock));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            stockService.removeStock(validDTO); // Wants 50
        });

        assertEquals("Quantidade insuficiente de estoque. Disponível: 10", exception.getMessage());
        verify(stockRepository, never()).save(any());
        verify(stockRepository, never()).delete(any());
    }
}
