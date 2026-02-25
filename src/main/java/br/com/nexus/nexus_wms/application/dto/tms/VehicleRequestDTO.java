package br.com.nexus.nexus_wms.application.dto.tms;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VehicleRequestDTO {

    @NotBlank(message = "A placa é obrigatória")
    @Size(max = 20, message = "A placa não pode exceder 20 caracteres")
    private String licensePlate;

    @NotBlank(message = "O modelo é obrigatório")
    @Size(max = 100, message = "O modelo não pode exceder 100 caracteres")
    private String model;

    @NotNull(message = "O peso máximo é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "O peso máximo deve ser maior que zero")
    private BigDecimal maxWeightKg;

    @NotNull(message = "O volume máximo é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "O volume máximo deve ser maior que zero")
    private BigDecimal maxVolumeM3;

    private Boolean active;
}
