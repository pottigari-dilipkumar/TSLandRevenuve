/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

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
