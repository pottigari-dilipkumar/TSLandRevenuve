/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

package in.gov.landrevenue.clean.dto.land;

import java.time.Instant;

/**
 * One entry in the ownership / encumbrance history of a land record.
 */
public record LandHistoryEntry(
        Instant date,
        String type,           // REGISTRATION | MUTATION
        String ref,            // registrationRef or mutationRef
        String previousOwner,
        String newOwner,
        String status,
        String details         // mutation type or consideration amount
) {}
