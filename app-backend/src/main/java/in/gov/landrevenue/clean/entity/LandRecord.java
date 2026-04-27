package in.gov.landrevenue.clean.entity;

import in.gov.landrevenue.clean.enums.LandType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "land_records")
public class LandRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String surveyNumber;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String village;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal areaInAcres;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LandType landType = LandType.PRIVATE;

    /** Pattadar Passbook number (PPB) — issued for assigned/government-granted lands */
    private String passpbookNumber;

    /** GeoJSON Polygon string representing the land parcel boundary */
    @Column(columnDefinition = "TEXT")
    private String geometry;

    /** Open Location Code (PLUS Code) for the land centroid — e.g. 8GQ7V8H8+WM */
    @Column(length = 20)
    private String plusCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @OneToMany(mappedBy = "landRecord", cascade = CascadeType.ALL)
    private List<RevenueRecord> revenueRecords = new ArrayList<>();

    public Long getId() { return id; }
    public String getSurveyNumber() { return surveyNumber; }
    public void setSurveyNumber(String surveyNumber) { this.surveyNumber = surveyNumber; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public BigDecimal getAreaInAcres() { return areaInAcres; }
    public void setAreaInAcres(BigDecimal areaInAcres) { this.areaInAcres = areaInAcres; }
    public Owner getOwner() { return owner; }
    public void setOwner(Owner owner) { this.owner = owner; }
    public List<RevenueRecord> getRevenueRecords() { return revenueRecords; }
    public LandType getLandType() { return landType; }
    public void setLandType(LandType landType) { this.landType = landType; }
    public String getPassbookNumber() { return passpbookNumber; }
    public void setPassbookNumber(String passpbookNumber) { this.passpbookNumber = passpbookNumber; }
    public String getGeometry() { return geometry; }
    public void setGeometry(String geometry) { this.geometry = geometry; }
    public String getPlusCode() { return plusCode; }
    public void setPlusCode(String plusCode) { this.plusCode = plusCode; }

    private static final Set<LandType> PROHIBITED_TYPES = Set.of(LandType.GOVERNMENT, LandType.FOREST, LandType.WAQF);
    public boolean isProhibited() { return PROHIBITED_TYPES.contains(landType); }
}
