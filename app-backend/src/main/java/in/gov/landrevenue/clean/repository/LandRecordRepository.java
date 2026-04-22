package in.gov.landrevenue.clean.repository;

import in.gov.landrevenue.clean.entity.LandRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface LandRecordRepository extends JpaRepository<LandRecord, Long>, JpaSpecificationExecutor<LandRecord> {
    Page<LandRecord> findAllByOwner_NationalId(String nationalId, Pageable pageable);

    Optional<LandRecord> findByIdAndOwner_NationalId(Long id, String nationalId);
}
