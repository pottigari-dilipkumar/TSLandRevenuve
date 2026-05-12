/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

package in.gov.landrevenue.model.platform;

import java.util.List;

public record LandParcel(
        String landId,
        String village,
        String surveyNumber,
        String seller,
        String buyer,
        List<GeoPoint> polygon,
        List<LandEvent> history,
        List<LandDocument> documents
) {
}
