package br.com.nexus.nexus_wms.application.service;

import br.com.nexus.nexus_wms.application.dto.tms.DriverResponseDTO;
import br.com.nexus.nexus_wms.application.dto.tms.ShipmentRequestDTO;
import br.com.nexus.nexus_wms.application.dto.tms.ShipmentResponseDTO;
import br.com.nexus.nexus_wms.application.dto.tms.ShipmentStatusUpdateDTO;
import br.com.nexus.nexus_wms.application.dto.tms.VehicleResponseDTO;
import br.com.nexus.nexus_wms.domain.entity.tms.Driver;
import br.com.nexus.nexus_wms.domain.entity.tms.Shipment;
import br.com.nexus.nexus_wms.domain.entity.tms.Vehicle;
import br.com.nexus.nexus_wms.domain.enums.ShipmentStatus;
import br.com.nexus.nexus_wms.domain.repository.DriverRepository;
import br.com.nexus.nexus_wms.domain.repository.ShipmentRepository;
import br.com.nexus.nexus_wms.domain.repository.VehicleRepository;
import br.com.nexus.nexus_wms.infrastructure.exception.BusinessException;
import br.com.nexus.nexus_wms.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public ShipmentService(ShipmentRepository shipmentRepository, VehicleRepository vehicleRepository,
            DriverRepository driverRepository) {
        this.shipmentRepository = shipmentRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional
    public ShipmentResponseDTO create(ShipmentRequestDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado."));

        if (Boolean.FALSE.equals(vehicle.getActive())) {
            throw new BusinessException("Veículo selecionado está inativo.");
        }

        Driver driver = driverRepository.findById(dto.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Motorista não encontrado."));

        if (Boolean.FALSE.equals(driver.getActive())) {
            throw new BusinessException("Motorista selecionado está inativo.");
        }

        Shipment shipment = new Shipment();
        shipment.setVehicle(vehicle);
        shipment.setDriver(driver);
        shipment.setNotes(dto.getNotes());
        shipment.setStatus(ShipmentStatus.AGUARDANDO);

        shipment = shipmentRepository.save(shipment);
        return toResponseDTO(shipment);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentResponseDTO> findAll(Pageable pageable) {
        return shipmentRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ShipmentResponseDTO findById(Long id) {
        Shipment shipment = getShipmentOrThrow(id);
        return toResponseDTO(shipment);
    }

    @Transactional
    public ShipmentResponseDTO updateStatus(Long id, ShipmentStatusUpdateDTO dto) {
        Shipment shipment = getShipmentOrThrow(id);

        shipment.setStatus(dto.getStatus());

        if (dto.getStatus() == ShipmentStatus.EM_TRANSITO && shipment.getDepartureTime() == null) {
            shipment.setDepartureTime(LocalDateTime.now());
        }

        if (dto.getStatus() == ShipmentStatus.ENTREGUE && shipment.getArrivalTime() == null) {
            shipment.setArrivalTime(LocalDateTime.now());
        }

        shipment = shipmentRepository.save(shipment);
        return toResponseDTO(shipment);
    }

    private Shipment getShipmentOrThrow(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manifesto de Carga/Shipment não encontrado para o ID: " + id));
    }

    private ShipmentResponseDTO toResponseDTO(Shipment shipment) {

        VehicleResponseDTO vehicleDTO = null;
        if (shipment.getVehicle() != null) {
            vehicleDTO = new VehicleResponseDTO(
                    shipment.getVehicle().getId(),
                    shipment.getVehicle().getLicensePlate(),
                    shipment.getVehicle().getModel(),
                    shipment.getVehicle().getMaxWeightKg(),
                    shipment.getVehicle().getMaxVolumeM3(),
                    shipment.getVehicle().getActive(),
                    shipment.getVehicle().getCreatedAt(),
                    shipment.getVehicle().getUpdatedAt());
        }

        DriverResponseDTO driverDTO = null;
        if (shipment.getDriver() != null) {
            driverDTO = new DriverResponseDTO(
                    shipment.getDriver().getId(),
                    shipment.getDriver().getFullName(),
                    shipment.getDriver().getCnh(),
                    shipment.getDriver().getCnhCategory(),
                    shipment.getDriver().getPhone(),
                    shipment.getDriver().getActive(),
                    shipment.getDriver().getCreatedAt(),
                    shipment.getDriver().getUpdatedAt());
        }

        return new ShipmentResponseDTO(
                shipment.getId(),
                vehicleDTO,
                driverDTO,
                shipment.getStatus(),
                shipment.getDepartureTime(),
                shipment.getArrivalTime(),
                shipment.getNotes(),
                shipment.getVersion(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt());
    }
}
