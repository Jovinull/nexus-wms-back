package br.com.nexus.nexus_wms.domain.entity.tms;

import br.com.nexus.nexus_wms.domain.entity.BaseEntity;
import br.com.nexus.nexus_wms.domain.enums.CnhCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Driver extends BaseEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true, length = 20)
    private String cnh;

    @Enumerated(EnumType.STRING)
    @Column(name = "cnh_category", nullable = false, length = 5)
    private CnhCategory cnhCategory;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false)
    private Boolean active = true;
}
