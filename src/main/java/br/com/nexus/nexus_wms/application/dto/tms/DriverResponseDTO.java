package br.com.nexus.nexus_wms.application.dto.tms;

import br.com.nexus.nexus_wms.domain.enums.CnhCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponseDTO {
    private Long id;
    private String fullName;
    private String cnh;
    private CnhCategory cnhCategory;
    private String phone;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
