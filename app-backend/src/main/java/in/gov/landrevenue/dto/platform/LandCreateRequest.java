/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

package in.gov.landrevenue.dto.platform;

import in.gov.landrevenue.model.platform.GeoPoint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record LandCreateRequest(
        @NotBlank String landId,
        @NotBlank String village,
        @NotBlank String surveyNumber,
        @NotBlank String seller,
        @NotBlank String buyer,
        @NotEmpty List<GeoPoint> polygon,
        @NotBlank String actor
) {
}
