package br.com.nexus.nexus_wms.modules.wms.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WarehouseLocationRequestDTO {

    @NotBlank(message = "O corredor (aisle) é obrigatório")
    @Size(max = 10, message = "O corredor deve ter no máximo 10 caracteres")
    private String aisle;

    @NotBlank(message = "A prateleira (shelf) é obrigatória")
    @Size(max = 10, message = "A prateleira deve ter no máximo 10 caracteres")
    private String shelf;

    @NotBlank(message = "O nível (level) é obrigatório")
    @Size(max = 10, message = "O nível deve ter no máximo 10 caracteres")
    private String level;

    @NotBlank(message = "O vão (bin) é obrigatório")
    @Size(max = 10, message = "O vão deve ter no máximo 10 caracteres")
    private String bin;

    @DecimalMin(value = "0.0", inclusive = false, message = "O peso máximo deve ser maior que zero")
    private BigDecimal maxWeightKg;

    private Boolean active;
}
