package br.com.nexus.nexus_wms.infrastructure.controller;

import br.com.nexus.nexus_wms.application.dto.tms.DriverRequestDTO;
import br.com.nexus.nexus_wms.application.dto.tms.DriverResponseDTO;
import br.com.nexus.nexus_wms.application.service.DriverService;
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
@RequestMapping("/api/drivers")
@Tag(name = "Motoristas (TMS)", description = "Endpoints para gerenciamento de motoristas no sistema de transporte")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os motoristas com paginação")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'OPERADOR_ESTOQUE')")
    public ResponseEntity<Page<DriverResponseDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(driverService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar um motorista pelo ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'OPERADOR_ESTOQUE')")
    public ResponseEntity<DriverResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(driverService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Criar um novo motorista", description = "Apenas ADMIN ou GERENTE. A CNH deve ser única.")
    public ResponseEntity<DriverResponseDTO> create(@Valid @RequestBody DriverRequestDTO dto) {
        DriverResponseDTO created = driverService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Atualizar um motorista existente", description = "Apenas ADMIN ou GERENTE podem editar.")
    public ResponseEntity<DriverResponseDTO> update(@PathVariable Long id, @Valid @RequestBody DriverRequestDTO dto) {
        return ResponseEntity.ok(driverService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Excluir um motorista", description = "Apenas ADMIN ou GERENTE.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        driverService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
