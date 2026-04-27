package in.gov.landrevenue.clean.repository;

import in.gov.landrevenue.clean.entity.RegistrationBlockchainEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationBlockchainEventRepository extends JpaRepository<RegistrationBlockchainEvent, Long> {
    List<RegistrationBlockchainEvent> findByRegistrationRefOrderBySequenceAsc(String registrationRef);
    long countByRegistrationRef(String registrationRef);
}
