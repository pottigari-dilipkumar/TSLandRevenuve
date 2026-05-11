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
    List<LandRegistration> findBySellerUserIdOrderByCreatedAtDesc(Long sellerUserId);
    List<LandRegistration> findByBuyerAadhaarAndStatusOrderByCreatedAtDesc(String buyerAadhaar, RegistrationStatus status);

    @Query("SELECT r FROM LandRegistration r WHERE r.sellerAadhaar = :aadhaar OR r.buyerAadhaar = :aadhaar ORDER BY r.createdAt DESC")
    List<LandRegistration> findByPartyAadhaar(@Param("aadhaar") String aadhaar);

    @Query("SELECT r FROM LandRegistration r JOIN r.witnesses w WHERE w.aadhaarNumber = :aadhaar")
    List<LandRegistration> findByWitnessAadhaar(@Param("aadhaar") String aadhaar);

    List<LandRegistration> findByPropertySurveyNumberIgnoreCaseOrderByCreatedAtAsc(String surveyNumber);

    @Query("""
            SELECT r FROM LandRegistration r
            WHERE LOWER(r.propertyDistrict)     = LOWER(:district)
              AND LOWER(r.propertyVillage)      = LOWER(:village)
              AND LOWER(r.propertySurveyNumber) = LOWER(:surveyNumber)
            ORDER BY r.createdAt ASC
            """)
    List<LandRegistration> findByProperty(
            @Param("district") String district,
            @Param("village")  String village,
            @Param("surveyNumber") String surveyNumber);
}
