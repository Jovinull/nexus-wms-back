package br.com.nexus.nexus_wms.modules.wms.api.controller;

import br.com.nexus.nexus_wms.modules.wms.api.dto.WarehouseLocationRequestDTO;
import br.com.nexus.nexus_wms.modules.wms.api.dto.WarehouseLocationResponseDTO;
import br.com.nexus.nexus_wms.modules.wms.infra.service.WarehouseLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warehouse-locations")
@Tag(name = "Localizações (Warehouse)", description = "Endpoints para gerenciamento de endereços no armazém (Corredor, Prateleira, etc)")
public class WarehouseLocationController {

    private final WarehouseLocationService locationService;

    public WarehouseLocationController(WarehouseLocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    @Operation(summary = "Listar todas as localizações com paginação")
    public ResponseEntity<Page<WarehouseLocationResponseDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(locationService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar uma localização pelo ID")
    public ResponseEntity<WarehouseLocationResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Criar uma nova localização", description = "Apenas ADMIN ou GERENTE. Endereço (Corredor, Prateleira, Nível, Vão) deve ser único.")
    public ResponseEntity<WarehouseLocationResponseDTO> create(@Valid @RequestBody WarehouseLocationRequestDTO dto) {
        WarehouseLocationResponseDTO created = locationService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Atualizar uma localização existente", description = "Apenas ADMIN ou GERENTE podem editar.")
    public ResponseEntity<WarehouseLocationResponseDTO> update(@PathVariable Long id,
            @Valid @RequestBody WarehouseLocationRequestDTO dto) {
        return ResponseEntity.ok(locationService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Excluir uma localização", description = "Apenas ADMIN ou GERENTE. Bloqueado se houver estoque vinculado.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
