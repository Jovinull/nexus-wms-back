package br.com.nexus.nexus_wms.modules.wms.domain.repository;

import br.com.nexus.nexus_wms.modules.wms.domain.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByProductId(Long productId);

    List<Stock> findByLocationId(Long locationId);
}
