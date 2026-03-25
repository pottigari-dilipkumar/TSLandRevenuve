package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.audit.AuditLogResponse;
import in.gov.landrevenue.clean.entity.AuditLog;
import in.gov.landrevenue.clean.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String entityName, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityName(entityName);
        log.setActor(getActor());
        log.setDetails(details);
        log.setCreatedAt(Instant.now());
        auditLogRepository.save(log);
    }

    public Page<AuditLogResponse> list(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::map);
    }

    private String getActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "SYSTEM";
        }
        return authentication.getName();
    }

    private AuditLogResponse map(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getAction(), log.getEntityName(), log.getActor(), log.getDetails(), log.getCreatedAt());
    }
}
