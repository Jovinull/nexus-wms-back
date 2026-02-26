package br.com.nexus.nexus_wms.modules.orders.api.controller;

import br.com.nexus.nexus_wms.modules.orders.api.dto.OrderRequestDTO;
import br.com.nexus.nexus_wms.modules.orders.api.dto.OrderResponseDTO;
import br.com.nexus.nexus_wms.modules.orders.api.dto.OrderStatusUpdateDTO;
import br.com.nexus.nexus_wms.modules.orders.infra.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Pedidos (Orders)", description = "Endpoints para gerenciamento de pedidos e máquina de estados")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os pedidos com paginação")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'OPERADOR_ESTOQUE')")
    public ResponseEntity<Page<OrderResponseDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(orderService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar um pedido pelo ID, exibindo os Snapshot de preços e Skus")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'OPERADOR_ESTOQUE')")
    public ResponseEntity<OrderResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Criar um novo pedido com itens", description = "Inicia as entradas como status CRIADO. Ele também gera e fixa os Snapshots de Valores para cada item baseando na tabela de produtos base naquele momento.")
    public ResponseEntity<OrderResponseDTO> create(@Valid @RequestBody OrderRequestDTO dto) {
        OrderResponseDTO created = orderService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'OPERADOR_ESTOQUE')")
    @Operation(summary = "Atualizar o status e seguir a máquina de estados do pacote (Em_Separacão, Transito, etc.)")
    public ResponseEntity<OrderResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateDTO dto) {

        return ResponseEntity.ok(orderService.updateStatus(id, dto));
    }
}
