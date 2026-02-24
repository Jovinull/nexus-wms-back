package br.com.nexus.nexus_wms.domain.entity.tms;

import br.com.nexus.nexus_wms.domain.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle extends BaseEntity {

    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "max_weight_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxWeightKg;

    @Column(name = "max_volume_m3", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxVolumeM3;

    @Column(nullable = false)
    private Boolean active = true;
}
