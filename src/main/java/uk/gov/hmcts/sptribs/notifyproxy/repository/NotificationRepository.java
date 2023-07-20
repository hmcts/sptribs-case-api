package uk.gov.hmcts.sptribs.notifyproxy.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.notifyproxy.model.Notification;


import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Integer> {

//    TODO change query to select all pending notification
    @Query("select n from Notification n "
        + "where n.reference = ?1  AND n.notificationType = ?2 order by n.dateUpdated desc")
    Optional<List<Notification>> findByReferenceAndNotificationTypeOrderByDateUpdatedDesc(String reference,
                                                                                          String notificationType);
}
