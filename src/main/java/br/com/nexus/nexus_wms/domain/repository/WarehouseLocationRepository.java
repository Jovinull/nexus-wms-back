package br.com.nexus.nexus_wms.domain.repository;

import br.com.nexus.nexus_wms.domain.entity.wms.WarehouseLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseLocationRepository extends JpaRepository<WarehouseLocation, Long> {
    Optional<WarehouseLocation> findByAisleAndShelfAndLevelAndBin(String aisle, String shelf, String level, String bin);
}
