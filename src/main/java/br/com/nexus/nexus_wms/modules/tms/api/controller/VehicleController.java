package br.com.nexus.nexus_wms.modules.tms.api.controller;

import br.com.nexus.nexus_wms.modules.tms.api.dto.VehicleRequestDTO;
import br.com.nexus.nexus_wms.modules.tms.api.dto.VehicleResponseDTO;
import br.com.nexus.nexus_wms.modules.tms.infra.service.VehicleService;
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
@RequestMapping("/api/vehicles")
@Tag(name = "Veículos (TMS)", description = "Endpoints para gerenciamento de veículos da frota")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os veículos com paginação")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'OPERADOR_ESTOQUE')")
    public ResponseEntity<Page<VehicleResponseDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(vehicleService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar um veículo pelo ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'OPERADOR_ESTOQUE')")
    public ResponseEntity<VehicleResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Criar um novo veículo", description = "Apenas ADMIN ou GERENTE. A Placa deve ser única.")
    public ResponseEntity<VehicleResponseDTO> create(@Valid @RequestBody VehicleRequestDTO dto) {
        VehicleResponseDTO created = vehicleService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Atualizar um veículo existente", description = "Apenas ADMIN ou GERENTE podem editar.")
    public ResponseEntity<VehicleResponseDTO> update(@PathVariable Long id, @Valid @RequestBody VehicleRequestDTO dto) {
        return ResponseEntity.ok(vehicleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Excluir um veículo", description = "Apenas ADMIN ou GERENTE.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
