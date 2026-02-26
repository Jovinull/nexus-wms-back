package br.com.nexus.nexus_wms.modules.wms.domain.entity;

import br.com.nexus.nexus_wms.core.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_warehouse_locations", uniqueConstraints = {
        @UniqueConstraint(name = "uq_locations_address", columnNames = { "aisle", "shelf", "level", "bin" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseLocation extends BaseEntity {

    @Column(nullable = false, length = 10)
    private String aisle;

    @Column(nullable = false, length = 10)
    private String shelf;

    @Column(nullable = false, length = 10)
    private String level;

    @Column(nullable = false, length = 10)
    private String bin;

    @Column(name = "max_weight_kg", precision = 10, scale = 2)
    private BigDecimal maxWeightKg;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "location")
    private List<Stock> stocks = new ArrayList<>();
}
