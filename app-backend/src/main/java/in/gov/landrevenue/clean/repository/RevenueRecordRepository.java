package in.gov.landrevenue.clean.repository;

import in.gov.landrevenue.clean.entity.RevenueRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RevenueRecordRepository extends JpaRepository<RevenueRecord, Long>, JpaSpecificationExecutor<RevenueRecord> {
}
