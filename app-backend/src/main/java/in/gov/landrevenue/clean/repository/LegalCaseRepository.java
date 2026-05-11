package in.gov.landrevenue.clean.repository;

import in.gov.landrevenue.clean.entity.LegalCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LegalCaseRepository extends JpaRepository<LegalCase, Long> {
    List<LegalCase> findByLandRecordIdOrderByFiledDateDesc(Long landRecordId);
    long countByLandRecordIdAndStatusIn(Long landRecordId, List<String> statuses);
}
