package in.gov.landrevenue.clean.dto.land;

import java.math.BigDecimal;

public record LandRecordResponse(
        Long id,
        String surveyNumber,
        String district,
        String village,
        BigDecimal areaInAcres,
        Long ownerId,
        String ownerName
) {}
