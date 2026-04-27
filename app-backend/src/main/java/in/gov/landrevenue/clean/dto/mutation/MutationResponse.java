package in.gov.landrevenue.clean.dto.mutation;

import java.time.Instant;
import java.time.LocalDate;

public record MutationResponse(
        Long id,
        String mutationRef,
        Long landRecordId,
        String registrationRef,
        String mutationType,
        String status,

        String previousOwnerName,
        String previousOwnerAadhaar,

        String newOwnerName,
        String newOwnerAadhaar,
        String newOwnerMobile,
        String newOwnerEmail,
        String newOwnerAddress,

        String relationToDeceased,
        LocalDate dateOfDeath,

        Long appliedByUserId,
        Long decidedByUserId,
        Instant appliedAt,
        Instant reviewedAt,
        Instant decidedAt,

        String rejectionReason,
        String remarks
) {}
