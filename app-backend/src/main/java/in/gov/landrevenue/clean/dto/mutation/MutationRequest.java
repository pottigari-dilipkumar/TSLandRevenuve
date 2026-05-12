/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

package in.gov.landrevenue.clean.dto.mutation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MutationRequest(
        @NotNull Long landRecordId,
        String registrationRef,       // optional — links to an approved registration

        /**
         * SALE | SUCCESSION | GIFT | COURT_ORDER | PARTITION | NALA_CONVERSION
         */
        @NotBlank String mutationType,

        // Previous owner
        @NotBlank String previousOwnerName,
        String previousOwnerAadhaar,

        // New owner
        @NotBlank String newOwnerName,
        @NotBlank String newOwnerAadhaar,
        String newOwnerMobile,
        String newOwnerEmail,
        String newOwnerAddress,

        // Succession-specific
        String relationToDeceased,
        LocalDate dateOfDeath,

        String remarks
) {}
