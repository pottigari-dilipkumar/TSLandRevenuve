/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

package in.gov.landrevenue.clean.enums;

public enum LandType {
    PRIVATE,         // Normal privately-owned agricultural / residential land
    GOVERNMENT,      // Government-owned — cannot be transferred
    FOREST,          // Reserved forest land — cannot be transferred
    ASSIGNED,        // Government-assigned land (PPB holders) — restricted transfer
    INAM,            // Inam / grant land — restricted
    WAQF,            // Waqf board / trust property
    NALA_CONVERTED,  // Formerly agricultural, converted to non-agricultural (NALA)
    LAKE             // Water body / lake / tank — protected, cannot be transferred
}
