package br.com.nexus.nexus_wms.domain.repository;

import br.com.nexus.nexus_wms.domain.entity.wms.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByProductId(Long productId);

    List<Stock> findByLocationId(Long locationId);
}
