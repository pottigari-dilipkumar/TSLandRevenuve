package in.gov.landrevenue.clean.repository;

import in.gov.landrevenue.clean.entity.MarketValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MarketValueRepository extends JpaRepository<MarketValue, Long> {

    @Query("SELECT mv FROM MarketValue mv WHERE mv.district = :district AND mv.village = :village AND mv.effectiveTo IS NULL ORDER BY mv.effectiveFrom DESC")
    Optional<MarketValue> findCurrentRate(@Param("district") String district, @Param("village") String village);

    List<MarketValue> findByDistrictIgnoreCaseOrderByVillageAsc(String district);

    @Query("SELECT mv FROM MarketValue mv WHERE mv.effectiveTo IS NULL ORDER BY mv.district, mv.village")
    List<MarketValue> findAllCurrent();
}
