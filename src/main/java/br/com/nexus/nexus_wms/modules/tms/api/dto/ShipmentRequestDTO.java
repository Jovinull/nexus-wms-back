package br.com.nexus.nexus_wms.modules.tms.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipmentRequestDTO {

    @NotNull(message = "O ID do veículo é obrigatório")
    private Long vehicleId;

    @NotNull(message = "O ID do motorista é obrigatório")
    private Long driverId;

    private String notes;
}
