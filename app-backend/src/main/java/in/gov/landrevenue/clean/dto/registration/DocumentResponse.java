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

import java.time.Instant;

public record DocumentResponse(
        Long id,
        String originalName,
        String documentType,
        String description,
        String downloadUrl,
        Instant uploadedAt
) {}
