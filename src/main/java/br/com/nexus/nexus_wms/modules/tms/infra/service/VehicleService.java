package br.com.nexus.nexus_wms.modules.tms.infra.service;

import br.com.nexus.nexus_wms.modules.tms.api.dto.VehicleRequestDTO;
import br.com.nexus.nexus_wms.modules.tms.api.dto.VehicleResponseDTO;
import br.com.nexus.nexus_wms.modules.tms.domain.entity.Vehicle;
import br.com.nexus.nexus_wms.modules.tms.domain.repository.VehicleRepository;
import br.com.nexus.nexus_wms.core.exception.BusinessException;
import br.com.nexus.nexus_wms.core.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public VehicleResponseDTO create(VehicleRequestDTO dto) {
        if (vehicleRepository.findByLicensePlate(dto.getLicensePlate()).isPresent()) {
            throw new BusinessException("Já existe um veículo cadastrado com esta placa.");
        }

        Vehicle vehicle = new Vehicle();
        mapDtoToEntity(dto, vehicle);

        if (dto.getActive() == null) {
            vehicle.setActive(true);
        }

        vehicle = vehicleRepository.save(vehicle);
        return toResponseDTO(vehicle);
    }

    @Transactional(readOnly = true)
    public Page<VehicleResponseDTO> findAll(Pageable pageable) {
        return vehicleRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public VehicleResponseDTO findById(Long id) {
        Vehicle vehicle = getVehicleOrThrow(id);
        return toResponseDTO(vehicle);
    }

    @Transactional
    public VehicleResponseDTO update(Long id, VehicleRequestDTO dto) {
        Vehicle vehicle = getVehicleOrThrow(id);

        vehicleRepository.findByLicensePlate(dto.getLicensePlate())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException("Já existe outro veículo usando esta placa.");
                    }
                });

        mapDtoToEntity(dto, vehicle);

        if (dto.getActive() != null) {
            vehicle.setActive(dto.getActive());
        }

        vehicle = vehicleRepository.save(vehicle);
        return toResponseDTO(vehicle);
    }

    @Transactional
    public void delete(Long id) {
        Vehicle vehicle = getVehicleOrThrow(id);
        vehicleRepository.delete(vehicle);
    }

    private Vehicle getVehicleOrThrow(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado para o ID: " + id));
    }

    private void mapDtoToEntity(VehicleRequestDTO dto, Vehicle vehicle) {
        vehicle.setLicensePlate(dto.getLicensePlate().toUpperCase());
        vehicle.setModel(dto.getModel());
        vehicle.setMaxWeightKg(dto.getMaxWeightKg());
        vehicle.setMaxVolumeM3(dto.getMaxVolumeM3());
    }

    private VehicleResponseDTO toResponseDTO(Vehicle vehicle) {
        return new VehicleResponseDTO(
                vehicle.getId(),
                vehicle.getLicensePlate(),
                vehicle.getModel(),
                vehicle.getMaxWeightKg(),
                vehicle.getMaxVolumeM3(),
                vehicle.getActive(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt());
    }
}
