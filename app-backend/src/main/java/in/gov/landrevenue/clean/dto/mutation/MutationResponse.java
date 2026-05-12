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

import java.time.Instant;
import java.time.LocalDate;

public record MutationResponse(
        Long id,
        String mutationRef,
        Long landRecordId,
        String registrationRef,
        String mutationType,
        String status,

        String previousOwnerName,
        String previousOwnerAadhaar,

        String newOwnerName,
        String newOwnerAadhaar,
        String newOwnerMobile,
        String newOwnerEmail,
        String newOwnerAddress,

        String relationToDeceased,
        LocalDate dateOfDeath,

        Long appliedByUserId,
        Long decidedByUserId,
        Instant appliedAt,
        Instant reviewedAt,
        Instant decidedAt,

        String rejectionReason,
        String remarks
) {}
