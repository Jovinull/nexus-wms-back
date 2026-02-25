package br.com.nexus.nexus_wms.application.dto.wms;

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
public class WarehouseLocationResponseDTO {
    private Long id;
    private String aisle;
    private String shelf;
    private String level;
    private String bin;
    private String fullAddress; // Ex: A-01-01-01
    private BigDecimal maxWeightKg;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
