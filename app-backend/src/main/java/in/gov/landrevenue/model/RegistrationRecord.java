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
