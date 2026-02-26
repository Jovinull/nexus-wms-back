package br.com.nexus.nexus_wms.modules.tms.domain.repository;

import br.com.nexus.nexus_wms.modules.tms.domain.entity.ShipmentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentOrderRepository extends JpaRepository<ShipmentOrder, Long> {
    List<ShipmentOrder> findByShipmentId(Long shipmentId);

    List<ShipmentOrder> findByOrderId(Long orderId);
}
