package in.gov.landrevenue.clean.dto.mutation;

public record MutationDecisionRequest(
        String rejectionReason,   // required only when rejecting
        String remarks
) {}
