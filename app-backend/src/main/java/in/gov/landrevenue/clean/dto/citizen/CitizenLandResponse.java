package in.gov.landrevenue.clean.dto.citizen;

import java.math.BigDecimal;

public record CitizenLandResponse(
        Long id,
        String surveyNumber,
        String district,
        String village,
        BigDecimal areaInAcres,
        BigDecimal marketValuePerAcre,
        BigDecimal estimatedMarketValue
) {}
