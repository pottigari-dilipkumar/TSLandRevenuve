package in.gov.landrevenue.clean.repository;

import in.gov.landrevenue.clean.entity.LandRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LandRecordRepository extends JpaRepository<LandRecord, Long>, JpaSpecificationExecutor<LandRecord> {
}
