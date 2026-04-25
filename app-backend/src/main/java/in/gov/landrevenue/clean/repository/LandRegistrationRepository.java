package in.gov.landrevenue.clean.repository;

import in.gov.landrevenue.clean.entity.LandRegistration;
import in.gov.landrevenue.clean.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LandRegistrationRepository extends JpaRepository<LandRegistration, Long> {
    Optional<LandRegistration> findByRegistrationRef(String registrationRef);
    boolean existsByRegistrationRef(String registrationRef);
    List<LandRegistration> findByStatus(RegistrationStatus status);
    List<LandRegistration> findByStatusIn(List<RegistrationStatus> statuses);

    @Query("SELECT r FROM LandRegistration r WHERE r.sellerAadhaar = :aadhaar OR r.buyerAadhaar = :aadhaar ORDER BY r.createdAt DESC")
    List<LandRegistration> findByPartyAadhaar(@Param("aadhaar") String aadhaar);

    @Query("SELECT r FROM LandRegistration r JOIN r.witnesses w WHERE w.aadhaarNumber = :aadhaar")
    List<LandRegistration> findByWitnessAadhaar(@Param("aadhaar") String aadhaar);
}
