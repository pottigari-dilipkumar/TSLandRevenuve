package in.gov.landrevenue.clean.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "market_values")
public class MarketValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String village;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal ratePerAcre;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo; // null = currently active

    public Long getId() { return id; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public BigDecimal getRatePerAcre() { return ratePerAcre; }
    public void setRatePerAcre(BigDecimal ratePerAcre) { this.ratePerAcre = ratePerAcre; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }
}
