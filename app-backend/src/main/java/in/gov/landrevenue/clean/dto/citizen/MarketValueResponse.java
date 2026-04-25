package in.gov.landrevenue.clean.dto.citizen;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MarketValueResponse(
        Long id,
        String district,
        String village,
        BigDecimal ratePerAcre,
        LocalDate effectiveFrom
) {}
