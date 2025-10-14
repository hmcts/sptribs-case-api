package uk.gov.hmcts.sptribs.notification.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationsRepository extends JpaRepository<NotificationRecord, Long> {
}
