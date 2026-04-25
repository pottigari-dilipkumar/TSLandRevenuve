package in.gov.landrevenue.clean.entity;

import in.gov.landrevenue.clean.enums.DocumentType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "registration_documents")
public class RegistrationDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id")
    private LandRegistration landRegistration;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String originalName;

    private String contentType;

    @Column(nullable = false)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Instant uploadedAt = Instant.now();

    private Long uploadedByUserId;

    public Long getId() { return id; }
    public LandRegistration getLandRegistration() { return landRegistration; }
    public void setLandRegistration(LandRegistration landRegistration) { this.landRegistration = landRegistration; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
    public Long getUploadedByUserId() { return uploadedByUserId; }
    public void setUploadedByUserId(Long uploadedByUserId) { this.uploadedByUserId = uploadedByUserId; }
}
