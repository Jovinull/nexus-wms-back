package br.com.nexus.nexus_wms.modules.wms.domain.repository;

import br.com.nexus.nexus_wms.modules.wms.domain.entity.WarehouseLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseLocationRepository extends JpaRepository<WarehouseLocation, Long> {
    Optional<WarehouseLocation> findByAisleAndShelfAndLevelAndBin(String aisle, String shelf, String level, String bin);
}
