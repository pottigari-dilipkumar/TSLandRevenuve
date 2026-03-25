package in.gov.landrevenue.clean.dto.dashboard;

import java.math.BigDecimal;

public record RevenueTrendResponse(String month, BigDecimal amount) {}
