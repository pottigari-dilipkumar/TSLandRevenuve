package in.gov.landrevenue.clean.dto.revenue;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueRecordResponse(
        Long id,
        BigDecimal amount,
        LocalDate paymentDate,
        String paymentReference,
        Long landRecordId
) {}
