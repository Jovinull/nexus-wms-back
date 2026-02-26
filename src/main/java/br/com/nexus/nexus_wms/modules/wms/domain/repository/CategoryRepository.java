package br.com.nexus.nexus_wms.modules.wms.domain.repository;

import br.com.nexus.nexus_wms.modules.wms.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
