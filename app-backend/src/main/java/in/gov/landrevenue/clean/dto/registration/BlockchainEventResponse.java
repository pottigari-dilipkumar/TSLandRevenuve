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
