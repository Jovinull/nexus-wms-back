package br.com.nexus.nexus_wms.application.service;

import br.com.nexus.nexus_wms.domain.entity.order.Order;
import br.com.nexus.nexus_wms.domain.entity.order.OrderItem;
import br.com.nexus.nexus_wms.domain.entity.order.PickingList;
import br.com.nexus.nexus_wms.domain.entity.wms.Product;
import br.com.nexus.nexus_wms.domain.repository.OrderRepository;
import br.com.nexus.nexus_wms.domain.repository.ProductRepository;
import br.com.nexus.nexus_wms.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    // TODO: Write explicit Picking List generation logic in the system before
    // testing
}
