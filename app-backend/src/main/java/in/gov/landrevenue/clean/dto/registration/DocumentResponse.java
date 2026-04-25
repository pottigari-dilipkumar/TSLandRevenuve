package in.gov.landrevenue.clean.dto.registration;

import java.time.Instant;

public record DocumentResponse(
        Long id,
        String originalName,
        String documentType,
        String description,
        String downloadUrl,
        Instant uploadedAt
) {}
