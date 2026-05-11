package in.gov.landrevenue.notification.repository;

import in.gov.landrevenue.notification.entity.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    Page<NotificationLog> findByChannel(String channel, Pageable pageable);
    Page<NotificationLog> findByReference(String reference, Pageable pageable);
    Page<NotificationLog> findByStatus(String status, Pageable pageable);
}
