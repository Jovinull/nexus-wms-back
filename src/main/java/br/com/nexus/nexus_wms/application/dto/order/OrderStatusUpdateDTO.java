package br.com.nexus.nexus_wms.application.dto.order;

import br.com.nexus.nexus_wms.domain.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusUpdateDTO {

    @NotNull(message = "O novo status é obrigatório")
    private OrderStatus status;
}
