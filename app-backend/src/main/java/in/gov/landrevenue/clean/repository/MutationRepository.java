package in.gov.landrevenue.clean.repository;

import in.gov.landrevenue.clean.entity.MutationApplication;
import in.gov.landrevenue.clean.enums.MutationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MutationRepository extends JpaRepository<MutationApplication, Long> {
    Optional<MutationApplication> findByMutationRef(String mutationRef);
    boolean existsByMutationRef(String mutationRef);
    Page<MutationApplication> findByLandRecordId(Long landRecordId, Pageable pageable);
    Page<MutationApplication> findByStatus(MutationStatus status, Pageable pageable);
    Page<MutationApplication> findByAppliedByUserId(Long userId, Pageable pageable);
    List<MutationApplication> findByLandRecordIdOrderByAppliedAtDesc(Long landRecordId);
}
