package br.com.nexus.nexus_wms.application.service;

import br.com.nexus.nexus_wms.application.dto.order.OrderItemRequestDTO;
import br.com.nexus.nexus_wms.application.dto.order.OrderItemResponseDTO;
import br.com.nexus.nexus_wms.application.dto.order.OrderRequestDTO;
import br.com.nexus.nexus_wms.application.dto.order.OrderResponseDTO;
import br.com.nexus.nexus_wms.application.dto.order.OrderStatusUpdateDTO;
import br.com.nexus.nexus_wms.domain.entity.order.Order;
import br.com.nexus.nexus_wms.domain.entity.order.OrderItem;
import br.com.nexus.nexus_wms.domain.entity.wms.Product;
import br.com.nexus.nexus_wms.domain.enums.OrderStatus;
import br.com.nexus.nexus_wms.domain.repository.OrderRepository;
import br.com.nexus.nexus_wms.domain.repository.ProductRepository;
import br.com.nexus.nexus_wms.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponseDTO create(OrderRequestDTO dto) {
        Order order = new Order();
        order.setCustomerName(dto.getCustomerName());
        order.setCustomerAddress(dto.getCustomerAddress());
        order.setCustomerPhone(dto.getCustomerPhone());
        order.setNotes(dto.getNotes());
        order.setStatus(OrderStatus.CRIADO);

        BigDecimal totalValue = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemRequestDTO itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Produto não encontrado para o ID: " + itemDto.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());

            // Snapshot do preço no momento da criação do pedido
            orderItem.setUnitPrice(product.getUnitPrice());

            BigDecimal subTotal = product.getUnitPrice().multiply(new BigDecimal(itemDto.getQuantity()));
            totalValue = totalValue.add(subTotal);

            items.add(orderItem);
        }

        order.setTotalValue(totalValue);
        order.setItems(items);

        order = orderRepository.save(order);
        // O JPA Cascading salva os orderItems automaticamente por causa de
        // CascadeType.ALL

        return toResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO findById(Long id) {
        Order order = getOrderOrThrow(id);
        return toResponseDTO(order);
    }

    @Transactional
    public OrderResponseDTO updateStatus(Long id, OrderStatusUpdateDTO dto) {
        Order order = getOrderOrThrow(id);

        // Regras de negócio podem ser adicionadas aqui
        // Ex: Não permitir voltar status, ou não permitir transição inválida da máquina
        // de estado.

        order.setStatus(dto.getStatus());
        order = orderRepository.save(order);

        return toResponseDTO(order);
    }

    private Order getOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado para o ID: " + id));
    }

    private OrderResponseDTO toResponseDTO(Order order) {
        List<OrderItemResponseDTO> itemDTOs = order.getItems().stream().map(item -> {
            BigDecimal subTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            return new OrderItemResponseDTO(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getProduct().getSku(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    subTotal);
        }).toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerAddress(),
                order.getCustomerPhone(),
                order.getTotalValue(),
                order.getStatus(),
                order.getNotes(),
                order.getVersion(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemDTOs);
    }
}
