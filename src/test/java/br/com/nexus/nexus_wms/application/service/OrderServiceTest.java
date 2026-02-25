package br.com.nexus.nexus_wms.application.service;

import br.com.nexus.nexus_wms.application.dto.order.OrderRequestDTO;
import br.com.nexus.nexus_wms.domain.entity.order.Order;
import br.com.nexus.nexus_wms.domain.entity.order.OrderItem;
import br.com.nexus.nexus_wms.domain.entity.order.PickingList;
import br.com.nexus.nexus_wms.domain.entity.order.PickingListItem;
import br.com.nexus.nexus_wms.domain.entity.wms.Product;
import br.com.nexus.nexus_wms.domain.entity.wms.Stock;
import br.com.nexus.nexus_wms.domain.enums.OrderStatus;
import br.com.nexus.nexus_wms.domain.enums.PickingListStatus;
import br.com.nexus.nexus_wms.domain.repository.OrderRepository;
import br.com.nexus.nexus_wms.domain.repository.PickingListRepository;
import br.com.nexus.nexus_wms.domain.repository.ProductRepository;
import br.com.nexus.nexus_wms.domain.repository.StockRepository;
import br.com.nexus.nexus_wms.infrastructure.exception.BusinessException;
import br.com.nexus.nexus_wms.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PickingListRepository pickingListRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<PickingList> pickingListCaptor;

    private Order order;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setup() {
        product1 = new Product();
        product1.setId(10L);
        product1.setName("Computador");

        product2 = new Product();
        product2.setId(20L);
        product2.setName("Mouse");

        order = new Order();
        order.setId(100L);
        order.setStatus(OrderStatus.CRIADO);

        OrderItem item1 = new OrderItem();
        item1.setProduct(product1);
        item1.setQuantity(2);

        OrderItem item2 = new OrderItem();
        item2.setProduct(product2);
        item2.setQuantity(5);

        order.setItems(List.of(item1, item2));
    }

    @Test
    void generatePickingList_WhenEnoughStock_ShouldCreateListSuccessfully() {
        // Arrange
        // Stock from product 1
        Stock stockP1 = new Stock();
        stockP1.setId(1000L);
        stockP1.setProduct(product1);
        stockP1.setQuantity(10); // needs 2, has 10 = ok

        // Stock from product 2
        Stock stockP2 = new Stock();
        stockP2.setId(2000L);
        stockP2.setProduct(product2);
        stockP2.setQuantity(5); // needs 5, has 5 = ok

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(stockRepository.findByProductId(10L)).thenReturn(List.of(stockP1));
        when(stockRepository.findByProductId(20L)).thenReturn(List.of(stockP2));

        // Act
        orderService.generatePickingList(100L);

        // Assert
        verify(pickingListRepository).save(pickingListCaptor.capture());
        verify(orderRepository).save(order); // verify status was updated to EM_SEPARACAO

        PickingList savedList = pickingListCaptor.getValue();
        assertNotNull(savedList);
        assertEquals(order, savedList.getOrder());
        assertEquals(PickingListStatus.PENDENTE, savedList.getStatus());
        assertEquals(2, savedList.getItems().size());

        // Assert items picked map directly
        assertTrue(savedList.getItems().stream()
                .anyMatch(i -> i.getProduct().getId().equals(10L) && i.getQuantity().equals(2)));
        assertTrue(savedList.getItems().stream()
                .anyMatch(i -> i.getProduct().getId().equals(20L) && i.getQuantity().equals(5)));

        assertEquals(OrderStatus.EM_SEPARACAO, order.getStatus());
    }

    @Test
    void generatePickingList_WhenInsufficientStock_ShouldThrowException() {
        // Arrange
        // Stock from product 1 is insufficient
        Stock stockP1 = new Stock();
        stockP1.setId(1000L);
        stockP1.setProduct(product1);
        stockP1.setQuantity(1); // needs 2, has 1 = fail!

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(stockRepository.findByProductId(10L)).thenReturn(List.of(stockP1));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.generatePickingList(100L);
        });

        assertTrue(exception.getMessage().contains("Estoque insuficiente para o produto"));
        verify(pickingListRepository, never()).save(any());
    }

    @Test
    void generatePickingList_WhenOrderNotCriado_ShouldThrowException() {
        // Arrange
        order.setStatus(OrderStatus.EM_SEPARACAO); // invalid state
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.generatePickingList(100L);
        });

        assertEquals("O pedido não está no status CRIADO para gerar lista de separação.", exception.getMessage());
        verify(stockRepository, never()).findByProductId(any());
        verify(pickingListRepository, never()).save(any());
    }
}
