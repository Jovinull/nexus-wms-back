package br.com.nexus.nexus_wms.modules.iam.domain.repository;

import br.com.nexus.nexus_wms.modules.iam.domain.entity.RefreshToken;
import br.com.nexus.nexus_wms.modules.iam.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    int deleteByUser(User user);
}
