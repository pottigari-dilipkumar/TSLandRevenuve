package in.gov.landrevenue.clean.enums;

public enum MutationStatus {
    APPLIED,         // Application submitted
    MANDAL_REVIEW,   // Under review by Mandal Revenue Officer
    APPROVED,        // Mutation approved — land record updated
    REJECTED         // Rejected with reason
}
