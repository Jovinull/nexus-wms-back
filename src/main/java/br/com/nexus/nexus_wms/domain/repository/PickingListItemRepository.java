package br.com.nexus.nexus_wms.domain.repository;

import br.com.nexus.nexus_wms.domain.entity.order.PickingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PickingListItemRepository extends JpaRepository<PickingListItem, UUID> {
    List<PickingListItem> findByPickingListIdOrderBySequenceOrderAsc(UUID pickingListId);
}
