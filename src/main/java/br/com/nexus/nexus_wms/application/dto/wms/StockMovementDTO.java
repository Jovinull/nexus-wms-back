package br.com.nexus.nexus_wms.application.dto.wms;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockMovementDTO {

    @NotNull(message = "O ID do produto é obrigatório")
    private Long productId;

    @NotNull(message = "O ID da localização é obrigatório")
    private Long locationId;

    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade movimentada deve ser maior que zero")
    private Integer quantity;

    private String batchNumber; // Lote

    // expiryDate e outras informacoes para Entrada.
}
