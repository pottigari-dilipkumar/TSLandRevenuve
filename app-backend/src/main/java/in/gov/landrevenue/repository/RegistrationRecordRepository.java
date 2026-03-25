package in.gov.landrevenue.repository;

import in.gov.landrevenue.model.RegistrationRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRecordRepository extends JpaRepository<RegistrationRecordEntity, String> {
    boolean existsByRegistrationRef(String registrationRef);
}
