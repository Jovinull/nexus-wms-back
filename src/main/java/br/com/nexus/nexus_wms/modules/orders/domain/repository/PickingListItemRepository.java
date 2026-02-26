package br.com.nexus.nexus_wms.modules.orders.domain.repository;

import br.com.nexus.nexus_wms.modules.orders.domain.entity.PickingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PickingListItemRepository extends JpaRepository<PickingListItem, Long> {
    List<PickingListItem> findByPickingListIdOrderByPickSequenceAsc(Long pickingListId);
}
