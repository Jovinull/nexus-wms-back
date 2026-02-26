package br.com.nexus.nexus_wms.modules.wms.infra.service;

import br.com.nexus.nexus_wms.modules.wms.api.dto.WarehouseLocationRequestDTO;
import br.com.nexus.nexus_wms.modules.wms.api.dto.WarehouseLocationResponseDTO;
import br.com.nexus.nexus_wms.modules.wms.domain.entity.WarehouseLocation;
import br.com.nexus.nexus_wms.modules.wms.domain.repository.WarehouseLocationRepository;
import br.com.nexus.nexus_wms.core.exception.BusinessException;
import br.com.nexus.nexus_wms.core.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseLocationService {

    private final WarehouseLocationRepository locationRepository;

    public WarehouseLocationService(WarehouseLocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Transactional
    public WarehouseLocationResponseDTO create(WarehouseLocationRequestDTO dto) {
        if (locationRepository
                .findByAisleAndShelfAndLevelAndBin(dto.getAisle(), dto.getShelf(), dto.getLevel(), dto.getBin())
                .isPresent()) {
            throw new BusinessException(
                    "Já existe uma localização com este endereçamento (Corredor, Prateleira, Nível, Vão).");
        }

        WarehouseLocation location = new WarehouseLocation();
        mapDtoToEntity(dto, location);

        // Ativo por padrão se não for enviado
        if (dto.getActive() == null) {
            location.setActive(true);
        }

        location = locationRepository.save(location);
        return toResponseDTO(location);
    }

    @Transactional(readOnly = true)
    public Page<WarehouseLocationResponseDTO> findAll(Pageable pageable) {
        return locationRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public WarehouseLocationResponseDTO findById(Long id) {
        WarehouseLocation location = getLocationOrThrow(id);
        return toResponseDTO(location);
    }

    @Transactional
    public WarehouseLocationResponseDTO update(Long id, WarehouseLocationRequestDTO dto) {
        WarehouseLocation location = getLocationOrThrow(id);

        // Verifica unicidade antes de atualizar (se os campos chaves mudaram e o novo
        // já existe)
        locationRepository
                .findByAisleAndShelfAndLevelAndBin(dto.getAisle(), dto.getShelf(), dto.getLevel(), dto.getBin())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException(
                                "Já existe uma outra localização com este endereçamento (Corredor, Prateleira, Nível, Vão).");
                    }
                });

        mapDtoToEntity(dto, location);

        if (dto.getActive() != null) {
            location.setActive(dto.getActive());
        }

        location = locationRepository.save(location);
        return toResponseDTO(location);
    }

    @Transactional
    public void delete(Long id) {
        WarehouseLocation location = getLocationOrThrow(id);
        if (!location.getStocks().isEmpty()) {
            throw new BusinessException(
                    "Não é possível excluir uma localização que possui registros de estoque. Tente inativá-la.");
        }
        locationRepository.delete(location);
    }

    private WarehouseLocation getLocationOrThrow(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada para o ID: " + id));
    }

    private void mapDtoToEntity(WarehouseLocationRequestDTO dto, WarehouseLocation location) {
        location.setAisle(dto.getAisle().toUpperCase());
        location.setShelf(dto.getShelf().toUpperCase());
        location.setLevel(dto.getLevel().toUpperCase());
        location.setBin(dto.getBin().toUpperCase());
        location.setMaxWeightKg(dto.getMaxWeightKg());
    }

    private WarehouseLocationResponseDTO toResponseDTO(WarehouseLocation location) {
        String fullAddress = String.format("%s-%s-%s-%s",
                location.getAisle(),
                location.getShelf(),
                location.getLevel(),
                location.getBin());

        return new WarehouseLocationResponseDTO(
                location.getId(),
                location.getAisle(),
                location.getShelf(),
                location.getLevel(),
                location.getBin(),
                fullAddress,
                location.getMaxWeightKg(),
                location.getActive(),
                location.getCreatedAt(),
                location.getUpdatedAt());
    }
}
