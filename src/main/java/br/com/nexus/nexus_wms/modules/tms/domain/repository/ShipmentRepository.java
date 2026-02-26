package br.com.nexus.nexus_wms.modules.tms.domain.repository;

import br.com.nexus.nexus_wms.modules.tms.domain.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}
