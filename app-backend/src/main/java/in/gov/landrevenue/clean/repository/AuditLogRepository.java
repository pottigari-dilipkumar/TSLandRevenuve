package in.gov.landrevenue.clean.repository;

import in.gov.landrevenue.clean.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
