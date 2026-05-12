/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

package in.gov.landrevenue.model;

import java.io.Serializable;
import java.time.Instant;

public record RegistrationRecord(
        String registrationRef,
        String parcelId,
        String sellerName,
        String buyerName,
        String deedHash,
        String verifiedIdentityToken,
        String ownerWalletAddress,
        Instant createdAt,
        String status,
        String blockchainSyncStatus,
        String blockchainTxHash,
        Long blockchainBlockNumber,
        Instant blockchainSyncedAt,
        String blockchainErrorMessage
) implements Serializable {
}
