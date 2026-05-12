/*
 * Copyright (c) 2026 DivaTech. All rights reserved.
 *
 * This software is the proprietary and confidential property of DivaTech.
 * Unauthorized copying, modification, distribution, or use of this software,
 * in whole or in part, is strictly prohibited without prior written permission.
 *
 * Website: https://www.divatech.in | Contact: legal@divatech.in
 */

package in.gov.landrevenue.blockchain;

import java.time.Instant;

public record BlockchainAnchorResult(
        String syncStatus,
        String transactionHash,
        Long blockNumber,
        Instant syncedAt,
        String errorMessage
) {
    public static BlockchainAnchorResult synced(String transactionHash, Long blockNumber, Instant syncedAt) {
        return new BlockchainAnchorResult("SYNCED", transactionHash, blockNumber, syncedAt, null);
    }

    public static BlockchainAnchorResult failed(String errorMessage) {
        return new BlockchainAnchorResult("FAILED", null, null, null, errorMessage);
    }

    public static BlockchainAnchorResult skipped() {
        return new BlockchainAnchorResult("SKIPPED", null, null, null, null);
    }
}
