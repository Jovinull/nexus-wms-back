package br.com.nexus.nexus_wms.modules.tms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponseDTO {
    private Long id;
    private String licensePlate;
    private String model;
    private BigDecimal maxWeightKg;
    private BigDecimal maxVolumeM3;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
