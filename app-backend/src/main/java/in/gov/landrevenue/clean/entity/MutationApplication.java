package in.gov.landrevenue.clean.entity;

import in.gov.landrevenue.clean.enums.MutationStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "mutation_applications",
       indexes = @Index(name = "idx_mutation_land", columnList = "landRecordId"))
public class MutationApplication {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String mutationRef;

    @Column(nullable = false)
    private Long landRecordId;

    /** Optional — set when mutation follows an approved registration */
    private String registrationRef;

    /**
     * SALE | SUCCESSION | GIFT | COURT_ORDER | PARTITION | NALA_CONVERSION
     */
    @Column(nullable = false, length = 20)
    private String mutationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MutationStatus status = MutationStatus.APPLIED;

    // Previous owner (seller / deceased)
    @Column(nullable = false) private String previousOwnerName;
    private String previousOwnerAadhaar;

    // New owner (buyer / heir)
    @Column(nullable = false) private String newOwnerName;
    @Column(nullable = false) private String newOwnerAadhaar;
    private String newOwnerMobile;
    private String newOwnerEmail;
    @Column(length = 500) private String newOwnerAddress;

    // Succession-specific
    private String relationToDeceased;   // son / daughter / spouse / etc.
    private LocalDate dateOfDeath;

    // Workflow
    private Long appliedByUserId;
    private Long decidedByUserId;
    @Column(nullable = false) private Instant appliedAt = Instant.now();
    private Instant reviewedAt;
    private Instant decidedAt;

    @Column(length = 1000) private String rejectionReason;
    @Column(length = 2000) private String remarks;

    // ── Getters & Setters ────────────────────────────────────────────────────
    public Long getId() { return id; }
    public String getMutationRef() { return mutationRef; }
    public void setMutationRef(String mutationRef) { this.mutationRef = mutationRef; }
    public Long getLandRecordId() { return landRecordId; }
    public void setLandRecordId(Long landRecordId) { this.landRecordId = landRecordId; }
    public String getRegistrationRef() { return registrationRef; }
    public void setRegistrationRef(String registrationRef) { this.registrationRef = registrationRef; }
    public String getMutationType() { return mutationType; }
    public void setMutationType(String mutationType) { this.mutationType = mutationType; }
    public MutationStatus getStatus() { return status; }
    public void setStatus(MutationStatus status) { this.status = status; }
    public String getPreviousOwnerName() { return previousOwnerName; }
    public void setPreviousOwnerName(String previousOwnerName) { this.previousOwnerName = previousOwnerName; }
    public String getPreviousOwnerAadhaar() { return previousOwnerAadhaar; }
    public void setPreviousOwnerAadhaar(String previousOwnerAadhaar) { this.previousOwnerAadhaar = previousOwnerAadhaar; }
    public String getNewOwnerName() { return newOwnerName; }
    public void setNewOwnerName(String newOwnerName) { this.newOwnerName = newOwnerName; }
    public String getNewOwnerAadhaar() { return newOwnerAadhaar; }
    public void setNewOwnerAadhaar(String newOwnerAadhaar) { this.newOwnerAadhaar = newOwnerAadhaar; }
    public String getNewOwnerMobile() { return newOwnerMobile; }
    public void setNewOwnerMobile(String newOwnerMobile) { this.newOwnerMobile = newOwnerMobile; }
    public String getNewOwnerEmail() { return newOwnerEmail; }
    public void setNewOwnerEmail(String newOwnerEmail) { this.newOwnerEmail = newOwnerEmail; }
    public String getNewOwnerAddress() { return newOwnerAddress; }
    public void setNewOwnerAddress(String newOwnerAddress) { this.newOwnerAddress = newOwnerAddress; }
    public String getRelationToDeceased() { return relationToDeceased; }
    public void setRelationToDeceased(String relationToDeceased) { this.relationToDeceased = relationToDeceased; }
    public LocalDate getDateOfDeath() { return dateOfDeath; }
    public void setDateOfDeath(LocalDate dateOfDeath) { this.dateOfDeath = dateOfDeath; }
    public Long getAppliedByUserId() { return appliedByUserId; }
    public void setAppliedByUserId(Long appliedByUserId) { this.appliedByUserId = appliedByUserId; }
    public Long getDecidedByUserId() { return decidedByUserId; }
    public void setDecidedByUserId(Long decidedByUserId) { this.decidedByUserId = decidedByUserId; }
    public Instant getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Instant appliedAt) { this.appliedAt = appliedAt; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
