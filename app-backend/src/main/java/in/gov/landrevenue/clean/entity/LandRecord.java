package in.gov.landrevenue.clean.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
}
