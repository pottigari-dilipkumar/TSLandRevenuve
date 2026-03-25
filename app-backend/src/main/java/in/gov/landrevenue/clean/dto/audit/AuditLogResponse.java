package in.gov.landrevenue.clean.dto.audit;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        String action,
        String entityName,
        String actor,
        String details,
        Instant createdAt
) {}
