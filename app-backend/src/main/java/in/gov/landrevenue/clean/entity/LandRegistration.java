package in.gov.landrevenue.clean.entity;

import in.gov.landrevenue.clean.enums.RegistrationStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "land_registrations")
public class LandRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String registrationRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.DRAFT;

    // Property details
    @Column(nullable = false)
    private String propertyDistrict;

    @Column(nullable = false)
    private String propertyVillage;

    @Column(nullable = false)
    private String propertySurveyNumber;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal propertyAreaInAcres;

    @Column(precision = 15, scale = 2)
    private BigDecimal marketValuePerAcre;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalMarketValue;

    @Column(precision = 15, scale = 2)
    private BigDecimal considerationAmount; // agreed sale price

    @Column(precision = 15, scale = 2)
    private BigDecimal stampDuty;

    // Geo-location (captured at time of registration)
    private Double propertyLatitude;
    private Double propertyLongitude;

    /** GeoJSON Polygon string drawn on map representing the land parcel boundary */
    @Column(columnDefinition = "TEXT")
    private String propertyGeometry;

    /** Open Location Code (PLUS Code) for the polygon centroid */
    @Column(length = 20)
    private String propertyPlusCode;

    // Blockchain anchor (populated after APPROVED + EVM anchor)
    @Column(length = 100)
    private String blockchainTxHash;

    private Long blockchainBlockNumber;

    /** SYNCED | FAILED | SKIPPED */
    @Column(length = 10)
    private String blockchainSyncStatus;

    private Instant blockchainSyncedAt;

    // Seller details
    @Column(nullable = false)
    private String sellerName;

    @Column(nullable = false)
    private String sellerAadhaar;

    private String sellerMobile;

    private String sellerEmail;

    @Column(length = 500)
    private String sellerAddress;

    // Buyer details
    @Column(nullable = false)
    private String buyerName;

    @Column(nullable = false)
    private String buyerAadhaar;

    private String buyerMobile;

    private String buyerEmail;

    @Column(length = 500)
    private String buyerAddress;

    // Workflow tracking
    private Long draftedByUserId;
    private Long approvedByUserId;

    @Column(length = 1000)
    private String rejectionReason;

    @Column(length = 2000)
    private String notes;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant submittedAt;
    private Instant decidedAt;

    @OneToMany(mappedBy = "landRegistration", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RegistrationWitness> witnesses = new ArrayList<>();

    @OneToMany(mappedBy = "landRegistration", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RegistrationDocument> documents = new ArrayList<>();

    public Long getId() { return id; }
    public String getRegistrationRef() { return registrationRef; }
    public void setRegistrationRef(String registrationRef) { this.registrationRef = registrationRef; }
    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }
    public String getPropertyDistrict() { return propertyDistrict; }
    public void setPropertyDistrict(String propertyDistrict) { this.propertyDistrict = propertyDistrict; }
    public String getPropertyVillage() { return propertyVillage; }
    public void setPropertyVillage(String propertyVillage) { this.propertyVillage = propertyVillage; }
    public String getPropertySurveyNumber() { return propertySurveyNumber; }
    public void setPropertySurveyNumber(String propertySurveyNumber) { this.propertySurveyNumber = propertySurveyNumber; }
    public BigDecimal getPropertyAreaInAcres() { return propertyAreaInAcres; }
    public void setPropertyAreaInAcres(BigDecimal propertyAreaInAcres) { this.propertyAreaInAcres = propertyAreaInAcres; }
    public BigDecimal getMarketValuePerAcre() { return marketValuePerAcre; }
    public void setMarketValuePerAcre(BigDecimal marketValuePerAcre) { this.marketValuePerAcre = marketValuePerAcre; }
    public BigDecimal getTotalMarketValue() { return totalMarketValue; }
    public void setTotalMarketValue(BigDecimal totalMarketValue) { this.totalMarketValue = totalMarketValue; }
    public BigDecimal getConsiderationAmount() { return considerationAmount; }
    public void setConsiderationAmount(BigDecimal considerationAmount) { this.considerationAmount = considerationAmount; }
    public BigDecimal getStampDuty() { return stampDuty; }
    public void setStampDuty(BigDecimal stampDuty) { this.stampDuty = stampDuty; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getSellerAadhaar() { return sellerAadhaar; }
    public void setSellerAadhaar(String sellerAadhaar) { this.sellerAadhaar = sellerAadhaar; }
    public String getSellerMobile() { return sellerMobile; }
    public void setSellerMobile(String sellerMobile) { this.sellerMobile = sellerMobile; }
    public String getSellerEmail() { return sellerEmail; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }
    public String getSellerAddress() { return sellerAddress; }
    public void setSellerAddress(String sellerAddress) { this.sellerAddress = sellerAddress; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getBuyerAadhaar() { return buyerAadhaar; }
    public void setBuyerAadhaar(String buyerAadhaar) { this.buyerAadhaar = buyerAadhaar; }
    public String getBuyerMobile() { return buyerMobile; }
    public void setBuyerMobile(String buyerMobile) { this.buyerMobile = buyerMobile; }
    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }
    public String getBuyerAddress() { return buyerAddress; }
    public void setBuyerAddress(String buyerAddress) { this.buyerAddress = buyerAddress; }
    public Long getDraftedByUserId() { return draftedByUserId; }
    public void setDraftedByUserId(Long draftedByUserId) { this.draftedByUserId = draftedByUserId; }
    public Long getApprovedByUserId() { return approvedByUserId; }
    public void setApprovedByUserId(Long approvedByUserId) { this.approvedByUserId = approvedByUserId; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
    public List<RegistrationWitness> getWitnesses() { return witnesses; }
    public List<RegistrationDocument> getDocuments() { return documents; }
    public Double getPropertyLatitude() { return propertyLatitude; }
    public void setPropertyLatitude(Double propertyLatitude) { this.propertyLatitude = propertyLatitude; }
    public Double getPropertyLongitude() { return propertyLongitude; }
    public void setPropertyLongitude(Double propertyLongitude) { this.propertyLongitude = propertyLongitude; }
    public String getPropertyGeometry() { return propertyGeometry; }
    public void setPropertyGeometry(String propertyGeometry) { this.propertyGeometry = propertyGeometry; }
    public String getPropertyPlusCode() { return propertyPlusCode; }
    public void setPropertyPlusCode(String propertyPlusCode) { this.propertyPlusCode = propertyPlusCode; }
    public String getBlockchainTxHash() { return blockchainTxHash; }
    public void setBlockchainTxHash(String blockchainTxHash) { this.blockchainTxHash = blockchainTxHash; }
    public Long getBlockchainBlockNumber() { return blockchainBlockNumber; }
    public void setBlockchainBlockNumber(Long blockchainBlockNumber) { this.blockchainBlockNumber = blockchainBlockNumber; }
    public String getBlockchainSyncStatus() { return blockchainSyncStatus; }
    public void setBlockchainSyncStatus(String blockchainSyncStatus) { this.blockchainSyncStatus = blockchainSyncStatus; }
    public Instant getBlockchainSyncedAt() { return blockchainSyncedAt; }
    public void setBlockchainSyncedAt(Instant blockchainSyncedAt) { this.blockchainSyncedAt = blockchainSyncedAt; }
}
