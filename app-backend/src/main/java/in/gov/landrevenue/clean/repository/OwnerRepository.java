package in.gov.landrevenue.clean.repository;

import in.gov.landrevenue.clean.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
}
