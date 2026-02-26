package br.com.nexus.nexus_wms.modules.tms.api.dto;

import br.com.nexus.nexus_wms.modules.tms.domain.entity.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipmentStatusUpdateDTO {

    @NotNull(message = "O novo status é obrigatório")
    private ShipmentStatus status;
}
