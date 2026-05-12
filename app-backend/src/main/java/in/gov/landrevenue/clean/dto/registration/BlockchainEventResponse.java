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

public record BlockchainEventResponse(
        Long id,
        String registrationRef,
        String eventType,
        String actorUsername,
        String actorRole,
        String payloadHash,
        String txHash,
        Long blockNumber,
        String chainSyncStatus,
        Instant timestamp,
        Long sequence,
        String details
) {}
