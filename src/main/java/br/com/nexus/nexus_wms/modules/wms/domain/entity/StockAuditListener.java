package br.com.nexus.nexus_wms.modules.wms.domain.entity;

import br.com.nexus.nexus_wms.core.config.ApplicationContextHolder;
import br.com.nexus.nexus_wms.modules.iam.domain.entity.AuditLog;
import br.com.nexus.nexus_wms.modules.iam.domain.repository.AuditLogRepository;
import br.com.nexus.nexus_wms.modules.iam.domain.repository.UserRepository;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class StockAuditListener {

    @PostPersist
    public void onPostPersist(Stock stock) {
        logAction("CREATE", stock, null, stock.getQuantity().toString());
    }

    @PostUpdate
    public void onPostUpdate(Stock stock) {
        logAction("UPDATE", stock, null, stock.getQuantity().toString());
    }

    @PostRemove
    public void onPostRemove(Stock stock) {
        logAction("DELETE", stock, stock.getQuantity().toString(), null);
    }

    private void logAction(String action, Stock stock, String oldValue, String newValue) {
        AuditLogRepository repository = ApplicationContextHolder.getBean(AuditLogRepository.class);
        UserRepository userRepository = ApplicationContextHolder.getBean(UserRepository.class);

        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityName("Stock");
        log.setEntityId(stock.getId());
        log.setOldValue(oldValue);
        log.setNewValue(newValue);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            userRepository.findByEmail(auth.getName()).ifPresent(log::setUser);
        }

        repository.save(log);
    }
}
