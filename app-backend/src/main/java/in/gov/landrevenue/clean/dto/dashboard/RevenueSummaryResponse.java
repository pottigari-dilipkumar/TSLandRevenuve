package in.gov.landrevenue.clean.dto.dashboard;

import java.math.BigDecimal;

public record RevenueSummaryResponse(String district, BigDecimal demand, BigDecimal collected, BigDecimal arrears) {}
