/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

package in.gov.landrevenue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegistrationCreateRequest(
        @NotBlank String parcelId,
        @NotBlank String sellerName,
        @NotBlank String buyerName,
        @NotBlank String registrationRef,
        @NotBlank String deedHash,
        @NotBlank String verifiedIdentityToken,
        @Pattern(regexp = "^$|^0x[a-fA-F0-9]{40}$", message = "ownerWalletAddress must be a valid hex Ethereum address")
        String ownerWalletAddress
) {
}
