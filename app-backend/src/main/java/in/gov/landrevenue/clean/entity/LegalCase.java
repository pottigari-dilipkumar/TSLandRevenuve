package in.gov.landrevenue.clean.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "land_legal_cases",
       indexes = @Index(name = "idx_legal_case_land", columnList = "landRecordId"))
public class LegalCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long landRecordId;

    @Column(nullable = false, length = 60)
    private String caseNumber;

    @Column(length = 150)
    private String court;

    /** CIVIL | CRIMINAL | REVENUE | BOUNDARY */
    @Column(length = 20)
    private String caseType;

    /** PENDING | STAYED | DISPOSED */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(length = 200)
    private String filedBy;

    private LocalDate filedDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public LegalCase() {}

    public Long getId() { return id; }
    public Long getLandRecordId() { return landRecordId; }
    public void setLandRecordId(Long landRecordId) { this.landRecordId = landRecordId; }
    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
    public String getCourt() { return court; }
    public void setCourt(String court) { this.court = court; }
    public String getCaseType() { return caseType; }
    public void setCaseType(String caseType) { this.caseType = caseType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFiledBy() { return filedBy; }
    public void setFiledBy(String filedBy) { this.filedBy = filedBy; }
    public LocalDate getFiledDate() { return filedDate; }
    public void setFiledDate(LocalDate filedDate) { this.filedDate = filedDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
