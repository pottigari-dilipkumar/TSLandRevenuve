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
        Instant createdAt,
        String status
) implements Serializable {
}
