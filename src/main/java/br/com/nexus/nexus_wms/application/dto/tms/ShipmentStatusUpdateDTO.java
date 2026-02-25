package br.com.nexus.nexus_wms.application.dto.tms;

import br.com.nexus.nexus_wms.domain.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipmentStatusUpdateDTO {

    @NotNull(message = "O novo status é obrigatório")
    private ShipmentStatus status;
}
