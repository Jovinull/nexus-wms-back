package br.com.nexus.nexus_wms.application.dto.wms;

import br.com.nexus.nexus_wms.domain.enums.AbcCurve;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequestDTO {

    @NotBlank(message = "O SKU é obrigatório")
    @Size(max = 50, message = "O SKU deve ter no máximo 50 caracteres")
    private String sku;

    @NotBlank(message = "O nome do produto é obrigatório")
    private String name;

    private String description;

    @NotNull(message = "O ID da categoria é obrigatório")
    private Long categoryId;

    private AbcCurve abcCurve;

    @NotNull(message = "O peso é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "O peso não pode ser negativo")
    private BigDecimal weightKg;

    @NotNull(message = "A altura é obrigatória")
    @DecimalMin(value = "0.0", inclusive = true, message = "A altura não pode ser negativa")
    private BigDecimal heightCm;

    @NotNull(message = "A largura é obrigatória")
    @DecimalMin(value = "0.0", inclusive = true, message = "A largura não pode ser negativa")
    private BigDecimal widthCm;

    @NotNull(message = "A profundidade é obrigatória")
    @DecimalMin(value = "0.0", inclusive = true, message = "A profundidade não pode ser negativa")
    private BigDecimal depthCm;

    @NotNull(message = "O preço unitário é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "O preço não pode ser negativo")
    private BigDecimal unitPrice;

    @NotNull(message = "O estoque mínimo é obrigatório")
    @Min(value = 0, message = "O estoque mínimo não pode ser negativo")
    private Integer minStockLevel;

    private Boolean active = true;
}
