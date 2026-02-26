package br.com.nexus.nexus_wms.modules.tms.domain.repository;

import br.com.nexus.nexus_wms.modules.tms.domain.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByCnh(String cnh);
}
