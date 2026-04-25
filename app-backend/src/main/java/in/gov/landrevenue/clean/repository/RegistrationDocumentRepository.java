package in.gov.landrevenue.clean.repository;

import in.gov.landrevenue.clean.entity.RegistrationDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationDocumentRepository extends JpaRepository<RegistrationDocument, Long> {
    List<RegistrationDocument> findByLandRegistrationId(Long registrationId);
}
