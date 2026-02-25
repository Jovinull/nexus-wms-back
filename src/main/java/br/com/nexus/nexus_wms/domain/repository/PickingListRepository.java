package br.com.nexus.nexus_wms.domain.repository;

import br.com.nexus.nexus_wms.domain.entity.order.PickingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PickingListRepository extends JpaRepository<PickingList, UUID> {
    Optional<PickingList> findByOrderId(UUID orderId);
}
