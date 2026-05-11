package in.gov.landrevenue.clean.dto.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record SaleRequestCreateRequest(
        @NotNull Long landRecordId,
        @NotNull @Positive BigDecimal considerationAmount,
        @NotBlank String buyerName,
        @NotBlank String buyerAadhaar,
        String buyerMobile,
        String buyerEmail,
        String buyerAddress,
        String notes
) {}
