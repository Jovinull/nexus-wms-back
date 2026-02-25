package br.com.nexus.nexus_wms.application.dto.tms;

import br.com.nexus.nexus_wms.domain.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponseDTO {
    private Long id;
    private VehicleResponseDTO vehicle;
    private DriverResponseDTO driver;
    private ShipmentStatus status;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String notes;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
