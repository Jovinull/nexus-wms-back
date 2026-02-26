package br.com.nexus.nexus_wms.modules.wms.api.dto;

import br.com.nexus.nexus_wms.modules.wms.domain.entity.AbcCurve;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String sku;
    private String name;
    private String description;

    // Simplificando o envio da Categoria, mandamos o essencial
    private Long categoryId;
    private String categoryName;

    private AbcCurve abcCurve;
    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private BigDecimal widthCm;
    private BigDecimal depthCm;
    private BigDecimal unitPrice;
    private Integer minStockLevel;
    private Boolean active;
    private Long version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
