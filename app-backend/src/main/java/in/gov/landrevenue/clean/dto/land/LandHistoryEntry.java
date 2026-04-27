package in.gov.landrevenue.clean.dto.land;

import java.time.Instant;

/**
 * One entry in the ownership / encumbrance history of a land record.
 */
public record LandHistoryEntry(
        Instant date,
        String type,           // REGISTRATION | MUTATION
        String ref,            // registrationRef or mutationRef
        String previousOwner,
        String newOwner,
        String status,
        String details         // mutation type or consideration amount
) {}
