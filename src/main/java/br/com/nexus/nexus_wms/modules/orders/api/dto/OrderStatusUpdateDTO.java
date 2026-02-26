package br.com.nexus.nexus_wms.modules.orders.api.dto;

import br.com.nexus.nexus_wms.modules.orders.domain.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusUpdateDTO {

    @NotNull(message = "O novo status é obrigatório")
    private OrderStatus status;
}
