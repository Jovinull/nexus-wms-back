package br.com.nexus.nexus_wms.modules.tms.domain.entity;

import br.com.nexus.nexus_wms.core.domain.BaseEntity;
import br.com.nexus.nexus_wms.modules.orders.domain.entity.Order;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_shipment_orders", uniqueConstraints = {
        @UniqueConstraint(name = "uq_shipment_orders", columnNames = { "shipment_id", "order_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
